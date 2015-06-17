/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.handler;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.INpc;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.IEntityTeleport;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.item.ItemBombFlowerSeed;
import zeldaswordskills.item.ItemCustomEgg;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.LeapingBlow;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Event handler for non-combat related entity events
 *
 */
public class ZSSEntityEvents
{
	/**
	 * NOTE: LivingFallEvent is not called in Creative mode, so must
	 * 		also listen for {@link PlayerFlyableFallEvent}
	 * Should receive canceled to make sure LeapingBlow triggers/deactivates
	 */
	@SubscribeEvent(receiveCanceled=true)
	public void onFall(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			ZSSPlayerSkills skills = info.getPlayerSkills();
			if (skills.isSkillActive(SkillBase.leapingBlow)) {
				((LeapingBlow) skills.getPlayerSkill(SkillBase.leapingBlow)).onImpact(player, event.distance);
			}

			if (!event.isCanceled() && info.reduceFallAmount > 0.0F) {
				event.distance -= info.reduceFallAmount;
				info.reduceFallAmount = 0.0F;
			}
		}
	}

	@SubscribeEvent
	public void onCreativeFall(PlayerFlyableFallEvent event) {
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(event.entityPlayer);
		if (skills.isSkillActive(SkillBase.leapingBlow)) {
			((LeapingBlow) skills.getPlayerSkill(SkillBase.leapingBlow)).onImpact(event.entityPlayer, event.distance);
		}
	}

	@SubscribeEvent
	public void onJump(LivingJumpEvent event) {
		if (event.entityLiving.getHeldItem() != null && event.entityLiving.getHeldItem().getItem() == ZSSItems.rocsFeather) {
			event.entityLiving.motionY += (event.entityLiving.isSprinting() ? 0.30D : 0.15D);
		}
		if (event.entityLiving.getEquipmentInSlot(ArmorIndex.EQUIPPED_BOOTS) != null && event.entityLiving.getEquipmentInSlot(ArmorIndex.EQUIPPED_BOOTS).getItem() == ZSSItems.bootsPegasus) {
			event.entityLiving.motionY += 0.15D;
			if (event.entity instanceof EntityPlayer) {
				ZSSPlayerInfo.get((EntityPlayer) event.entity).reduceFallAmount += 1.0F;
			}
		}
		ItemStack helm = event.entityLiving.getEquipmentInSlot(ArmorIndex.EQUIPPED_HELM);
		if (helm != null) {
			if (helm.getItem() == ZSSItems.maskBunny) {
				event.entityLiving.motionY += 0.30D;
				if (event.entity instanceof EntityPlayer) {
					ZSSPlayerInfo.get((EntityPlayer) event.entity).reduceFallAmount += 5.0F;
				}
			} else if (helm.getItem() == ZSSItems.maskDeku) {
				event.entityLiving.motionY += 0.30D;
			}
		}
	}

	@SubscribeEvent
	public void onInteract(EntityInteractEvent event) {
		ItemStack stack = event.entityPlayer.getHeldItem();
		if (event.target.getClass().isAssignableFrom(EntityVillager.class)) {
			EntityVillager villager = (EntityVillager) event.target;
			boolean flag2 = villager.getCustomNameTag().contains("Mask Salesman");
			if (!event.entityPlayer.worldObj.isRemote && !villager.isChild()) {
				if (("Barnes").equals(villager.getCustomNameTag())) {
					flag2 = EntityNpcBarnes.convertFromVillager(villager, event.entityPlayer, stack);
				} else if (("Cursed Man").equals(villager.getCustomNameTag())) {
					if (stack == null || stack.getItem() != ZSSItems.skulltulaToken) {
						int tokens = ZSSPlayerInfo.get(event.entityPlayer).getSkulltulaTokens();
						if (tokens > 0) {
							PlayerUtils.sendFormattedChat(event.entityPlayer, "chat.zss.npc.cursed_man.amount", tokens);
						} else {
							PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.npc.cursed_man." + event.entity.worldObj.rand.nextInt(4));
						}
						flag2 = true;
					}
				} else if (stack != null && stack.getItem() == ZSSItems.treasure && Treasures.byDamage(stack.getItemDamage()) == Treasures.ZELDAS_LETTER) {
					if (flag2) {
						PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.treasure." + Treasures.ZELDAS_LETTER.name + ".for_me");
					} else {
						PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.treasure." + Treasures.ZELDAS_LETTER.name + ".fail");
					}
					flag2 = true;
				} else if (flag2) {
					PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.npc.mask_trader.closed." + event.entity.worldObj.rand.nextInt(4));
				}
			}
			event.setCanceled(flag2);
		} else if (event.target instanceof EntityChicken && stack != null && stack.getItem() instanceof ItemBombFlowerSeed) {
			if (!event.target.worldObj.isRemote && ((EntityChicken) event.target).interact(event.entityPlayer)) {
				int time = 60 + event.target.worldObj.rand.nextInt(60);
				EntityBomb bomb = new EntityBomb(event.target.worldObj).setType(BombType.BOMB_STANDARD).setTime(time);
				ZSSEntityInfo.get((EntityChicken) event.target).onBombIngested(bomb);
				event.setCanceled(true);
			}
		}
		if (!event.isCanceled() && event.target instanceof INpc) {
			ItemStack helm = event.entityPlayer.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm != null && helm.getItem() instanceof ItemMask) {
				event.setCanceled(((ItemMask) helm.getItem()).onInteract(helm, event.entityPlayer, event.target));
			}
		}
		if (!event.isCanceled() && stack != null) {
			if (stack.getItem() instanceof ItemInstrument && event.target instanceof EntityLiving) {
				event.setCanceled(((ItemInstrument) stack.getItem()).onRightClickEntity(stack, event.entityPlayer, (EntityLiving) event.target));
			}
			// allow custom spawn eggs to create child entities:
			else if (stack.getItem() instanceof ItemCustomEgg && event.target instanceof EntityAgeable) {
				event.setCanceled(ItemCustomEgg.spawnChild(event.entity.worldObj, stack, event.entityPlayer, (EntityAgeable) event.target));
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerInfo.get(player).onUpdate();
			if (player.motionY < -0.25D) {
				boolean flag = player.getHeldItem() != null && player.getHeldItem().getItem() == ZSSItems.rocsFeather;
				if (flag || (player.getCurrentArmor(ArmorIndex.WORN_HELM) != null && player.getCurrentArmor(ArmorIndex.WORN_HELM).getItem() == ZSSItems.maskDeku)) {
					player.motionY = -0.25D;
					player.fallDistance = 0.0F;
				}
			}
		}
		if (event.entity instanceof EntityLivingBase) {
			ZSSEntityInfo.get((EntityLivingBase) event.entity).onUpdate();
		}
		if (event.entity instanceof EntityVillager) {
			ZSSVillagerInfo.get((EntityVillager) event.entity).onUpdate();
		}
	}

	@SubscribeEvent
	public void onClonePlayer(PlayerEvent.Clone event) {
		// Can't send update packets from here - use EntityJoinWorldEvent
		ZSSEntityInfo.get(event.entityPlayer).copy(ZSSEntityInfo.get(event.original));
		ZSSPlayerInfo.get(event.entityPlayer).copy(ZSSPlayerInfo.get(event.original));
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.entity.worldObj.isRemote) {
			if (event.entity instanceof EntityPlayer) {
				ZSSEntityInfo.get((EntityPlayer) event.entity).onJoinWorld();
				ZSSPlayerInfo.get((EntityPlayer) event.entity).onJoinWorld();
			} else if (event.entity.getClass().isAssignableFrom(EntityVillager.class)) {
				EntityGoron.doVillagerSpawn((EntityVillager) event.entity, event.entity.worldObj);
			}
			if (!Config.areVanillaBuffsDisabled() && event.entity instanceof EntityLivingBase) {
				initBuffs((EntityLivingBase) event.entity);
			}
		}
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityConstructing event) {
		if (event.entity instanceof EntityLivingBase && ZSSEntityInfo.get((EntityLivingBase) event.entity) == null) {
			ZSSEntityInfo.register((EntityLivingBase) event.entity);
		}
		if (event.entity instanceof EntityVillager && ZSSVillagerInfo.get((EntityVillager) event.entity) == null) {
			ZSSVillagerInfo.register((EntityVillager) event.entity);
		}
		if (event.entity instanceof EntityPlayer && ZSSPlayerInfo.get((EntityPlayer) event.entity) == null) {
			ZSSPlayerInfo.register((EntityPlayer) event.entity);
		}
	}

	@SubscribeEvent
	public void postTeleport(EntityAITeleport.PostEnderTeleport event) {
		EntityAITeleport.disruptTargeting(event.entityLiving);
		if (event.entity instanceof IEntityTeleport) {
			((IEntityTeleport) event.entity).getTeleportAI().onPostTeleport(event.targetX, event.targetY, event.targetZ);
		}
	}

	/**
	 * Applies permanent buffs / debuffs to vanilla mobs
	 */
	private void initBuffs(EntityLivingBase entity) {
		if (!ZSSEntityInfo.get(entity).getActiveBuffsMap().isEmpty()) {
			return;
		}
		// double damage from cold effects, highly resistant to fire damage
		if (entity.isImmuneToFire()) {
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 75);
			ZSSEntityInfo.get(entity).applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
		}
		if (entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
			if (!entity.isImmuneToFire()) {
				ZSSEntityInfo.get(entity).applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 50);
			}
			ZSSEntityInfo.get(entity).applyBuff(Buff.WEAKNESS_HOLY, Integer.MAX_VALUE, 300);
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 50);
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 50);
		}
		if (entity instanceof EntityGolem) {
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
		}
		if (entity instanceof EntityWitch) {
			ZSSEntityInfo.get(entity).applyBuff(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 75);
		}
		if (entity instanceof EntityWither) {
			ZSSEntityInfo.get(entity).removeBuff(Buff.WEAKNESS_COLD);
		}
	}
}

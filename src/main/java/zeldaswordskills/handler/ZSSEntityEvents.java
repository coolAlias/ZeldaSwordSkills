/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.handler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.INpc;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import zeldaswordskills.api.entity.NpcHelper;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.ai.IEntityTeleport;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncConfigPacket;
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

	/**
	 * This event fires on BOTH sides
	 */
	@SubscribeEvent
	public void onInteract(EntityInteractEvent event) {
		ItemStack stack = event.entityPlayer.getHeldItem();
		if (event.target instanceof EntityVillager && Result.DEFAULT != NpcHelper.convertVillager(event.entityPlayer, (EntityVillager) event.target, true)) {
			event.setCanceled(true);
		}
		// Check if the held item has any special interaction upon right-clicking an entity
		if (!event.isCanceled() && stack != null && stack.getItem() instanceof IRightClickEntity) {
			event.setCanceled(((IRightClickEntity) stack.getItem()).onRightClickEntity(stack, event.entityPlayer, event.target));
		}
		// If the event is not yet canceled, check for Mask interactions
		if (!event.isCanceled() && event.target instanceof INpc) {
			ItemStack helm = event.entityPlayer.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm != null && helm.getItem() instanceof ItemMask) {
				event.setCanceled(((ItemMask) helm.getItem()).onInteract(helm, event.entityPlayer, event.target));
			}
		}
		// Check for Lon Lon Milk cow interaction
		if (!event.isCanceled() && stack != null && stack.getItem() == Items.glass_bottle && event.target.getClass() == EntityCow.class) {
			event.setCanceled(ZSSPlayerSongs.get(event.entityPlayer).milkLonLonCow(event.entityPlayer, (EntityCow) event.target));
		}
		// Finally, check for interactions with the Cursed Man
		if (!event.isCanceled() && event.target.getClass() == EntityVillager.class && ("Cursed Man").equals(event.target.getCustomNameTag())) {
			EntityVillager villager = (EntityVillager) event.target;
			if (stack == null || (stack.getItem() != ZSSItems.skulltulaToken && stack.getItem() != Items.name_tag)) {
				int tokens = ZSSPlayerInfo.get(event.entityPlayer).getSkulltulaTokens();
				if (villager.worldObj.isRemote) {
					// don't send chat - will be sent from server
				} else if (villager.isChild()) {
					PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.npc.cursed_man.child");
				} else if (tokens > 0) {
					PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.npc.cursed_man.amount", tokens);
				} else {
					PlayerUtils.sendTranslatedChat(event.entityPlayer, "chat.zss.npc.cursed_man." + event.entity.worldObj.rand.nextInt(4));
				}
				event.setCanceled(true);
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
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		ZSSPlayerInfo.get(event.player).onPlayerLoggedIn();
		if (event.player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncConfigPacket(), (EntityPlayerMP) event.player);
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (event.entity instanceof EntityPlayer) {
			ZSSEntityInfo.get((EntityPlayer) event.entity).onJoinWorld();
			ZSSPlayerInfo.get((EntityPlayer) event.entity).onJoinWorld();
		}
		if (!event.entity.worldObj.isRemote) {
			if (event.entity.getClass() == EntityVillager.class) {
				EntityGoron.doVillagerSpawn((EntityVillager) event.entity, event.entity.worldObj);
			}
			if (!Config.areVanillaBuffsDisabled() && event.entity instanceof EntityLivingBase) {
				initBuffs((EntityLivingBase) event.entity);
			}
			// Clean up old villager data
			if (event.entity instanceof EntityVillager) {
				ZSSEntityEvents.convertVillagerData((EntityVillager) event.entity);
			}
		}
	}

	/**
	 * Cleans up old villager data
	 */
	private static void convertVillagerData(EntityVillager villager) {
		if (villager.getEntityData() != null && villager.getEntityData().hasKey("NextSkulltulaReward")) {
			ZSSVillagerInfo info = ZSSVillagerInfo.get(villager);
			if (info != null) {
				info.setNextSkulltulaReward(villager.getEntityData().getLong("NextSkulltulaReward"));
			}
			villager.getEntityData().removeTag("NextSkulltulaReward");
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
		ZSSEntityInfo info = ZSSEntityInfo.get(entity);
		if (!info.getActiveBuffs().isEmpty()) {
			return;
		}
		// double damage from cold and water effects, immune to fire damage
		if (entity.isImmuneToFire()) {
			info.applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.WEAKNESS_WATER, Integer.MAX_VALUE, 100);
		}
		if (entity.getCreatureAttribute() == EnumCreatureAttribute.UNDEAD) {
			if (!entity.isImmuneToFire()) {
				info.applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 50);
				info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 50);
			}
			info.applyBuff(Buff.WEAKNESS_HOLY, Integer.MAX_VALUE, 300);
			info.applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 50);
		}
		if (entity instanceof EntityGolem) {
			info.applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
			info.applyBuff(Buff.RESIST_STUN, Integer.MAX_VALUE, 100);
		}
		// EntityWitch has hard-coded 85% magic damage resistance - don't need to add more
		if (entity instanceof EntityWither) {
			info.removeBuff(Buff.WEAKNESS_COLD);
		}
	}
}

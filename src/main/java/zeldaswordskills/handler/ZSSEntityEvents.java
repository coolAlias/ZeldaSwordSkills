/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemHookShot.ShotType;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.SyncEntityInfoPacket;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.LeapingBlow;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * 
 * Event handler for non-combat related entity events
 *
 */
public class ZSSEntityEvents
{
	/**
	 * NOTE 1: Leaping Blow's onImpact method is client-side only
	 * NOTE 2: LivingFallEvent is only called when not in Creative mode
	 */
	@ForgeSubscribe
	public void onFall(LivingFallEvent event) {
		if (event.entity instanceof EntityPlayer && event.entity.worldObj.isRemote) {
			EntityPlayer player = (EntityPlayer) event.entity;
			if (ZSSPlayerInfo.get(player) != null && ZSSPlayerInfo.get(player).isSkillActive(SkillBase.leapingBlow)) {
				((LeapingBlow) ZSSPlayerInfo.get(player).getPlayerSkill(SkillBase.leapingBlow)).onImpact(player, event.distance);
			}
		}
		if (event.entityLiving.getCurrentItemOrArmor(4) != null && event.entityLiving.getCurrentItemOrArmor(4).getItem() == ZSSItems.maskBunny) {
			event.distance -= 5.0F;
		}
	}

	/**
	 * NOTE: Leaping Blow's onImpact method is client-side only
	 */
	@ForgeSubscribe
	public void onCreativeFall(PlayerFlyableFallEvent event) {
		if (ZSSPlayerInfo.get(event.entityPlayer) != null && event.entityPlayer.worldObj.isRemote) {
			if (ZSSPlayerInfo.get(event.entityPlayer).isSkillActive(SkillBase.leapingBlow)) {
				EntityPlayer player = event.entityPlayer;
				((LeapingBlow) ZSSPlayerInfo.get(player).getPlayerSkill(SkillBase.leapingBlow)).onImpact(player, event.distance);
			}
		}
	}
	
	@ForgeSubscribe
	public void onJump(LivingJumpEvent event) {
		if (event.entityLiving.getHeldItem() != null && event.entityLiving.getHeldItem().getItem() == ZSSItems.rocsFeather) {
			event.entityLiving.motionY += (event.entityLiving.isSprinting() ? 0.30D : 0.15D);
		}
		if (event.entityLiving.getCurrentItemOrArmor(1) != null && event.entityLiving.getCurrentItemOrArmor(1).getItem() == ZSSItems.bootsPegasus) {
			event.entityLiving.motionY += 0.15D;
		}
		if (event.entityLiving.getCurrentItemOrArmor(4) != null && event.entityLiving.getCurrentItemOrArmor(4).getItem() == ZSSItems.maskBunny) {
			event.entityLiving.motionY += 0.30D;
		}
	}

	@ForgeSubscribe
	public void onLivingUpdate(LivingUpdateEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerInfo.get(player).onUpdate();
			if (player.getHeldItem() != null && player.getHeldItem().getItem() == ZSSItems.rocsFeather && player.motionY < -0.25D) {
				player.motionY = -0.25D;
				player.fallDistance = 0.0F;
			}
		}
		if (event.entity instanceof EntityLivingBase) {
			ZSSEntityInfo.get((EntityLivingBase) event.entity).onUpdate();
		}
		if (event.entity instanceof EntityVillager) {
			ZSSVillagerInfo.get((EntityVillager) event.entity).onUpdate();
		}
	}

	@ForgeSubscribe
	public void onEntityJoinWorld(EntityJoinWorldEvent event) {
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerInfo.loadProxyData(player);
			PacketDispatcher.sendPacketToPlayer(new SyncEntityInfoPacket(ZSSEntityInfo.get(player)).makePacket(), (Player) player);
			// TODO remove CHEAT for testing on server!!!
			if (Config.areCheatsEnabled()) {
				player.inventory.addItemStackToInventory(new ItemStack(Item.swordIron));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.hookshot,1,ShotType.WOOD_SHOT.ordinal()));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bombBag));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bomb));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bomb));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bomb));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bomb));
				player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.bomb));
				player.inventory.addItemStackToInventory(new ItemStack(Item.monsterPlacer.itemID,64,57)); // 92 cow, 57 pigman
				player.inventory.addItemStackToInventory(new ItemStack(Item.beefCooked,64));
				for (int i = 0; i < SkillBase.MAX_NUM_SKILLS; ++i) {
					SkillBase skill = SkillBase.getSkillList()[i]; 
					if (skill != null && ZSSPlayerInfo.get(player).getSkillLevel(skill) < 1) {
						ZSSPlayerInfo.get(player).grantSkill(skill);
						player.inventory.addItemStackToInventory(new ItemStack(ZSSItems.skillOrb,1,skill.id));
					}
				}
			} else {
				ZSSPlayerInfo.get(player).verifyStartingGear();
			}
		}
		
		if (!event.entity.worldObj.isRemote && !Config.areVanillaBuffsDisabled() && event.entity instanceof EntityLivingBase) {
			initBuffs((EntityLivingBase) event.entity);
		}
	}

	@ForgeSubscribe
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

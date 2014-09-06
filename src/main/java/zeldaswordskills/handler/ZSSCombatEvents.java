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

import java.util.Set;

import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.EventPriority;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceArmorBreak;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.damage.IDamageType;
import zeldaswordskills.api.damage.IPostDamageEffect;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IArmorBreak;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemArmorTunic;
import zeldaswordskills.item.ItemFairyBottle;
import zeldaswordskills.item.ItemZeldaShield;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.AddExhaustionPacket;
import zeldaswordskills.network.UnpressKeyPacket;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * 
 * Event handler for all combat-related events
 *
 */
public class ZSSCombatEvents
{
	/**
	 * Sets the attack timer for the player if using an ISwingSpeed item
	 * All other items default to vanilla behavior, which is spam-happy
	 * Note that the attackTime is deliberately NOT synced between client
	 * and server; otherwise the smash mechanics will break: left-click is
	 * processed first on the client, and the server gets notified before
	 * the smash can process
	 */
	public static void setPlayerAttackTime(EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			ItemStack stack = player.getHeldItem();
			int nextSwing = Config.getBaseSwingSpeed();
			if (stack != null && stack.getItem() instanceof ISwingSpeed) {
				nextSwing += Math.max(player.attackTime, ((ISwingSpeed) stack.getItem()).getSwingSpeed());
				if (player.worldObj.isRemote) {
					float exhaustion = ((ISwingSpeed) stack.getItem()).getExhaustion();
					if (exhaustion > 0.0F) {
						PacketDispatcher.sendPacketToServer(new AddExhaustionPacket(exhaustion).makePacket());
					}
				} else {
					PacketDispatcher.sendPacketToPlayer(new UnpressKeyPacket(UnpressKeyPacket.LMB).makePacket(), (Player) player);
				}
			}
			player.attackTime = Math.max(player.attackTime, nextSwing);
		}
	}

	/**
	 * Using this event to set attack time on the server side only in order
	 * to prevent left-click processing on blocks with ISmashBlock items
	 */
	@ForgeSubscribe(priority=EventPriority.HIGHEST, receiveCanceled=true)
	public void onPlayerAttack(AttackEntityEvent event) {
		if (!event.entityPlayer.worldObj.isRemote) {
			setPlayerAttackTime(event.entityPlayer);
		}
	}

	@ForgeSubscribe
	public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
		if (event.target instanceof EntityPlayer && event.entity instanceof EntityLiving) {
			ItemStack mask = ((EntityPlayer) event.target).getCurrentArmor(ArmorIndex.WORN_HELM);
			if (mask != null && mask.getItem() == ZSSItems.maskSpooky && event.entityLiving.func_94060_bK() != event.target) {
				((EntityLiving) event.entity).setAttackTarget(null);
			}
		}
	}

	/**
	 * This event is called when an entity is attacked by another entity; it is only
	 * called on the server unless the source of the attack is an EntityPlayer
	 */
	@ForgeSubscribe
	public void onAttacked(LivingAttackEvent event) {
		if (event.source.getEntity() instanceof EntityLivingBase) {
			event.setCanceled(ZSSEntityInfo.get((EntityLivingBase) event.source.getEntity()).isBuffActive(Buff.STUN));
		}
		if (!event.isCanceled() && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerInfo.get(player).onBeingAttacked(event);
			// prevent non-entity sources of fire damage here to avoid hurt animation while in fire / lava
			if (event.source.isFireDamage() && event.source.getSourceOfDamage() == null && !event.isCanceled()) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
		} else if (!event.isCanceled() && event.source.getEntity() != null) {
			EntityLivingBase entity = event.entityLiving;
			float evade = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_UP) * 0.01F;
			if (evade > 0.0F) {
				float penalty = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_DOWN) * 0.01F;
				if (entity.worldObj.rand.nextFloat() < evade - penalty) {
					WorldUtils.playSoundAtEntity(entity, Sounds.SWORD_MISS, 0.4F, 0.5F);
					event.setCanceled(true);
				}
			}
		}
	}

	/**
	 * Pre-event to handle shield blocking
	 */
	@ForgeSubscribe(priority=EventPriority.NORMAL)
	public void onPreHurt(LivingHurtEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() instanceof ItemZeldaShield && player.isUsingItem()) {
				ItemZeldaShield shield = (ItemZeldaShield) stack.getItem();
				if (ZSSPlayerInfo.get(player).canBlock() && shield.canBlockDamage(stack, event.source)) {
					Entity opponent = event.source.getEntity();
					boolean shouldBlock = opponent != null;
					if (shouldBlock) {
						// thanks again to Battlegear2 for the following code snippet
						double dx = opponent.posX - player.posX;
						double dz;
						for (dz = opponent.posZ - player.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random()) * 0.01D) {
							dx = (Math.random() - Math.random()) * 0.01D;
						}
						float yaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - player.rotationYaw;
						yaw = yaw - 90;
						while (yaw < -180) { yaw += 360; }
						while (yaw >= 180) { yaw -= 360; }
						shouldBlock = yaw < 60 && yaw > -60; // all Zelda shields use default block angles
					}
					if (shouldBlock) {
						shield.onBlock(player, stack, event.source, event.ammount);
						event.setCanceled(true);
					}
				}
			}
		}
	}

	/**
	 * Use LOW or LOWEST priority to prevent interrupting a combo when the event may be canceled elsewhere.
	 */
	@ForgeSubscribe(priority=EventPriority.LOWEST)
	public void onHurt(LivingHurtEvent event) {
		// handle armor break first, since it will post LivingHurtEvent once again
		if (event.source.getEntity() instanceof EntityPlayer && !(event.source instanceof DamageSourceArmorBreak)) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
			ICombo combo = skills.getComboSkill();
			if (combo != null && combo.isComboInProgress()) {
				event.ammount += combo.getCombo().getSize();
			}
			if (skills.isSkillActive(SkillBase.armorBreak)) {
				((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).onImpact(player, event);
				return;
			} else if (skills.isSkillActive(SkillBase.mortalDraw)) {
				((MortalDraw) skills.getPlayerSkill(SkillBase.mortalDraw)).onImpact(player, event);
			}
			if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof IArmorBreak && event.source.damageType.equals("player")) {
				float damage = (event.ammount * ((IArmorBreak) player.getHeldItem().getItem()).getPercentArmorIgnored() * 0.01F);
				// use dirty accessor to avoid checking / setting hurt resistant time, which
				// allows the current remaining damage to process normally and the armor break
				// damage to be applied from the second event posted from #damageEntity
				DirtyEntityAccessor.damageEntity(event.entityLiving, DamageUtils.causeIArmorBreakDamage(player), damage);
				event.ammount -= damage; // subtract armor break damage
			}
		}

		applyDamageModifiers(event);

		// apply magic armor and combo onHurt last, after other resistances
		if (event.ammount > 0.0F && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			/* TODO magic armor:
				if (player.getCurrentArmor(ArmorIndex.WORN_CHEST) != null && player.getCurrentArmor(ArmorIndex.WORN_CHEST).getItem() == ZSSItems.armorMagicChest) {
					while (event.ammount > 0 && player.inventory.consumeInventoryItem(Item.emerald.itemID)) {
						event.ammount -= 1.0F;
					}
					event.setCanceled(event.ammount < 0.1F);
				}
			 */
			if (event.source.isFireDamage() && !event.isCanceled()) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
			if (event.isCanceled()) {
				return;
			}
			ICombo combo = ZSSPlayerInfo.get(player).getComboSkill();
			if (combo != null && event.ammount > 0) {
				combo.onPlayerHurt(player, event);
			}
		}
		// final call for active skills to modify damage
		// update combo last, after all resistances and weaknesses are accounted for
		if (event.ammount > 0.0F && event.source.getEntity() instanceof EntityPlayer) {
			ZSSPlayerInfo.get((EntityPlayer) event.source.getEntity()).onPostImpact(event);
		}

		handleSecondaryEffects(event);
	}

	/**
	 * Set to highest priority to prevent loss of "extra lives" from HQM mod
	 */
	@ForgeSubscribe(priority=EventPriority.HIGHEST)
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
			event.setCanceled(ItemFairyBottle.onDeath((EntityPlayer) event.entity));
			if (!event.isCanceled()) {
				ZSSPlayerInfo.saveProxyData((EntityPlayer) event.entity);
			}
		}
		if (event.source.getEntity() instanceof EntityPlayer && event.entity instanceof IMob) {
			ItemZeldaSword.onKilledMob((EntityPlayer) event.source.getEntity(), (IMob) event.entity);
		}
	}

	/**
	 * Applies all damage modifiers
	 */
	private void applyDamageModifiers(LivingHurtEvent event) {
		if (event.source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) event.source.getEntity();
			event.ammount *= 1.0F - (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.ATTACK_DOWN) * 0.01F);
			event.ammount *= 1.0F + (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.ATTACK_UP) * 0.01F);
		}
		applyDamageWeaknesses(event);
		applyDamageResistances(event);
	}

	/**
	 * Modifies damage of LivingHurtEvent based on entity resistances
	 */
	private void applyDamageResistances(LivingHurtEvent event) {
		float defenseUp = (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.DEFENSE_UP) * 0.01F);
		float defenseDown = (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.DEFENSE_DOWN) * 0.01F);
		event.ammount *= (1.0F + defenseDown - defenseUp);
		if (event.source instanceof IDamageType && event.ammount > 0.0F) {
			Set<EnumDamageType> damageTypes = ((IDamageType) event.source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					if (EnumDamageType.damageResistMap.get(type) != null) {
						event.ammount *= 1.0F - (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(EnumDamageType.damageResistMap.get(type)) * 0.01F);
					}
				}
			}
		}
		if (event.source.isFireDamage()) {
			event.ammount *= 1.0F - (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.RESIST_FIRE) * 0.01F);
		}
		if (event.source.isMagicDamage()) {
			event.ammount *= 1.0F - (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.RESIST_MAGIC) * 0.01F);
		}
	}

	/**
	 * Modifies damage of LivingHurtEvent based on entity weaknesses
	 */
	private void applyDamageWeaknesses(LivingHurtEvent event) {
		if (event.source instanceof IDamageType) {
			Set<EnumDamageType> damageTypes = ((IDamageType) event.source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					if (EnumDamageType.damageWeaknessMap.get(type) != null) {
						event.ammount *= 1.0F + (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(EnumDamageType.damageWeaknessMap.get(type)) * 0.01F);
					}
				}
			}
		}
		if (event.source.isFireDamage()) {
			event.ammount *= 1.0F + (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.WEAKNESS_FIRE) * 0.01F);
		}
		if (event.source.isMagicDamage()) {
			event.ammount *= 1.0F + (ZSSEntityInfo.get(event.entityLiving).getBuffAmplifier(Buff.WEAKNESS_MAGIC) * 0.01F);
		}
	}

	/**
	 * Applies any secondary effects that may occur when a living entity is injured
	 */
	private void handleSecondaryEffects(LivingHurtEvent event) {
		if (event.ammount > 0.0F && event.source instanceof IDamageType && event.source instanceof IPostDamageEffect) {
			Set<EnumDamageType> damageTypes = ((IDamageType) event.source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					type.handleSecondaryEffects((IPostDamageEffect) event.source, event.entityLiving, event.ammount);
				}
			}
		}
	}
}

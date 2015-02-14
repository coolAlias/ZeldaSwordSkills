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

import java.util.Set;

import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
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
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemArmorTunic;
import zeldaswordskills.item.ItemFairyBottle;
import zeldaswordskills.item.ItemZeldaShield;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.network.server.AddExhaustionPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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
						PacketDispatcher.sendToServer(new AddExhaustionPacket(exhaustion));
					}
				} else {
					PacketDispatcher.sendTo(new UnpressKeyPacket(UnpressKeyPacket.LMB), (EntityPlayerMP) player);
				}
			}
			player.attackTime = Math.max(player.attackTime, nextSwing);
		}
	}

	/**
	 * Using this event to set attack time on the server side only in order
	 * to prevent left-click processing on blocks with ISmashBlock items
	 */
	@SubscribeEvent(priority=EventPriority.HIGHEST, receiveCanceled=true)
	public void onPlayerAttack(AttackEntityEvent event) {
		if (!event.entityPlayer.worldObj.isRemote) {
			setPlayerAttackTime(event.entityPlayer);
		}
	}

	@SubscribeEvent
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
	@SubscribeEvent
	public void onAttacked(LivingAttackEvent event) {
		if (event.source.getEntity() instanceof EntityLivingBase) {
			event.setCanceled(ZSSEntityInfo.get((EntityLivingBase) event.source.getEntity()).isBuffActive(Buff.STUN));
		}
		// Possible for damage to be negated by resistances, in which case we don't want the hurt animation to play
		float amount = applyDamageModifiers(event.entityLiving, event.source, event.ammount);
		if (!event.isCanceled() && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerSkills.get(player).onBeingAttacked(event);
			if (amount < 0.1F) {
				event.setCanceled(true);
			} else if (!event.isCanceled() && event.source.isFireDamage() && event.source.getSourceOfDamage() == null) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
		} else if (amount < 0.1F) {
			event.setCanceled(true);
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
	@SubscribeEvent(priority=EventPriority.NORMAL)
	public void onPreHurt(LivingHurtEvent event) {
		if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() instanceof ItemZeldaShield && player.isUsingItem()) {
				ItemZeldaShield shield = (ItemZeldaShield) stack.getItem();
				if (ZSSPlayerInfo.get(player).canBlock() && shield.canBlockDamage(stack, event.source)) {
					Entity opponent = event.source.getEntity();
					if (opponent != null && TargetUtils.isTargetInFrontOf(opponent, player, 60)) {
						event.ammount = shield.onBlock(player, stack, event.source, event.ammount);
						event.setCanceled(event.ammount < 0.1F);
					}
				}
			}
		}
	}

	/**
	 * Use LOW or LOWEST priority to prevent interrupting a combo when the event may be canceled elsewhere.
	 */
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onHurt(LivingHurtEvent event) {
		// handle armor break first, since it will post LivingHurtEvent once again
		if (event.source.getEntity() instanceof EntityPlayer && !(event.source instanceof DamageSourceArmorBreak)) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
			ICombo combo = skills.getComboSkill();
			if (combo != null && combo.isComboInProgress()) {
				event.ammount += combo.getCombo().getSize();
			}
			if (skills.isSkillActive(SkillBase.armorBreak)) {
				//LogHelper.info("Entity hurt by armor break; player weapon pre-impact damage: " + player.getHeldItem().getItemDamage());
				((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).onImpact(player, event);
				//LogHelper.info("Entity hurt by armor break; player weapon post-impact damage: " + player.getHeldItem().getItemDamage());
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

		event.ammount = applyDamageModifiers(event.entityLiving, event.source, event.ammount);

		// apply magic armor and combo onHurt last, after other resistances
		if (event.ammount > 0.0F && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			/* TODO magic armor:
				if (player.getCurrentArmor(3) != null && player.getCurrentArmor(3).getItem() == ZSSItems.tunicGoronChest) {
					while (event.ammount > 0 && player.inventory.consumeInventoryItem(Item.emerald.itemID)) {
						event.ammount -= 1.0F;
					}
					event.setCanceled(event.ammount < 0.1F);
				}
			 */
			if (event.isCanceled()) {
				return;
			}
			ICombo combo = ZSSPlayerSkills.get(player).getComboSkill();
			if (combo != null && event.ammount > 0) {
				combo.onPlayerHurt(player, event);
			}
		}
		// final call for active skills to modify damage
		// update combo and last, after all resistances and weaknesses are accounted for
		if (event.ammount > 0.0F && event.source.getEntity() instanceof EntityPlayer) {
			ZSSPlayerSkills.get((EntityPlayer) event.source.getEntity()).onPostImpact(event);
		}
		handleSecondaryEffects(event);
	}

	/**
	 * Set to highest priority to prevent loss of "extra lives" from HQM mod
	 */
	@SubscribeEvent(priority=EventPriority.HIGHEST)
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
			event.setCanceled(ItemFairyBottle.onDeath((EntityPlayer) event.entity));
		}
		if (event.source.getEntity() instanceof EntityPlayer && event.entity instanceof IMob) {
			ItemZeldaSword.onKilledMob((EntityPlayer) event.source.getEntity(), (IMob) event.entity);
		}
	}

	/**
	 * Returns the damage amount modified by both the attacker's and the defender's relevant buffs
	 */
	public static float applyDamageModifiers(EntityLivingBase defender, DamageSource source, float amount) {
		if (source.getEntity() instanceof EntityLivingBase) {
			EntityLivingBase entity = (EntityLivingBase) source.getEntity();
			amount *= 1.0F - (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.ATTACK_DOWN) * 0.01F);
			amount *= 1.0F + (ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.ATTACK_UP) * 0.01F);
		}
		amount = applyDamageWeaknesses(defender, source, amount);
		amount = applyDamageResistances(defender, source, amount);
		return amount;
	}

	/**
	 * Returns modified damage amount based on the defender's resistances vs. the damage source
	 */
	private static float applyDamageResistances(EntityLivingBase defender, DamageSource source, float amount) {
		ZSSEntityInfo info = ZSSEntityInfo.get(defender);
		float defenseUp = info.getBuffAmplifier(Buff.DEFENSE_UP) * 0.01F;
		float defenseDown = info.getBuffAmplifier(Buff.DEFENSE_DOWN) * 0.01F;
		amount *= (1.0F + defenseDown - defenseUp);
		if (source instanceof IDamageType && amount > 0.0F) {
			Set<EnumDamageType> damageTypes = ((IDamageType) source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					if (EnumDamageType.damageResistMap.get(type) != null) {
						amount *= 1.0F - (info.getBuffAmplifier(EnumDamageType.damageResistMap.get(type)) * 0.01F);
					}
				}
			}
		}
		if (source.isFireDamage()) {
			amount *= 1.0F - (info.getBuffAmplifier(Buff.RESIST_FIRE) * 0.01F);
		}
		if (source.isMagicDamage()) {
			amount *= 1.0F - (info.getBuffAmplifier(Buff.RESIST_MAGIC) * 0.01F);
		}
		return amount;
	}

	/**
	 * Returns modified damage amount based on the defender's weaknesses to the damage source
	 */
	private static float applyDamageWeaknesses(EntityLivingBase defender, DamageSource source, float amount) {
		ZSSEntityInfo info = ZSSEntityInfo.get(defender);
		if (source instanceof IDamageType) {
			Set<EnumDamageType> damageTypes = ((IDamageType) source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					if (EnumDamageType.damageWeaknessMap.get(type) != null) {
						amount *= 1.0F + (info.getBuffAmplifier(EnumDamageType.damageWeaknessMap.get(type)) * 0.01F);
					}
				}
			}
		}
		if (source.isFireDamage()) {
			amount *= 1.0F + (info.getBuffAmplifier(Buff.WEAKNESS_FIRE) * 0.01F);
		}
		if (source.isMagicDamage()) {
			amount *= 1.0F + (info.getBuffAmplifier(Buff.WEAKNESS_MAGIC) * 0.01F);
		}
		return amount;
	}

	/**
	 * Applies any secondary effects that may occur when a living entity is injured
	 */
	private static void handleSecondaryEffects(LivingHurtEvent event) {
		if (event.ammount >= 1.0F && event.source instanceof IDamageType && event.source instanceof IPostDamageEffect) {
			Set<EnumDamageType> damageTypes = ((IDamageType) event.source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					type.handleSecondaryEffects((IPostDamageEffect) event.source, event.entityLiving, event.ammount);
				}
			}
		}
	}
}

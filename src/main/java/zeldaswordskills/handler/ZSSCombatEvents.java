/**
    Copyright (C) <2018> <coolAlias>

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

import mods.battlegear2.api.core.BattlegearUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceArmorBreak;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.damage.IDamageAoE;
import zeldaswordskills.api.damage.IDamageType;
import zeldaswordskills.api.damage.IDamageUnavoidable;
import zeldaswordskills.api.damage.IPostDamageEffect;
import zeldaswordskills.api.entity.IReflectable;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.api.item.IArmorBreak;
import zeldaswordskills.api.item.IReflective;
import zeldaswordskills.api.item.ISwingSpeed;
import zeldaswordskills.entity.DirtyEntityAccessor;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
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
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

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
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			ItemStack stack = player.getHeldItem();
			int nextSwing = Config.getBaseSwingSpeed();
			if (stack != null && stack.getItem() instanceof ISwingSpeed) {
				nextSwing += Math.max(info.getAttackTime(), ((ISwingSpeed) stack.getItem()).getSwingSpeed());
				if (player.worldObj.isRemote) {
					float exhaustion = ((ISwingSpeed) stack.getItem()).getExhaustion();
					if (exhaustion > 0.0F) {
						PacketDispatcher.sendToServer(new AddExhaustionPacket(exhaustion));
					}
				} else {
					PacketDispatcher.sendTo(new UnpressKeyPacket(UnpressKeyPacket.LMB), (EntityPlayerMP) player);
				}
			}
			info.setAttackTime(Math.max(info.getAttackTime(), nextSwing));
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
			if (mask != null && mask.getItem() == ZSSItems.maskSpooky && event.entityLiving.getAttackingEntity() != event.target) {
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
		if (event.isCanceled()) {
			return;
		}
		// Check if attack is a projectile and can be reflected by player with shield
		if (event.entity instanceof EntityPlayer && event.source.getEntity() != null) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack stack = PlayerUtils.getHeldItem(player, false); // get offhand item
			if (stack == null || !PlayerUtils.isBlockingWithShield(player) || !ZSSPlayerInfo.get(player).canBlock()) {
				// nothing to do here if held item is not a shield or not being used to block
			} else if (!TargetUtils.isTargetInFrontOf(player, event.source.getEntity(), 60)) {
				// opponent is not in front of player, can't block incoming attacks with shield
			} else if (ZSSCombatEvents.wasProjectileReflected(stack, player, event.source, event.ammount)) {
				if (stack.getItem() instanceof IReflective) {
					((IReflective) stack.getItem()).onReflected(stack, player, event.source, event.ammount);
				} else {
					ZSSPlayerInfo.get(player).onAttackBlocked(stack, event.ammount);
					// Try to damage non-IReflective shields each time they reflect a projectile
					stack.damageItem(1, player);
					if (stack.stackSize <= 0) {
						ForgeEventFactory.onPlayerDestroyItem(player, stack);
						if (ZSSMain.isBG2Enabled && BattlegearUtils.isPlayerInBattlemode(player)) {
							BattlegearUtils.setPlayerOffhandItem(player, null);
						} else {
							player.destroyCurrentEquippedItem();
						}
					}
				}
				event.setCanceled(true);
			}
		}
		// Possible for damage to be negated by resistances, in which case we don't want the hurt animation to play
		float amount = applyDamageModifiers(event.entityLiving, event.source, event.ammount);
		if (event.isCanceled()) {
			// nothing further to do
		} else if (event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ZSSPlayerSkills.get(player).onBeingAttacked(event);
			if (amount < 0.1F) {
				event.setCanceled(true);
			} else if (event.source.isFireDamage() && event.source.getSourceOfDamage() == null) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
		} else if (amount < 0.1F) {
			event.setCanceled(true);
		}
		// Check if damage can be evaded if not already canceled
		if (!event.isCanceled() && event.source.getEntity() != null && ZSSCombatEvents.canEvadeDamage(event.source)) {
			EntityLivingBase entity = event.entityLiving;
			float evade = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_UP) * 0.01F;
			if (evade > 0.0F && !ZSSEntityInfo.get(entity).isBuffActive(Buff.STUN)) {
				float penalty = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_DOWN) * 0.01F;
				if (entity.worldObj.rand.nextFloat() < evade - penalty) {
					WorldUtils.playSoundAtEntity(entity, Sounds.SWORD_MISS, 0.4F, 0.5F);
					event.setCanceled(true);
				}
			}
		}
	}

	/**
	 * Returns true if the DamageSource is a type that can be evaded by e.g. the evasion Buff
	 */
	public static boolean canEvadeDamage(DamageSource source) {
		if (source instanceof IDamageUnavoidable && ((IDamageUnavoidable) source).isUnavoidable()) {
			return false;
		} else if (source instanceof IDamageAoE && ((IDamageAoE) source).isAoEDamage()) {
			return false;
		}
		return true;
	}

	/**
	 * Pre-event to handle shield blocking
	 */
	@SubscribeEvent(priority=EventPriority.NORMAL)
	public void onPreHurt(LivingHurtEvent event) {
		// Check ZSS Shields to call #onBlock - projectile was not reflected and not in BG2 slots
		if (event.entity instanceof EntityPlayer && event.source.getEntity() != null) {
			EntityPlayer player = (EntityPlayer) event.entity;
			ItemStack stack = player.getHeldItem();
			if (!PlayerUtils.isShield(stack) || !PlayerUtils.isBlocking(player) || !ZSSPlayerInfo.get(player).canBlock()) {
				// nothing to do here if held item is not a shield or not being used to block
			} else if (!TargetUtils.isTargetInFrontOf(player, event.source.getEntity(), 60)) {
				// opponent is not in front of player, can't block incoming attacks with shield
			} else if (stack != null && stack.getItem() instanceof ItemZeldaShield) {
				ItemZeldaShield shield = (ItemZeldaShield) stack.getItem();
				if (shield.canBlockDamage(stack, event.source)) {
					event.ammount = shield.onBlock(player, stack, event.source, event.ammount, true);
					event.setCanceled(event.isCanceled() || event.ammount < 0.1F);
				}
			}
		}
	}

	/**
	 * Tries to reflect any projectile source back toward its initiator when blocked with a shield
	 * @param shield the shield used to block the projectile
	 * @param player the player defending against the attack
	 * @param source {@link DamageSource#getSourceOfDamage} should return the projectile
	 * @param damage amount of damage the attack would cause if not reflected
	 * @return false if for some reason the projectile could not be reflected
	 */
	// public so it can be used in Battlegear ShieldBlockEvent handler
	public static boolean wasProjectileReflected(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		if (!source.isProjectile() || source.isExplosion() || source.getSourceOfDamage() == null) {
			return false;
		}
		float chance = 0.0F; // no chance unless IReflective shield or IReflectable projectile says otherwise
		Entity projectile = source.getSourceOfDamage();
		// IReflectable chance takes precedence over IReflective
		if (projectile instanceof IReflectable) {
			chance = ((IReflectable) projectile).getReflectChance(shield, player, source, damage);
		} else if (shield.getItem() instanceof IReflective) {
			chance = ((IReflective) shield.getItem()).getReflectChance(shield, player, source, damage); 
		}
		// Player attacks trigger LivingAttackEvent on both sides; only other players will be reflecting projectiles
		if (player.worldObj.isRemote) {
			return chance > 0.49F; // best guess; client side attack event resolution shouldn't really matter too much
		} else if (player.worldObj.rand.nextFloat() < chance) {
			WorldUtils.playSoundAtEntity(player, Sounds.HAMMER, 0.4F, 0.5F);
			return ZSSCombatEvents.reflectProjectile(shield, player, source);
		}
		return false;
	}

	/**
	 * Sends the projectile back toward its initiator
	 * @param shield the shield used to block the projectile
	 * @param player the player defending against the attack
	 * @param source {@link DamageSource#getSourceOfDamage} should return the projectile
	 * @param damage amount of damage the attack would cause if not reflected
	 * @return false if for some reason the projectile could not be reflected
	 */
	private static boolean reflectProjectile(ItemStack shield, EntityPlayer player, DamageSource source) {
		// Create a new instance in case projectile calls #setDead on itself due to the impact
		Entity projectile = null;
		try {
			projectile = source.getSourceOfDamage().getClass().getConstructor(World.class).newInstance(player.worldObj);
			NBTTagCompound data = new NBTTagCompound();
			// This *should* make an exact copy of the projectile to reflect
			source.getSourceOfDamage().writeToNBT(data);
			projectile.readFromNBT(data);
		} catch (Exception e) {
			ZSSMain.logger.error(String.format("Unable to reflect projectile - failed to create new instance of class %s: %s", source.getSourceOfDamage().getClass().getSimpleName(), e.getMessage()));
		}
		if (projectile == null) {
			return false;
		}
		projectile.getEntityData().setBoolean("isReflected", true);
		projectile.posX -= projectile.motionX;
		projectile.posY -= projectile.motionY;
		projectile.posZ -= projectile.motionZ;
		// Set new trajectory based on player's look vector a la EntityFireball
		Vec3 vec = player.getLookVec();
		double motionX = vec.xCoord;
		double motionY = vec.yCoord;
		double motionZ = vec.zCoord;
		float wobble = 2.0F + (20.0F * player.worldObj.rand.nextFloat());
		if (projectile instanceof IReflectable) {
			float alt_wobble = ((IReflectable) projectile).getReflectedWobble(shield, player, source);
			wobble = (alt_wobble < 0.0F ? wobble : alt_wobble);
		}
		TargetUtils.setEntityHeading(projectile, motionX, motionY, motionZ, 1.0F, wobble, false);
		if (projectile instanceof IReflectable) {
			((IReflectable) projectile).onReflected(shield, player, source);
		} else {
			// Handle common non-IReflectable projectiles
			if (projectile instanceof EntityThrowable) {
				DirtyEntityAccessor.setThrowableThrower((EntityThrowable) projectile, player);
			} else if (projectile instanceof EntityArrow) {
				((EntityArrow) projectile).shootingEntity = player;
			} else if (projectile instanceof EntityLargeFireball) {
				// EntityLargeFireball kills ghasts in one hit when reflected by an attack (which results in the player being set as the 'shooting entity')
				// Only let this happen when using a Mirror Shield or it becomes too easy
				if (shield.getItem() instanceof IReflective && ((IReflective) shield.getItem()).isMirrorShield(shield)) {
					((EntityLargeFireball) projectile).shootingEntity = player;
				}
			}
			// Make sure original projectile is dead; IReflectables are given the power to skip this
			source.getSourceOfDamage().setDead();
		}
		player.worldObj.spawnEntityInWorld(projectile);
		return true;
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
				event.ammount += combo.getCombo().getNumHits();
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
			/* 
			// TODO magic armor:
			if (player.getCurrentArmor(ArmorIndex.EQUIPPED_CHEST) != null && player.getCurrentArmor(ArmorIndex.EQUIPPED_CHEST).getItem() == ZSSItems.magicArmor) {
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
		if (source instanceof IDamageType) {
			Set<EnumDamageType> damageTypes = ((IDamageType) source).getEnumDamageTypes();
			if (damageTypes != null) {
				for (EnumDamageType type : damageTypes) {
					if (EnumDamageType.damageResistMap.get(type) != null) {
						int resist = info.getBuffAmplifier(EnumDamageType.damageResistMap.get(type));
						resist -= ((IDamageType) source).getIgnoreResistAmount(type);
						if (resist > 0) {
							amount *= 1.0F - (resist * 0.01F);
						}
					}
				}
			}
		} else {
			// Handle resistances to vanilla damage types from non-IDamageType damage sources
			if (source.isFireDamage()) {
				amount *= 1.0F - (info.getBuffAmplifier(Buff.RESIST_FIRE) * 0.01F);
			}
			if (source.isMagicDamage()) {
				amount *= 1.0F - (info.getBuffAmplifier(Buff.RESIST_MAGIC) * 0.01F);
			}
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
		} else {
			// Handle weaknesses to vanilla damage types from non-IDamageType damage sources
			if (source.isFireDamage()) {
				amount *= 1.0F + (info.getBuffAmplifier(Buff.WEAKNESS_FIRE) * 0.01F);
			}
			if (source.isMagicDamage()) {
				amount *= 1.0F + (info.getBuffAmplifier(Buff.WEAKNESS_MAGIC) * 0.01F);
			}
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

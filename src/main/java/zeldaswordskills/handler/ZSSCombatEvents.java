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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.Event;
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
import zeldaswordskills.api.item.IZoom;
import zeldaswordskills.api.item.IZoomHelper;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ItemArmorTunic;
import zeldaswordskills.item.ItemFairyBottle;
import zeldaswordskills.item.ItemHeldBlock;
import zeldaswordskills.item.ItemZeldaShield;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.ActivateSkillPacket;
import zeldaswordskills.network.AddExhaustionPacket;
import zeldaswordskills.network.MortalDrawPacket;
import zeldaswordskills.network.UnpressKeyPacket;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.skills.sword.Dodge;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.skills.sword.SpinAttack;
import zeldaswordskills.skills.sword.SwordBasic;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Event handler for all combat-related events
 *
 */
public class ZSSCombatEvents
{
	/**
	 * FOV is determined initially in EntityPlayerSP; fov is recalculated for
	 * the vanilla bow only in the case that zoom-enhancing gear is worn
	 */
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void updateFOV(FOVUpdateEvent event) {
		ItemStack stack = (event.entity.isUsingItem() ? event.entity.getItemInUse() : null);
		if (stack != null) {
			boolean flag = stack.getItem() instanceof IZoom;
			if (flag || stack.getItem() == Item.bow) {
				float magnify = 1.0F;
				for (ItemStack armor : event.entity.inventory.armorInventory) {
					if (armor != null && armor.getItem() instanceof IZoomHelper) {
						magnify += ((IZoomHelper) armor.getItem()).getMagnificationFactor();
					}
				}
				if (flag || magnify != 1.0F) {
					float maxTime = (flag ? ((IZoom) stack.getItem()).getMaxZoomTime() : 20.0F);
					float factor = (flag ? ((IZoom) stack.getItem()).getZoomFactor() : 0.15F);
					float charge = (float) event.entity.getItemInUseDuration() / maxTime;
					AttributeInstance attributeinstance = event.entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
					float fov = (event.entity.capabilities.allowFlying ? 1.1F : 1.0F);
					fov *= (attributeinstance.getAttributeValue() / (double) event.entity.capabilities.getWalkSpeed() + 1.0D) / 2.0D;
					if (event.entity.capabilities.getWalkSpeed() == 0.0F || Float.isNaN(fov) || Float.isInfinite(fov)) {
						fov = 1.0F;
					}
					if (charge > 1.0F) {
						charge = 1.0F;
					} else {
						charge *= charge;
					}
					event.newfov = fov * (1.0F - charge * magnify * factor);
				}
			}
		}
	}

	/**
	 * If the player is using an ILockOnTarget skill, this event will cancel mouse motion when locked
	 * on a target; if the skill is also an ICombo, the onAttack method will be called on left click
	 * no button clicked -1, left button 0, right click 1, middle click 2
	 * @return setCanceled(true) will prevent both the click and pressed information from executing
	 * @return setResult(Result.DENY) will stop the click, but allow the key to still register as pressed
	 */
	@ForgeSubscribe
	@SideOnly(Side.CLIENT)
	public void onMouseChanged(MouseEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
		// check pre-conditions for attacking and item use (not stunned, etc.):
		if (event.buttonstate || event.button == -1) {
			if (skills.isSkillActive(SkillBase.mortalDraw)) {
				event.setCanceled(true);
			} else if (event.button == 0) {
				Item heldItem = (player.getHeldItem() != null ? player.getHeldItem().getItem() : null);
				event.setCanceled(ZSSEntityInfo.get(player).isBuffActive(Buff.STUN) || heldItem instanceof ItemHeldBlock ||
						(player.attackTime > 0 && (Config.affectAllSwings() || heldItem instanceof ISwingSpeed)));
			} else if (event.button == 1) {
				event.setCanceled(ZSSEntityInfo.get(player).isBuffActive(Buff.STUN));
			}
		}
		if (event.isCanceled()) {
			return;
		}
		ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
		if (skill != null && skill.isLockedOn()) {
			if (event.button == 0 && event.buttonstate) {
				if (Config.allowVanillaControls() && PlayerUtils.isHoldingSword(player)) {
					if (skill instanceof SwordBasic) {
						// Whether or not ArmorBreak should receive key pressed information for charging up
						boolean canCharge = true;

						if (!skills.canInteract()) {
							if (skills.isSkillActive(SkillBase.spinAttack)) {
								((SpinAttack) skills.getPlayerSkill(SkillBase.spinAttack)).keyPressed(mc.gameSettings.keyBindAttack, mc.thePlayer);
							}
							canCharge = false;
							event.setCanceled(true);
						} else if (player.isSneaking() && skills.canUseSkill(SkillBase.swordBeam)) {
							PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.swordBeam).makePacket());
							// set to canceled to prevent secondary attack from occurring that doesn't occur with dash (thanks to blocking)
							event.setCanceled(true);
						} else if (skills.hasSkill(SkillBase.dash) && player.onGround && ((Dash) skills.getPlayerSkill(SkillBase.dash)).isRMBDown()) {
							PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.dash).makePacket());
							event.setCanceled(player.getItemInUse() == null);
						} else {
							performComboAttack(mc, skill);
						}
						// handle separately so can attack and begin charging without pressing key twice
						if (skills.hasSkill(SkillBase.armorBreak) && canCharge) {
							((ArmorBreak) skills.getPlayerSkill(SkillBase.armorBreak)).keyPressed(player);
						}
					} else { // Generic ILockOnTarget skill simply attacks; handles possibility of being ICombo
						performComboAttack(mc, skill);
					}
				} else if (skills.hasSkill(SkillBase.mortalDraw) && ((MortalDraw) skills.getPlayerSkill(SkillBase.mortalDraw)).isRMBDown() && player.getHeldItem() == null) {
					PacketDispatcher.sendPacketToServer(new ActivateSkillPacket(SkillBase.mortalDraw).makePacket());
					event.setCanceled(true);
				} else { // Vanilla controls not enabled simply attacks; handles possibility of being ICombo
					performComboAttack(mc, skill);
				}
				// Setting result to DENY prevents click processing but still sets button.pressed to true
				event.setResult(Event.Result.DENY);
			} else if (event.button == 1 && Config.allowVanillaControls() && skill instanceof SwordBasic) {
				if (skills.isSkillActive(SkillBase.spinAttack) || skills.isSkillActive(SkillBase.leapingBlow)) {
					event.setCanceled(true);
				} else if (skills.hasSkill(SkillBase.dash) && PlayerUtils.isHoldingSword(player)) {
					((Dash) skills.getPlayerSkill(SkillBase.dash)).keyPressed(event.buttonstate);
				} else if (skills.hasSkill(SkillBase.mortalDraw) && player.getHeldItem() == null) {
					((MortalDraw) skills.getPlayerSkill(SkillBase.mortalDraw)).keyPressed(event.buttonstate);
				}
			} else if (event.button == -1) {
				player.inventory.changeCurrentItem(event.dwheel);
				event.setCanceled(true);
			}
		} else { // not locked on to a target, normal item swing
			if (event.button == 0 && event.buttonstate) {
				setPlayerAttackTime(player);
			}
		}
	}

	/**
	 * Attacks current target if player is not currently using an item and ICombo.onAttack
	 * doesn't return false (i.e. doesn't miss)
	 * @param skill must implement BOTH ILockOnTarget AND ICombo
	 */
	@SideOnly(Side.CLIENT)
	public static void performComboAttack(Minecraft mc, ILockOnTarget skill) {
		if (!mc.thePlayer.isUsingItem() || ZSSPlayerInfo.get(mc.thePlayer).isSkillActive(SkillBase.mortalDraw)) {
			mc.thePlayer.swingItem();
			setPlayerAttackTime(mc.thePlayer);
			if (skill instanceof ICombo && ((ICombo) skill).onAttack(mc.thePlayer)) {
				Entity entity = TargetUtils.getMouseOverEntity();
				mc.playerController.attackEntity(mc.thePlayer, (entity != null ? entity : skill.getCurrentTarget()));
			}
		}
	}

	/**
	 * Sets the attack timer for the player if using an ISwingSpeed item
	 * All other items default to vanilla behavior, which is spam-happy
	 * Note that the attackTime is deliberately NOT synced between client
	 * and server; otherwise the smash mechanics will break
	 */
	public static void setPlayerAttackTime(EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			ItemStack stack = player.getHeldItem();
			if (stack != null && stack.getItem() instanceof ISwingSpeed) {
				player.attackTime = Math.max(player.attackTime, ((ISwingSpeed) stack.getItem()).getSwingSpeed());
				if (player.worldObj.isRemote) {
					float exhaustion = ((ISwingSpeed) stack.getItem()).getExhaustion();
					if (exhaustion > 0.0F) {
						PacketDispatcher.sendPacketToServer(new AddExhaustionPacket(exhaustion).makePacket());
					}
				} else {
					PacketDispatcher.sendPacketToPlayer(new UnpressKeyPacket(UnpressKeyPacket.LMB).makePacket(), (Player) player);
				}
			}
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
			ZSSPlayerInfo skills = ZSSPlayerInfo.get(player);
			if (skills.isSkillActive(SkillBase.dodge)) {
				event.setCanceled(((Dodge) skills.getPlayerSkill(SkillBase.dodge)).dodgeAttack(player));
			} else if (skills.isSkillActive(SkillBase.parry)) {
				if (event.source.getSourceOfDamage() instanceof EntityLivingBase) {
					EntityLivingBase attacker = (EntityLivingBase) event.source.getSourceOfDamage();
					event.setCanceled(((Parry) skills.getPlayerSkill(SkillBase.parry)).parryAttack(player, attacker));
				}
			} else if (skills.isSkillActive(SkillBase.mortalDraw) && event.source.getEntity() != null) {
				if (!player.worldObj.isRemote) {
					if (((MortalDraw) skills.getPlayerSkill(SkillBase.mortalDraw)).drawSword(player, event.source.getEntity())) {
						PacketDispatcher.sendPacketToPlayer(new MortalDrawPacket().makePacket(), (Player) player);
						event.setCanceled(true);
					}
				}
			}
			// prevent non-entity sources of fire damage here to avoid hurt animation while in fire / lava
			if (event.source.isFireDamage() && event.source.getSourceOfDamage() == null && !event.isCanceled()) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
		} else if (!event.isCanceled() && event.entity instanceof EntityLivingBase && event.source.getEntity() != null) {
			EntityLivingBase entity = (EntityLivingBase) event.entity;
			float evade = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_UP) * 0.01F;
			if (evade > 0.0F) {
				float penalty = ZSSEntityInfo.get(entity).getBuffAmplifier(Buff.EVADE_DOWN) * 0.01F;
				if (entity.worldObj.rand.nextFloat() < evade - penalty) {
					entity.worldObj.playSoundAtEntity(entity, ModInfo.SOUND_SWORDMISS, (entity.worldObj.rand.nextFloat() * 0.4F + 0.5F), 1.0F / (entity.worldObj.rand.nextFloat() * 0.4F + 0.5F));
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
			ItemStack stack = player.getItemInUse();
			if (stack != null && stack.getItem() instanceof ItemZeldaShield) {
				ItemZeldaShield shield = (ItemZeldaShield) stack.getItem();
				if (ZSSPlayerInfo.get(player).canBlock() && shield.canBlockDamage(stack, event.source)) {
					boolean shouldBlock = true;
					Entity opponent = event.source.getEntity();
					if (opponent != null) {
						double dx = opponent.posX - event.entity.posX;
						double dz;
						for (dz = opponent.posZ - player.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random()) * 0.01D) {
							dx = (Math.random() - Math.random()) * 0.01D;
						}

						float yaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - player.rotationYaw;
						yaw = yaw - 90;
						while (yaw < -180) { yaw+= 360; }
						while (yaw >= 180) { yaw-=360; }
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
			if (combo != null && combo.getCombo() != null && !combo.getCombo().isFinished()) {
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
				// use dirty accessor to avoid checking / setting hurt resistant time
				DirtyEntityAccessor.damageEntity(event.entityLiving, DamageUtils.causeIArmorBreakDamage(player), damage);
				event.ammount -= damage;
			}
		}

		applyDamageModifiers(event);

		// apply magic armor and combo onHurt last, after other resistances
		if (event.ammount > 0.0F && event.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entity;
			/* TODO magic armor:
				if (player.getCurrentArmor(ArmorIndex.WORN_CHEST) != null && player.getCurrentArmor(ArmorIndex.WORN_CHEST).getItem() == ZSSItems.tunicGoronChest) {
					while (event.ammount > 0 && player.inventory.consumeInventoryItem(Item.emerald.itemID)) {
						event.ammount -= 1.0F;
					}
					event.setCanceled(event.ammount < 0.1F);
				}
			 */
			if (event.source.isFireDamage() && !event.isCanceled()) {
				event.setCanceled(ItemArmorTunic.onFireDamage(player, event.ammount));
			}
			if (!event.isCanceled()) {
				return;
			}
			ICombo combo = ZSSPlayerInfo.get(player).getComboSkill();
			if (combo != null && event.ammount > 0) {
				combo.onPlayerHurt(player, event);
			}
		}
		// update combo last, after all resistances and weaknesses are accounted for
		if (event.ammount > 0.0F && event.source.getEntity() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.source.getEntity();
			ICombo combo = ZSSPlayerInfo.get(player).getComboSkill();
			if (combo != null) {
				combo.onHurtTarget(player, event);
			}
		}
		handleSecondaryEffects(event);
	}

	@ForgeSubscribe
	public void onLivingDeathEvent(LivingDeathEvent event) {
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer) {
			event.setCanceled(ItemFairyBottle.onDeath((EntityPlayer) event.entity));
			if (!event.isCanceled()) {
				ZSSPlayerInfo.saveProxyData((EntityPlayer) event.entity);
			}
		}
		if (event.source.getEntity() instanceof EntityPlayer && event.entity instanceof EntityMob) {
			ItemZeldaSword.onKilledMob((EntityPlayer) event.source.getEntity(), (EntityMob) event.entity);
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
		if (event.source instanceof IDamageType) {
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

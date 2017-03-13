/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.skills.sword;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zeldaswordskills.api.damage.IComboDamage;
import zeldaswordskills.api.damage.IComboDamage.IComboDamageFull;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.EndComboPacket;
import zeldaswordskills.network.server.TargetIdPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * BASIC SWORD SKILL
 * Description: Foundation for all other Sword Skills
 * Activation: Standard (toggle), but must be looking near a target within range
 * Effects:	1. must be active in order to use any of the Sword Skills (see below)
 * 			2. camera locks on to target so long as player remains within range
 * 			3. chain up to (2 + level) attacks:
 * 				- each attack adds the combo's current size minus one to damage
 * 				- taking more than (0.5F * level) in damage at once will terminate an ongoing combo, as will
 * 					missing a strike or taking too long between consecutive hits
 * Exhaustion: 0.0F - does not cost exertion to use
 * Duration: (a) targeting: unlimited
 * 			 (b) combo: time allowed between strikes is 20 ticks + (2 * level)
 * Range: 6 + level, distance within which targets can be acquired, in blocks
 * Special:	- intended (but not required) for player to use keyboard instead of mouse while skill is active
 * 			- deactivates if the player is no longer holding a sword or if there are no longer any valid targets
 * 
 * Basic sword technique skill; it is a prerequisite for all other sword skills and may only
 * remain active while a sword is in hand.
 * 
 * While active, the player's field of view is locked onto the current target; pressing the 'next
 * target' key (default TAB) will switch to the next closest available target that hasn't been
 * targeted before, or the previous target if no new targets are available
 * 
 * While active, 'R-Ctrl' may be used to block in lieu of the right mouse button
 * 
 * Combos may be performed while locked on using normal attacks and any known sword skills.
 * 
 * Up to 3 attacks can be chained at level 1, plus an additional attack per skill level. Additionally,
 * each Basic Sword level increases the amount of time allowed between successive attacks while
 * chaining combos, the amount of damage the player can take before the combo is broken and also the
 * maximum distance at which the player may remain locked on to a target.
 * 
 * Default Controls
 * Tab (tap) - acquire next target
 * RCtrl (hold) - block
 * Up arrow (tap) - regular attack
 * Up arrow (tap while jumping) - Leaping Blow
 * Up arrow (tap while blocking) - Slam
 * Up arrow (hold) - Armor Break
 * Left / Right arrow (tap) - Dodge
 * Left / Right arrow (hold) - Spin Attack
 * Down arrow (tap) - Parry/Disarm
 * 
 */
public class SwordBasic extends SkillActive implements ICombo, ILockOnTarget
{
	/** True if this skill is currently active */
	private boolean isActive = false;

	/** The current target, if any; kept synchronized between the client and server */
	private EntityLivingBase currentTarget = null;

	/** The previous target; only used client side */
	@SideOnly(Side.CLIENT)
	private EntityLivingBase prevTarget;

	/** Set to a new instance each time a combo begins */
	private Combo combo = null;

	public SwordBasic(String name) {
		super(name);
	}

	private SwordBasic(SwordBasic skill) {
		super(skill);
	}

	@Override
	public SwordBasic newInstance() {
		return new SwordBasic(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(List<String> desc, EntityPlayer player) {
		desc.add(getRangeDisplay(getRange()));
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 1), getMaxComboSize()));
		desc.add(getTimeLimitDisplay(getComboTimeLimit()));
		desc.add(StatCollector.translateToLocalFormatted(getInfoString("info", 2), String.format("%.1f", (0.5F * level))));
	}

	@Override
	public boolean canUse(EntityPlayer player) {
		return level > 0;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public boolean hasAnimation() {
		return false;
	}

	@Override
	protected float getExhaustion() {
		return 0.0F;
	}

	@Override
	public byte getMaxLevel() {
		return (MAX_LEVEL * 2);
	}

	/** Returns amount of time allowed between successful attacks before combo terminates */
	private final int getComboTimeLimit() {
		return (20 + (level * 2));
	}

	/** Returns the max combo size attainable (2 plus skill level) */
	private final int getMaxComboSize() {
		return (2 + level);
	}

	/** Returns max distance at which targets may be acquired or remain targetable */
	private final int getRange() {
		return (6 + level);
	}

	@Override
	protected boolean onActivated(World world, EntityPlayer player) {
		if (isActive) { // deactivate if already active
			onDeactivated(world, player); // don't need to use deactivate, as packets already sent
		} else { // otherwise activate
			isActive = true;
			if (!isComboInProgress()) {
				combo = null;
			}
			currentTarget = TargetUtils.acquireLookTarget(player, getRange(), getRange(), true);
		}
		return true;
	}

	@Override
	protected void onDeactivated(World world, EntityPlayer player) {
		isActive = false;
		currentTarget = null;
		if (world.isRemote) {
			prevTarget = null;
		}
	}

	@Override
	public void onUpdate(EntityPlayer player) {
		if (isActive() && player.worldObj.isRemote) {
			if (Minecraft.getMinecraft().currentScreen != null  || !updateTargets(player)) {
				deactivate(player);
			}
		}
		if (isComboInProgress()) {
			combo.onUpdate(player);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onRenderTick(EntityPlayer player, float partialTickTime) {
		double dx = player.posX - currentTarget.posX;
		double dz = player.posZ - currentTarget.posZ;
		double angle = Math.atan2(dz, dx) * 180 / Math.PI;
		double pitch = Math.atan2(player.posY - (currentTarget.posY + (currentTarget.height / 2.0F)), Math.sqrt(dx * dx + dz * dz)) * 180 / Math.PI;
		double distance = player.getDistanceToEntity(currentTarget);
		float rYaw = (float)(angle - player.rotationYaw);
		while (rYaw > 180) { rYaw -= 360; }
		while (rYaw < -180) { rYaw += 360; }
		rYaw += 90F;
		float rPitch = (float) pitch - (float)(10.0F / Math.sqrt(distance)) + (float)(distance * Math.PI / 90);
		player.setAngles(rYaw, -(rPitch - player.rotationPitch));
		return false;
	}

	@Override
	public final boolean isLockedOn() {
		return currentTarget != null;
	}

	@Override
	public final EntityLivingBase getCurrentTarget() {
		return currentTarget;
	}

	@Override
	public void setCurrentTarget(EntityPlayer player, Entity target) {
		if (target instanceof EntityLivingBase) {
			currentTarget = (EntityLivingBase) target;
		} else { // null or invalid target, deactivate skill
			deactivate(player);
		}
	}

	/**
	 * Returns the next closest new target or locks on to the previous target, if any
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public final void getNextTarget(EntityPlayer player) {
		EntityLivingBase nextTarget = null;
		double dTarget = 0;
		List<EntityLivingBase> list = TargetUtils.acquireAllLookTargets(player, getRange(), getRange());
		for (EntityLivingBase entity : list) {
			if (entity == player) { continue; }
			if (entity != currentTarget && entity != prevTarget && isTargetValid(player, entity)) {
				if (nextTarget == null) {
					dTarget = player.getDistanceSqToEntity(entity);
					nextTarget = entity;
				} else {
					double distance = player.getDistanceSqToEntity(entity);
					if (distance < dTarget) {
						nextTarget = entity;
						dTarget = distance;
					}
				}
			}
		}
		if (nextTarget != null) {
			prevTarget = currentTarget;
			currentTarget = nextTarget;
		} else {
			nextTarget = currentTarget;
			currentTarget = prevTarget;
			prevTarget = nextTarget;
		}
		PacketDispatcher.sendToServer(new TargetIdPacket(this));
	}

	/**
	 * Updates targets, setting to null if no longer valid and acquiring new target if necessary
	 * @return returns true if the current target is valid
	 */
	@SideOnly(Side.CLIENT)
	private boolean updateTargets(EntityPlayer player) {
		if (!isTargetValid(player, prevTarget) || !TargetUtils.isTargetInSight(player, prevTarget)) {
			prevTarget = null;
		}
		if (!isTargetValid(player, currentTarget)) {
			currentTarget = null;
			if (Config.enableAutoTarget) {
				getNextTarget(player);
			}
		}
		return isTargetValid(player, currentTarget);
	}

	/**
	 * Returns true if target entity is valid: not dead and still within lock-on range
	 */
	@SideOnly(Side.CLIENT)
	private boolean isTargetValid(EntityPlayer player, EntityLivingBase target) {
		return (target != null && !target.isDead && target.getHealth() > 0F &&
				player.getDistanceToEntity(target) < (float) getRange() && !target.isInvisible() &&
				(Config.canTargetPlayers || !(target instanceof EntityPlayer)));
	}

	@Override
	public final Combo getCombo() {
		return combo;
	}

	@Override
	public final void setCombo(Combo combo) {
		this.combo = combo;
	}

	@Override
	public final boolean isComboInProgress() {
		return (combo != null && !combo.isFinished());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean onAttack(EntityPlayer player) {
		Entity mouseOver = TargetUtils.getMouseOverEntity();
		boolean attackHit = (isLockedOn() && mouseOver != null && TargetUtils.canReachTarget(player, mouseOver));
		if (!attackHit) {
			PlayerUtils.playRandomizedSound(player, Sounds.SWORD_MISS, 0.4F, 0.5F);
			if (isComboInProgress()) {
				PacketDispatcher.sendToServer(new EndComboPacket(this));
			}
		}
		return attackHit;
	}

	@Override
	public void onHurtTarget(EntityPlayer player, LivingHurtEvent event) {
		if (!isValidComboDamage(player, event.source)) { return; }
		if (combo == null || combo.isFinished()) {
			combo = new Combo(player, this, getMaxComboSize(), getComboTimeLimit());
		}
		float damage = DirtyEntityAccessor.getModifiedDamage(event.entityLiving, event.source, event.ammount);
		if (damage > 0) {
			if (!(event.source instanceof IComboDamageFull) || ((IComboDamageFull) event.source).increaseComboCount(player)) {
				combo.add(player, event.entityLiving, damage);
			} else {
				combo.addDamageOnly(player, damage, ((IComboDamageFull) event.source).applyDamageToPrevious(player));
			}
		}
		String sound = getComboDamageSound(player, event.source);
		if (sound != null) {
			WorldUtils.playSoundAtEntity(player, sound, 0.4F, 0.5F);
		}
	}

	private boolean isValidComboDamage(EntityPlayer player, DamageSource source) {
		if (source instanceof IComboDamage) {
			return ((IComboDamage) source).isComboDamage(player);
		}
		return !source.isProjectile();
	}

	private String getComboDamageSound(EntityPlayer player, DamageSource source) {
		if (source instanceof IComboDamageFull && !((IComboDamageFull) source).playDefaultSound(player)) {
			return ((IComboDamageFull) source).getHitSound(player);
		} else if (source.getDamageType().equals("player")) {
			return (PlayerUtils.isSword(player.getHeldItem()) ? Sounds.SWORD_CUT : Sounds.HURT_FLESH);
		}
		return null;
	}

	@Override
	public void onPlayerHurt(EntityPlayer player, LivingHurtEvent event) {
		if (isComboInProgress() && DirtyEntityAccessor.getModifiedDamage(player, event.source, event.ammount) > (0.5F * level)) {
			WorldUtils.playSoundAtEntity(player, Sounds.GRUNT, 0.3F, 0.8F);
			combo.endCombo(player);
		}
	}
}

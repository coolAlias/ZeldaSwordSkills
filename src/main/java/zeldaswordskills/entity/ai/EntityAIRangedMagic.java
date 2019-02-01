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

package zeldaswordskills.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;

/**
 * 
 * AI for mobs with ranged magical attacks which require charging up for some
 * number of ticks before they can be released.
 * 
 * Caster must first be within range and, after enough time passes since the last attack,
 * will begin charging up the spell. While charging, the entity will not move until the
 * spell is cast, calling {@link IMagicUser#castRangedSpell}.
 * 
 * If the spell needs to be interrupted, for example if the casting entity is damaged,
 * call {@link #interruptCasting}.
 * 
 * Uses MutexBit 8 + 4 + 1.
 *
 */
public class EntityAIRangedMagic extends EntityAIBase
{
	/** Same entity as caster, but as an instance of EntityLiving */
	private final EntityLiving entity;

	/** Same as entity, but as an instance of IMagicUser */
	private final IMagicUser caster;

	/** Timer for casting time */
	private int castingTimer;

	/** Minimum time between casting attempts */
	private final int minCastInterval;

	/** Maximum time between casting attempts */
	private final int maxCastInterval;

	/** Time since last attack */
	private int attackTimer;

	/** Distance at which max damage will be inflicted, with damage decreasing moving closer to the target */
	private final double minDistance;

	/** Distance squared at which a target is considered 'close enough' to attack */
	private final double minDistanceSq;

	/** Current attack target */
	private EntityLivingBase attackTarget;

	/** Number of ticks target has been out of sight */
	private int unseenTimer;

	/** Maximum time target can remain out of sight before caster aborts spell */
	private static final int MAX_TIME_UNSEEN = 10;

	/**
	 * @param entity			The spell-casting entity
	 * @param minCastInterval	Minimum interval between spell-casting attempts
	 * @param maxCastInterval	Maximum interval between spell-casting attempts
	 * @param distance			Interval between casting attempts reaches max at this distance,
	 * 							and is also the casting range for attacking targets
	 */
	public <T extends EntityLiving & IMagicUser> EntityAIRangedMagic(T entity, int minCastInterval, int maxCastInterval, double distance) {
		this.entity = entity;
		this.caster = entity;
		this.minCastInterval = minCastInterval;
		this.maxCastInterval = maxCastInterval;
		this.minDistance = distance;
		this.minDistanceSq = (distance * distance);
		this.setMutexBits(15); // incompatible with swimming, teleporting, and bit1 and bit2 tasks
	}

	/**
	 * Forcefully interrupts the spell-casting attempt, such as when taking damage
	 */
	public void interruptCasting() {
		resetTask();
	}

	@Override
	public boolean continueExecuting() {
		if (attackTarget == null) {
			return false;
		} else if (castingTimer < 1) {
			return false;
		} else if (!entity.getEntitySenses().canSee(attackTarget) && ++unseenTimer > MAX_TIME_UNSEEN) {
			unseenTimer = 0;
			return false;
		} else if (!caster.canContinueCasting()) {
			return false;
		}
		return entity.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ) < minDistanceSq;
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = entity.getAttackTarget();
		if (target == null) {
			return false;
		}
		attackTarget = target;
		double d = entity.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);
		boolean flag = entity.getEntitySenses().canSee(attackTarget);

		if (!flag) {
			interruptCasting(); // calls resetTask
			return false;
		}
		// Handle attackTimer here so other tasks such as teleportation have a chance to execute
		if (castingTimer == 0) {
			entity.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
			if (--attackTimer == 0) {
				if (d > minDistanceSq || !flag) {
					return false;
				}
				// task finally starts executing if spell begins charging
				castingTimer = caster.beginSpellCasting(attackTarget);
				return castingTimer > 0;
			} else if (attackTimer < 0) {
				float f = (float)(MathHelper.sqrt_double(d) / minDistance);
				attackTimer = MathHelper.floor_float(f * (float)(maxCastInterval - minCastInterval) + (float) minCastInterval);
			}
		}
		return false;
	}

	@Override
	public void startExecuting() {
		entity.getNavigator().clearPathEntity();
	}

	@Override
	public void resetTask() {
		caster.stopCasting();
		attackTarget = null;
		castingTimer = 0;
		unseenTimer = 0;
	}

	@Override
	public void updateTask() {
		if (castingTimer > 0) {
			--castingTimer;
			entity.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);
			if (castingTimer == 0) {
				double d = entity.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);
				float f = (float)(MathHelper.sqrt_double(d) / minDistance);
				float f1 = MathHelper.clamp_float(f, 0.1F, 1.0F);
				caster.castRangedSpell(attackTarget, f1);
				attackTimer = MathHelper.floor_float(f * (float)(maxCastInterval - minCastInterval) + (float) minCastInterval);
			}
		}
	}
}

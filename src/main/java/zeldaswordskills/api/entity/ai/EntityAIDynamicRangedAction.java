/**
    Copyright (C) <2016> <coolAlias>

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

package zeldaswordskills.api.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.MathHelper;

/**
 * 
 * AI to perform an action when the target is at least a certain distance away.
 *
 */
public class EntityAIDynamicRangedAction extends EntityAIDynamicAction
{
	/** Maximum range */
	protected final float range;

	protected final float minRangeSq;

	/** Minimum and maximum delay between actions */
	protected final int minDelay, maxDelay;

	/** Speed at which entity may move towards the target if it cannot be seen or is out of range */
	protected final float speed;

	/** Time for which the target has been within line-of-sight */
	protected int visibilityTimer;

	protected int delay;

	protected int timer;

	/** Whether the delay scales with difficulty, i.e. becomes shorter on higher difficulties */
	protected boolean difficultScaled;

	public <T extends EntityCreature & IEntityDynamic> EntityAIDynamicRangedAction(T entity, EntityAction action, float rangeMin, float rangeMax, int minDelay, int maxDelay, float speed, boolean require_ground) {
		this(entity, action, rangeMin, rangeMax, minDelay, maxDelay, speed, require_ground, true);
	}

	/**
	 * See {@link EntityAIDynamicAction#EntityAIDynamicAction(EntityCreature, EntityAction, float, boolean, boolean) EntityAIDynamicAction}
	 * @param rangeMin Mininum range at which this action should be begun
	 * @param minDelay Minimum delay between actions, give or take a little
	 * @param maxDelay Maximum delay between actions, give or take a little
	 * @param speed    Speed at which the entity will move towards a target to close range or gain visibility
	 */
	public <T extends EntityCreature & IEntityDynamic> EntityAIDynamicRangedAction(T entity, EntityAction action, float rangeMin, float rangeMax, int minDelay, int maxDelay, float speed, boolean require_ground, boolean require_sight) {
		super(entity, action, rangeMax, require_ground, require_sight);
		this.range = rangeMax;
		this.minRangeSq = rangeMin * rangeMin;
		this.minDelay = minDelay;
		this.maxDelay = maxDelay;
		this.speed = speed;
	}

	/**
	 * Sets the delay timer to scale inversely with difficulty (i.e. shorter delay on higher difficulty)
	 */
	public EntityAIDynamicRangedAction setDifficultyScaled() {
		this.difficultScaled = true;
		return this;
	}

	@Override
	protected boolean checkRange() {
		double distance = entity.getDistanceSqToEntity(target);
		return distance > minRangeSq && distance <= rangeSq;
	}

	@Override
	protected boolean canPerformAction() {
		if (target == null || !target.isEntityAlive()) {
			return false;
		} else if (require_ground && !entity.onGround) {
			return false;
		} else if (require_sight && !entity.getEntitySenses().canSee(target)) {
			return false;
		} // don't check range again; shoot even if target is too close or too far
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return super.continueExecuting() || !this.entity.getNavigator().noPath();
	}

	@Override
	public boolean shouldExecute() {
		if (super.shouldExecute()) {
			if (target == null) {
				return true; // action in progress, but no target
			} else if (delay == 0) {
				float f = this.entity.getDistanceToEntity(this.target) / this.range;
				float scale = (this.difficultScaled ? 2.0F - 0.5F * this.entity.worldObj.difficultySetting.getDifficultyId() : 1.0F);
				this.delay = MathHelper.floor_float(f * scale * (float)(this.maxDelay - this.minDelay));
				this.delay += this.minDelay - this.entity.worldObj.rand.nextInt(Math.max(1, this.minDelay));
			}
			return timer++ > delay;
		} else if (target != null) {
			moveIntoPosition();
		}
		return false;
	}

	/**
	 * Attempts to move the actor to within a suitable range from the target;
	 * expects current target to be non-null
	 */
	protected boolean moveIntoPosition() {
		double distance = this.entity.getDistanceSq(this.target.posX, this.target.boundingBox.minY, this.target.posZ);
		boolean flag = this.entity.getEntitySenses().canSee(this.target);
		if (flag) {
			++this.visibilityTimer;
		} else {
			this.visibilityTimer = 0;
		}
		if (distance <= (double) this.rangeSq && this.visibilityTimer >= 20) {
			this.entity.getNavigator().clearPathEntity();
			return false;
		} else if (speed > 0.0F) {
			this.entity.getNavigator().tryMoveToEntityLiving(this.target, this.speed);
		}
		this.entity.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
		return speed > 0.0F;
	}

	@Override
	public void resetTask() {
		super.resetTask();
		target = null;
		delay = 0;
		timer = 0;
		visibilityTimer = 0;
	}
}

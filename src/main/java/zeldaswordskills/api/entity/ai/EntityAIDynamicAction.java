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

package zeldaswordskills.api.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import zeldaswordskills.api.entity.IEntityCustomTarget;

/**
 * 
 * Basic AI to perform any action on the current AI target once the action frame is reached.
 *
 */
public class EntityAIDynamicAction<T extends EntityCreature & IEntityDynamic> extends EntityAIDynamic<T>
{
	protected Entity target;
	protected final float rangeSq;
	protected final boolean require_ground;
	protected final boolean require_sight;

	public EntityAIDynamicAction(T entity, EntityAction action, float range, boolean require_ground) {
		this(entity, action, range, require_ground, true);
	}

	/**
	 * See {@link EntityAIDynamicAction#EntityAIDynamicAction(EntityCreature, EntityAction, float, boolean, boolean) EntityAIDynamicAction}
	 * @param range Maximum range at which this action can be performed
	 * @param require_ground Whether the entity must be on the ground to perform the action
	 * @param require_sight Whether the entity requires line-of-sight to the target to perform the action
	 */
	public EntityAIDynamicAction(T entity, EntityAction action, float range, boolean require_ground, boolean require_sight) {
		super(entity, action, 0, 3);
		this.rangeSq = (range * range);
		this.require_ground = require_ground;
		this.require_sight = require_sight;
	}

	/**
	 * Return the entity's current attack target
	 */
	protected Entity getTarget() {
		return this.actor.getAttackTarget();
	}

	@Override
	public boolean shouldExecute() {
		this.target = this.getTarget();
		if (this.actor.getActionTime(this.action.id) > 0) {
			return true; // keeps the action going even if target moves out of range
		}
		return canPerformAction() && super.shouldExecute();
	}

	@Override
	protected void updateActionState(int frame, int action_frame, float speed) {
		if (frame < action_frame && this.target != null) {
			this.actor.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
		}
	}

	@Override
	protected boolean canPerformAction() {
		if (this.target == null || !this.target.isEntityAlive()) {
			return false;
		} else if (this.require_ground && !this.actor.onGround) {
			return false;
		} else if (this.require_sight && !this.actor.getEntitySenses().canSee(this.target)) {
			return false;
		} else if (!checkRange()) {
			return false;
		}
		return true;
	}

	/**
	 * Checks whether entity is within range of the target to perform the action
	 */
	protected boolean checkRange() {
		return this.actor.getDistanceSqToEntity(this.target) <= this.rangeSq;
	}

	@Override
	public void resetTask() {
		super.resetTask();
		this.target = null;
	}

	/**
	 * 
	 * Same as EntityAIDynamicAction but it uses {@link IEntityCustomTarget#getCustomTarget()}
	 * to retrieve the current target.
	 *
	 */
	public static class EntityAIDynamicCustomTarget<T extends EntityCreature & IEntityDynamic & IEntityCustomTarget> extends EntityAIDynamicAction<T>
	{
		public EntityAIDynamicCustomTarget(T entity, EntityAction action, float range, boolean require_ground) {
			this(entity, action, range, require_ground, true);
		}

		public EntityAIDynamicCustomTarget(T entity, EntityAction action, float range, boolean require_ground, boolean require_sight) {
			super(entity, action, range, require_ground, require_sight);
		}

		@Override
		protected Entity getTarget() {
			return this.actor.getCurrentTarget();
		}
	}
}

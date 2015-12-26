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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import zeldaswordskills.api.entity.IEntityCustomTarget;

/**
 * 
 * Basic AI to perform any action on the current AI target once the action frame is reached.
 *
 */
public class EntityAIDynamicAction extends EntityAIDynamic
{
	protected Entity target;
	protected final float rangeSq;
	protected final boolean require_ground;
	protected final boolean require_sight;

	public <T extends EntityCreature & IEntityDynamic> EntityAIDynamicAction(T entity, EntityAction action, float range, boolean require_ground) {
		this(entity, action, range, require_ground, true);
	}

	/**
	 * See {@link EntityAIDynamicAction#EntityAIDynamicAction(EntityCreature, EntityAction, float, boolean, boolean) EntityAIDynamicAction}
	 * @param range Maximum range at which this action can be performed
	 * @param require_ground Whether the entity must be on the ground to perform the action
	 * @param require_sight Whether the entity requires line-of-sight to the target to perform the action
	 */
	public <T extends EntityCreature & IEntityDynamic> EntityAIDynamicAction(T entity, EntityAction action, float range, boolean require_ground, boolean require_sight) {
		super(entity, action, 0, 3);
		this.rangeSq = (range * range);
		this.require_ground = require_ground;
		this.require_sight = require_sight;
	}

	/**
	 * Return the entity's current attack target
	 */
	protected Entity getTarget() {
		return entity.getAttackTarget();
	}

	@Override
	public boolean shouldExecute() {
		this.target = this.getTarget();
		if (actor.getActionTime(action.id) > 0) {
			return true; // keeps the action going even if target moves out of range
		}
		return canPerformAction() && super.shouldExecute();
	}

	@Override
	protected void updateActionState(int frame, int action_frame, float speed) {
		if (frame < action_frame && target != null) {
			entity.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
		}
	}

	@Override
	protected boolean canPerformAction() {
		if (target == null || !target.isEntityAlive()) {
			return false;
		} else if (require_ground && !entity.onGround) {
			return false;
		} else if (require_sight && !entity.getEntitySenses().canSee(target)) {
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
		return entity.getDistanceSqToEntity(target) <= rangeSq;
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
	public static class EntityAIDynamicCustomTarget extends EntityAIDynamicAction
	{
		/** IEntityCustomTarget version of parent entity instance used to retrieve current target */
		protected final IEntityCustomTarget targeting;

		public <T extends EntityCreature & IEntityDynamic & IEntityCustomTarget> EntityAIDynamicCustomTarget(T entity, EntityAction action, float range, boolean require_ground) {
			this(entity, action, range, require_ground, true);
		}

		public <T extends EntityCreature & IEntityDynamic & IEntityCustomTarget> EntityAIDynamicCustomTarget(T entity, EntityAction action, float range, boolean require_ground, boolean require_sight) {
			super(entity, action, range, require_ground, require_sight);
			this.targeting = entity;
		}

		@Override
		protected Entity getTarget() {
			return targeting.getCurrentTarget();
		}
	}
}

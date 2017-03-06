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

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * 
 * Base AI class for AI tasks that interact with {@link IEntityDynamic} entities.
 *
 */
public class EntityAIDynamic<T extends EntityCreature & IEntityDynamic> extends EntityAIBase implements IEntityDynamicAI
{
	/** The dynamic entity */
	protected final T actor;

	/** The action to perform */
	protected final EntityAction action;

	/** Flag set to true once the action tick has been performed */
	protected boolean performed;

	/**
	 * Basic AI class that interacts with an {@link IEntityDynamic}, performing a delayed action
	 * @param entity The dynamic entity
	 * @param action The action to perform once the action_tick is reached
	 * @param action_tick Frame on which the action should be performed
	 * @param mutex Mutex bits for determining if this AI can run concurrently with others
	 */
	public  EntityAIDynamic(T entity, EntityAction action, int action_tick, int mutex) {
		this.actor = entity;
		this.action = action;
		this.setMutexBits(mutex);
	}

	@Override
	public boolean shouldExecute() {
		return this.actor.canExecute(this.action.id, this);
	}

	@Override
	public void startExecuting() {
		this.actor.beginAction(this.action.id, this);
	}

	@Override
	public boolean continueExecuting() {
		if (this.actor.getActiveActions().isEmpty() || !this.actor.getActiveActions().contains(this.action)) {
			return false; // stop executing if action no longer active
		} else if (!super.continueExecuting()) {
			return false; // super calls #shouldExecute which calls IEntityDynamic#canExecute
		}
		int frame = this.actor.getActionTime(this.action.id);
		int action_duration = this.action.getDuration(this.actor.getActionSpeed(this.action.id));
		if (frame >= action_duration) {
			return false;
		}
		return true;
	}

	@Override
	public void updateTask() {
		float speed = this.actor.getActionSpeed(this.action.id);
		int action_frame = this.action.getActionFrame(speed);
		int frame = this.actor.getActionTime(this.action.id);
		updateActionState(frame, action_frame, speed);
		if (frame == action_frame && !this.performed && canPerformAction()) {
			this.actor.performAction(this.action.id, this);
			this.performed = true; // prevent action from happening twice at slower speeds
		}
	}

	/**
	 * Called from {@link #updateTask()} to allow additional behavior
	 * @param frame the current frame from {@link IEntityDynamic#getActionTime}
	 * @param action_frame the frame on which {@link IEntityDynamic#actionTick} will be called
	 * @param speed The current action speed of the entity
	 */
	protected void updateActionState(int frame, int action_frame, float speed) {}

	/**
	 * Final check before calling {@link IEntityDynamic#performAction(int, IEntityDynamicAI) actionTick}
	 * @return true to allow the action, or false to prevent it
	 */
	protected boolean canPerformAction() {
		return true;
	}

	@Override
	public void resetTask() {
		this.actor.endAction(this.action.id, this);
		this.performed = false;
	}
}

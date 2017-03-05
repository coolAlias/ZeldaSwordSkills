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

import java.util.Collections;
import java.util.List;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import zeldaswordskills.api.entity.IEntityBomb;
import zeldaswordskills.api.entity.IEntityCustomTarget;
import zeldaswordskills.api.entity.ai.EntityAIDynamicAction.EntityAIDynamicCustomTarget;

/**
 * 
 * AI that targets nearest bomb within the given range after a random delay
 *
 */
public class EntityAITargetBombs<T extends EntityCreature & IEntityDynamic & IEntityCustomTarget> extends EntityAIDynamicCustomTarget<T>
{
	/** Sorts potential targets by range */
	protected final EntityAINearestAttackableTarget.Sorter sorter;
	protected static final IEntitySelector SELECTOR = new IEntitySelector() {
		@Override
		public boolean isEntityApplicable(Entity entity) {
			return entity instanceof IEntityBomb;
		}
	};
	protected final float range;
	protected Entity targetBomb;
	protected int delay;
	protected int timer;

	/**
	 * Passes true for require_sight; see {@link EntityAITargetBombs#EntityAITargetBombs(EntityCreature, EntityAction, float, boolean, boolean) EntityAITargetBombs}
	 */
	public EntityAITargetBombs(T entity, EntityAction action, float range, boolean require_ground) {
		this(entity, action, range, require_ground, true);
	}

	/**
	 * Also see {@link EntityAIDynamicCustomTarget#EntityAIDynamicCustomTarget(EntityCreature, EntityAction, float, boolean, boolean) EntityAIDynamicCustomTarget}
	 * @param range Additionally used as the radius within which to search for bombs
	 */
	public EntityAITargetBombs(T entity, EntityAction action, float range, boolean require_ground, boolean require_sight) {
		super(entity, action, range, require_ground, require_sight);
		this.sorter = new EntityAINearestAttackableTarget.Sorter(entity);
		this.range = range;
	}

	@Override
	public boolean shouldExecute() {
		if (this.actor.getActionTime(this.action.id) > 0) {
			return true; // otherwise animation will cut short
		} else if (this.targetBomb != null && !this.targetBomb.isEntityAlive()) {
			this.softReset(); // target is no longer valid
		} else if (super.shouldExecute()) {
			if (this.delay == 0) {
				this.delay = 5 + this.actor.worldObj.rand.nextInt(10) + this.actor.worldObj.rand.nextInt(10);
				if (this.actor.getAttackTarget() != null) {
					double d = this.actor.getDistanceToEntity(this.actor.getAttackTarget());
					this.delay += MathHelper.ceiling_double_int(this.range - d);
				}
			}
			return this.timer++ > this.delay;
		}
		return false;
	}

	@Override
	public void startExecuting() {
		this.actor.setCustomTarget(targetBomb);
		super.startExecuting();
	}

	@Override
	public void resetTask() {
		super.resetTask();
		this.softReset();
	}

	/**
	 * Soft reset of AI fields for when action not yet in progress (i.e. during delay period)
	 */
	protected void softReset() {
		this.actor.setCustomTarget(null);
		this.targetBomb = null;
		this.delay = 0;
		this.timer = 0;
	}

	@Override
	protected Entity getTarget() {
		if (this.targetBomb == null && this.actor.getCustomTarget() == null) {
			if (this.timer++ < this.actor.getRNG().nextInt(20)) {
				return null;
			}
			AxisAlignedBB aabb = this.actor.boundingBox.expand(this.range, this.range / 2.0F, this.range);
			List<Entity> bombs = this.actor.worldObj.selectEntitiesWithinAABB(Entity.class, aabb, EntityAITargetBombs.SELECTOR);
			Collections.sort(bombs, sorter);
			if (!bombs.isEmpty()) {
				this.targetBomb = bombs.get(0);
			}
			this.timer = 0;
		}
		return this.targetBomb;
	}
}

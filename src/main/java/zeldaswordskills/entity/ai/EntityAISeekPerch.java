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

package zeldaswordskills.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

/**
 * 
 * Similar to EntityAIWander but the entity searches for a place to perch based on
 * {@link EntityCreature#getBlockPathWeight(BlockPos) getBlockPathWeight} and {@link IWallPerch#canPerch() canPerch}.
 * 
 * Once a suitable perch is found, the entity will become {@link IWallPerch#setPerched(boolean) perched}.
 *
 */
public class EntityAISeekPerch extends EntityAIBase
{
	protected final EntityCreature entity;
	protected IWallPerch entityPerch;
	protected BlockPos targetPos;
	protected int ticksEnRoute;
	protected final double speed;

	public <T extends EntityCreature & IWallPerch> EntityAISeekPerch(T entity, double speed) {
		this.entity = entity;
		this.entityPerch = (IWallPerch) entity;
		this.speed = speed;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		if (!entityPerch.isPerched() && (entity.getAttackTarget() == null || entity.getAITarget() == null)) {
			Vec3 vec3 = RandomPositionGenerator.findRandomTarget(entity, 10, 7);
			if (vec3 == null) {
				return false;
			} else {
				this.targetPos = new BlockPos(vec3);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean continueExecuting() {
		if (entityPerch.isPerched()) {
			return false;
		} else if (entityPerch.canPerch()) {
			entityPerch.setPerched(true);
			entity.getNavigator().clearPathEntity();
			return false;
		} else if (entity.getNavigator().noPath()) {
			double d = (entity.width * entity.width);
			return (++ticksEnRoute < 100 && entity.getDistanceSqToCenter(targetPos) >= d);
		}
		return true;
	}

	@Override
	public void startExecuting() {
		ticksEnRoute = 0;
		entity.getNavigator().tryMoveToXYZ(targetPos.getX(), targetPos.getY(), targetPos.getZ(), speed);
	}
}

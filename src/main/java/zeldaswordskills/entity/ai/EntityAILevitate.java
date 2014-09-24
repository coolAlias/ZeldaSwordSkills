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

package zeldaswordskills.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.MathHelper;

/**
 * 
 * Keeps entity within a certain distance of ground. Uses MutexBit 16.
 *
 */
public class EntityAILevitate extends EntityAIBase
{
	/** The levitating entity */
	private final EntityLivingBase entity;

	/** Minimum height to maintain */
	private final double minHeightSq;

	public EntityAILevitate(EntityLivingBase entity, double minHeight) {
		this.entity = entity;
		this.minHeightSq = minHeight * minHeight;
		this.setMutexBits(16); // compatible with everything
	}

	/**
	 * Returns true if the entity is too close to the ground
	 */
	public boolean checkHeight() {
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY);
		int z = MathHelper.floor_double(entity.posZ);
		int i = 0;
		while (i < y && !entity.worldObj.getBlock(x, y - i, z).getMaterial().isSolid() && !entity.worldObj.getBlock(x, y - i, z).getMaterial().isLiquid()) {
			++i;
		}
		return entity.getDistanceSq(x, (y - i) + 1, z) < minHeightSq;
	}

	@Override
	public boolean shouldExecute() {
		return checkHeight();
	}

	@Override
	public boolean continueExecuting() {
		return checkHeight();
	}

	@Override
	public void updateTask() {
		entity.motionY += entity.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue(); 
	}
}

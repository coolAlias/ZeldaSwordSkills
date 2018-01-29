/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.entity.projectile;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * 
 * Base class for arrows that have some special AoE effect upon impacting blocks.
 *
 */
public abstract class EntityArrowElemental extends EntityArrowCustom
{
	public EntityArrowElemental(World world) {
		super(world);
	}

	public EntityArrowElemental(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowElemental(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowElemental(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	protected double getBaseDamage() {
		return 4.0D;
	}

	@Override
	protected void onImpactBlock(MovingObjectPosition mop) {
		super.onImpactBlock(mop);
		if (!this.worldObj.isRemote && this.affectBlocks()) {
			this.setDead();
		}
	}

	/**
	 * Affects all blocks within AoE; returns true if arrow should be consumed
	 */
	protected boolean affectBlocks() {
		boolean flag = false;
		Set<BlockPos> affectedBlocks = new HashSet<BlockPos>(WorldUtils.getAffectedBlocksList(worldObj, rand, 1.5F, posX, posY, posZ, null));
		for (BlockPos pos : affectedBlocks) {
			Block block = worldObj.getBlockState(pos).getBlock();
			if (this.affectBlock(block, pos)) {
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * Called from {@link #affectBlocks()} for each block in the list
	 * @param block
	 * @param pos
	 * @return true if the block was affected and the arrow should be consumed
	 */
	protected abstract boolean affectBlock(Block block, BlockPos pos);

}

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

package zeldaswordskills.entity.projectile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityThrowingRock extends EntityMobThrowable
{
	private static final int IGNORE_WATER = 22;

	public EntityThrowingRock(World world) {
		super(world);
	}

	public EntityThrowingRock(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityThrowingRock(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityThrowingRock(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	/** Sets this throwing rock to ignore water during motion updates */
	public EntityThrowingRock setIgnoreWater() {
		dataWatcher.updateObject(IGNORE_WATER, (byte) 1);
		return this;
	}

	/** Whether this throwing rock ignores water for motion updates */
	public boolean getIgnoresWater() {
		return dataWatcher.getWatchableObjectByte(IGNORE_WATER) != 0;
	}

	@Override
	public void entityInit() {
		dataWatcher.addObject(IGNORE_WATER, (byte) 0);
		setDamage(2.0F);
	}

	@Override
	public boolean handleWaterMovement() {
		return !getIgnoresWater();
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		for (int l = 0; l < 4; ++l) {
			worldObj.spawnParticle("crit", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
		}
		if (mop.typeOfHit == EnumMovingObjectType.TILE) {
			int blockId = worldObj.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
			Block block = (blockId > 0 ? Block.blocksList[blockId] : null);
			if (block != null && block.blockMaterial != Material.air) {
				block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			}
		} else if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), getDamage());
		}
		if (!worldObj.isRemote) {
			setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("ignoreWater", getIgnoresWater() ? (byte) 1 : (byte) 0);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.getByte("ignoreWater") != 0) {
			setIgnoreWater();
		}
	}
}

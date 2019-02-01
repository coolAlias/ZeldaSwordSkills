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

package zeldaswordskills.entity.projectile;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;

public class EntityBombosFireball extends EntityMobThrowable
{
	private int lifespan = 20;

	public EntityBombosFireball(World world) {
		super(world);
		this.setFire(5);
	}

	public EntityBombosFireball(World world, EntityLivingBase thrower) {
		super(world, thrower);
		this.setFire(5);
	}

	/**
	 * Sets the number of ticks this fireball can survive
	 */
	public EntityBombosFireball setLifespan(int ticks) {
		this.lifespan = ticks;
		return this;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.015F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!worldObj.isRemote && ticksExisted > lifespan) {
			setDead();
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (!worldObj.isRemote) {
			boolean flag;
			if (mop.entityHit != null) {
				flag = mop.entityHit.attackEntityFrom(new DamageSourceFireIndirect("fireball", this, getThrower()), getDamage());
				if (flag) {
					applyEnchantments(getThrower(), mop.entityHit);
					if (!mop.entityHit.isImmuneToFire()) {
						mop.entityHit.setFire(5);
					}
				}
			} else {
				flag = true;
				if (getThrower() instanceof EntityLiving) {
					flag = worldObj.getGameRules().getBoolean("mobGriefing");
				}
				if (flag) {
					BlockPos blockpos = mop.getBlockPos().offset(mop.sideHit);
					if (worldObj.isAirBlock(blockpos)) {
						worldObj.setBlockState(blockpos, Blocks.fire.getDefaultState());
					}
				}
			}
			setDead();
		}
	}
}

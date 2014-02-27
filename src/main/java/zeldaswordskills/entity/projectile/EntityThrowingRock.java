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

package zeldaswordskills.entity.projectile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityThrowingRock extends EntityMobThrowable
{
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
	
	@Override
	public void entityInit() {
		setDamage(2.0F);
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		for (int l = 0; l < 4; ++l) {
			worldObj.spawnParticle("crit", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
		}
		
		if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), getDamage());
		}
		
		if (!worldObj.isRemote) {
			setDead();
		}
	}
}

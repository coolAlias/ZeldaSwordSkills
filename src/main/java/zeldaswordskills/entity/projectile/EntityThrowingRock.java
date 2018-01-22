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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class EntityThrowingRock extends EntityMobThrowable
{
	public EntityThrowingRock(World world) {
		super(world);
		this.setDamage(2.0F);
	}

	public EntityThrowingRock(World world, EntityLivingBase entity) {
		super(world, entity);
		this.setDamage(2.0F);
	}

	public EntityThrowingRock(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setDamage(2.0F);
	}

	public EntityThrowingRock(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		this.setDamage(2.0F);
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		return 1.0F; // reflectable by any shield
	}

	@Override
	public float getReflectedWobble(ItemStack shield, EntityPlayer player, DamageSource source) {
		return 2.0F + this.rand.nextFloat() * 13.0F;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.typeOfHit == MovingObjectType.BLOCK) {
			Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			if (block.getMaterial() != Material.air) {
				block.onEntityCollidedWithBlock(worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			}
		} else if (mop.entityHit != null) {
			mop.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, getThrower()), getDamage());
		}
		if (this.worldObj.isRemote) {
			for (int i = 0; i < 4; ++i) {
				this.worldObj.spawnParticle("crit", posX, posY, posZ, 0.0D, 0.0D, 0.0D);
			}
		} else {
			setDead();
		}
	}
}

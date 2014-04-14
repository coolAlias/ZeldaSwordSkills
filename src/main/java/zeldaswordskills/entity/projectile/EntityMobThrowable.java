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
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

/**
 * 
 * Abstract class that provides constructor for throwing entity as a mob
 *
 */
public abstract class EntityMobThrowable extends EntityThrowable
{
	/** Usually the damage this entity will cause upon impact */
	private float damage;

	public EntityMobThrowable(World world) {
		super(world);
	}

	public EntityMobThrowable(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityMobThrowable(World world, double x, double y, double z) {
		super(world, x, y, z);
	}
	
	/**
	 * Constructs a throwable entity heading towards target's initial position with given velocity, with possible abnormal trajectory;
	 * @param wobble amount of deviation from base trajectory, used by Skeletons and the like; set to 0.0F for no x/z deviation
	 */
	public EntityMobThrowable(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter);
		this.posY = shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D;
		double d0 = target.posX - shooter.posX;
		double d1 = target.boundingBox.minY + (double) target.height - this.posY;
		double d2 = target.posZ - shooter.posZ;
		double d3 = (double) MathHelper.sqrt_double(d0 * d0 + d2 * d2);

		if (d3 >= 1.0E-7D) {
			float f2 = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
			float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
			double d4 = d0 / d3;
			double d5 = d2 / d3;
			setLocationAndAngles(shooter.posX + d4, this.posY, shooter.posZ + d5, f2, f3);
			yOffset = 0.0F;
			float f4 = (float) d3 * 0.2F;
			setThrowableHeading(d0, d1 + (double) f4, d2, velocity, wobble);
		}
	}
	
	/** Returns the amount of damage this entity will cause upon impact */
	public float getDamage() {
		return damage;
	}
	
	/**
	 * Sets the damage this entity will cause upon impact
	 */
	public EntityMobThrowable setDamage(float amount) {
		this.damage = amount;
		return this;
	}
	
	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setFloat("damage", damage);
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		damage = compound.getFloat("damage");
	}
}

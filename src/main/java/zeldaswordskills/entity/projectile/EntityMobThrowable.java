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

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.IReflectable.IReflectableOrigin;
import zeldaswordskills.api.item.IReflective;

/**
 * 
 * Abstract class that provides constructor for throwing entity as a mob
 *
 */
public abstract class EntityMobThrowable extends EntityThrowable implements IEntityAdditionalSpawnData, IReflectableOrigin
{
	/** The throwing entity's ID, in case it is not a player. Only used after loading from NBT */
	private int throwerId;

	/** Usually the damage this entity will cause upon impact */
	private float damage;

	/** Projectile gravity velocity */
	private float gravity = 0.03F;

	/** Entity ID of the original thrower, set when the projectile is reflected; not synced to client by default */
	protected int originId = -1;

	/** Set to true if this projectile gets reflected back toward the original thrower */
	protected boolean wasReflected;

	/** Whether this projectile ignores water during motion updates */
	protected boolean ignoreWater;

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
		double d1 = target.boundingBox.minY + (double)(target.height / 3.0F) - this.posY;
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

	@Override
	public EntityLivingBase getThrower() {
		EntityLivingBase thrower = super.getThrower();
		if (thrower == null) {
			Entity entity = worldObj.getEntityByID(throwerId);
			if (entity instanceof EntityLivingBase) {
				thrower = (EntityLivingBase) entity;
				this.setThrower(thrower); // save for next time
			}
		}
		return thrower;
	}

	/**
	 * Sets this projectile's thrower
	 */
	public void setThrower(EntityLivingBase thrower) {
		DirtyEntityAccessor.setThrowableThrower(this, thrower);
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

	/**
	 * Re-sets the projectile's heading on the exact same trajectory but using the given velocity
	 */
	public EntityMobThrowable setProjectileVelocity(float velocity) {
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, velocity, 0.0F);
		return this;
	}

	@Override
	protected float getGravityVelocity() {
		return gravity;
	}

	/**
	 * Sets the projectile's gravity velocity
	 */
	public EntityMobThrowable setGravityVelocity(float amount) {
		this.gravity = amount;
		return this;
	}

	/**
	 * Sets this projectile to ignore water during motion updates
	 */
	public EntityMobThrowable setIgnoreWater() {
		this.ignoreWater = true;
		return this;
	}

	@Override
	public boolean handleWaterMovement() {
		return !this.ignoreWater;
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		if (shield != null && shield.getItem() instanceof IReflective) {
			return ((IReflective) shield.getItem()).getReflectChance(shield, player, source, damage);
		}
		return 0.0F;
	}

	@Override
	public float getReflectedWobble(ItemStack shield, EntityPlayer player, DamageSource source) {
		return -1.0F; // default randomized wobble
	}

	@Override
	public void onReflected(ItemStack mirrorShield, EntityPlayer player, DamageSource source) {
		this.wasReflected = true;
		this.originId = (this.getThrower() == null ? -1 : this.getThrower().getEntityId());
		this.setThrower(player);
	}

	@Override
	public Entity getReflectedOriginEntity() {
		return (this.originId > -1 ? this.worldObj.getEntityByID(this.originId) : null);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if ((!gravityCheck() || posY > 255.0F) && !worldObj.isRemote) {
			setDead();
		}
	}

	/**
	 * Sanity check for gravity - return true if the entity can stay alive.
	 * Note that it will be killed anyway once it surpasses y=255.
	 */
	protected boolean gravityCheck() {
		return getGravityVelocity() > 0.0F || ticksExisted < 60;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("throwerId", (getThrower() == null ? -1 : getThrower().getEntityId()));
		compound.setInteger("originId", originId);
		compound.setFloat("damage", damage);
		compound.setFloat("gravity", gravity);
		compound.setBoolean("ignoreWater", ignoreWater);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		throwerId = compound.getInteger("throwerId");
		originId = compound.getInteger("originId");
		damage = compound.getFloat("damage");
		gravity = compound.getFloat("gravity");
		ignoreWater = compound.getBoolean("ignoreWater");
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		this.throwerId = (this.getThrower() == null ? -1 : this.getThrower().getEntityId());
		buffer.writeInt(this.throwerId);
		buffer.writeFloat(this.gravity);
		buffer.writeBoolean(this.ignoreWater);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		this.throwerId = buffer.readInt();
		this.gravity = buffer.readFloat();
		this.ignoreWater = buffer.readBoolean();
	}
}

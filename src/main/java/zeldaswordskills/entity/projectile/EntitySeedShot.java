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

import net.minecraft.block.Block;
import net.minecraft.block.BlockButtonWood;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

public class EntitySeedShot extends EntityMobThrowable
{
	/** Watchable object index for critical status, used for damage and possibly trailing particles */
	protected static final int CRITICAL_INDEX = 22;

	/** Knockback strength, if any */
	private int knockback = 0;

	public EntitySeedShot(World world) {
		super(world);
		this.setDamage(this.getBaseDamage());
		this.setGravityVelocity(0.05F);
	}

	public EntitySeedShot(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
	}

	public EntitySeedShot(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		this.setDamage(this.getBaseDamage());
		this.setGravityVelocity(0.05F);
	}

	/**
	 * @param n the nth shot fired; all shots after the 1st will vary the trajectory
	 * by the spread given, while the first shot will have a true course
	 */
	public EntitySeedShot(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity);
		this.setDamage(this.getBaseDamage());
		this.setGravityVelocity(0.05F);
		if (n > 1) {
			setLocationAndAngles(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);
			float rotFactor = (float)(n / 2) * spread;
			rotationYaw += rotFactor * (n % 2 == 0 ? 1 : -1);
			posX -= (double)(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			posY -= 0.10000000149011612D;
			posZ -= (double)(MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			setPosition(posX, posY, posZ);
			float f = 0.4F;
			motionX = (double)(-MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
			motionZ = (double)(MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
			motionY = (double)(-MathHelper.sin((rotationPitch + getInaccuracy()) / 180.0F * (float)Math.PI) * f);
		}
		setThrowableHeading(motionX, motionY, motionZ, velocity * 1.5F, 1.0F);
	}

	public EntitySeedShot(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setDamage(this.getBaseDamage());
		this.setGravityVelocity(0.05F);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(CRITICAL_INDEX, Byte.valueOf((byte) 0));
	}

	/** Whether the seed has a stream of critical hit particles flying behind it */
	public void setIsCritical(boolean isCrit) {
		dataWatcher.updateObject(CRITICAL_INDEX, Byte.valueOf((byte)(isCrit ? 1 : 0)));
	}

	/** Whether the seed has a stream of critical hit particles flying behind it */
	public boolean getIsCritical() {
		return dataWatcher.getWatchableObjectByte(CRITICAL_INDEX) > 0;
	}

	/** Return the damage value to set as the default during entity construction */
	protected float getBaseDamage() {
		return 1.0F;
	}

	/**
	 * Returns the damage source caused by this seed type
	 */
	public DamageSource getDamageSource() {
		return new EntityDamageSourceIndirect("slingshot", this, this.getThrower()).setProjectile();
	}

	/** Sets the amount of knockback the arrow applies when it hits a mob. */
	public void setKnockback(int value) {
		knockback = value;
	}

	/** Returns the amount of knockback the arrow applies when it hits a mob */
	public int getKnockback() {
		return knockback;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/** The particle to spawn upon impact */
	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.CRIT;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			if (isBurning() && !(mop.entityHit instanceof EntityEnderman)) {
				mop.entityHit.setFire(5);
			}
			if (mop.entityHit.attackEntityFrom(this.getDamageSource(), this.calculateDamage())) {
				playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				if (knockback > 0) {
					float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
					if (f > 0.0F) {
						double d = (double) knockback * 0.6000000238418579D / (double) f;
						mop.entityHit.addVelocity(motionX * d, 0.1D, motionZ * d);
					}
				}
				if (mop.entityHit instanceof EntityLivingBase) {
					this.handlePostDamageEffects((EntityLivingBase) mop.entityHit);
					EntityLivingBase thrower = this.getThrower();
					if (thrower != null) {
						EnchantmentHelper.applyThornEnchantments((EntityLivingBase) mop.entityHit, thrower);
						EnchantmentHelper.applyArthropodEnchantments(thrower, mop.entityHit);
					}
				}
				if (!(mop.entityHit instanceof EntityEnderman)) {
					setDead();
				}
			} else {
				motionX *= -0.10000000149011612D;
				motionY *= -0.10000000149011612D;
				motionZ *= -0.10000000149011612D;
				rotationYaw += 180.0F;
				prevRotationYaw += 180.0F;
			}
		} else {
			BlockPos pos = mop.getBlockPos();
			Block block = this.worldObj.getBlockState(pos).getBlock();
			if (block.getMaterial() != Material.air) {
				block.onEntityCollidedWithBlock(this.worldObj, pos, this);
			}
			if (this.onImpactBlock(block, pos, mop.sideHit)) {
				playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 0.3F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				setDead();
			}
		}
		// Only spawn particles if it hit something for sure
		if (!this.isEntityAlive() && this.worldObj.isRemote) {
			this.spawnImpactParticles();
		}
	}

	/**
	 * Called when the seed shot impacts a block after calling Block#onEntityCollidedWithBlock
	 * @return true if the seed shot should be killed, usually if the block is solid on the side hit
	 */
	protected boolean onImpactBlock(Block block, BlockPos pos, EnumFacing face) {
		if (block instanceof BlockButtonWood) {
			WorldUtils.activateButton(this.worldObj, this.worldObj.getBlockState(pos), pos);
			return true;
		}
		return block.isSideSolid(worldObj, pos, face);
	}

	/**
	 * Called when the seed shot has caused damage to the entity hit
	 */
	protected void handlePostDamageEffects(EntityLivingBase entity) {}

	/**
	 * Spawns particles upon impact
	 */
	protected void spawnImpactParticles() {
		EnumParticleTypes particle = this.getParticle();
		for (int i = 0; i < 4 && particle != null; ++i) {
			this.worldObj.spawnParticle(particle,
					this.posX - this.motionX * (double) i / 4.0D,
					this.posY - this.motionY * (double) i / 4.0D,
					this.posZ - this.motionZ * (double) i / 4.0D,
					this.motionX, this.motionY + 0.2D, this.motionZ);
		}
	}

	/**
	 * Calculates and returns damage based on velocity
	 */
	protected float calculateDamage() {
		float f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
		float dmg = f * getDamage();
		if (getIsCritical()) {
			dmg += (rand.nextFloat() * (getDamage() / 4.0F)) + 0.25F;
		}
		return dmg;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("isCritical", getIsCritical());
		compound.setInteger("knockback", knockback);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setIsCritical(compound.getBoolean("isCritical"));
		knockback = compound.getInteger("knockback");
	}
}

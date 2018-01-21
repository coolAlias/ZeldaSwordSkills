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
import net.minecraft.block.BlockButtonWood;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

public class EntitySeedShot extends EntityMobThrowable
{
	public static enum SeedType {
		NONE(0.0F, EnumParticleTypes.CRIT),
		BOMB(3.0F, EnumParticleTypes.EXPLOSION_LARGE),
		COCOA(1.25F, EnumParticleTypes.CRIT),
		DEKU(1.5F, EnumParticleTypes.EXPLOSION_LARGE),
		GRASS(1.0F, EnumParticleTypes.CRIT),
		MELON(1.25F, EnumParticleTypes.CRIT),
		NETHERWART(1.5F, EnumParticleTypes.CRIT),
		PUMPKIN(1.25F, EnumParticleTypes.CRIT);

		private final float damage;

		/** Particle to spawn upon impact */
		public final EnumParticleTypes particle;

		private SeedType(float damage, EnumParticleTypes particle) {
			this.damage = damage;
			this.particle = particle;
		}
		/** Returns the base damage for this seed type */
		public float getDamage() {
			return damage;
		}
	};

	/** Watchable object index for critical and seed's type */
	protected static final int CRITICAL_INDEX = 22, SEEDTYPE_INDEX = 23;

	/** Knockback strength, if any */
	private int knockback = 0;

	public EntitySeedShot(World world) {
		super(world);
		setGravityVelocity(0.05F);
	}

	public EntitySeedShot(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
	}

	public EntitySeedShot(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setGravityVelocity(0.05F);
	}

	/**
	 * @param n the nth shot fired; all shots after the 1st will vary the trajectory
	 * by the spread given, while the first shot will have a true course
	 */
	public EntitySeedShot(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity);
		setGravityVelocity(0.05F);
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
		setGravityVelocity(0.05F);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		// only needed if spawning trailing particles on client side:
		dataWatcher.addObject(CRITICAL_INDEX, Byte.valueOf((byte) 0));
		// only needed if rendering differently for each type:
		dataWatcher.addObject(SEEDTYPE_INDEX, SeedType.GRASS.ordinal());
	}

	/** Whether the seed has a stream of critical hit particles flying behind it */
	public void setIsCritical(boolean isCrit) {
		dataWatcher.updateObject(CRITICAL_INDEX, Byte.valueOf((byte)(isCrit ? 1 : 0)));
	}

	/** Whether the seed has a stream of critical hit particles flying behind it */
	public boolean getIsCritical() {
		return dataWatcher.getWatchableObjectByte(CRITICAL_INDEX) > 0;
	}

	/** Set the seed's type */
	public EntitySeedShot setType(SeedType type) {
		dataWatcher.updateObject(SEEDTYPE_INDEX, type.ordinal());
		return this;
	}

	/** Get the seed's type */
	public SeedType getType() {
		return SeedType.values()[dataWatcher.getWatchableObjectInt(SEEDTYPE_INDEX) % SeedType.values().length];
	}

	/**
	 * Returns the damage source caused by this seed type
	 */
	public DamageSource getDamageSource() {
		switch(getType()) {
		case BOMB: return new DamageSourceBaseIndirect("slingshot", this, getThrower()).setExplosion().setProjectile();
		case DEKU: return new DamageSourceBaseIndirect("slingshot", this, getThrower()).setStunDamage(80, 2, true).setProjectile();
		case NETHERWART: return new EntityDamageSourceIndirect("slingshot", this, getThrower()).setFireDamage().setProjectile();
		default: return new EntityDamageSourceIndirect("slingshot", this, getThrower()).setProjectile();
		}
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

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			if (isBurning() && !(mop.entityHit instanceof EntityEnderman)) {
				mop.entityHit.setFire(5);
			}
			if (getType() == SeedType.BOMB) {
				CustomExplosion.createExplosion(new EntityBomb(worldObj, getThrower()), worldObj, posX, posY, posZ, 3.0F, SeedType.BOMB.getDamage(), false);
				setDead();
			} else if (mop.entityHit.attackEntityFrom(getDamageSource(), calculateDamage())) {
				playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				if (knockback > 0) {
					float f = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
					if (f > 0.0F) {
						double d = (double) knockback * 0.6000000238418579D / (double) f;
						mop.entityHit.addVelocity(motionX * d, 0.1D, motionZ * d);
					}
				}
				if (mop.entityHit instanceof EntityLivingBase) {
					EntityLivingBase entity = (EntityLivingBase) mop.entityHit;
					switch(getType()) {
					case COCOA: entity.addPotionEffect(new PotionEffect(Potion.weakness.id,100,0)); break;
					case PUMPKIN: entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,100,0)); break;
					default:
					}
					if (getThrower() instanceof EntityLivingBase) {
						EnchantmentHelper.applyThornEnchantments((EntityLivingBase) mop.entityHit, getThrower());
						EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) getThrower(), mop.entityHit);
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
			IBlockState state = worldObj.getBlockState(pos); 
			Block block = state.getBlock();
			if (block.getMaterial() != Material.air) {
				block.onEntityCollidedWithBlock(worldObj, pos, this);
			}
			boolean flag = block.isSideSolid(worldObj, pos, mop.sideHit);
			if (getType() == SeedType.BOMB && flag) {
				double dx = mop.sideHit == EnumFacing.WEST ? -0.5D : mop.sideHit == EnumFacing.EAST ? 0.5D : 0.0D;
				double dy = mop.sideHit == EnumFacing.DOWN ? -0.5D : mop.sideHit == EnumFacing.UP ? 0.5D : 0.0D;
				double dz = mop.sideHit == EnumFacing.NORTH ? -0.5D : mop.sideHit == EnumFacing.SOUTH ? 0.5D : 0.0D;
				if (!worldObj.isRemote) {
					CustomExplosion.createExplosion(new EntityBomb(worldObj, getThrower()), worldObj, posX + dx, posY + dy, posZ + dz, 3.0F, SeedType.BOMB.getDamage(), false);
				}
			} else if (block instanceof BlockButtonWood) {
				WorldUtils.activateButton(worldObj, state, pos);
				flag = true;
			}
			if (flag) {
				playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 0.3F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
				setDead();
			}
		}
		// Only spawn particles if it hit something for sure
		if (!isEntityAlive() && worldObj.isRemote) {
			EnumParticleTypes particle = getType().particle;
			for (int i = 0; i < 4; ++i) {
				worldObj.spawnParticle(particle,
						posX - motionX * (double) i / 4.0D,
						posY - motionY * (double) i / 4.0D,
						posZ - motionZ * (double) i / 4.0D,
						motionX, motionY + 0.2D, motionZ);
			}
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
		compound.setByte("seedType", (byte) getType().ordinal());
		compound.setInteger("knockback", knockback);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setIsCritical(compound.getBoolean("isCritical"));
		setType(SeedType.values()[compound.getByte("seedType") % SeedType.values().length]);
		knockback = compound.getInteger("knockback");
	}
}

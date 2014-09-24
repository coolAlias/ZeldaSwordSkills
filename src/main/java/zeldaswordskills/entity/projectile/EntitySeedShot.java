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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.WorldUtils;

public class EntitySeedShot extends EntityMobThrowable
{
	public static enum SeedType {
		NONE(0.0F),
		COCOA(1.25F),
		DEKU(1.5F),
		GRASS(1.0F),
		MELON(1.25F),
		NETHERWART(1.5F),
		PUMPKIN(1.25F);

		private final float damage;
		private SeedType(float damage) {
			this.damage = damage;
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
	}

	public EntitySeedShot(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
	}

	public EntitySeedShot(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	/**
	 * @param n the nth shot fired; all shots after the 1st will vary the trajectory
	 * by the spread given, while the first shot will have a true course
	 */
	public EntitySeedShot(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity);
		if (n > 1) {
			setLocationAndAngles(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ, entity.rotationYaw, entity.rotationPitch);
			float rotFactor = (float)(n / 2) * spread;
			rotationYaw += rotFactor * (n % 2 == 0 ? 1 : -1);
			posX -= (double)(MathHelper.cos(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			posY -= 0.10000000149011612D;
			posZ -= (double)(MathHelper.sin(rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
			setPosition(posX, posY, posZ);
			yOffset = 0.0F;
			float f = 0.4F;
			motionX = (double)(-MathHelper.sin(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
			motionZ = (double)(MathHelper.cos(rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(rotationPitch / 180.0F * (float) Math.PI) * f);
			motionY = (double)(-MathHelper.sin((rotationPitch + func_70183_g()) / 180.0F * (float)Math.PI) * f);
		}
		setThrowableHeading(motionX, motionY, motionZ, velocity * 1.5F, 1.0F);
	}

	public EntitySeedShot(World world, double x, double y, double z) {
		super(world, x, y, z);
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
	protected float getGravityVelocity() {
		return 0.05F;
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		String particle = (getType() == SeedType.DEKU ? "largeexplode" : "crit");
		for (int i = 0; i < 4; ++i) {
			worldObj.spawnParticle(particle,
					posX - motionX * (double) i / 4.0D,
					posY - motionY * (double) i / 4.0D,
					posZ - motionZ * (double) i / 4.0D,
					motionX, motionY + 0.2D, motionZ);
		}

		if (mop.entityHit != null) {
			if (isBurning() && !(mop.entityHit instanceof EntityEnderman)) {
				mop.entityHit.setFire(5);
			}

			if (mop.entityHit.attackEntityFrom(getDamageSource(), calculateDamage())) {
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
						// func_151384_a is the new way Thorns is handled
						EnchantmentHelper.func_151384_a((EntityLivingBase) mop.entityHit, getThrower());
						// TODO not sure what the following does yet, but it's in EntityArro
						EnchantmentHelper.func_151385_b((EntityLivingBase) getThrower(), mop.entityHit);
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
			playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 0.3F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
			if (worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ) == Blocks.wooden_button) {
				WorldUtils.activateButton(worldObj, Blocks.wooden_button, mop.blockX, mop.blockY, mop.blockZ);
			}
			setDead();
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

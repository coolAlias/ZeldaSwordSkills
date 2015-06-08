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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

import zeldaswordskills.api.damage.DamageUtils;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;

public class EntityLeapingBlow extends EntityThrowable
{
	/** Keeps track of entities already affected so they don't get attacked twice */
	private List<Integer> affectedEntities = new ArrayList<Integer>(); 

	/** Base damage should be set from player's Leaping Blow skill */
	private float damage = 2.0F;

	/** Base number of ticks this entity can exist */
	private int lifespan = 12;

	/** Skill level of swordsman; used in many calculations */
	private int level = 0;

	/** Whether the swordsman is wielding the master sword */
	private boolean isMaster = false;

	private static final float BASE_SIZE = 1.0F, HEIGHT = 0.5F;

	public EntityLeapingBlow(World world) {
		super(world);
		this.setSize(BASE_SIZE, HEIGHT);
	}

	public EntityLeapingBlow(World world, EntityLivingBase thrower) {
		super(world, thrower);
		this.setSize(BASE_SIZE, HEIGHT);
		this.posY = thrower.posY + 0.2D;
		this.motionY = 0.0D;
		this.setThrowableHeading(motionX, motionY, motionZ, getVelocity(), 1.0F);
	}

	public EntityLeapingBlow(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setSize(BASE_SIZE, HEIGHT);
	}

	/**
	 * Each level increases the distance traveled as well as the AoE
	 * Master version increases weakness effect duration
	 */
	public EntityLeapingBlow setLevel(int level, boolean isMaster) {
		this.level = level;
		this.lifespan += level;
		this.isMaster = isMaster;
		return this;
	}

	/**
	 * Sets amount of damage that will be caused onImpact
	 */
	public EntityLeapingBlow setDamage(float amount) {
		this.damage = amount;
		return this;
	}

	/** Max distance (squared) from thrower that damage can still be applied */
	private double getRangeSquared() {
		return (3.0D + level) * (3.0D + level);
	}

	/** Duration of weakness effect */
	private int getPotionDuration() {
		return ((isMaster ? 110 : 50) + (level * 10));
	}

	/** Returns area within which to search for targets each tick */
	private AxisAlignedBB getAoE() {
		return getEntityBoundingBox().expand((0.25F * level), 0.0F, (0.25F * level));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (inGround || ticksExisted > lifespan) { setDead(); }
		if (!worldObj.isRemote) {
			List<EntityLivingBase> targets = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, getAoE());
			for (EntityLivingBase target : targets) {
				if (!affectedEntities.contains(target.getEntityId()) && target != getThrower() && !TargetUtils.isTargetInFrontOf(this, target, 30F)) {
					affectedEntities.add(target.getEntityId());
					float d = damage;
					if (getThrower() != null) {
						double d0 = (1.0D - getThrower().getDistanceSqToEntity(target) / getRangeSquared());
						d *= (d0 > 1.0D ? 1.0D : d0);
						if (d < 0.5D) { return; }
					}
					if (target.attackEntityFrom(DamageUtils.causeIndirectSwordDamage(this, getThrower()), d)) {
						target.addPotionEffect(new PotionEffect(Potion.weakness.id, getPotionDuration()));
					}
				}
			}
		}
		int x = MathHelper.floor_double(posX);
		int y = MathHelper.floor_double(posY - 0.20000000298023224D);
		int z = MathHelper.floor_double(posZ);
		IBlockState state = worldObj.getBlockState(new BlockPos(x, y, z));
		Block block = state.getBlock();
		int[] extra = {};
		EnumParticleTypes particle = (isMaster ? EnumParticleTypes.CRIT_MAGIC : EnumParticleTypes.CRIT);
		if (block.getRenderType() != -1) {
			particle = EnumParticleTypes.BLOCK_CRACK;
			extra = new int[]{Block.getStateId(state)};
			worldObj.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, this.getEntityBoundingBox().minY + 0.1D, this.posZ + ((double)this.rand.nextFloat() - 0.5D) * (double)this.width, -this.motionX * 4.0D, 1.5D, -this.motionZ * 4.0D, extra);
		}
		spawnParticles(particle, 4, motionZ, 0.01D, motionX, extra);
		spawnParticles(particle, 4, -motionZ, 0.01D, -motionX, extra);
	}

	/**
	 * Spawns the designated particle at the current entity's position, with optional
	 * motion to add to the otherwise random amount
	 * @param n  Number of particles to spawn
	 * @param dX Motion along X-axis (+/- this.motionZ for lateral motion)
	 * @param dY Motion along Y-axis
	 * @param dZ Motion along Z-axis (+/- this.motionX for lateral motion)
	 * @param extra additional arguments for World#spawnParticle
	 */
	private void spawnParticles(EnumParticleTypes particle, int n, double dX, double dY, double dZ, int ... extra) {
		for (int i = 0; i < n; ++i) {
			worldObj.spawnParticle(particle, posX, posY, posZ, dX + rand.nextGaussian(), dY, dZ + rand.nextGaussian(), extra);
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (!worldObj.isRemote) {
			if (mop.typeOfHit == MovingObjectType.ENTITY) {
				Entity entity = mop.entityHit;
				if (entity instanceof EntityLivingBase && !affectedEntities.contains(entity.getEntityId()) && entity != getThrower()) {
					affectedEntities.add(entity.getEntityId());
					if (entity.attackEntityFrom(DamageUtils.causeIndirectSwordDamage(this, getThrower()), damage)) {
						WorldUtils.playSoundAtEntity(entity, Sounds.HURT_FLESH, 0.4F, 0.5F);
						if (entity instanceof EntityLivingBase) {
							((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.weakness.id, 60));
						}
					}
				}
			} else {
				Block block = worldObj.getBlockState(mop.getBlockPos()).getBlock();
				if (block.getMaterial().blocksMovement()) {
					setDead();
				}
			}
		}
	}

	@Override
	protected float getVelocity() {
		return 0.5F;
	}

	@Override
	public float getGravityVelocity() {
		return 0.0F;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("isMaster", isMaster);
		compound.setFloat("damage", damage);
		compound.setInteger("level", level);
		compound.setInteger("lifespan", lifespan);
		compound.setIntArray("affectedEntities", ArrayUtils.toPrimitive(affectedEntities.toArray(new Integer[affectedEntities.size()])));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		isMaster = (compound.getBoolean("isMaster"));
		damage = compound.getFloat("damage");
		level = compound.getInteger("level");
		lifespan = compound.getInteger("lifespan");
		int[] entities = compound.getIntArray("affectedEntities");
		for (int i = 0; i < entities.length; ++i) {
			affectedEntities.add(entities[i]);
		}
	}
}

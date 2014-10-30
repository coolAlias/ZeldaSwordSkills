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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
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
		this.setThrowableHeading(motionX, motionY, motionZ, func_70182_d(), 1.0F);
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
		return boundingBox.expand((0.25F * level), 0.0F, (0.25F * level));
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
		/** Velocity x and z for spawning particles to left and right of entity */
		double vX = motionZ;
		double vZ = motionX;
		String particle = (isMaster ? "magicCrit" : "crit");
		Block block = worldObj.getBlock((int) (posX + (boundingBox.maxX - boundingBox.minX) / 2), (int) posY - 1, (int) (posZ + (boundingBox.maxZ - boundingBox.minZ) / 2));
		if (block.getMaterial() != Material.air) {
			particle = "blockcrack_" + Block.getIdFromBlock(block) + "_" + worldObj.getBlockMetadata((int) (posX + (boundingBox.maxX - boundingBox.minX) / 2), (int) posY - 1, (int) (posZ + (boundingBox.maxZ - boundingBox.minZ) / 2));
		}
		for (int i = 0; i < 4; ++i) {
			worldObj.spawnParticle(particle, posX, posY, posZ, vX + rand.nextGaussian(), 0.01D, vZ + rand.nextGaussian());
			worldObj.spawnParticle(particle, posX, posY, posZ, -vX + rand.nextGaussian(), 0.01D, -vZ + rand.nextGaussian());
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
				Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
				if (block.getMaterial().blocksMovement()) {
					setDead();
				}
			}
		}
	}

	/** Entity's velocity factor */
	@Override
	protected float func_70182_d() {
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

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

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseDirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIceIndirect;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.entity.IEntityEvil;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Fire Arrow
 * Ignites nearby blocks and enemies, and melts even the coldest of ice
 * 
 * Ice Arrow
 * Freezes water and enemies, as well as turning lava to stone or obsidian, even in the Nether
 * Inflicts double damage on fire-based enemies. Be warned that freezing creepers does
 * not prevent them from exploding.
 * 
 * Light Arrow
 * Ignores armor.
 * Double damage against undead, quad damage against the Wither
 * Slays non-boss Endermen and Wither Skeletons instantly, regardless of health
 * Can also dispel certain magical barriers and pierce through any block,
 * making activating buttons particularly tricky, but possible through walls
 *
 */
public class EntityArrowElemental extends EntityArrowCustom
{
	/** Valid element types for elemental arrows */
	public static enum ElementType {
		FIRE("fire", EnumParticleTypes.FLAME),
		ICE("ice", EnumParticleTypes.CRIT_MAGIC),
		LIGHT("light", EnumParticleTypes.EXPLOSION_NORMAL);
		public final String unlocalizedName;
		public final EnumParticleTypes particle;
		private ElementType(String unlocalizedName, EnumParticleTypes particle) {
			this.unlocalizedName = unlocalizedName;
			this.particle = particle;
		}
	};

	/** Watchable object index for arrow's element type */
	private static final int ARROWTYPE_DATAWATCHER_INDEX = 25;

	public EntityArrowElemental(World world) {
		super(world);
	}

	public EntityArrowElemental(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowElemental(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowElemental(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		setDamage(4.0F); // compensate for lower velocity
		dataWatcher.addObject(ARROWTYPE_DATAWATCHER_INDEX, ElementType.FIRE.ordinal());
	}

	/**
	 * Returns the {@link ElementType} of this arrow
	 */
	public ElementType getType() {
		return ElementType.values()[dataWatcher.getWatchableObjectInt(ARROWTYPE_DATAWATCHER_INDEX)];
	}

	/**
	 * Sets this arrow's {@link ElementType}
	 */
	public EntityArrowElemental setType(ElementType type) {
		dataWatcher.updateObject(ARROWTYPE_DATAWATCHER_INDEX, type.ordinal());
		if (type == ElementType.FIRE) { setFire(100); }
		return this;
	}

	@Override
	protected float getVelocityFactor() {
		return 1.3F;
	}

	@Override
	protected DamageSource getDamageSource(Entity entity) {
		switch(getType()) {
		case FIRE: return new DamageSourceFireIndirect("arrow.fire", this, shootingEntity).setProjectile().setMagicDamage();
		case ICE: return new DamageSourceIceIndirect("arrow.ice", this, shootingEntity, 50, 1).setProjectile().setMagicDamage();
		case LIGHT: return (entity instanceof EntityEnderman
				? new DamageSourceBaseDirect("arrow.light", (shootingEntity != null ? shootingEntity : this), EnumDamageType.HOLY).setProjectile().setMagicDamage().setDamageBypassesArmor()
						: new DamageSourceBaseIndirect("arrow.light", this, shootingEntity, EnumDamageType.HOLY).setProjectile().setMagicDamage().setDamageBypassesArmor());
		}
		return super.getDamageSource(entity);
	}

	@Override
	protected boolean canTargetEntity(Entity entity) {
		return getType() == ElementType.LIGHT || super.canTargetEntity(entity);
	}

	@Override
	protected EnumParticleTypes getParticle() {
		return getType().particle;
	}

	@Override
	protected boolean shouldSpawnParticles() {
		return true;
	}

	@Override
	protected void updateInAir() {
		super.updateInAir();
		boolean flag = (getType() == ElementType.FIRE && worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.water, this));
		if (!worldObj.isRemote && getType() == ElementType.ICE &&
				(worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.water, this) ||
						worldObj.handleMaterialAcceleration(getEntityBoundingBox(), Material.lava, this))) {
			flag = affectBlocks();
		}
		if (flag) {
			if (getType() == ElementType.FIRE) {
				worldObj.playSoundEffect(posX, posY, posZ, Sounds.FIRE_FIZZ, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
			}
			if (!worldObj.isRemote) {
				setDead();
			}
		}
	}

	@Override
	protected void onImpactBlock(MovingObjectPosition mop) {
		boolean flag = (getType() == ElementType.LIGHT && !Config.enableLightArrowNoClip());
		if (getType() != ElementType.LIGHT || flag) { 
			super.onImpactBlock(mop);
			if (!worldObj.isRemote && affectBlocks()) {
				setDead();
			}
			if (flag) {
				extinguishLightArrow();
			}
		} else {
			if (ticksExisted < 25) {
				Block block = worldObj.getBlockState(mop.getBlockPos()).getBlock();
				if (block.getMaterial() != Material.air) {
					block.onEntityCollidedWithBlock(worldObj, mop.getBlockPos(), this);
				}
			} else {
				extinguishLightArrow();
			}
		}
	}

	@Override
	protected void onImpactEntity(MovingObjectPosition mop) {
		if (getType() == ElementType.LIGHT && mop.entityHit instanceof EntityLivingBase && canOneHitKill(mop.entityHit)) {
			float velocity = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ);
			EntityLivingBase entity = (EntityLivingBase) mop.entityHit;
			entity.attackEntityFrom(getDamageSource(entity), entity.getMaxHealth() * 0.425F * velocity);
			playSound(Sounds.BOW_HIT, 1.0F, 1.2F / (rand.nextFloat() * 0.2F + 0.9F));
			// TODO render bright flash, different sound effect?
			if (!worldObj.isRemote) {
				setDead();
			}
		} else {
			super.onImpactEntity(mop);
		}
	}

	@Override
	protected float calculateDamage(Entity entityHit) {
		float dmg = super.calculateDamage(entityHit);
		if (getType() == ElementType.LIGHT && entityHit instanceof IEntityEvil) {
			dmg = ((IEntityEvil) entityHit).getLightArrowDamage(dmg);
		}
		return dmg;
	}

	@Override
	protected void handlePostDamageEffects(EntityLivingBase entity) {
		super.handlePostDamageEffects(entity);
		if (!entity.isDead && getType() == ElementType.ICE) {
			ZSSEntityInfo.get(entity).stun(MathHelper.ceiling_float_int(calculateDamage(entity)) * 10, true);
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			worldObj.setBlockState(new BlockPos(i, j, k), Blocks.ice.getDefaultState());
			worldObj.setBlockState(new BlockPos(i, j + 1, k), Blocks.ice.getDefaultState());
			worldObj.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, Sounds.GLASS_BREAK, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
		}
	}

	/**
	 * Returns true if the light arrow can kill this entity in one hit (endermen and wither skeletons)
	 */
	private boolean canOneHitKill(Entity entity) {
		if (entity instanceof IEntityEvil) {
			return ((IEntityEvil) entity).isLightArrowFatal();
		}
		boolean flag = (entity instanceof EntitySkeleton && ((EntitySkeleton) entity).getSkeletonType() == 1);
		return (!(entity instanceof IBossDisplayData)) && (flag || entity instanceof EntityEnderman);
	}

	/**
	 * Affects all blocks within AoE; returns true if arrow should be consumed
	 */
	protected boolean affectBlocks() {
		boolean flag = false;
		Set<BlockPos> affectedBlocks = new HashSet<BlockPos>(WorldUtils.getAffectedBlocksList(worldObj, rand, 1.5F, posX, posY, posZ, null));
		Block block;
		for (BlockPos pos : affectedBlocks) {
			block = worldObj.getBlockState(pos).getBlock();
			switch(getType()) {
			case FIRE:
				if (block.getMaterial() == Material.air && Config.enableFireArrowIgnite()) {
					Block block2 = worldObj.getBlockState(pos.down()).getBlock();
					if (block2.isFullBlock() && rand.nextInt(8) == 0) {
						worldObj.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.FIRE_IGNITE, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
						worldObj.setBlockState(pos, Blocks.fire.getDefaultState());
						flag = true;
					}
				} else if (WorldUtils.canMeltBlock(worldObj, block, pos)) {
					worldObj.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.FIRE_FIZZ, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
					worldObj.setBlockToAir(pos);
					flag = true;
				}
				break;
			case ICE:
				if (block.getMaterial() == Material.water) {
					worldObj.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.GLASS_BREAK, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
					worldObj.setBlockState(pos, Blocks.ice.getDefaultState());
					flag = true;
				} else if (block.getMaterial() == Material.lava) {
					worldObj.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.FIRE_FIZZ, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
					worldObj.setBlockState(pos, (block == Blocks.lava ? Blocks.obsidian : Blocks.cobblestone).getDefaultState());
					flag = true;
				} else if (block.getMaterial() == Material.fire) {
					worldObj.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, Sounds.FIRE_FIZZ, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
					worldObj.setBlockToAir(pos);
					flag = true;
				}
				break;
			case LIGHT:
				// TODO dispel magical barriers
				break;
			}
		}

		return flag;
	}

	/**
	 * Sets this arrow to dead after spawning some particles
	 */
	private void extinguishLightArrow() {
		for (int i = 0; i < 10; ++i) {
			double d0 = rand.nextGaussian() * 0.02D;
			double d1 = rand.nextGaussian() * 0.02D;
			double d2 = rand.nextGaussian() * 0.02D;
			worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL, posX + (double)(rand.nextFloat() * width * 2.0F) - (double) width,
					posY + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2.0F) - (double) width, d0, d1, d2);
		}
		if (!worldObj.isRemote) {
			setDead();
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("arrowType", getType().ordinal());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(ElementType.values()[compound.getInteger("arrowType") % ElementType.values().length]);
	}
}

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIceIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIndirect;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.item.ItemMagicRod;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Magic spell projectile entity; rendering is two intersecting cubes with texture
 * determined by spell type (i.e. fire, ice, etc.)
 * 
 * Upon impact, the spell will 'explode', damaging all entities within the area of
 * effect for the full damage amount.
 *
 */
public class EntityMagicSpell extends EntityMobThrowable
{
	public static enum MagicType {
		/** Causes fire damage, melts ice, ignites blocks */
		FIRE,
		/** Causes cold damage, freezes targets, extinguishes flames and lava */
		ICE,
		/** Currently no special effects; used only to give Tornado Rod a dummy magic type */
		WIND
	}

	/** Watchable object index for spell's magic type */
	private static final int SPELL_TYPE_INDEX = 22;

	/** Watchable object index for spell's area of effect */
	private static final int AREA_INDEX = 23;

	public EntityMagicSpell(World world) {
		super(world);
	}

	public EntityMagicSpell(World world, EntityLivingBase entity) {
		super(world, entity);
	}

	public EntityMagicSpell(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityMagicSpell(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	public void entityInit() {
		super.entityInit();
		dataWatcher.addObject(SPELL_TYPE_INDEX, MagicType.FIRE.ordinal());
		dataWatcher.addObject(AREA_INDEX, 2.0F);
	}

	public MagicType getType() {
		return MagicType.values()[dataWatcher.getWatchableObjectInt(SPELL_TYPE_INDEX) % MagicType.values().length];
	}

	public EntityMagicSpell setType(MagicType type) {
		dataWatcher.updateObject(SPELL_TYPE_INDEX, type.ordinal());
		return this;
	}

	/** Returns the spell's area of effect */
	public float getArea() {
		return dataWatcher.getWatchableObjectFloat(AREA_INDEX);
	}

	/** Sets the spell's area of effect radius */
	public EntityMagicSpell setArea(float radius) {
		dataWatcher.updateObject(AREA_INDEX, radius);
		return this;
	}

	/**
	 * Returns a damage source corresponding to the magic type (e.g. fire for fire, etc.)
	 */
	protected DamageSource getDamageSource() {
		switch(getType()) {
		case ICE: return new DamageSourceIceIndirect("blast.ice", this, getThrower(), 50, 1).setProjectile().setMagicDamage();
		case WIND: return new EntityDamageSourceIndirect("blast.wind", this, getThrower()).setProjectile().setMagicDamage();
		default: return new DamageSourceIndirect("blast.fire", this, getThrower()).setFireDamage().setProjectile().setMagicDamage();
		}
	}

	@Override
	protected float func_70182_d() {
		return 1.0F;
	}

	@Override
	protected float getGravityVelocity() {
		return 0.02F;
	}

	private String getParticle() {
		switch(getType()) {
		case ICE: return "snowshovel";
		case WIND: return "cloud";
		default: return "flame";
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (worldObj.isRemote) {
			String particle = getParticle();
			boolean flag = getType() != MagicType.FIRE;
			for (int i = 0; i < 4; ++i) {
				worldObj.spawnParticle(particle,
						posX + motionX * (double) i / 4.0D,
						posY + motionY * (double) i / 4.0D,
						posZ + motionZ * (double) i / 4.0D,
						-motionX * 0.25D, -motionY + (flag ? 0.1D : 0.0D), -motionZ * 0.25D);
			}
		}
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		double x = (mop.entityHit != null ? mop.entityHit.posX : mop.blockX + 0.5D);
		double y = (mop.entityHit != null ? mop.entityHit.posY : mop.blockY + 0.5D);
		double z = (mop.entityHit != null ? mop.entityHit.posZ : mop.blockZ + 0.5D);
		float r = getArea();
		List<EntityLivingBase> list = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
				AxisAlignedBB.getBoundingBox(x - r, y - r, z - r, x + r, y + r, z + r));
		for (EntityLivingBase entity : list) {
			Vec3 vec3 = worldObj.getWorldVec3Pool().getVecFromPool(posX - motionX, posY - motionY, posZ - motionZ);
			Vec3 vec31 = worldObj.getWorldVec3Pool().getVecFromPool(entity.posX, entity.posY, entity.posZ);
			MovingObjectPosition mop1 = worldObj.clip(vec3, vec31);
			if (mop1 != null && mop1.typeOfHit == EnumMovingObjectType.TILE) {
				Block block = Block.blocksList[worldObj.getBlockId(mop1.blockX, mop1.blockY, mop1.blockZ)];
				if (block != null && block.blockMaterial.blocksMovement()) {
					continue;
				}
			}
			if (entity.attackEntityFrom(getDamageSource(), getDamage()) && !entity.isDead) {
				handlePostDamageEffects(entity);
			}
		}
		if (worldObj.isRemote) {
			spawnImpactParticles("largeexplode", 4, -0.1F);
			spawnImpactParticles(getParticle(), 16, getType() == MagicType.ICE ? 0.0F : -0.2F);
		} else {
			worldObj.playSoundAtEntity(this, Sounds.EXPLOSION, 2.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
			Set<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>(WorldUtils.getAffectedBlocksList(worldObj, rand, r, posX, posY, posZ, -1));
			ItemMagicRod.affectAllBlocks(worldObj, affectedBlocks, getType());
			setDead();
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnImpactParticles(String particle, int n, float offsetY) {
		for (int i = 0; i < n; ++i) {
			double dx = posX - motionX * (double) i / 4.0D;
			double dy = posY - motionY * (double) i / 4.0D;
			double dz = posZ - motionX * (double) i / 4.0D;
			worldObj.spawnParticle(particle, (dx + rand.nextFloat() - 0.5F),
					(dy + rand.nextFloat() - 0.5F),
					(dz + rand.nextFloat() - 0.5F), 0.25F * (rand.nextFloat() - 0.5F),
					(rand.nextFloat() * 0.25F) + offsetY, 0.25F * (rand.nextFloat() - 0.5F));
		}
	}

	protected void handlePostDamageEffects(EntityLivingBase entity) {
		switch(getType()) {
		case ICE:
			ZSSEntityInfo.get(entity).stun((int) Math.ceil(getDamage()) * 10, true);
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			worldObj.setBlock(i, j, k, Block.ice.blockID);
			worldObj.setBlock(i, j + 1, k, Block.ice.blockID);
			worldObj.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, Sounds.GLASS_BREAK, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
			break;
		case FIRE:
			if (!entity.isImmuneToFire()) {
				entity.setFire((int) Math.ceil(getDamage()));
			}
			break;
		default:
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setInteger("magicType", getType().ordinal());
		compound.setFloat("areaOfEffect", getArea());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(MagicType.values()[compound.getInteger("magicType") % MagicType.values().length]);
		setArea(compound.getFloat("areaOfEffect"));
	}
}

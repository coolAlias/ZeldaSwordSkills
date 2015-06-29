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
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIceIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShockIndirect;
import zeldaswordskills.api.entity.IReflectable;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.item.ItemMagicRod;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.WorldUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
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
 * Set the MagicType and AoE before spawning the entity or the client side will not know about it.
 *
 */
public class EntityMagicSpell extends EntityMobThrowable implements IEntityAdditionalSpawnData, IReflectable
{
	/** The spell's magic type */
	private MagicType type = MagicType.FIRE;

	/** The spell's effect radius also affects the render scale */
	private float radius = 2.0F;

	/** If true, the damage source will be set to ignore armor */
	private boolean bypassesArmor;

	/** If not set to positive value, default will return (1.0F - (getArea() / 4.0F)) */
	private float reflectChance = -1.0F;

	public EntityMagicSpell(World world) {
		super(world);
	}

	public EntityMagicSpell(World world, EntityLivingBase entity) {
		super(world, entity);
		resetSize();
	}

	public EntityMagicSpell(World world, double x, double y, double z) {
		super(world, x, y, z);
		resetSize();
	}

	public EntityMagicSpell(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		resetSize();
	}

	/** Re-sets the entity size based on the current radius */
	private void resetSize() {
		float f = (float)(radius / 4.0D);
		setSize(f, f);
	}

	public MagicType getType() {
		return type;
	}

	public EntityMagicSpell setType(MagicType type) {
		this.type = type;
		return this;
	}

	/** Makes this spell's damage source ignore armor */
	public EntityMagicSpell setDamageBypassesArmor() {
		bypassesArmor = true;
		return this;
	}

	/** Returns the spell's effect radius */
	public float getArea() {
		return radius;
	}

	/** Sets the spell's area of effect radius */
	public EntityMagicSpell setArea(float radius) {
		this.radius = radius;
		resetSize();
		return this;
	}

	/** Sets the chance of the spell being reflected when blocked with the Mirror Shield */
	public EntityMagicSpell setReflectChance(float chance) {
		this.reflectChance = chance;
		return this;
	}

	/**
	 * Returns a damage source corresponding to the magic type (e.g. fire for fire, etc.)
	 */
	protected DamageSource getDamageSource() {
		DamageSource source = new DamageSourceFireIndirect("blast.fire", this, getThrower(), true).setProjectile().setMagicDamage();
		switch(getType()) {
		case ICE: source = new DamageSourceIceIndirect("blast.ice", this, getThrower(), 50, 1, true).setStunDamage(60, 10, true).setProjectile().setMagicDamage(); break;
		case LIGHTNING: source = new DamageSourceShockIndirect("blast.lightning", this, getThrower(), 50, 1, true).setProjectile().setMagicDamage(); break;
		case WIND: source = new DamageSourceBaseIndirect("blast.wind", this, getThrower(), true).setProjectile().setMagicDamage(); break;
		default: break; // fire
		}
		if (bypassesArmor && source instanceof DamageSourceBaseIndirect) {
			((DamageSourceBaseIndirect) source).setDamageBypassesArmor();
		}
		return source;
	}

	@Override
	public float getReflectChance(ItemStack mirrorShield, EntityPlayer player, Entity shooter) {
		return (reflectChance < 0 ? 1.0F - (getArea() / 4.0F) : reflectChance);
	}

	@Override
	public void onReflected(ItemStack mirrorShield, EntityPlayer player, Entity shooter, Entity oldEntity) {}

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
		MagicType type = getType();
		if (worldObj.isRemote) {
			String particle = type.getTrailingParticle();
			boolean flag = type != MagicType.FIRE;
			for (int i = 0; i < 4; ++i) {
				worldObj.spawnParticle(particle,
						posX + motionX * (double) i / 4.0D,
						posY + motionY * (double) i / 4.0D,
						posZ + motionZ * (double) i / 4.0D,
						-motionX * 0.25D, -motionY + (flag ? 0.1D : 0.0D), -motionZ * 0.25D);
			}
		} else if (ticksExisted % type.getSoundFrequency() == 0) {
			worldObj.playSoundAtEntity(this, type.getMovingSound(), type.getSoundVolume(rand), type.getSoundPitch(rand));
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
			spawnImpactParticles(getType().getTrailingParticle(), 16, getType() == MagicType.ICE ? 0.0F : -0.2F);
		} else {
			worldObj.playSoundAtEntity(this, Sounds.EXPLOSION, 2.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
			if (getType().affectsBlocks(worldObj, getThrower())) {
				Set<ChunkPosition> affectedBlocks = new HashSet<ChunkPosition>(WorldUtils.getAffectedBlocksList(worldObj, rand, r, posX, posY, posZ, -1));
				ItemMagicRod.affectAllBlocks(worldObj, affectedBlocks, getType());
			}
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
			if (getThrower() instanceof EntityPlayer) {
				worldObj.setBlock(i, j, k, Block.ice.blockID);
				worldObj.setBlock(i, j + 1, k, Block.ice.blockID);
			}
			worldObj.playSoundEffect(i + 0.5D, j + 0.5D, k + 0.5D, Sounds.GLASS_BREAK, 1.0F, rand.nextFloat() * 0.4F + 0.8F);
			break;
		case FIRE:
			if (!entity.isImmuneToFire()) {
				entity.setFire((int) Math.ceil(getDamage()));
			}
			break;
		case WIND:
			double power = Math.min(3.0D, (getDamage() / 6.0D));
			if (power > 0) {
				float f3 = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ);
				if (f3 > 0.0F) {
					double knockback = power * 0.6000000238418579D / (double) f3;
					entity.addVelocity(motionX * knockback, 0.1D, motionZ * knockback);
				}
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
		compound.setFloat("reflectChance", reflectChance);
		compound.setBoolean("bypassesArmor", bypassesArmor);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(MagicType.values()[compound.getInteger("magicType") % MagicType.values().length]);
		setArea(compound.getFloat("areaOfEffect"));
		reflectChance = compound.getFloat("reflectChance");
		bypassesArmor = compound.getBoolean("bypassesArmor");
	}

	@Override
	public void writeSpawnData(ByteArrayDataOutput buffer) {
		buffer.writeInt(type.ordinal());
		buffer.writeFloat(radius);
	}

	@Override
	public void readSpawnData(ByteArrayDataInput buffer) {
		type = (MagicType.values()[buffer.readInt() % MagicType.values().length]);
		radius = buffer.readFloat();
	}
}

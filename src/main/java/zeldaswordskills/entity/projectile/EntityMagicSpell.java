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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIceIndirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceShockIndirect;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.item.ItemMagicRod;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

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
public class EntityMagicSpell extends EntityMobThrowable
{
	/** The spell's magic type */
	private MagicType type = MagicType.FIRE;

	/** Set to false to prevent the spell from affecting blocks */
	private boolean canGrief = true;

	/** The spell's effect radius also affects the render scale */
	private float radius = 2.0F;

	/** If true, the damage source will be set to ignore armor */
	private boolean bypassesArmor;

	/** If not set to positive value, default will return (1.0F - (getArea() / 4.0F)) */
	private float reflectChance = -1.0F;

	/** Set to false for no trailing particles */
	private boolean spawnParticles = true;

	public EntityMagicSpell(World world) {
		super(world);
		setGravityVelocity(0.02F);
	}

	public EntityMagicSpell(World world, EntityLivingBase entity) {
		super(world, entity);
		setGravityVelocity(0.02F);
		resetSize();
	}

	public EntityMagicSpell(World world, double x, double y, double z) {
		super(world, x, y, z);
		setGravityVelocity(0.02F);
		resetSize();
	}

	public EntityMagicSpell(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		setGravityVelocity(0.02F);
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

	/**
	 * Disables griefing - i.e. no blocks will be affected by this spell
	 */
	public EntityMagicSpell disableGriefing() {
		this.canGrief = false;
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

	/** Disables trailing particles */
	public EntityMagicSpell disableTrailingParticles() {
		this.spawnParticles = false;
		return this;
	}

	/**
	 * Returns a damage source corresponding to the magic type (e.g. fire for fire, etc.)
	 */
	protected DamageSource getDamageSource() {
		MagicType type = this.getType();
		DamageSource source = new DamageSourceFireIndirect("blast.fire", this, getThrower(), true);
		switch (type) {
		case ICE: source = new DamageSourceIceIndirect("blast.ice", this, this.getThrower(), 50, 1, true).setStunDamage(60, 10, true); break;
		case LIGHTNING: source = new DamageSourceShockIndirect("blast.lightning", this, this.getThrower(), 50, 1, true); break;
		case WATER: source = new DamageSourceBaseIndirect("blast.water", this, this.getThrower(), true, type.getDamageType()); break;
		case WIND: source = new DamageSourceBaseIndirect("blast.wind", this, this.getThrower(), true, type.getDamageType()); break;
		default: break; // fire
		}
		if (bypassesArmor) {
			source.setDamageBypassesArmor();
		}
		return source.setMagicDamage();
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		if (this.reflectChance >= 0.0F && PlayerUtils.isMirrorShield(shield)) {
			return this.reflectChance; // specified reflect chance takes precedence
		}
		// AoE modifier reduces reflect chance the greater the spell radius
		float aoeMod = (this.getArea() / 4.0F);
		float chance = super.getReflectChance(shield, player, source, damage);
		return chance - aoeMod;
	}

	@Override
	public float getReflectedWobble(ItemStack shield, EntityPlayer player, DamageSource source) {
		return 2.0F + this.rand.nextFloat() * 13.0F;
	}

	@Override
	protected float getVelocity() {
		return 1.0F;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!isDead) {
			// spell should 'impact' liquids as well
			Block block = worldObj.getBlockState(new BlockPos(this)).getBlock();
			if (block.getMaterial().isLiquid()) {
				onImpact(new MovingObjectPosition(new Vec3(posX, posY, posZ), EnumFacing.UP, new BlockPos(this)));
			}
		}
		MagicType type = getType();
		if (worldObj.isRemote && spawnParticles) {
			EnumParticleTypes particle = type.getTrailingParticle();
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
		// Give attacked entity a chance to block / reflect the projectile
		if (mop.entityHit instanceof EntityLivingBase) {
			if (mop.entityHit.attackEntityFrom(getDamageSource().setProjectile(), getDamage()) && !mop.entityHit.isDead) {
				handlePostDamageEffects((EntityLivingBase) mop.entityHit);
			}
		}
		// Skip AoE if projectile was reflected during above attack resolution
		if (this.wasReflected) {
			this.wasReflected = false; // set back to false so next impact can process
			return;
		}
		double x = (mop.entityHit != null ? mop.entityHit.posX : mop.getBlockPos().getX() + 0.5D);
		double y = (mop.entityHit != null ? mop.entityHit.posY : mop.getBlockPos().getY() + 0.5D);
		double z = (mop.entityHit != null ? mop.entityHit.posZ : mop.getBlockPos().getZ() + 0.5D);
		float r = getArea();
		List<EntityLivingBase> list = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x - r, y - r, z - r, x + r, y + r, z + r));
		for (EntityLivingBase entity : list) {
			if (entity == mop.entityHit) {
				continue; // already attacked entity struck directly
			}
			Vec3 vec3 = new Vec3(posX - motionX, posY - motionY, posZ - motionZ);
			Vec3 vec31 = new Vec3(entity.posX, entity.posY, entity.posZ);
			MovingObjectPosition mop1 = worldObj.rayTraceBlocks(vec3, vec31);
			if (mop1 != null && mop1.typeOfHit == MovingObjectType.BLOCK) {
				Block block = worldObj.getBlockState(mop1.getBlockPos()).getBlock();
				if (block.getMaterial().blocksMovement()) {
					continue;
				}
			}
			// Not projectile damage any longer
			if (entity.attackEntityFrom(getDamageSource(), getDamage()) && !entity.isDead) {
				handlePostDamageEffects(entity);
			}
		}
		if (worldObj.isRemote) {
			spawnImpactParticles(EnumParticleTypes.EXPLOSION_LARGE, 4, -0.1F);
			spawnImpactParticles(getType().getTrailingParticle(), 16, getType() == MagicType.ICE ? 0.0F : -0.2F);
		} else {
			worldObj.playSoundAtEntity(this, "random.explode", 2.0F, (1.0F + (worldObj.rand.nextFloat() - worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
			if (canGrief && getType().affectsBlocks(worldObj, getThrower())) {
				Set<BlockPos> affectedBlocks = new HashSet<BlockPos>(WorldUtils.getAffectedBlocksList(worldObj, rand, r, posX, posY, posZ, null));
				ItemMagicRod.affectAllBlocks(worldObj, affectedBlocks, getType());
			}
			setDead();
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnImpactParticles(EnumParticleTypes particle, int n, float offsetY) {
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
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			if (getThrower() instanceof EntityPlayer) {
				worldObj.setBlockState(new BlockPos(i, j, k), Blocks.ice.getDefaultState());
				worldObj.setBlockState(new BlockPos(i, j + 1, k), Blocks.ice.getDefaultState());
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
		compound.setBoolean("canGrief", canGrief);
		compound.setBoolean("spawnParticles", spawnParticles);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		setType(MagicType.values()[compound.getInteger("magicType") % MagicType.values().length]);
		setArea(compound.getFloat("areaOfEffect"));
		reflectChance = compound.getFloat("reflectChance");
		bypassesArmor = compound.getBoolean("bypassesArmor");
		canGrief = compound.getBoolean("canGrief");
		spawnParticles = compound.getBoolean("spawnParticles");
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		super.writeSpawnData(buffer);
		buffer.writeInt(type.ordinal());
		buffer.writeFloat(radius);
		buffer.writeBoolean(spawnParticles);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		super.readSpawnData(buffer);
		type = (MagicType.values()[buffer.readInt() % MagicType.values().length]);
		radius = buffer.readFloat();
		spawnParticles = buffer.readBoolean();
	}
}

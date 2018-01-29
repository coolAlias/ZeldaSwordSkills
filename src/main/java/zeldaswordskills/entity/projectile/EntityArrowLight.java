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
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseDirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.entity.IEntityEvil;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Light Arrows ignore armor and cause Holy damage.
 * 
 * Slays non-boss Endermen and Wither Skeletons instantly, regardless of health
 * 
 * Can also dispel certain magical barriers and pierce through any block,
 * making activating buttons particularly tricky, but possible through walls
 *
 */
public class EntityArrowLight extends EntityArrowElemental
{
	public EntityArrowLight(World world) {
		super(world);
	}

	public EntityArrowLight(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowLight(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowLight(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	protected double getBaseDamage() {
		return 6.0D;
	}

	@Override
	protected DamageSource getDamageSource(Entity entity) {
		if (entity instanceof EntityEnderman) {
			return new DamageSourceBaseDirect("arrow.light", (shootingEntity != null ? shootingEntity : this), EnumDamageType.HOLY).setProjectile().setMagicDamage().setDamageBypassesArmor();
		}
		DamageSource source = new DamageSourceBaseIndirect("arrow.light", this, shootingEntity, EnumDamageType.HOLY).setProjectile().setDamageBypassesArmor();
		// Witches have a hard-coded 85% magic damage reduction, plus the Buff; skip setting magic damage so they can be killed
		return (entity instanceof EntityWitch ? source : source.setMagicDamage());
	}

	@Override
	protected boolean canTargetEntity(Entity entity) {
		return true;
	}

	@Override
	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.EXPLOSION_NORMAL;
	}

	@Override
	protected boolean shouldSpawnParticles() {
		return true;
	}

	@Override
	public float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage) {
		return (PlayerUtils.isMirrorShield(shield) ? 1.0F : 0.0F);
	}

	@Override
	protected void onImpactBlock(MovingObjectPosition mop) {
		if (Config.enableLightArrowNoClip()) {
			if (this.ticksExisted < 25) {
				Block block = this.worldObj.getBlockState(mop.getBlockPos()).getBlock();
				this.affectBlock(block, mop.getBlockPos());
				if (block.getMaterial() != Material.air) {
					block.onEntityCollidedWithBlock(this.worldObj, mop.getBlockPos(), this);
				}
			} else {
				this.extinguishLightArrow();
			}
		} else {
			super.onImpactBlock(mop);
			this.extinguishLightArrow();
		}
	}

	@Override
	protected void onImpactEntity(MovingObjectPosition mop) {
		if (mop.entityHit instanceof EntityLivingBase && this.canOneHitKill(mop.entityHit)) {
			float velocity = this.getCurrentVelocity();
			EntityLivingBase entity = (EntityLivingBase) mop.entityHit;
			entity.attackEntityFrom(this.getDamageSource(entity), entity.getMaxHealth() * 0.425F * velocity);
			this.playSound(Sounds.BOW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
			// TODO render bright flash, different sound effect?
			if (!this.worldObj.isRemote) {
				this.setDead();
			}
		} else {
			super.onImpactEntity(mop);
		}
	}

	@Override
	protected float calculateDamage(Entity entityHit) {
		float dmg = super.calculateDamage(entityHit);
		if (entityHit instanceof IEntityEvil) {
			dmg = ((IEntityEvil) entityHit).getLightArrowDamage(dmg);
		}
		return dmg;
	}

	/**
	 * Returns true if the light arrow can kill this entity in one hit (endermen and wither skeletons)
	 */
	private boolean canOneHitKill(Entity entity) {
		if (entity instanceof IEntityEvil) {
			return ((IEntityEvil) entity).isLightArrowFatal();
		}
		boolean flag = (entity instanceof EntitySkeleton && ((EntitySkeleton) entity).getSkeletonType() == 1);
		return (!(entity instanceof IBossDisplayData)) && (flag || entity instanceof EntityEnderman || entity instanceof EntityWitch);
	}

	@Override
	protected boolean affectBlock(Block block, BlockPos pos) {
		// TODO dispel magical barriers
		return false;
	}

	/**
	 * Sets this arrow to dead after spawning some particles
	 */
	private void extinguishLightArrow() {
		for (int i = 0; i < 10; ++i) {
			double dx = this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double) this.width;
			double dy = this.posY + (double)(this.rand.nextFloat() * this.height);
			double dz = this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double) this.width;
			this.worldObj.spawnParticle(this.getParticle(), dx, dy, dz, this.rand.nextGaussian() * 0.02D, this.rand.nextGaussian() * 0.02D, this.rand.nextGaussian() * 0.02D);
		}
		if (!this.worldObj.isRemote) {
			this.setDead();
		}
	}
}

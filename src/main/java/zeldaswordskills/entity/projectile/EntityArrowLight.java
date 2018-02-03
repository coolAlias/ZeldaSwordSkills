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
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseDirect;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.damage.EnumDamageType;
import zeldaswordskills.api.entity.IEntityEvil;
import zeldaswordskills.entity.ai.IEntityTeleport;
import zeldaswordskills.ref.Config;
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
	protected void entityInit() {
		super.entityInit();
		this.setPiercing();
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
		DamageSourceBaseIndirect source = new DamageSourceBaseIndirect("arrow.light", this, shootingEntity, EnumDamageType.HOLY);
		source.setProjectile().setDamageBypassesArmor();
		// Flag as unavoidable vs. teleporting entities
		if (entity instanceof IEntityTeleport) {
			source.setUnavoidable();
		}
		// Witches have a hard-coded 85% magic damage reduction, plus the Buff; skip setting magic damage so they can be killed
		return (entity instanceof EntityWitch ? source : source.setMagicDamage());
	}

	@Override
	protected boolean canTargetEntity(Entity entity) {
		return true;
	}

	@Override
	protected String getParticleName() {
		return "explode";
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
		// Can only fly through blocks if it hasn't already hit an entity
		if (Config.enableLightArrowNoClip() && this.entitiesHit.isEmpty()) {
			if (this.ticksExisted < 25) {
				Block block = this.worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
				this.affectBlock(block, mop.blockX, mop.blockY, mop.blockZ);
				if (block.getMaterial() != Material.air) {
					block.onEntityCollidedWithBlock(this.worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
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
		super.onImpactEntity(mop);
		// Piercing arrows only get to kill one insta-kill mob
		if (this.canOneHitKill(mop.entityHit) && (!mop.entityHit.isEntityAlive() || (mop.entityHit instanceof EntityLivingBase && ((EntityLivingBase) mop.entityHit).getHealth() <= 0.0F))) {
			this.setDead();
		}
	}

	@Override
	protected float calculateDamage(Entity entityHit) {
		float dmg = super.calculateDamage(entityHit);
		// One-hit kills take precedence over IEntityEvil#getLightArrowDamage
		if (entityHit instanceof EntityLivingBase && this.canOneHitKill(entityHit)) {
			float velocity = this.getCurrentVelocity();
			dmg = Math.max(dmg, ((EntityLivingBase) entityHit).getMaxHealth() * 0.425F * velocity);
		} else if (entityHit instanceof IEntityEvil) {
			dmg = ((IEntityEvil) entityHit).getLightArrowDamage(this, dmg);
		}
		return dmg;
	}

	/**
	 * Returns true if the light arrow can kill this entity in one hit (endermen and wither skeletons)
	 */
	private boolean canOneHitKill(Entity entity) {
		if (entity instanceof IEntityEvil) {
			return ((IEntityEvil) entity).isLightArrowFatal(this);
		}
		boolean flag = (entity instanceof EntitySkeleton && ((EntitySkeleton) entity).getSkeletonType() == 1);
		return (!(entity instanceof IBossDisplayData)) && (flag || entity instanceof EntityEnderman || entity instanceof EntityWitch);
	}

	@Override
	protected boolean affectBlock(Block block, int x, int y, int z) {
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
			this.worldObj.spawnParticle(this.getParticleName(), dx, dy, dz, this.rand.nextGaussian() * 0.02D, this.rand.nextGaussian() * 0.02D, this.rand.nextGaussian() * 0.02D);
		}
		if (!this.worldObj.isRemote) {
			this.setDead();
		}
	}
}

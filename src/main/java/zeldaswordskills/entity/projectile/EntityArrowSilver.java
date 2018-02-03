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

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.entity.IEntityEvil;
import zeldaswordskills.entity.ai.IEntityTeleport;

/**
 * 
 * Silver Arrows inflict massive physical damage and may affect 
 * {@link IEntityEvil} enemies similarly to Light Arrows.
 *
 */
public class EntityArrowSilver extends EntityArrowCustom
{
	public EntityArrowSilver(World world) {
		super(world);
	}

	public EntityArrowSilver(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowSilver(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowSilver(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.setPiercing();
	}

	@Override
	protected double getBaseDamage() {
		return 8.0D;
	}

	@Override
	protected DamageSource getDamageSource(Entity entity) {
		if (entity instanceof EntityEnderman) {
			return new EntityDamageSource("arrow.silver", (shootingEntity != null ? shootingEntity : this)).setProjectile();
		}
		// Flag as unavoidable vs. teleporting entities
		DamageSourceBaseIndirect source = new DamageSourceBaseIndirect("arrow.silver", this, this.shootingEntity);
		return (entity instanceof IEntityTeleport ? source.setUnavoidable().setProjectile() : source.setProjectile());
	}

	@Override
	protected void onImpactBlock(MovingObjectPosition mop) {
		super.onImpactBlock(mop);
		// Prevent arrows from being reusable after hitting any entity
		if (!this.entitiesHit.isEmpty()) {
			this.setDead();
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
		float dmg =  super.calculateDamage(entityHit);
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
	 * Returns true if this arrow can kill the entity in one hit
	 */
	private boolean canOneHitKill(Entity entity) {
		if (entity instanceof IEntityEvil) {
			return ((IEntityEvil) entity).isLightArrowFatal(this);
		} else if (entity instanceof IBossDisplayData) {
			return false;
		}
		return (entity instanceof EntityCreeper || entity instanceof EntityWitch);
	}

	@Override
	protected void handlePostDamageEffects(EntityLivingBase entityHit) {
		super.handlePostDamageEffects(entityHit);
	}
}

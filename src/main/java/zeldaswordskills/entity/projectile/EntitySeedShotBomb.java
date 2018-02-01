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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.SideHit;

public class EntitySeedShotBomb extends EntitySeedShot
{
	public EntitySeedShotBomb(World world) {
		super(world);
	}

	public EntitySeedShotBomb(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
	}

	public EntitySeedShotBomb(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	public EntitySeedShotBomb(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity, velocity, n, spread);
	}

	public EntitySeedShotBomb(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected float getBaseDamage() {
		return 3.0F;
	}

	@Override
	public DamageSource getDamageSource() {
		return new DamageSourceBaseIndirect("slingshot", this, this.getThrower()).setExplosion().setProjectile();
	}

	@Override
	protected String getParticleName() {
		return "largeexplode";
	}

	@Override
	protected void onImpact(MovingObjectPosition mop) {
		if (mop.entityHit != null) {
			CustomExplosion.createExplosion(new EntityBomb(this.worldObj, this.getThrower()), this.worldObj, this.posX, this.posY, this.posZ, 3.0F, this.getDamage(), false);
			this.setDead();
		} else {
			Block block = worldObj.getBlock(mop.blockX, mop.blockY, mop.blockZ);
			if (block.getMaterial() != Material.air) {
				block.onEntityCollidedWithBlock(this.worldObj, mop.blockX, mop.blockY, mop.blockZ, this);
			}
			if (!block.getBlocksMovement(this.worldObj, mop.blockX, mop.blockY, mop.blockZ)) {
				double dx = mop.sideHit == SideHit.WEST ? -0.5D : mop.sideHit == SideHit.EAST ? 0.5D : 0.0D;
				double dy = mop.sideHit == SideHit.BOTTOM ? -0.5D : mop.sideHit == SideHit.TOP ? 0.5D : 0.0D;
				double dz = mop.sideHit == SideHit.NORTH ? -0.5D : mop.sideHit == SideHit.SOUTH ? 0.5D : 0.0D;
				if (!this.worldObj.isRemote) {
					CustomExplosion.createExplosion(new EntityBomb(this.worldObj, getThrower()), this.worldObj, this.posX + dx, this.posY + dy, this.posZ + dz, 3.0F, this.getDamage(), false);
				}
				this.playSound(Sounds.DAMAGE_SUCCESSFUL_HIT, 0.3F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
				this.setDead();
			}
		}
		// Only spawn particles if it hit something for sure
		if (!this.isEntityAlive() && this.worldObj.isRemote) {
			this.spawnImpactParticles();
		}
	}
}

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
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceFireIndirect;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Fire Arrows ignite nearby blocks and enemies as well as melt even the coldest of ice.
 *
 */
public class EntityArrowFire extends EntityArrowElemental
{
	public EntityArrowFire(World world) {
		super(world);
	}

	public EntityArrowFire(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowFire(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowFire(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	protected DamageSource getDamageSource(Entity entity) {
		return new DamageSourceFireIndirect("arrow.fire", this, this.shootingEntity).setProjectile().setMagicDamage();
	}

	@Override
	protected String getParticleName() {
		return "flame";
	}

	@Override
	protected boolean shouldSpawnParticles() {
		return true;
	}

	@Override
	protected void updateInAir() {
		super.updateInAir();
		if (this.worldObj.handleMaterialAcceleration(this.boundingBox, Material.water, this)) {
			this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, Sounds.FIRE_FIZZ, 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
			if (!this.worldObj.isRemote) {
				this.setDead();
			}
		}
	}

	@Override
	protected boolean affectBlock(Block block, int x, int y, int z) {
		if (block.getMaterial() == Material.air && Config.enableFireArrowIgnite()) {
			Block block2 = this.worldObj.getBlock(x, y - 1, z);
			if (block2.func_149730_j() && this.rand.nextInt(8) == 0) {
				this.worldObj.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, Sounds.FIRE_IGNITE, 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
				this.worldObj.setBlock(x, y, z, Blocks.fire);
				return true;
			}
		} else if (WorldUtils.canMeltBlock(this.worldObj, block, x, y, z)) {
			this.worldObj.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, Sounds.FIRE_FIZZ, 1.0F, this.rand.nextFloat() * 0.4F + 0.8F);
			this.worldObj.setBlockToAir(x, y, z);
			return true;
		}
		return false;
	}
}

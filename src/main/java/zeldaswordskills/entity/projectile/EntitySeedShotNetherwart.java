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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.World;

public class EntitySeedShotNetherwart extends EntitySeedShot
{
	public EntitySeedShotNetherwart(World world) {
		super(world);
		this.setFire(100);
	}

	public EntitySeedShotNetherwart(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
		this.setFire(100);
	}

	public EntitySeedShotNetherwart(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		this.setFire(100);
	}

	public EntitySeedShotNetherwart(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity, velocity, n, spread);
		this.setFire(100);
	}

	public EntitySeedShotNetherwart(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setFire(100);
	}

	@Override
	protected float getBaseDamage() {
		return 1.5F;
	}

	@Override
	public DamageSource getDamageSource() {
		return new EntityDamageSourceIndirect("slingshot", this, this.getThrower()).setFireDamage().setProjectile();
	}
}

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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceBaseIndirect;

public class EntitySeedShotDeku extends EntitySeedShot
{
	public EntitySeedShotDeku(World world) {
		super(world);
	}

	public EntitySeedShotDeku(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
	}

	public EntitySeedShotDeku(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	public EntitySeedShotDeku(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity, velocity, n, spread);
	}

	public EntitySeedShotDeku(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	protected float getBaseDamage() {
		return 1.5F;
	}

	@Override
	public DamageSource getDamageSource() {
		return new DamageSourceBaseIndirect("slingshot", this, this.getThrower()).setStunDamage(80, 2, true).setProjectile();
	}

	@Override
	protected EnumParticleTypes getParticle() {
		return EnumParticleTypes.EXPLOSION_LARGE;
	}
}

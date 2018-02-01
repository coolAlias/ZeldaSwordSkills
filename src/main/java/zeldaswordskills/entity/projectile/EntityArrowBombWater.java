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
import net.minecraft.world.World;
import zeldaswordskills.api.entity.BombType;

public class EntityArrowBombWater extends EntityArrowBomb
{
	public EntityArrowBombWater(World world) {
		super(world);
	}

	public EntityArrowBombWater(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	public EntityArrowBombWater(World world, EntityLivingBase shooter, float velocity) {
		super(world, shooter, velocity);
	}

	public EntityArrowBombWater(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
	}

	@Override
	public BombType getType() {
		return BombType.BOMB_WATER;
	}

	@Override
	protected boolean isDud(boolean inFire) {
		return inFire || this.worldObj.provider.getDimensionId() == -1;
	}
}

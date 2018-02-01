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
import net.minecraft.block.BlockButton;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import zeldaswordskills.util.WorldUtils;

public class EntitySeedShotMelon extends EntitySeedShot
{
	public EntitySeedShotMelon(World world) {
		super(world);
		this.setKnockback(1);
	}

	public EntitySeedShotMelon(World world, EntityLivingBase entity, float velocity) {
		this(world, entity, velocity, 1, 0F);
		this.setKnockback(1);
	}

	public EntitySeedShotMelon(World world, EntityLivingBase shooter, EntityLivingBase target, float velocity, float wobble) {
		super(world, shooter, target, velocity, wobble);
		this.setKnockback(1);
	}

	public EntitySeedShotMelon(World world, EntityLivingBase entity, float velocity, int n, float spread) {
		super(world, entity, velocity, n, spread);
		this.setKnockback(1);
	}

	public EntitySeedShotMelon(World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setKnockback(1);
	}

	@Override
	protected float getBaseDamage() {
		return 1.25F;
	}

	@Override
	protected boolean onImpactBlock(Block block, int x, int y, int z) {
		if (block instanceof BlockButton) {
			WorldUtils.activateButton(this.worldObj, block, x, y, z);
			return true;
		}
		return !block.getBlocksMovement(this.worldObj, x, y, z);
	}
}

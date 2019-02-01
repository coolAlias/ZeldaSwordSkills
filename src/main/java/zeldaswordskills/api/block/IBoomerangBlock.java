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

package zeldaswordskills.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import zeldaswordskills.entity.projectile.EntityBoomerang;

public interface IBoomerangBlock {

	/**
	 * Called instead {@code Block#onEntityCollidedWithBlock} when an {@code EntityBoomerang}
	 * enters the block's space. Any special interactions (e.g. destroying or activating
	 * the block) should be handled here, as the default interactions will not occur.
	 * @return true to cause the boomerang to reverse course after hitting the block
	 */
	boolean onBoomerangCollided(World world, BlockPos pos, IBlockState state, EntityBoomerang boomerang);

}

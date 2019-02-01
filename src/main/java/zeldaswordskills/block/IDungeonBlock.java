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

package zeldaswordskills.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * 
 * An interface to allow a variety of blocks to be considered valid during
 * TileEntityDungeonCore's structure validation process
 *
 */
public interface IDungeonBlock {

	/**
	 * Called when verifying the door block to allow for variant and tile entity checking.
	 * Typical implementation may look like this:
	 *		IBlockState expected = getStateFromMeta(meta);
	 *		return ((EnumType) state.getValue(VARIANT)) == ((EnumType) expected.getValue(VARIANT));
	 * @param	state Current state of the block at the given position
	 * @param	meta Metadata value of the expected door variant
	 * @return	True if the current state variant is equivalent to that for the metadata value
	 */
	boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta);

}

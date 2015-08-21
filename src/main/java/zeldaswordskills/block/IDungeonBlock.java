/**
    Copyright (C) <2015> <coolAlias>

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

package zeldaswordskills.block;

import net.minecraft.world.World;

/**
 * 
 * An interface to allow a variety of blocks to be considered valid during
 * TileEntityDungeonCore's structure validation process
 *
 */
public interface IDungeonBlock {

	/**
	 * Called when verifying the door block at x/y/z to allow for variant and tile entity checking.
	 * Typical implementation may look like this (assuming 1:1 correlation of metadata variants):
	 *		return (world.getBlockMetadata(x, y, z) == expected);
	 * @param	expected Metadata value of the expected door variant
	 * @return	True if the world block is equivalent to that of the same block with the expected metadata value
	 */
	boolean isSameVariant(World world, int x, int y, int z, int expected);

}

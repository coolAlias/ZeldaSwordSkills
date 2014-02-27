/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.api.item;

import zeldaswordskills.api.block.BlockWeight;

/**
 * 
 * Interface for items that can lift blocks, replacing the item temporarily with an
 * ItemHeldBlock and returning the original itemstack when the held block is placed.
 * 
 * Only solid opaque cubes without tile entities may be lifted, and vanilla blocks
 * may not be lifted in Adventure Mode or if the player is otherwise unable to edit
 * the world. ILiftable blocks are not restricted by editing permissions.
 *
 */
public interface ILiftBlock {
	
	/**
	 * Returns the strength of this item for the purpose of lifting blocks
	 */
	public BlockWeight getLiftStrength();

}

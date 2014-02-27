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
 * Interface for items that can smash (typically destroy) blocks immediately upon
 * left-clicking the block. Blocks that do not implement ISmashable cannot be
 * destroyed if the player is not allowed to edit the world; otherwise any
 * solid opaque normal cube can be smashed if the item's strength is greater
 * than or equal to the associated BlockWeight.
 * 
 * ISmashable blocks can define custom behaviors using the onSmashed method, but all
 * other blocks that meet the smash criteria will simply be destroyed.
 *
 */
public interface ISmashBlock {
	
	/**
	 * Returns the strength of this item for the purpose of smashing blocks
	 */
	public BlockWeight getSmashStrength();

}

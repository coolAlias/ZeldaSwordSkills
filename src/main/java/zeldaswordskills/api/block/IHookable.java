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

package zeldaswordskills.api.block;

import net.minecraft.block.material.Material;

/**
 * 
 * Interface for any block that wants to return a different material type
 * than its own for purposes of determining which hookshots can attach to it
 *
 */
public interface IHookable {
	
	/**
	 * Return true to always allow hookshots to attach, regardless of block material
	 */
	public boolean canAlwaysGrab();
	
	/**
	 * Returns the Material type that should be used to determine which, if
	 * any, hookshots can attach to this block or, if it can't attach, whether
	 * this block will be destroyed by the hookshot upon impact
	 */
	public Material getHookableMaterial();

}

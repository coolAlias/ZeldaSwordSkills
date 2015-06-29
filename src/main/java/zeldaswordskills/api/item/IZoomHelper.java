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

package zeldaswordskills.api.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * An interface specifically for items that can be worn in the vanilla Armor
 * Slots; when equipped and an item in use modifies the field of view, the
 * fov will be further modified by this amount.
 *
 */
public interface IZoomHelper {

	/**
	 * When this item is equipped in one of the vanilla armor slots and an
	 * item in use modifies the field of view, the fov will be magnified by
	 * this amount; negative values will zoom out
	 */
	@SideOnly(Side.CLIENT)
	float getMagnificationFactor();

}

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

import zeldaswordskills.item.IModItem;

/**
 * 
 * Interface to allow the Block to define any variants to return
 * in its ItemBlock class' {@link IModItem#getVariants}
 *
 */
public interface IBlockItemVariant {

	/**
	 * Variants for block Item's {@link IModItem#getVariants} method to return; null is allowed
	 */
	String[] getItemBlockVariants();

}

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

import net.minecraft.item.Item;
import zeldaswordskills.item.ZSSItems;

public class ZSSItemHelper {

	/**
	 * Adds a ZSS comparator mapping for the item. Call for each item that will
	 * display on a ZSS Creative Tab after registering the item.
	 */
	public static void addItemForZssCreativeTab(Item item) {
		ZSSItems.addItemComparatorMapping(item);
	}
}

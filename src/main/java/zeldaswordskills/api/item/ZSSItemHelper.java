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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import zeldaswordskills.creativetab.ZSSCreativeTabs.ZSSCreativeTab;
import zeldaswordskills.item.ZSSItems;

public class ZSSItemHelper {

	/**
	 * Adds a ZSS comparator mapping for the item if it displays on a ZSS Creative Tab.
	 * Call for each item set to display on a ZSS Creative Tab when registering the item.
	 */
	public static void addItemForZssCreativeTab(Item item) {
		CreativeTabs[] tabs = item.getCreativeTabs();
		if (tabs == null) {
			return;
		}
		for (CreativeTabs tab : tabs) {
			if (tab instanceof ZSSCreativeTab) {
				ZSSItems.addItemComparatorMapping(item);
				break;
			}
		}
	}
}

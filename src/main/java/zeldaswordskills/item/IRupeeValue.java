/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.RupeeValueRegistry;

/**
 * 
 * Interface for Items that have hard-coded RupeeTrades and wish to automatically
 * add user-configurable rupee value entries to the {@link RupeeValueRegistry}.
 *
 */
public interface IRupeeValue
{
	/**
	 * The default value of this item, in rupees. Used for setting the default config values
	 * and as a backup in some cases when the rupee value config is missing.
	 * <br>Use {@link RupeeValueRegistry#getRupeeValue(ItemStack)} to retrieve user-configured pricing.
	 * @param stack Only Item and damage values are relevant; stack size and NBT are ignored
	 * @return The default price a player should expect to pay for this item, in rupees
	 */
	int getDefaultRupeeValue(ItemStack stack);

	/**
	 * 
	 * Use this interface for Items whose default rupee values depend on the item's subtype.
	 *
	 */
	public static interface IMetaRupeeValue extends IRupeeValue
	{
		/**
		 * Only called for items that have subtypes (see {@link Item#getHasSubtypes()}).
		 * <br>Include a stack with {@link OreDictionary#WILDCARD_VALUE} to provide a catch-all rupee value for this item.
		 * @return List of all ItemStacks to add to the {@link RupeeValueRegistry} via {@link #getDefaultRupeeValue(ItemStack)}
		 */
		List<ItemStack> getRupeeValueSubItems();
	}
}

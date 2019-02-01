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

package zeldaswordskills.api.item;

import net.minecraft.item.Item;

/**
 * 
 * Items implementing this interface will not be enchantable using the vanilla
 * anvil and enchanted books SO LONG AS the item returns 0 or less from
 * {@link Item#getItemEnchantability()}.
 * 
 * This is merely to fix what I deem broken vanilla behavior, as items that return
 * 0 for enchantability should not be enchantable using enchanted books, in my opinion.
 * 
 * In Zelda Sword Skills, there is also a configuration option that, if true, extends
 * this behavior to all items, preventing the enchantment of things like carrots; if
 * that option is set to false, then only IUnenchantable items will be affected.
 * 
 * REQUIRES Forge 10.13.0.1180 or higher to function, but will not break the game
 * if used in earlier versions.
 *
 */
public interface IUnenchantable {
	/**
	 * Called when an item is placed in the anvil with an enchanted book
	 * @param toEnchant		The stack being enchanted
	 * @param enchantedBook	The Enchanted Book
	 * @return	Result of the combination, or null to prevent enchantment
	 */
	//ItemStack getAnvilResult(ItemStack toEnchant, ItemStack enchantedBook);
}

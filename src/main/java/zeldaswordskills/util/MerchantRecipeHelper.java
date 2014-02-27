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

package zeldaswordskills.util;

import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

/**
 * 
 * Helper methods for interacting with MerchantRecipeLists and MerchantRecipes
 *
 */
public class MerchantRecipeHelper {

	/**
	 * Returns true if a recipe (trade) with the exact same stacks already exists in the merchant's list
	 * This method uses vanilla ItemStack.areItemStacksEqual, so it should be NBT sensitive as well
	 * @return always returns true if the recipe is null
	 */
	public static boolean doesListContain(MerchantRecipeList list, MerchantRecipe recipe) {
		for (int i = 0; i < list.size() && recipe != null; ++i) {
			MerchantRecipe recipe1 = (MerchantRecipe) list.get(i);
			if (ItemStack.areItemStacksEqual(recipe.getItemToBuy(), recipe1.getItemToBuy())) {
				if (ItemStack.areItemStacksEqual(recipe.getSecondItemToBuy(), recipe1.getSecondItemToBuy())) {
					if (ItemStack.areItemStacksEqual(recipe.getItemToSell(), recipe1.getItemToSell())) {
						return true;
					}
				}
			}
		}

		return (recipe == null);
	}
	
	/**
	 * Adds a trade to the merchant's list only if a similar trade does not already exist
	 */
	public static boolean addUniqueTrade(MerchantRecipeList list, MerchantRecipe trade) {
		if (!doesListContain(list, trade)) {
			list.add(trade);
			return true;
		}
		return false;
	}
	
	/**
	 * Shortcut method to attempt adding a trade without replacing a currently existing trade
	 * @return returns true if the new trade was added or replaced a current trade
	 */
	public static boolean addToListWithCheck(MerchantRecipeList list, MerchantRecipe trade) {
		return addToListWithCheck(list, trade, false);
	}
	
	/**
	 * Adds the trade to the list if and only if a similar trade (i.e. same IDs but different
	 * stack sizes) doesn't already exist, or if 'replaceExistingTrade' is true.
	 * @return returns true if the new trade was added or replaced a current trade
	 */
	public static boolean addToListWithCheck(MerchantRecipeList list, MerchantRecipe trade, boolean replaceExistingTrade) {
		for (int i = 0; i < list.size(); ++i)
		{
			MerchantRecipe merchantrecipe1 = (MerchantRecipe) list.get(i);
			if (haveSameTradeItems(trade, merchantrecipe1)) {
				if (replaceExistingTrade) {
					list.set(i, trade);
					return true;
				}

				return false;
			}
		}

		list.add(trade);
		return true;
	}
	
	/**
	 * Returns true if all the items (to buy and sell) have matching item IDs and metadata
	 * Does not care about stack size or NBT tags.
	 */
	public static boolean haveSameTradeItems(MerchantRecipe a, MerchantRecipe b) {
		ItemStack item1a = a.getItemToBuy();
		ItemStack item1b = b.getItemToBuy();
		ItemStack item2a = a.getItemToSell();
		ItemStack item2b = b.getItemToSell();
		ItemStack item3a = a.getSecondItemToBuy();
		ItemStack item3b = b.getSecondItemToBuy();
		if (item1a.itemID == item1b.itemID && item1a.getItemDamage() == item1b.getItemDamage()) {
			if (item2a.itemID == item2b.itemID && item2a.getItemDamage() == item2b.getItemDamage()) {
				if ((item3a == null && item3b == null) || (item3a != null && item3b != null &&
					item3a.itemID == item3b.itemID && item3a.getItemDamage() == item3b.getItemDamage()))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}

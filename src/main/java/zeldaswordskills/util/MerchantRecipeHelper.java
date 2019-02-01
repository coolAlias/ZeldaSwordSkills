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

package zeldaswordskills.util;

import java.util.Iterator;

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
	public static boolean doesListContain(MerchantRecipeList trades, MerchantRecipe recipe) {
		if (recipe == null) {
			return true;
		}
		for (int i = 0; i < trades.size(); ++i) {
			if (areTradesIdentical(recipe, (MerchantRecipe) trades.get(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if a MerchantRecipe (trade) containing the same items already
	 * exists in the merchant's list; not stack-size or NBT sensitive
	 * @return returns false if the trade is null or no similar trade was found
	 */
	public static boolean hasSimilarTrade(MerchantRecipeList trades, MerchantRecipe trade) {
		if (trade == null || trades == null) {
			return false;
		}
		for (int i = 0; i < trades.size(); ++i) {
			MerchantRecipe trade1 = (MerchantRecipe) trades.get(i);
			if (haveSameTradeItems(trade, trade1)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a trade to the merchant's list only if a similar trade does not already exist
	 * @return	True if the trade was added, false if the trade was already present
	 */
	public static boolean addUniqueTrade(MerchantRecipeList trades, MerchantRecipe trade) {
		if (!doesListContain(trades, trade)) {
			trades.add(trade);
			return true;
		}
		return false;
	}

	/**
	 * Shortcut method to attempt adding a trade without replacing a currently existing trade
	 * @return returns true if the new trade was added
	 */
	public static boolean addToListWithCheck(MerchantRecipeList trades, MerchantRecipe trade) {
		return addToListWithCheck(trades, trade, false);
	}

	/**
	 * Adds the trade to the list if and only if a similar trade (i.e. same Items but different
	 * stack sizes) doesn't already exist, or if 'replaceExistingTrade' is true.
	 * @return returns true if the new trade was added or replaced a current trade
	 */
	public static boolean addToListWithCheck(MerchantRecipeList trades, MerchantRecipe trade, boolean replaceExistingTrade) {
		for (int i = 0; i < trades.size(); ++i) {
			MerchantRecipe merchantrecipe1 = (MerchantRecipe) trades.get(i);
			if (haveSameTradeItems(trade, merchantrecipe1)) {
				if (replaceExistingTrade) {
					trades.set(i, trade);
					return true;
				}
				return false;
			}
		}
		trades.add(trade);
		return true;
	}

	/**
	 * Removes matching trades from the recipe list
	 * @param exactMatch true to match stack sizes and NBTTagCompounds
	 * @param removeAll true to remove all matching trades, or false to remove only one
	 * @return true if trade was removed
	 */
	public static boolean removeTrade(MerchantRecipeList trades, MerchantRecipe trade, boolean exactMatch, boolean removeAll) {
		boolean found = false;
		Iterator<MerchantRecipe> iterator = trades.iterator();
		while (iterator.hasNext()) {
			MerchantRecipe recipe = iterator.next();
			if (exactMatch ? areTradesIdentical(trade, recipe) : haveSameTradeItems(trade, recipe)) {
				iterator.remove();
				if (!removeAll) {
					return true;
				}
				found = true;
			}
		}
		return found;
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
		if (item1a != null && item1b != null && item1a.getItem() == item1b.getItem() && item1a.getItemDamage() == item1b.getItemDamage()) {
			if (item2a != null && item2b != null && item2a.getItem() == item2b.getItem() && item2a.getItemDamage() == item2b.getItemDamage()) {
				return (item3a == null && item3b == null) || (item3a != null && item3b != null &&
						item3a.getItem() == item3b.getItem() && item3a.getItemDamage() == item3b.getItemDamage());
			}
		}

		return false;
	}

	/**
	 * Returns true if the two trades match identically, including stack sizes and NBT tag compounds for each item.
	 * Neither parameter should be null.
	 */
	public static boolean areTradesIdentical(MerchantRecipe a, MerchantRecipe b) {
		if (ItemStack.areItemStacksEqual(a.getItemToBuy(), b.getItemToBuy())) {
			if (ItemStack.areItemStacksEqual(a.getSecondItemToBuy(), b.getSecondItemToBuy())) {
				if (ItemStack.areItemStacksEqual(a.getItemToSell(), b.getItemToSell())) {
					return true;
				}
			}
		}
		return false;
	}
}

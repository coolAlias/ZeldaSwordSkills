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

package zeldaswordskills.api.entity.merchant;

import com.google.common.base.Predicate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import zeldaswordskills.api.item.ItemStackPredicate;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackNbtPredicate;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackQuantityPredicate;

/**
 * 
 * Predicates for comparing RupeeTrades, usually for determining which to keep
 * in the context of e.g. {@link RupeeTradeList#addOrUpdateTrade}
 *
 */
public abstract class RupeeTradePredicate implements Predicate<RupeeTrade>
{
	protected final RupeeTrade trade;

	public RupeeTradePredicate(RupeeTrade trade) {
		this.trade = trade;
	}

	/**
	 * 
	 * Predicate returns true if the trade is considered 'better' than the input as far as price and use limit.
	 * <br><br>Specifically:
	 * <li>An unlimited trade will only be replaced by an unlimited trade with better pricing.</li>
	 * <li>A limited trade will be replaced by an unlimited trade with better or equal pricing, or if the current trade disabled.</li>
	 * <li>A disabled limited trade will be replaced by a usable limited trade, regardless of pricing.</li>
	 * <li>Limited trades will be replaced by trades with at least equivalent pricing and more uses remaining.</li>
	 * 
	 */
	public static class RupeeTradeUpgradePredicate extends RupeeTradePredicate
	{
		/**
		 * See {@link RupeeTradeUpgradePredicate}
		 * @param trade The trade that will replace an existing trade if {@link #apply(RupeeTrade)} returns true 
		 */
		public RupeeTradeUpgradePredicate(RupeeTrade trade) {
			super(trade);
		}

		@Override
		public boolean apply(RupeeTrade input) {
			if (input == null) { return false; }
			float a_price = (float)input.getPrice() / (float)input.getTradeItem().stackSize;
			float b_price = (float)this.trade.getPrice() / (float)this.trade.getTradeItem().stackSize;
			// Unlimited use - only replace if replacement is also unlimited and has a better price ratio
			if (input.getMaxUses() == 0) {
				return this.trade.getMaxUses() == 0 && b_price < a_price;
			} else if (this.trade.getMaxUses() == 0) {
				return input.isDisabled() || b_price <= a_price;
			}
			// A trade that can be used is better than one that can't be, regardless of price
			if (input.isDisabled() && !this.trade.isDisabled()) {
				return true;
			} else if (a_price < b_price) {
				return false; // don't replace existing trade with a worse price
			}
			// Replace if trade has more uses remaining (and at least equivalent pricing - checked above)
			int a_use = input.getMaxUses() - input.getTimesUsed();
			int b_use = this.trade.getMaxUses() - this.trade.getTimesUsed();
			return b_use > a_use;
		}
	}

	/**
	 * 
	 * Wrapper for applying ItemStack predicate to RupeeTrade's trade item
	 *
	 */
	public static class RupeeTradeStackPredicate implements Predicate<RupeeTrade>
	{
		protected final Predicate<ItemStack> predicate;

		/**
		 * Predicate wrapper that matches trades based on matching stack items and stack damage, ignoring NBT and stack size
		 * @param trade The trade to be compared against
		 */
		public RupeeTradeStackPredicate(RupeeTrade trade) {
			this(ItemStackPredicate.get(trade.getTradeItem(), true, false, false));
		}

		/**
		 * @param trade The trade to be compared against
		 * @param matchDamage True to require an exact item damage match unless the stack to compare against uses {@link OreDictionary#WILDCARD_VALUE}
		 * @param matchNbt True to add {@link ItemStackNbtPredicate}
		 * @param matchQty True to add {@link ItemStackQuantityPredicate}
		 */
		public RupeeTradeStackPredicate(RupeeTrade trade, boolean matchDamage, boolean matchNbt, boolean matchQty) {
			this(ItemStackPredicate.get(trade.getTradeItem(), matchDamage, matchNbt, matchQty));
		}

		/**
		 * @param predicate Used to check whether the rupee trades' item stacks match
		 */
		public RupeeTradeStackPredicate(Predicate<ItemStack> predicate) {
			this.predicate = predicate;
		}

		@Override
		public boolean apply(RupeeTrade input) {
			return this.predicate.apply(input.getTradeItem());
		}
	}
}

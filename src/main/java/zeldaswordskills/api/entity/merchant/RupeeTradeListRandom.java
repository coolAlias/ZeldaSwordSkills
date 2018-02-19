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

import java.util.Collections;
import java.util.Random;

import com.google.gson.JsonElement;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;

/**
 * 
 * List of {@link RupeeTradeRandom} for building either an ordered list of
 * individually randomized trades or a completley randomized list of trades.
 * 
 */
public class RupeeTradeListRandom extends RupeeTradeList<RupeeTradeRandom>
{
	private static final long serialVersionUID = 1L;

	public RupeeTradeListRandom(String tagName) {
		super(tagName);
	}

	public RupeeTradeListRandom(NBTTagCompound tag) {
		super(tag);
	}

	public RupeeTradeListRandom(JsonElement json) {
		super(json);
	}

	public RupeeTradeListRandom(RupeeTradeListRandom original) {
		super(original);
	}

	/**
	 * Generates a rupee trading list possibly containing all entries from this list, in order,
	 * using the unmodified trade weight to determine if it should be included.
	 * <br><br>
	 * Any trades with variable properties will have those traits randomized accordingly.
	 */
	public RupeeTradeList<RupeeTrade> getRandomizedTradeList(Random rand) {
		RupeeTradeList<RupeeTrade> trades = new RupeeTradeList<RupeeTrade>(this.getTagName());
		for (int i = 0; i < this.size(); ++i) {
			RupeeTradeRandom trade = this.get(i);
			if (rand.nextFloat() < trade.getWeight()) {
				trades.add(trade.getRandomizedTrade(rand));
			}
		}
		return trades;
	}

	/**
	 * Generates a rupee trading list with random entries chosen from this list based on their
	 * modified weights (see {@link #adjustProbability(float)})
	 * @param rand
	 * @param mod  Trade weight modifier, usually from {@link #getDefaultModifier(RupeeTradeList)}
	 * @return Randomized list of rupee trades
	 */
	public RupeeTradeList<RupeeTrade> getRandomizedTradeList(Random rand, float mod) {
		RupeeTradeList<RupeeTrade> trades = new RupeeTradeList<RupeeTrade>(this.getTagName());
		for (int i = 0; i < this.size(); ++i) {
			RupeeTradeRandom trade = this.get(i);
			if (rand.nextFloat() < this.adjustProbability(trade.getWeight() + mod)) {
				trades.add(trade.getRandomizedTrade(rand));
			}
		}
		Collections.shuffle(trades);
		return trades;
	}

	/**
	 * Returns the default trade weight modifier based on the list's size as in EntityVillager
	 */
	public static float getDefaultModifier(RupeeTradeList<? extends RupeeTrade> list) {
		int size = (list == null ? 0 : list.size());
		return (size > 0 ? MathHelper.sqrt_float((float) size) * 0.2F : 0);
	}

	/**
	 * Adjusts the trade's weight using the same formula as found in EntityVillager
	 */
	protected float adjustProbability(float weight) {
		return weight > 0.9F ? 0.9F - (weight - 0.9F) : weight;
	}

	@Override
	protected RupeeTradeRandom getTradeFromNBT(NBTTagCompound compound) {
		return new RupeeTradeRandom(compound);
	}

	@Override
	protected RupeeTradeRandom getTradeFromJson(JsonElement json) {
		return new RupeeTradeRandom(json);
	}
}

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;

import com.google.common.base.Predicate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IJsonSerializable;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.api.entity.merchant.RupeeTradePredicate.RupeeTradeUpgradePredicate;

/**
 * 
 * Serves the same purpose as vanilla's MerchantRecipeList class, but for {@link RupeeTrade RupeeTrades}.
 */
public class RupeeTradeList extends ArrayList<RupeeTrade> implements IJsonSerializable
{
	private static final long serialVersionUID = 1L;

	/** Suggested NBT key to use for the list of items the rupee merchant will purchase */
	public static final String WILL_BUY = "RupeeTraderBuys";

	/** Suggested NBT key to use for the list of items the rupee merchant will offer for sale */
	public static final String FOR_SALE = "RupeeTraderSells";

	private String tagName;

	/**
	 * @param tagName Key used for NBT storage; suggest using {@link #WILL_BUY} or {@link #FOR_SALE}
	 */
	public RupeeTradeList(String tagName) {
		this.tagName = tagName;
	}

	public RupeeTradeList(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	public RupeeTradeList(JsonElement json) {
		this.fromJson(json);
	}

	/**
	 * Copy constructor
	 */
	public RupeeTradeList(RupeeTradeList original) {
		this.tagName = original.tagName;
		this.clear();
		this.addAll(original);
	}

	public String getTagName() {
		return this.tagName;
	}

	/**
	 * Adds the trade to the list if not already present according to the {@link RupeeTrade#DEFAULT default} comparator
	 * @return true if the trade was added
	 */
	public boolean addTrade(RupeeTrade trade) {
		return this.addOrUpdateTrade(trade, RupeeTrade.DEFAULT, null);
	}

	/**
	 * Adds the trade to the list if not already present according to the specified comparator
	 * @param comparator Used to determine whether the new trade matches any of the existing trades
	 * @return true if the trade was added
	 */
	public boolean addTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator) {
		return this.addOrUpdateTrade(trade, comparator, null);
	}

	/**
	 * Calls {@link #addOrUpdateTrade(RupeeTrade, Comparator, Predicate)} with the {@link RupeeTrade#DEFAULT default}
	 * comparator and a {@link RupeeTradeUpgradePredicate} predicate
	 */
	public boolean addOrUpdateTrade(RupeeTrade trade) {
		return this.addOrUpdateTrade(trade, RupeeTrade.DEFAULT, new RupeeTradeUpgradePredicate(trade));
	}

	/**
	 * Calls {@link #addOrUpdateTrade(RupeeTrade, Comparator, Predicate)} with a {@link RupeeTradeUpgradePredicate} predicate
	 */
	public boolean addOrUpdateTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator) {
		return this.addOrUpdateTrade(trade, comparator, new RupeeTradeUpgradePredicate(trade));
	}

	/**
	 * Calls {@link #addOrUpdateTrade(RupeeTrade, Comparator, Predicate)} with the {@link RupeeTrade#DEFAULT default} comparator
	 */
	public boolean addOrUpdateTrade(RupeeTrade trade, Predicate<RupeeTrade> predicate) {
		return this.addOrUpdateTrade(trade, RupeeTrade.DEFAULT, predicate);
	}

	/**
	 * Adds or updates a trade
	 * @param trade The trade to add or use in place of an existing trade provided the predicate's conditions are met
	 * @param comparator Used to determine whether the new trade matches any of the existing trades
	 * @param predicate Used to determine whether the new trade should replace an existing trade when a match is found
	 * @return true if the trade was added or replaced
	 */
	public boolean addOrUpdateTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator, Predicate<RupeeTrade> predicate) {
		boolean match = false;
		for (ListIterator<RupeeTrade> iterator = this.listIterator(); iterator.hasNext();) {
			RupeeTrade listTrade = iterator.next();
			if (listTrade.matches(trade, comparator)) {
				if (predicate == null) {
					return false; // trade already present, not replacing
				} else if (predicate.apply(listTrade)) {
					iterator.set(trade);
					return true; // existing trade replaced
				}
				match = true;
			}
		}
		if (match) {
			return false; // trade found but not replaced
		}
		this.add(trade);
		return true; // new trade added
	}

	/**
	 * Adds or removes (all instances) of the trade from this list
	 */
	public void addOrRemoveTrade(RupeeTrade trade, boolean add) {
		if (add) {
			this.addTrade(trade);
		} else {
			this.removeTrade(trade, true);
		}
	}

	/**
	 * Checks if the trade list contains the trade using the {@link RupeeTrade#DEFAULT default} comparator
	 */
	public boolean containsTrade(RupeeTrade trade) {
		return this.containsTrade(trade, RupeeTrade.DEFAULT);
	}

	/**
	 * Checks if the trade list contains the trade using the specified comparator 
	 * @param trade RupeeTrade to search for
	 * @param comparator Used to determine whether the new trade matches any of the existing trades
	 * @return true if this list contains a trade matching the one provided
	 */
	public boolean containsTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator) {
		for (int i = 0; i < this.size(); ++i) {
			if (this.get(i).matches(trade, comparator)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the trade list contains any trade that meets the predicate's conditions
	 */
	public boolean containsTrade(Predicate<RupeeTrade> predicate) {
		for (int i = 0; i < this.size(); ++i) {
			if (predicate.apply(this.get(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the first RupeeTrade from this list matching the specified trade using the {@link RupeeTrade#DEFAULT default} comparator
	 */
	public RupeeTrade getTrade(RupeeTrade trade) {
		return this.getTrade(trade, RupeeTrade.DEFAULT);
	}

	/**
	 * Returns the first RupeeTrade from this list matching the specified trade using the specified comparator 
	 * @param trade RupeeTrade to search for
	 * @param comparator Used to determine whether an existing trade matches the requested trade
	 * @return a matching RupeeTrade instance from this list, or null
	 */
	public RupeeTrade getTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator) {
		for (int i = 0; i < this.size(); ++i) {
			RupeeTrade listTrade = this.get(i);
			if (listTrade.matches(trade, comparator)) {
				return listTrade;
			}
		}
		return null;
	}

	/**
	 * Returns the first RupeeTrade from this list that meets the predicate's conditions
	 */
	public RupeeTrade getTrade(Predicate<RupeeTrade> predicate) {
		for (int i = 0; i < this.size(); ++i) {
			RupeeTrade listTrade = this.get(i);
			if (predicate.apply(listTrade)) {
				return listTrade;
			}
		}
		return null;
	}

	/**
	 * Removes the trade using the {@link RupeeTrade#DEFAULT default} comparator to search for matches
	 */
	public boolean removeTrade(RupeeTrade trade, boolean removeAll) {
		return this.removeTrade(trade, RupeeTrade.DEFAULT, removeAll);
	}

	/**
	 * Removes matching trades from the trade list
	 * @param comparator Used to determine whether an existing trade matches the one to remove
	 * @param removeAll true to remove all matching trades, or false to remove only one
	 * @return true if at least one trade was removed
	 */
	public boolean removeTrade(RupeeTrade trade, Comparator<RupeeTrade> comparator, boolean removeAll) {
		boolean found = false;
		Iterator<RupeeTrade> iterator = this.iterator();
		while (iterator.hasNext()) {
			RupeeTrade listTrade = iterator.next();
			if (listTrade.matches(trade, comparator)) {
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
	 * Removes matching trades from the trade list
	 * @param predicate Used to determine whether an existing trade matches the one to remove
	 * @param removeAll true to remove all matching trades, or false to remove only one
	 * @return true if at least one trade was removed
	 */
	public boolean removeTrade(Predicate<RupeeTrade> predicate, boolean removeAll) {
		boolean found = false;
		Iterator<RupeeTrade> iterator = this.iterator();
		while (iterator.hasNext()) {
			if (predicate.apply(iterator.next())) {
				iterator.remove();
				if (!removeAll) {
					return true;
				}
				found = true;
			}
		}
		return found;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("tag_name", this.tagName);
		NBTTagList trades = new NBTTagList();
		for (int i = 0; i < this.size(); ++i) {
			trades.appendTag(get(i).writeToNBT());
		}
		compound.setTag(this.tagName, trades);
		return compound;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.clear();
		this.tagName = compound.getString("tag_name");
		NBTTagList trades = compound.getTagList(this.tagName, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < trades.tagCount(); ++i) {
			this.add(new RupeeTrade(trades.getCompoundTagAt(i)));
		}
	}

	public void fromJson(JsonElement json) {
		this.func_152753_a(json);
	}

	@Override
	public void func_152753_a(JsonElement json) {
		if (!json.isJsonObject()) {
			throw new JsonParseException("RupeeTradeList JSON incorrectly formatted: expected JsonObject");
		}
		this.tagName = json.getAsJsonObject().get("tag_name").getAsString();
		JsonArray trades = json.getAsJsonObject().get("trades").getAsJsonArray();
		for (JsonElement e : trades) {
			this.add(new RupeeTrade(e));
		}
	}

	@Override
	public JsonElement getSerializableElement() {
		JsonObject object = new JsonObject();
		object.addProperty("tag_name", this.tagName);
		JsonArray trades = new JsonArray();
		for (RupeeTrade trade : this) {
			trades.add(trade.getSerializableElement());
		}
		object.add("trades", trades);
		return object;
	}
}

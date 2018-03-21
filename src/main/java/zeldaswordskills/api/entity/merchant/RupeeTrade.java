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

import java.util.Comparator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IJsonSerializable;
import net.minecraftforge.oredict.OreDictionary;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.INbtComparable;
import zeldaswordskills.entity.player.ZSSPlayerWallet.EnumWallet;

/**
 * 
 * Similar to vanilla's MerchantRecipe class but uses rupees from the player's wallet.
 * 
 * Any items a player tries to sell must {@link #matches(ItemStack) match} the trade's stack.
 *
 */
public class RupeeTrade implements IJsonSerializable
{
	/** Maximum trade price (based on max wallet capacity) */
	public static final int MAX_PRICE = EnumWallet.TYCOON.capacity;

	/** ItemStack to purchase */
	private ItemStack tradeStack;

	/** Purchase price in rupees */
	private int price;

	/** Number of times this trade may be used; 0 or less implies no limit */
	private int maxUses;

	/** Number of times this trade has been used */
	private int timesUsed;

	/** Max uses will not automatically increase when the trade list is refreshed */
	private boolean noRefresh;

	/** Automatically remove trade after it reaches its max use limit and the trade list is refreshed; requires {@link #noRefresh} to also be true */
	private boolean autoRemove;

	/**
	 * Creates an unlimited RupeeTrade
	 * @param tradeStack Use {@link OreDictionary#WILDCARD_VALUE} for the item damage to allow matching any damage value
	 * @param price
	 */
	public RupeeTrade(ItemStack tradeStack, int price) {
		this(tradeStack, price, 0, false, false);
	}

	/**
	 * Creates a RupeeTrade that refreshes once the max use limit is reached
	 * @param tradeStack Use {@link OreDictionary#WILDCARD_VALUE} for the item damage to allow matching any damage value
	 * @param price
	 * @param maxUses Number of times this trade may be used; see {@link #maxUses}
	 */
	public RupeeTrade(ItemStack tradeStack, int price, int maxUses) {
		this(tradeStack, price, maxUses, false, false);
	}

	/**
	 * Creates a RupeeTrade according to the arguments
	 * @param tradeStack Use {@link OreDictionary#WILDCARD_VALUE} for the item damage to allow matching any damage value
	 * @param price
	 * @param maxUses Number of times this trade may be used; see {@link #maxUses}
	 * @param noRefresh Prevent maxUses from increasing automatically; see {@link #noRefresh}
	 * @param autoRemove Automatically remove trade once maxUses is reached; see {@link #autoRemove}
	 */
	public RupeeTrade(ItemStack tradeStack, int price, int maxUses, boolean noRefresh, boolean autoRemove) {
		if (tradeStack == null) {
			throw new IllegalArgumentException("ItemStack can not be null");
		}
		this.tradeStack = tradeStack;
		this.setPrice(price);
		this.maxUses = maxUses;
		this.noRefresh = noRefresh;
		this.autoRemove = (autoRemove && noRefresh); // can't auto remove if allowed to refresh
	}

	public RupeeTrade(NBTTagCompound tag) {
		this.readFromNBT(tag);
	}

	public RupeeTrade(JsonElement json) {
		this.fromJson(json);
	}

	/**
	 * Returns an exact copy of the rupee trade, including timesUsed
	 */
	public RupeeTrade copy() {
		RupeeTrade trade = new RupeeTrade(this.tradeStack.copy(), this.price, this.maxUses, this.noRefresh, this.autoRemove);
		trade.timesUsed = this.timesUsed;
		return trade;
	}

	/**
	 * Returns a copy of the ItemStack to purchase (or sell)
	 */
	public ItemStack getTradeItem() {
		return this.tradeStack.copy();
	}

	public int getPrice() {
		return this.price;
	}

	protected void setPrice(int price) {
		this.price = Math.min(price, RupeeTrade.MAX_PRICE);
	}

	/**
	 * The number of times this trade may be used; 0 or less implies no limit
	 */
	public int getMaxUses() {
		return this.maxUses;
	}

	/**
	 * Sets the number of times this trade may be used; 0 implies no limit
	 */
	public RupeeTrade setMaxUses(int maxUses) {
		this.maxUses = Math.max(0, maxUses);
		return this;
	}

	/**
	 * Increases (or decreases) the number of times this trade may be used by n.
	 * <br>Check {@link #getMaxUses()} before calling this if you don't want to alter an unlimited trade.
	 */
	public void increaseMaxUses(int n) {
		this.maxUses += n;
	}

	public int getTimesUsed() {
		return this.timesUsed;
	}

	public void incrementTimesUsed() {
		++this.timesUsed;
	}

	/**
	 * Set the number of times this trade has been used
	 */
	public void setTimesUsed(int n) {
		this.timesUsed = Math.max(0, n);
	}

	/**
	 * @return True if this trade has limited uses and has reached that limit
	 */
	public boolean isDisabled() {
		return this.maxUses > 0 && this.timesUsed >= this.maxUses;
	}

	/**
	 * @return True if the trade is allowed to increase the max uses once the limit is reached
	 */
	public boolean allowRefresh() {
		return !this.noRefresh;
	}

	/**
	 * @return True if this trade should be removed automatically once it reaches its max use limit
	 */
	public boolean removeWhenDisabled() {
		return this.autoRemove;
	}

	/**
	 * Returns true if this trade matches the input trade exactly according to the specified comparator
	 */
	public boolean matches(RupeeTrade trade, Comparator<RupeeTrade> comparator) {
		return (comparator.compare(this, trade) == 0);
	}

	private static boolean doesNbtMatch(ItemStack a, ItemStack b) {
		if (!a.hasTagCompound() && !b.hasTagCompound()) {
			return true;
		} else if (a.getItem() instanceof INbtComparable) {
			return ((INbtComparable) a.getItem()).areTagsEquivalent(a, b);
		}
		return ItemStack.areItemStackTagsEqual(a, b);
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("tradeStack", this.tradeStack.writeToNBT(new NBTTagCompound()));
		tag.setInteger("price", this.price);
		if (this.maxUses > 0) {
			tag.setInteger("maxUses", this.maxUses);
		}
		if (this.timesUsed > 0) {
			tag.setInteger("timesUsed", this.timesUsed);
		}
		if (this.noRefresh) {
			tag.setBoolean("noRefresh", this.noRefresh);
		}
		if (this.autoRemove) {
			tag.setBoolean("autoRemove", this.autoRemove);
		}
		return tag;
	}

	public void readFromNBT(NBTTagCompound tag) {
		this.tradeStack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("tradeStack"));
		this.setPrice(tag.getInteger("price"));
		this.maxUses = tag.getInteger("maxUses");
		this.timesUsed = tag.getInteger("timesUsed");
		this.noRefresh = tag.getBoolean("noRefresh");
		this.autoRemove = tag.getBoolean("autoRemove");
	}

	public void fromJson(JsonElement json) {
		this.func_152753_a(json);
	}

	@Override
	public void func_152753_a(JsonElement json) {
		if (!json.isJsonObject()) {
			throw new JsonParseException("RupeeTrade JSON incorrectly formatted: expected JsonObject");
		}
		JsonObject object = json.getAsJsonObject();
		JsonElement itemstack = object.get("itemstack");
		if (!itemstack.isJsonObject()) {
			throw new JsonParseException("RupeeTrade JSON itemstack entry incorrectly formatted: expected JsonObject");
		}
		this.tradeStack = this.itemstackFromJson(itemstack.getAsJsonObject());
		this.setPrice(object.get("price").getAsInt());
		if (object.has("maxUses")) {
			this.maxUses = object.get("maxUses").getAsInt();
		}
		if (object.has("noRefresh")) {
			this.noRefresh = object.get("noRefresh").getAsBoolean();
		}
		if (object.has("autoRemove")) {
			this.autoRemove = object.get("autoRemove").getAsBoolean();
		}
	}

	@Override
	public JsonElement getSerializableElement() {
		JsonObject object = new JsonObject();
		object.add("itemstack", this.itemstackToJson(this.tradeStack));
		object.addProperty("price", this.getPrice());
		this.addAdditionalProperties(object);
		if (this.maxUses > 0) {
			object.addProperty("maxUses", this.maxUses);
		}
		if (this.noRefresh) {
			object.addProperty("noRefresh", this.noRefresh);
		}
		if (this.autoRemove) {
			object.addProperty("autoRemove", this.autoRemove);
		}
		return object;
	}

	/**
	 * Hook to allow adding additional properties when serializing this object
	 */
	protected void addAdditionalProperties(JsonObject object) {}

	private JsonObject itemstackToJson(ItemStack stack) {
		JsonObject object = new JsonObject();
		object.addProperty("item", Item.itemRegistry.getNameForObject(stack.getItem()));
		if (stack.getItemDamage() > 0) {
			object.addProperty("damage", stack.getItemDamage());
		}
		if (stack.stackSize > 1) {
			object.addProperty("stack_size", stack.stackSize);
		}
		if (stack.hasTagCompound()) {
			object.addProperty("nbt", stack.getTagCompound().toString());
		}
		return object;
	}

	private ItemStack itemstackFromJson(JsonObject object) {
		String id = object.get("item").getAsString();
		Item item = (Item) Item.itemRegistry.getObject(id);
		if (item == null) {
			throw new JsonParseException(String.format("Unable to find Item %s while loading rupee trade from JSON", id));
		}
		int damage = (object.has("damage") ? object.get("damage").getAsInt() : 0);
		int stack_size = (object.has("stack_size") ? object.get("stack_size").getAsInt() : 1);
		ItemStack stack = new ItemStack(item, stack_size, damage);
		if (object.has("nbt")) {
			try {
				NBTBase tag = JsonToNBT.func_150315_a(object.get("nbt").getAsString());
				if (tag instanceof NBTTagCompound) {
					stack.setTagCompound((NBTTagCompound) tag);
				}
			} catch (NBTException e) {
				ZSSMain.logger.error("Error reading rupee trade itemstack NBT from JSON: " + e.getMessage());
			}
		}
		return stack;
	}

	/**
	 * 
	 * Note that the provided implementations of this class are not consistent with {@link #equals(Object)};
	 * as such, they should not be used to sort sorted sets or sorted maps.
	 * 
	 * Furthermore, comparisons should usually be made such that the first parameter of {@link #compare(RupeeTrade, RupeeTrade)}
	 * is an existing trade in the RupeeTradeList. This is so that items in the player inventory
	 * cannot be 'hacked' to use {@link OreDictionary#WILDCARD_VALUE} to bypass item damage requirements.
	 *
	 */
	public static abstract class RupeeTradeComparator implements Comparator<RupeeTrade> {}

	/**
	 * Simple comparison considers two trades 'equal' if they sell the same item, ignoring everything else.
	 */
	public static final Comparator<RupeeTrade> SIMPLE = new RupeeTradeComparator() {
		@Override
		public int compare(RupeeTrade a, RupeeTrade b) {
			ItemStack x = a.getTradeItem();
			ItemStack y = b.getTradeItem();
			if (x.getItem() != y.getItem()) {
				return (Item.getIdFromItem(x.getItem()) < Item.getIdFromItem(y.getItem()) ? -1 : 1);
			}
			return 0;
		}
	};

	/**
	 * Default comparator considers two trades 'equal' if they have the same item, damage, and NBT.
	 * <br>If the left-hand RupeeTrade uses {@link OreDictionary#WILDCARD_VALUE} value, item damage is ignored.
	 * <br>Items implementing {@link INbtComparable} will use that for comparing NBT.
	 * <br>Note that if two NBT tags are deemed not equal but their String representations
	 * are identical, they will be considered equal.
	 */
	public static class DefaultRupeeTradeComparator extends RupeeTradeComparator {
		@Override
		public int compare(RupeeTrade a, RupeeTrade b) {
			ItemStack x = a.getTradeItem();
			ItemStack y = b.getTradeItem();
			if (x.getItem() != y.getItem()) {
				return (Item.getIdFromItem(x.getItem()) < Item.getIdFromItem(y.getItem()) ? -1 : 1);
			} else if (x.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
				// don't need to compare item damage
			} else if (x.getItemDamage() != y.getItemDamage()) {
				return (x.getItemDamage() < y.getItemDamage() ? -1 : 1);
			} else if (!RupeeTrade.doesNbtMatch(x, y)) {
				// If both have tags, compare based on String version of tags
				if (x.hasTagCompound() == y.hasTagCompound()) {
					return x.getTagCompound().toString().compareTo(y.getTagCompound().toString());
				}
				// Simple ordering prioritizes stacks without tags over those with tags
				return (x.hasTagCompound() ? 1 : -1);
			}
			return 0;
		}
	};

	/**
	 * See {@link DefaultRupeeTradeComparator}
	 */
	public static final Comparator<RupeeTrade> DEFAULT = new DefaultRupeeTradeComparator();

	/**
	 * Same as the {@link RupeeTrade#DEFAULT} comparator with additional stack size comparison.
	 */
	public static final Comparator<RupeeTrade> DEFAULT_QTY = new DefaultRupeeTradeComparator() {
		@Override
		public int compare(RupeeTrade a, RupeeTrade b) {
			int i = super.compare(a, b);
			if (i == 0 && a.getTradeItem().stackSize != b.getTradeItem().stackSize) {
				i = (a.getTradeItem().stackSize < b.getTradeItem().stackSize ? -1 : 1);
			}
			return i;
		}
	};

	/**
	 * Same as the {@link RupeeTrade#DEFAULT} comparator with additional trade use limit comparison.
	 */
	public static final Comparator<RupeeTrade> DEFAULT_MAX_USES = new DefaultRupeeTradeComparator() {
		@Override
		public int compare(RupeeTrade a, RupeeTrade b) {
			if (a.getMaxUses() != b.getMaxUses()) {
				return (a.getMaxUses() < b.getMaxUses() ? -1 : 1);
			}
			return super.compare(a, b);
		}
	};

	/**
	 * Strict comparator considers two trades 'equal' if the itemstacks to sell are
	 * identical in every way, including NBT, and sell for the same price.
	 * <br>Items implementing {@link INbtComparable} will use that for comparing NBT.
	 * <br>Note that if two NBT tags are deemed not equal but their String representations
	 * are identical, they will be considered equal.
	 */
	public static final Comparator<RupeeTrade> STRICT = new RupeeTradeComparator() {
		@Override
		public int compare(RupeeTrade a, RupeeTrade b) {
			if (a.getPrice() != b.getPrice()) {
				return (a.getPrice() < b.getPrice() ? -1 : 1);
			}
			ItemStack x = a.getTradeItem();
			ItemStack y = b.getTradeItem();
			if (x.getItem() != y.getItem()) {
				return (Item.getIdFromItem(x.getItem()) < Item.getIdFromItem(y.getItem()) ? -1 : 1);
			} else if (x.stackSize != y.stackSize) {
				return (x.stackSize < y.stackSize ? -1 : 1);
			} else if (x.getItemDamage() != y.getItemDamage()) {
				return (x.getItemDamage() < y.getItemDamage() ? -1 : 1);
			} else if (!RupeeTrade.doesNbtMatch(x, y)) {
				// If both have tags, compare based on String version of tags
				if (x.hasTagCompound() == y.hasTagCompound()) {
					return x.getTagCompound().toString().compareTo(y.getTagCompound().toString());
				}
				// Simple ordering prioritizes stacks without tags over those with tags
				return (x.hasTagCompound() ? 1 : -1);
			}
			return 0;
		}
	};
}

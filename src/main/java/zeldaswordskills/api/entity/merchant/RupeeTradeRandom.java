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

import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import zeldaswordskills.api.item.RupeeValueRegistry;

/**
 * 
 * RupeeTrade template with a specific stack used to generate rupee trades with a
 * randomized price and either randomized stack size or randomized enchantability.
 * <br><br>
 * For non-variable pricing, set {@link #price_min} to 0 or equal to the max price.
 * <br>
 * For non-variable stack size / enchantability, set {@link #rng_max} to 0.
 * <br><br>
 * See also {@link RupeeTradeListRandom} for generating randomized trade lists.
 *
 */
public class RupeeTradeRandom extends RupeeTrade
{
	/** The minimum price; price will not be variable if this value is less than 1 */
	protected int price_min;

	/** Lower bound for random distribution of either stack size or enchantability */
	protected int rng_min;

	/** Upper bound for random distribution of either stack size or enchantability; set to 0 for no variability */
	protected int rng_max;

	/** The base chance (0.0F to 1.0F) that this trade should be included in a randomized trade list */
	protected float weight;

	/** Whether to use the lower and upper bounds to add enchantments with {@link EnchantmentHelper#addRandomEnchantment} instead of randomizing stack size */
	protected boolean enchant;

	/** Whether price should scale with stack size, using the original stack size to determine the price to quantity ratio */
	protected boolean scale_price;

	/**
	 * Creates a {@link #RupeeTradeRandom(ItemStack, int, int, int, int, int, float) RupeeTradeRandom} with no variability of any kind and a weight of 1.0F.
	 * @param tradeStack
	 * @param price Fixed trade price
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 */
	public RupeeTradeRandom(ItemStack tradeStack, int price, int maxUses) {
		this(tradeStack, 0, price, 0, 0, maxUses, 1.0F);
	}

	/**
	 * Creates a {@link #RupeeTradeRandom(ItemStack, int, int, int, int, int, float) RupeeTradeRandom} with no variability of any kind.
	 * @param tradeStack
	 * @param price Fixed trade price
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 * @param weight Chance (0.0F to 1.0F) that this trade will be included in a randomized trade list; see {@link RupeeTradeListRandom#getRandomizedTradeList}
	 */
	public RupeeTradeRandom(ItemStack tradeStack, int price, int maxUses, float weight) {
		this(tradeStack, 0, price, 0, 0, maxUses, weight);
	}

	/**
	 * Creates a {@link #RupeeTradeRandom(ItemStack, int, int, int, int, int, float) RupeeTradeRandom} with a fixed stack size and no random enchantments.
	 * @param tradeStack
	 * @param price_range Pair containing the upper (right) and lower (left) bounds for the trade price
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 * @param weight Chance (0.0F to 1.0F) that this trade will be included in a randomized trade list; see {@link RupeeTradeListRandom#getRandomizedTradeList}
	 */
	public RupeeTradeRandom(ItemStack tradeStack, Pair<Integer, Integer> price_range, int maxUses, float weight) {
		this(tradeStack, price_range.getLeft(), price_range.getRight(), 0, 0, maxUses, weight);
	}

	/**
	 * Creates a {@link #RupeeTradeRandom(ItemStack, int, int, int, int, float) RupeeTradeRandom} with no enchantability
	 * @param tradeStack
	 * @param min The minimum price; if less than 0, the price will be randomized using {@link RupeeValueRegistry#getDefaultPriceRange(ItemStack, int)}
	 * @param max The maximum price
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 * @param weight Chance (0.0F to 1.0F) that this trade will be included in a randomized trade list; see {@link RupeeTradeListRandom#getRandomizedTradeList}
	 */
	public RupeeTradeRandom(ItemStack tradeStack, int min, int max, int maxUses, float weight) {
		this(tradeStack, min, max, 0, 0, maxUses, weight);
	}

	/**
	 * @param tradeStack
	 * @param price_range Pair containing the upper (right) and lower (left) bounds for the trade price
	 * @param rng_min Lower bound for random distribution of either stack size or enchantability
	 * @param rng_max Upper bound for random distribution of either stack size or enchantability
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 * @param weight Chance (0.0F to 1.0F) that this trade will be included in a randomized trade list; see {@link RupeeTradeListRandom#getRandomizedTradeList}
	 */
	public RupeeTradeRandom(ItemStack tradeStack, Pair<Integer, Integer> price_range, int rng_min, int rng_max, int maxUses, float weight) {
		this(tradeStack, price_range.getLeft(), price_range.getRight(), rng_min, rng_max, maxUses, weight);
	}

	/**
	 * @param tradeStack
	 * @param min The minimum price; if less than 0, the price will be randomized using {@link RupeeValueRegistry#getDefaultPriceRange(ItemStack, int)}
	 * @param max The maximum price
	 * @param rng_min Lower bound for random distribution of either stack size or enchantability
	 * @param rng_max Upper bound for random distribution of either stack size or enchantability
	 * @param maxUses Number of times this trade may be used; 0 or less implies no limit
	 * @param weight Chance (0.0F to 1.0F) that this trade will be included in a randomized trade list; see {@link RupeeTradeListRandom#getRandomizedTradeList}
	 */
	public RupeeTradeRandom(ItemStack tradeStack, int min, int max, int rng_min, int rng_max, int maxUses, float weight) {
		super(tradeStack, max, maxUses);
		this.price_min = min;
		this.rng_min = rng_min;
		this.rng_max = rng_max;
		this.weight = weight;
		this.enchant = false;
		this.scale_price = true;
	}

	public RupeeTradeRandom(NBTTagCompound tag) {
		super(tag);
		this.readAdditionalNbtFields(tag);
	}

	public RupeeTradeRandom(JsonElement json) {
		super(json);
		this.readAdditionalJsonFields(json.getAsJsonObject());
	}

	/**
	 * @return {@link #weight}
	 */
	public float getWeight() {
		return this.weight;
	}

	/**
	 * Sets the trade to add random enchantments instead of randomizing stack size
	 */
	public RupeeTradeRandom setEnchanted() {
		this.enchant = true;
		return this;
	}

	/**
	 * Set the price as absolute, i.e. it will not scale based on stack size when stack size is randomized
	 */
	public RupeeTradeRandom setPriceAbsolute() {
		this.scale_price = false;
		return this;
	}

	/**
	 * Returns randomized price clamped between the min and max.
	 * <br><br>
	 * If the min price is less than 0, {@link RupeeValueRegistry#getDefaultPriceRange}
	 * will be called with the max price as the backup to determine a random price value.
	 */
	public int getRandomPrice(Random rand) {
		int price = this.getPrice();
		if (this.price_min < 0) {
			Pair<Integer, Integer> pair = RupeeValueRegistry.getDefaultPriceRange(this.getTradeItem(), price);
			return pair.getLeft() + rand.nextInt(1 + pair.getRight() - pair.getLeft());
		} else if (this.price_min < 1) {
			return price;
		} else if (this.price_min >= price) {
			return this.price_min;
		}
		return this.price_min + rand.nextInt(1 + price - this.price_min);
	}

	/**
	 * @return RupeeTrade with randomized price and randomized stack size or enchantments
	 */
	public RupeeTrade getRandomizedTrade(Random rand) {
		ItemStack stack = this.getTradeItem();
		int price = this.getRandomPrice(rand);
		if (this.rng_max > 0 && this.rng_max >= this.rng_min) {
			int value = this.rng_min + rand.nextInt(1 + this.rng_max - this.rng_min);
			if (!this.enchant) {
				int size = stack.stackSize;
				stack.stackSize = MathHelper.clamp_int(value, 1, stack.getMaxStackSize());
				if (this.scale_price) {
					float ratio = (float) price / (float) size;
					price = MathHelper.ceiling_float_int(ratio * stack.stackSize);
				}
			} else if (value > 0) {
				EnchantmentHelper.addRandomEnchantment(rand, stack, value);
			}
		}
		return new RupeeTrade(stack, price, this.getMaxUses(), !this.allowRefresh(), this.removeWhenDisabled());
	}

	@Override
	public NBTTagCompound writeToNBT() {
		NBTTagCompound tag = super.writeToNBT();
		if (this.price_min < this.getPrice()) {
			tag.setInteger("price_min", this.price_min);
		}
		if (this.rng_max > 0) {
			tag.setInteger("rng_min", this.rng_min);
			tag.setInteger("rng_max", this.rng_max);
		}
		// Opposite of JSON - nbt #get methods default to 0 when value not present
		if (this.weight > 0.0F) {
			tag.setFloat("weight", this.weight);
		}
		if (this.enchant) {
			tag.setBoolean("enchant", this.enchant);
		}
		if (this.scale_price) {
			tag.setBoolean("scale_price", this.scale_price);
		}
		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.readAdditionalNbtFields(tag);
	}

	protected void readAdditionalNbtFields(NBTTagCompound tag) {
		this.price_min = tag.getInteger("price_min");
		this.rng_min = tag.getInteger("rng_min");
		this.rng_max = tag.getInteger("rng_max");
		this.weight = tag.getFloat("weight");
		this.enchant = tag.getBoolean("enchant");
		this.scale_price = tag.getBoolean("scale_price");
	}

	@Override
	public void func_152753_a(JsonElement json) {
		super.func_152753_a(json);
		this.readAdditionalJsonFields(json.getAsJsonObject());
	}

	protected void readAdditionalJsonFields(JsonObject object) {
		this.price_min = (object.has("price_min") ? (("random").equalsIgnoreCase(object.get("price_min").getAsString()) ? -1 : object.get("price_min").getAsInt()) : 0);
		this.rng_min = (object.has("rng_min") ? object.get("rng_min").getAsInt() : 0);
		this.rng_max = (object.has("rng_max") ? object.get("rng_max").getAsInt() : 0);
		// Weight is assumed to be 1.0F if not present
		this.weight = (object.has("weight") ? object.get("weight").getAsFloat() : 1.0F);
		this.enchant = (object.has("enchant") ? object.get("enchant").getAsBoolean() : false);
		this.scale_price = (object.has("scale_price") ? object.get("scale_price").getAsBoolean() : false);
	}

	@Override
	protected void addAdditionalProperties(JsonObject object) {
		if (this.price_min > 0 && this.price_min < this.getPrice()) {
			object.addProperty("price_min", this.price_min);
		} else if (this.price_min < 0) {
			object.addProperty("price_min", "random");
		}
		if (this.rng_max > 0) {
			object.addProperty("rng_min", this.rng_min);
			object.addProperty("rng_max", this.rng_max);
		}
		// Weight defaults to 1.0F when not present in the JSON file to make
		// default initial trade list JSONs with no variability simpler.
		if (this.weight < 1.0F) {
			object.addProperty("weight", this.weight);
		}
		if (this.enchant) {
			object.addProperty("enchant", this.enchant);
		}
		if (this.scale_price) {
			object.addProperty("scale_price", this.scale_price);
		}
	}
}

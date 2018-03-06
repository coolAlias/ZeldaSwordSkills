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

package zeldaswordskills.api.item;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.oredict.OreDictionary;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.item.IRupeeValue;
import zeldaswordskills.ref.Config;

/**
 * 
 * Creates a user-configurable registry of default rupee pricing for various Items.
 * Note that the rupee values in this registry are intended to be used for trades
 * added to merchants' lists via code, not for trades loaded from .json files.
 * <br><br>
 * Does not support damage values for items without subtypes.
 * <br><br>
 * Items with subtypes may use {@link OreDictionary#WILDCARD_VALUE} to provide a default
 * value for all subtypes in addition to storing up to one specific value per subtype.
 *
 */
public class RupeeValueRegistry
{
	public static final RupeeValueRegistry INSTANCE = new RupeeValueRegistry();

	/** FML Inter-Mod Communication key for registering item prices */
	public static final String IMC_RUPEE_KEY = "ZssRegisterRupeeValue";

	private final ConcurrentHashMap<Item, Integer> simpleRegistry;

	private final ConcurrentHashMap<Pair<Item, Integer>, Integer> subtypeRegistry;

	private static final Pattern PARSER = Pattern.compile("^(\\w+):([\\w\\.]+)(?:@(\\d+))?=(\\d+)$");

	private RupeeValueRegistry() {
		this.simpleRegistry = new ConcurrentHashMap<Item, Integer>();
		this.subtypeRegistry = new ConcurrentHashMap<Pair<Item, Integer>, Integer>();
	}

	/**
	 * Returns {@link #getRupeeTrade(ItemStack, int, int)} with no use limit.
	 * @param backup Price if no configured value found (see {@link #getRupeeValue(ItemStack, int)})
	 */
	public static RupeeTrade getRupeeTrade(ItemStack stack, int backup) {
		return RupeeValueRegistry.getRupeeTrade(stack, backup, 0);
	}

	/**
	 * Creates a RupeeTrade for the stack using the default price, unmodified for stack size.
	 * @param stack
	 * @param backup Price if no configured value found (see {@link #getRupeeValue(ItemStack, int)})
	 * @param maxUses See {@link RupeeTrade#getMaxUses()}
	 */
	public static RupeeTrade getRupeeTrade(ItemStack stack, int backup, int maxUses) {
		return new RupeeTrade(stack, RupeeValueRegistry.getRupeeValue(stack, backup), maxUses);
	}

	/**
	 * Generates the minimum and maximum price for the random rupee trade based on the configured default price
	 * (or the backup price) using {@link Config#getMinRupeePriceMultiplier} and {@link Config#getMaxRupeePriceMultiplier}.
	 * See {@link #getDefaultPriceRange(ItemStack, int, float, float)}
	 */
	public static Pair<Integer, Integer> getDefaultPriceRange(ItemStack stack, int backup) {
		return RupeeValueRegistry.getDefaultPriceRange(stack, backup, Config.getMinRupeePriceMultiplier(), Config.getMaxRupeePriceMultiplier());
	}

	/**
	 * Generates the minimum and maximum price for the random rupee trade based on the configured
	 * default price (or the backup price).
	 * @param stack
	 * @param backup Price if no configured value found
	 * @param min Multiplier to determine the minimum price
	 * @param max Multiplier to determine the maximum price
	 * @return
	 */
	public static Pair<Integer, Integer> getDefaultPriceRange(ItemStack stack, int backup, float min, float max) {
		int price = RupeeValueRegistry.getRupeeValue(stack, backup);
		int lower = Math.max(1, MathHelper.floor_float((min > max ? max : min) * price));
		int upper = Math.max(1, MathHelper.ceiling_float_int((max > min ? max : min) * price));
		return Pair.of(lower, upper);
	}

	/**
	 * Returns {@link #getRupeeValue(ItemStack)} or the backup price if no configured value found.
	 * @param Price if no configured value found; if < 0, will try to use {@link IRupeeValue#getDefaultRupeeValue(ItemStack)}
	 */
	public static Integer getRupeeValue(ItemStack stack, int backup) {
		if (backup < 0 && stack.getItem() instanceof IRupeeValue) {
			backup = ((IRupeeValue) stack.getItem()).getDefaultRupeeValue(stack);
		}
		Integer price = RupeeValueRegistry.getRupeeValue(stack);
		return (price == null ? backup : price);
	}

	/**
	 * Returns the user-specified rupee value for this stack's Item and, if the item has subtypes, damage value.
	 * <br>Note that these are recommended values only; individual merchant pricing may vary. 
	 * @param stack Items with subtypes try for an exact match, falling back on the {@link OreDictionary#WILDCARD_VALUE} entry otherwise
	 * @return NULL if no matching entry found or the rupee cost to purchase the stack
	 */
	public static Integer getRupeeValue(ItemStack stack) {
		if (!stack.getHasSubtypes()) {
			return INSTANCE.simpleRegistry.get(stack.getItem());
		}
		// Subtyped items try to return exact match first, then fall back on OreDictionary.WILDCARD_VALUE
		Pair<Item, Integer> pair = Pair.of(stack.getItem(), stack.getItemDamage());
		if (INSTANCE.subtypeRegistry.containsKey(pair)) {
			INSTANCE.subtypeRegistry.get(pair);
		}
		return INSTANCE.subtypeRegistry.get(Pair.of(stack.getItem(), OreDictionary.WILDCARD_VALUE));
	}

	/**
	 * Messages sent to {@link #IMC_RUPEE_KEY} are expected to have a String with the
	 * format "mod_id:unlocalized_item_name=rupee_value", e.g. "minecraft:apple=10".
	 * If the item price varies by subtype, append '@damage' to the item name, e.g. "minecraft:dye@2=10"
	 */
	public void processMessage(FMLInterModComms.IMCMessage msg) {
		if (msg.isStringMessage() && msg.key.equalsIgnoreCase(IMC_RUPEE_KEY)) {
			this.registerItems(new String[]{msg.getStringValue()}, "IMC");
		}
	}

	/**
	 * Registers rupee valuations for an array of item entries
	 * @param names Expected entry format 'modid:registered_item_name@damage=value'
	 * @param origin Information about the origin of the names array, e.g. "Config"
	 */
	public void registerItems(String[] names, String origin) {
		for (String entry : names) {
			String[] parts = parseString(entry);
			if (parts == null || parts.length < 4) {
				continue;
			}
			Integer value = this.parseInt(entry, parts[3]);
			Item item = GameRegistry.findItem(parts[0], parts[1]);
			if (item == null) {
				ZSSMain.logger.error(String.format("[RupeeValueRegistry] [%s] %s:%s Item could not be found - the mod may not be installed, or you may have typed it incorrectly", origin, parts[0], parts[1]));
			} else if (value == null || value < 0) {
				ZSSMain.logger.error(String.format("[RupeeValueRegistry] [%s] %s is not either not an integer or is less than 0", origin, parts[3]));
			} else if (!item.getHasSubtypes()) {
				Integer previous = this.simpleRegistry.put(item, Math.min(value, 9999));
				if (value == previous) {
					ZSSMain.logger.warn(String.format("[RupeeValueRegistry] [%s] Duplicate entry: %s", origin, entry));
				} else if (previous != null) {
					ZSSMain.logger.warn(String.format("[RupeeValueRegistry] [%s] Duplicate entry overwritten: %s:%s is now worth %d rupees instead of %d", origin, parts[0], parts[1], value, previous));
				} else {
					ZSSMain.logger.info(String.format("[RupeeValueRegistry] [%s] Simple entry added: %s:%s is worth %d rupees", origin, parts[0], parts[1], value));
				}
			} else {
				Integer damage = this.parseInt(entry, parts[2]);
				if (damage == null) {
					damage = OreDictionary.WILDCARD_VALUE;
					ZSSMain.logger.warn(String.format("[RupeeValueRegistry] [%s] Failed to parse damage value for %s; using OreDictionary.WILDCARD_VALUE instead", origin, entry));
				}
				Integer previous = this.subtypeRegistry.put(Pair.of(item, damage), Math.min(value, 9999));
				if (value == previous) {
					ZSSMain.logger.warn(String.format("[RupeeValueRegistry] [%s] Duplicate entry: %s", origin, entry));
				} else if (previous != null) {
					ZSSMain.logger.warn(String.format("[RupeeValueRegistry] [%s] Duplicate entry overwritten: %s:%s@%d is now worth %d rupees instead of %d", origin, parts[0], parts[1], damage, value, previous));
				} else {
					ZSSMain.logger.info(String.format("[RupeeValueRegistry] [%s] Subtype entry added: %s:%s@%d is worth %d rupees", origin, parts[0], parts[1], damage, value));
				}
			}
		}
	}

	private Integer parseInt(String entry, String integer) {
		try {
			return (integer == null ? null : Integer.valueOf(integer));
		} catch (NumberFormatException e) {
			ZSSMain.logger.error(String.format("[RupeeValueRegistry] %s could not be parsed as an integer in entry %s ", integer, entry));
		}
		return null;
	}

	/**
	 * Parses a String into an array containing the mod id, item's registry name, optional damage value, and rupee value.
	 * @param string Expected format is "modid:item_registry_name=value" or "modid:item_registry_name@damage=value"
	 * @return NULL if format was not valid
	 */
	public static String[] parseString(String string) {
		Matcher matcher = PARSER.matcher(string);
		if (matcher.matches()) {
			List<String> result = new ArrayList<String>();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				result.add(matcher.group(i));
			}
			return result.toArray(new String[result.size()]);
		}
		ZSSMain.logger.error(String.format("[RupeeValueRegistry] String must be in the format 'modid:registered_item_name@damage=value', received: %s", string));
		return null;
	}
}

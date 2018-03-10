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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncRupeeMerchantPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.PlayerUtils;

public class RupeeMerchantHelper
{
	private static final Logger LOGGER = LogManager.getLogger("zss_rupee_trades");

	private static final String BASE_PATH = Config.config.getConfigFile().getParentFile().getAbsolutePath() + "/rupee_trades/";

	/** Default initial trade lists; may contain null values for invalid ResourceLocations - this prevents potential redundant file requests */
	private static final Map<ResourceLocation, Pair<RupeeTradeTemplateList, RupeeTradeTemplateList>> tradeLists = new HashMap<ResourceLocation, Pair<RupeeTradeTemplateList, RupeeTradeTemplateList>>();

	/** Default trade lists typically used for adding new trades; may contain null values for invalid ResourceLocations - this prevents potential redundant file requests */
	private static final Map<ResourceLocation, Pair<RupeeTradeTemplateList, RupeeTradeTemplateList>> randomTradeLists = new HashMap<ResourceLocation, Pair<RupeeTradeTemplateList, RupeeTradeTemplateList>>();

	private RupeeMerchantHelper() {}

	/**
	 * Fetches the object as an IRupeeMerchant if possible
	 * @param object Currently supports null and any IRupeeMerchant or EntityVillager
	 * @return the IRupeeMerchant for the object, or null
	 */
	public static IRupeeMerchant getRupeeMerchant(Object object) {
		if (object instanceof IRupeeMerchant) {
			return (IRupeeMerchant) object;
		} else if (object instanceof EntityVillager) {
			return ZSSVillagerInfo.get((EntityVillager) object).getRupeeMerchant();
		}
		return null;
	}

	/**
	 * Checks if the object can be converted to an IRupeeMerchant and if the merchant has at least one rupee trade
	 * @param object Passed to {@link #getRupeeMerchant(Object)} to retrieve corresponding IRupeeMerchant
	 * @param player If not null, trade lists will be retrieved using {@link IRupeeMerchant#getCustomizedRupeeTrades(EntityPlayer, boolean)}
	 * @return true if the object has any non-empty rupee trading list
	 */
	public static boolean hasRupeeTrades(Object object, EntityPlayer player) {
		RupeeTradeList<RupeeTrade> sell = RupeeMerchantHelper.getRupeeTrades(object, true, player);
		if (sell != null && !sell.isEmpty()) {
			return true;
		}
		RupeeTradeList<RupeeTrade> buys = RupeeMerchantHelper.getRupeeTrades(object, false, player);
		if (buys != null && !buys.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns {@link RupeeMerchantHelper#getRupeeTrades(Object, boolean, EntityPlayer)} using a null EntityPlayer
	 */
	public static RupeeTradeList<RupeeTrade> getRupeeTrades(Object object, boolean getItemsToSell) {
		return RupeeMerchantHelper.getRupeeTrades(object, getItemsToSell, null);
	}

	/**
	 * Fetches the desired rupee trade list if the object can be converted to a rupee merchant (see {@link #getRupeeMerchant(Object)})
	 * @param getItemsToSell whether to fetch the merchant's items to sell or items to buy; see {@link IRupeeMerchant#getRupeeTrades(boolean)}
	 * @param player If not null and the merchant doesn't currently have a customer, trade lists will be retrieved using {@link IRupeeMerchant#getCustomizedRupeeTrades(EntityPlayer, boolean)}  
	 * @return null if unable to get the RupeeTradeList
	 */
	public static RupeeTradeList<RupeeTrade> getRupeeTrades(Object object, boolean getItemsToSell, EntityPlayer player) {
		IRupeeMerchant merchant = RupeeMerchantHelper.getRupeeMerchant(object);
		if (merchant == null) {
			return null;
		} else if (player == null || merchant.getRupeeCustomer() != null) {
			return merchant.getRupeeTrades(getItemsToSell);
		}
		RupeeTradeList<RupeeTrade> trades = merchant.getCustomizedRupeeTrades(player, getItemsToSell);
		return trades;
	}

	/**
	 * Calls {@link RupeeMerchantHelper#addVillagerRupeeTrade(EntityPlayer, RupeeTrade, EntityVillager, EnumVillager, boolean, float)}
	 * with no profession or strict class requirements
	 */
	public static void addVillagerRupeeTrade(EntityPlayer player, RupeeTrade trade, EntityVillager villager, float chance) {
		RupeeMerchantHelper.addVillagerRupeeTrade(player, trade, villager, null, null, chance);
	}

	/**
	 * Attempts to add or update the RupeeTrade to the villager's list of items to buy, sending appropriate
	 * generic chat messages depending on the villager and whether the trade is added or not.
	 * If the villager has special profession or strict class requirements, check them beforehand.
	 * @param player
	 * @param trade See {@link RupeeTradeList#addOrUpdateTrade(RupeeTrade)}
	 * @param villager
	 * @param profession Required villager profession, or null if none required
	 * @param clazz If not null, the villager entity's class must match exactly (i.e. sub-classes do not match)
	 * @param chance A value between 0.0F and 1.0F, inclusive, where 1.0F means always add if possible
	 */
	public static void addVillagerRupeeTrade(EntityPlayer player, RupeeTrade trade, EntityVillager villager, EnumVillager profession, Class<? extends EntityVillager> clazz, float chance) {
		IRupeeMerchant merchant = RupeeMerchantHelper.getRupeeMerchant(villager);
		if (merchant == null) {
			return;
		}
		RupeeTradeList<RupeeTrade> trades = RupeeMerchantHelper.getRupeeTrades(villager, false, player);
		if (trades == null) {
			merchant.setRupeeTrades(new RupeeTradeList<RupeeTrade>(RupeeTradeList.WILL_BUY), false);
			trades = merchant.getRupeeTrades(false);
		}
		if (merchant.getRupeeCustomer() != null) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
		} else if (villager.isChild()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.child");
		} else if (clazz != null && villager.getClass() != clazz) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		} else if (profession != null && !profession.is(villager)) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		} else if (trades != null) {
			if (player.worldObj.rand.nextFloat() < chance && trades.addOrUpdateTrade(trade)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell." + player.worldObj.rand.nextInt(2));
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.1");
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		}
	}

	/**
	 * Refreshes a RupeeTradeList, usually after a customer has completed one or more trades.
	 * <br>Do not call this while the rupee merchant is still interacting with their customer. 
	 * <br>Respects all RupeeTrade methods e.g. {@link RupeeTrade#allowRefresh allowRefresh} and {@link RupeeTrade#removeWhenDisabled removeWhenDisabled}.
	 * @param trades The trade list to be refreshed
	 * @param rand Required to increase the trade's max uses by a random amount
	 */
	public static void refreshTradingList(RupeeTradeList<RupeeTrade> trades, Random rand) {
		for (Iterator<RupeeTrade> iterator = trades.iterator(); iterator.hasNext();) {
			RupeeTrade trade = iterator.next();
			if (trade.isDisabled()) {
				if (trade.allowRefresh()) {
					trade.increaseMaxUses(rand.nextInt(6) + rand.nextInt(6) + 2);
				} else if (trade.removeWhenDisabled()) {
					iterator.remove();
				}
			}
		}
		// TODO if IRupeeMerchant available, could check for additional trades
		//	- would also need ResourceLocation and getItemsToSell flag
	}

	/**
	 * Calls {@link #addRandomTrades(RupeeTradeList, RupeeTradeTemplateList, Random, int)} for the
	 * merchant using the random trade list found for the resource location, if any.
	 * @param merchant
	 * @param location Used to fetch random trade list from {@link #getRandomTradeList(ResourceLocation, boolean)}
	 * @param getItemsToSell Determines whether to use the buying or selling lists; see {@link IRupeeMerchant#getRupeeTrades(boolean)}
	 * @param rand
	 * @param n
	 */
	public static void addRandomTrades(IRupeeMerchant merchant, ResourceLocation location, boolean getItemsToSell, Random rand, int n) {
		RupeeTradeTemplateList randomTrades = RupeeMerchantHelper.getRandomTradeList(location, getItemsToSell);
		ZSSMain.logger.info(String.format("Random trade list for %s is %s", location.toString(), randomTrades));
		RupeeMerchantHelper.addRandomTrades(merchant.getRupeeTrades(getItemsToSell), randomTrades, rand, n);
	}

	/**
	 * Adds n number of random trades to the specified trade list, updating existing trades if possible.
	 * @param trades The list of trades to be modified
	 * @param randomTrades The trade list used to generate random trades
	 * @param rand
	 * @param n Maximum number of random trades to add
	 */
	public static void addRandomTrades(RupeeTradeList<RupeeTrade> trades, RupeeTradeTemplateList randomTrades, Random rand, int n) {
		if (trades == null || randomTrades == null || randomTrades.isEmpty()) {
			return;
		}
		RupeeTradeList<RupeeTrade> shuffled = randomTrades.getRandomizedTradeList(rand, RupeeTradeTemplateList.getDefaultModifier(trades));
		if (shuffled != null && !shuffled.isEmpty()) {
			for (int i = 0; i < n && i < shuffled.size(); i++) {
				trades.addOrUpdateTrade(shuffled.get(i));
			}
		}
	}

	/**
	 * Handles opening the rupee trading interface for the player; does not do anything on client side.
	 * <br>Specifically, it calls {@link IRupeeMerchant#setRupeeCustomer(EntityPlayer) setRupeeCustomer},
	 * {@link IRupeeMerchant#getRupeeTrades(boolean) getRupeeTrades} to ensure there is at least one trade,
	 * {@link IRupeeMerchant#openRupeeGui(EntityPlayer, boolean) openRupeeGui}, and then syncs the trade lists.
	 * @param getItemsToSell whether to fetch the merchant's items to sell or items to buy; see {@link IRupeeMerchant#getRupeeTrades(boolean)}
	 * @return true if the gui was opened 
	 */
	public static boolean openRupeeMerchantGui(IRupeeMerchant merchant, EntityPlayer player, boolean getItemsToSell) {
		if (player instanceof EntityPlayerMP) {
			// Trade lists should already have been populated by this point by e.g. IRupeeMerchant#onInteract
			RupeeTradeList<RupeeTrade> trades = merchant.getRupeeTrades(getItemsToSell);
			if (trades != null && !trades.isEmpty()) {
				merchant.setRupeeCustomer(player);
				merchant.openRupeeGui(player, getItemsToSell);
				PacketDispatcher.sendTo(new SyncRupeeMerchantPacket((EntityPlayerMP) player, merchant), (EntityPlayerMP) player);
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets the merchant's rupee trade lists to those found at the given ResourceLocation
	 */
	public static void setDefaultTrades(IRupeeMerchant merchant, ResourceLocation location, Random rand) {
		Pair<RupeeTradeTemplateList, RupeeTradeTemplateList> trades = null;
		if (tradeLists.containsKey(location)) {
			LOGGER.info("Fetching default trades from CACHE for " + location.toString());
			trades = tradeLists.get(location);
		} else {
			LOGGER.info("Fetching default trades from FILE for " + location.toString());
			trades = RupeeMerchantHelper.readRandomTradesFromFile(location);
			tradeLists.put(location, trades);
		}
		// #getRandomizedTradeList returns a new list so don't need to make a copy
		if (trades != null) {
			if (trades.getLeft() != null) {
				merchant.setRupeeTrades(trades.getLeft().getRandomizedTradeList(rand), false);
			}
			if (trades.getRight() != null) {
				merchant.setRupeeTrades(trades.getRight().getRandomizedTradeList(rand), true);
			}
		}
	}

	/**
	 * Returns the random trade list for the given ResourceLocation, if any
	 */
	public static RupeeTradeTemplateList getRandomTradeList(ResourceLocation location, boolean getItemsToSell) {
		Pair<RupeeTradeTemplateList, RupeeTradeTemplateList> trades = null;
		if (randomTradeLists.containsKey(location)) {
			LOGGER.info("Fetching default random trades from CACHE for " + location.toString());
			trades = randomTradeLists.get(location);
		} else {
			LOGGER.info("Fetching default random trades from FILE for " + location.toString());
			trades = RupeeMerchantHelper.readRandomTradesFromFile(location);
			randomTradeLists.put(location, trades);
		}
		// Return a copy so modifications don't affect the stored list
		if (trades != null) {
			RupeeTradeTemplateList list = (getItemsToSell ? trades.getLeft() : trades.getRight());
			return (list == null ? null : new RupeeTradeTemplateList(list));
		}
		return null;
	}

	private static Pair<RupeeTradeTemplateList, RupeeTradeTemplateList> readRandomTradesFromFile(ResourceLocation location) {
		String filename = RupeeMerchantHelper.getResourcePath(location);
		if (filename == null) {
			return null;
		}
		try {
			Path path = Paths.get(filename);
			JsonParser parser = new JsonParser();
			JsonElement json = parser.parse(new FileReader(path.toFile()));
			if (json.isJsonObject()) {
				JsonObject object = json.getAsJsonObject();
				Pair<RupeeTradeTemplateList, RupeeTradeTemplateList> trades = null;
				if (object.has("parent")) {
					ResourceLocation parent = new ResourceLocation(object.get("parent").getAsString());
					if (randomTradeLists.containsKey(parent)) {
						ZSSMain.logger.info("Parent found in stored random lists");
						trades = randomTradeLists.get(parent);
					} else {
						trades = RupeeMerchantHelper.readRandomTradesFromFile(parent);
						randomTradeLists.put(parent, trades);
						// TODO remove debugging
						if (trades != null) {
							ZSSMain.logger.info("Parent loaded from file: " + parent.toString());
						} else {
							ZSSMain.logger.error("Error loading parent: " + parent.toString());
						}
					}
				}
				RupeeTradeTemplateList buys = (object.has("buys") ? new RupeeTradeTemplateList(object.get("buys")) : null);
				RupeeTradeTemplateList sells = (object.has("sells") ? new RupeeTradeTemplateList(object.get("sells")) : null);
				if (trades != null) {
					// Make a copy to prevent the stored parent lists from being modified
					RupeeTradeTemplateList left = (trades.getLeft() == null ? null : new RupeeTradeTemplateList(trades.getLeft()));
					RupeeTradeTemplateList right = (trades.getRight() == null ? null : new RupeeTradeTemplateList(trades.getRight()));
					int n = (right == null ? 0 : right.size());
					if (buys != null) {
						if (left == null) {
							left = buys;
						} else {
							left.addAll(buys);
						}
					}
					if (sells != null) {
						if (right == null) {
							right = sells;
						} else {
							right.addAll(sells);
						}
					}
					trades = new ImmutablePair<RupeeTradeTemplateList, RupeeTradeTemplateList>(left, right);
					ZSSMain.logger.info(String.format("Added to existing random trade Pair; sales size before = %d, size after = %d", n, (right == null ? 0 : right.size())));
				} else {
					ZSSMain.logger.info("Creating new random trade Pair");
					trades = new ImmutablePair<RupeeTradeTemplateList, RupeeTradeTemplateList>(buys, sells);
				}
				return trades;
			} else {
				LOGGER.error(String.format("Invalid JSON format in file %s: expected JsonObject", filename));
			}
		} catch (InvalidPathException e) {
			LOGGER.error(String.format("Invalid resource path: %s", filename));
		} catch (FileNotFoundException e) {
			LOGGER.error(String.format("System could not find file %s", filename));
		} catch (Exception e) {
			LOGGER.error(String.format("Error reading from file %s: %s", filename, e.getMessage()));
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Calls {@link #writeTradesToFile(RupeeTradeList, RupeeTradeList, ResourceLocation, ResourceLocation) writeTradesToFile}
	 * with trade lists populated using {@link IRupeeMerchant#getRupeeTrades(boolean)} and a NULL parent location.
	 */
	public static void writeTradesToFile(IRupeeMerchant merchant, ResourceLocation location) {
		RupeeMerchantHelper.writeTradesToFile(merchant.getRupeeTrades(false), merchant.getRupeeTrades(true), location, null);
	}

	/**
	 * Calls {@link #writeTradesToFile(RupeeTradeList, RupeeTradeList, ResourceLocation, ResourceLocation) writeTradesToFile} with a NULL parent location.
	 */
	public static <T extends RupeeTradeList<? extends RupeeTrade>> void writeTradesToFile(T buys, T sells, ResourceLocation location) {
		RupeeMerchantHelper.writeTradesToFile(buys, sells, location, null);
	}

	/**
	 * Converts the rupee trade lists to JSON and writes them to the specified
	 * resource location in /assets/mod_id/rupee_trades/{location}.json
	 * @param location e.g. villager_blacksmith or /villager/blacksmith
	 * @param parent The trade list to inherit from, if any
	 */
	public static <T extends RupeeTradeList<? extends RupeeTrade>> void writeTradesToFile(T buys, T sells, ResourceLocation location, ResourceLocation parent) {
		JsonObject json = new JsonObject();
		if (parent != null) {
			json.addProperty("parent", parent.toString());
		}
		if (buys != null) {
			json.add("buys", buys.getSerializableElement());
		}
		if (sells != null) {
			json.add("sells", sells.getSerializableElement());
		}
		String filename = RupeeMerchantHelper.getResourcePath(location);
		if (filename != null) {
			try {
				Gson gson = new GsonBuilder().setPrettyPrinting().create();
				String data = gson.toJson(json);
				Path path = Paths.get(filename);
				File dir = (path.getParent() == null ? null : path.getParent().toFile());
				if (dir != null && !dir.exists()) {
					dir.mkdirs();
				}
				BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
				writer.write(data, 0, data.length());
				writer.close();
			} catch (InvalidPathException e) {
				LOGGER.error(String.format("Invalid resource path: %s", filename));
			} catch (Exception e) {
				LOGGER.error(String.format("Error writing to file %s", filename));
				e.printStackTrace();
			}
		} else {
			LOGGER.warn("Unable to determine file path from resource location: " + location.toString());
		}
	}

	private static String getResourcePath(ResourceLocation location) {
		String path = BASE_PATH + location.getResourceDomain() + "/" + StringUtils.strip(location.getResourcePath(), "\\/");
		return path.replace("\\", "/") + ".json";
	}
}

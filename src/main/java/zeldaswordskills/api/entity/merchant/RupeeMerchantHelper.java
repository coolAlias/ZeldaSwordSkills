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

	/** May contain null values for invalid ResourceLocations - this prevents potential redundant file requests */
	private static final Map<ResourceLocation, Pair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>>> tradeLists = new HashMap<ResourceLocation, Pair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>>>();

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
	public static void setDefaultTrades(IRupeeMerchant merchant, ResourceLocation location) {
		Pair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>> trades = null;
		if (tradeLists.containsKey(location)) {
			LOGGER.info("Fetching default trades from CACHE for " + location.toString());
			trades = tradeLists.get(location);
		} else {
			LOGGER.info("Fetching default trades from FILE for " + location.toString());
			trades = RupeeMerchantHelper.readTradesFromFile(location);
			tradeLists.put(location, trades);
		}
		// Make a copy to prevent the stored lists from being modified
		if (trades != null) {
			if (trades.getLeft() != null) {
				merchant.setRupeeTrades(new RupeeTradeList<RupeeTrade>(trades.getLeft()), false);
			}
			if (trades.getRight() != null) {
				merchant.setRupeeTrades(new RupeeTradeList<RupeeTrade>(trades.getRight()), true);
			}
		}
	}

	private static Pair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>> readTradesFromFile(ResourceLocation location) {
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
				Pair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>> trades = null;
				if (object.has("parent")) {
					ResourceLocation parent = new ResourceLocation(object.get("parent").getAsString());
					if (tradeLists.containsKey(parent)) {
						ZSSMain.logger.info("Parent found in stored lists");
						trades = tradeLists.get(parent);
					} else {
						trades = RupeeMerchantHelper.readTradesFromFile(parent);
						tradeLists.put(parent, trades);
						if (trades != null) {
							ZSSMain.logger.info("Parent loaded from file: " + parent.toString());
							tradeLists.put(parent, trades);
							// TODO this is throwing NPE since one or both trade lists may be null!!!
							//ZSSMain.logger.info(String.format("Parent list sizes: sales (%d) | shop (%d)", trades.getLeft().size(), trades.getRight().size()));
						} else {
							ZSSMain.logger.error("Error loading parent: " + parent.toString());
						}
					}
				}
				RupeeTradeList<RupeeTrade> buys = (object.has("buys") ? new RupeeTradeList<RupeeTrade>(object.get("buys")) : null);
				RupeeTradeList<RupeeTrade> sells = (object.has("sells") ? new RupeeTradeList<RupeeTrade>(object.get("sells")) : null);
				if (trades != null) {
					// Make a copy to prevent the stored parent lists from being modified
					RupeeTradeList<RupeeTrade> left = (trades.getLeft() == null ? null : new RupeeTradeList<RupeeTrade>(trades.getLeft()));
					RupeeTradeList<RupeeTrade> right = (trades.getRight() == null ? null : new RupeeTradeList<RupeeTrade>(trades.getRight()));
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
					trades = new ImmutablePair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>>(left, right);
					ZSSMain.logger.info(String.format("Added to existing trade Pair; sales size before = %d, size after = %d", n, (right == null ? 0 : right.size())));
				} else {
					ZSSMain.logger.info("Creating new trade Pair");
					trades = new ImmutablePair<RupeeTradeList<RupeeTrade>, RupeeTradeList<RupeeTrade>>(buys, sells);
				}
				return trades;
			} else {
				LOGGER.error(String.format("Invalid JSON format in file %s: expected JsonObject", filename));
			}
		} catch (InvalidPathException e) {
			LOGGER.error(String.format("Invalid resource path: %s", filename));
		}/* catch (NullPointerException e) {
			LOGGER.error(String.format("Could not read file %s: file does not exist", filename));
		}*/ catch (FileNotFoundException e) {
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

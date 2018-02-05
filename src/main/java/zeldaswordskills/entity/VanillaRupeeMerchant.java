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

package zeldaswordskills.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.Village;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.api.item.RupeeValueRegistry;
import zeldaswordskills.entity.mobs.EntityChu.ChuType;
import zeldaswordskills.entity.player.quests.QuestWhipTrader;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemChuJelly;
import zeldaswordskills.item.ItemZeldaPotion;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * An instance of this class should be added to each vanilla villager's additional data
 * to allow them to use the rupee trading interface.
 * 
 * Villagers use the trade lists located at /rupee_trades/zeldaswordskills/villager_{profession}
 * as their default trades.
 *
 */
public class VanillaRupeeMerchant implements IRupeeMerchant
{
	/**
	 * Returns the default rupee trade list location for the villager based on profession
	 * @param villager Non-vanilla villager professions are supported as "minecraft:villager/profession_{n}",
	 * 		  where n is the value returned from {@link EntityVillager#getProfession()}
	 */
	public static ResourceLocation getDefaultTradeLocation(EntityVillager villager) {
		EnumVillager profession = EnumVillager.get(villager);
		if (profession == null) {
			new ResourceLocation("minecraft", "villager/profession_" + villager.getProfession());
		}
		return new ResourceLocation("minecraft", "villager/" + profession.unlocalizedName);
	}

	/**
	 * Returns the default rupee trade list location for the villager type (used when generating default trade lists)
	 */
	public static ResourceLocation getDefaultTradeLocation(EnumVillager villager) {
		return (villager == null ? null : new ResourceLocation("minecraft", "villager/" + villager.unlocalizedName));
	}

	/** The vanilla villager that is also a rupee trader */
	protected final EntityVillager villager;

	/** Uses the villager's World's random, for ease of use */
	protected final Random rand;

	/** List of RupeeTrades this merchant is willing to buy */
	protected RupeeTradeList<RupeeTrade> waresToBuy;

	/** List of RupeeTrades this merchant has for sale */
	protected RupeeTradeList<RupeeTrade> waresToSell;

	/** The merchant's current rupee customer */
	protected EntityPlayer rupeeCustomer;

	/** The merchant's last customer to use a trade, used for adding to that player's reputation */
	protected String lastCustomer;

	/** Set to true when trades will gain more uses, e.g. when a trade is used for the first time */
	protected boolean refreshTrades;

	/** Time remaining until trades are refreshed */
	protected int refreshTimer;

	public VanillaRupeeMerchant(EntityVillager villager) {
		this.villager = villager;
		this.rand = villager.worldObj.rand;
	}

	/**
	 * Returns the entity villager merchant
	 */
	public EntityVillager getVillager() {
		return this.villager;
	}

	@Override
	public EntityPlayer getRupeeCustomer() {
		return this.rupeeCustomer;
	}

	@Override
	public void setRupeeCustomer(EntityPlayer player) {
		this.rupeeCustomer = player;
	}

	@Override
	public void openRupeeGui(EntityPlayer player, boolean getItemsToSell) {
		int gui_id = (getItemsToSell ? GuiHandler.GUI_RUPEE_SHOP : GuiHandler.GUI_RUPEE_SALES);
		player.openGui(ZSSMain.instance, gui_id, player.worldObj, this.villager.getEntityId(), 0, 0);
	}

	/**
	 * @return True if the merchant has at least one rupee trade to either buy or sell
	 */
	public boolean hasRupeeTrades() {
		return (this.waresToSell != null && !this.waresToSell.isEmpty()) || (this.waresToBuy != null && !this.waresToBuy.isEmpty());
	}

	@Override
	public RupeeTradeList<RupeeTrade> getRupeeTrades(boolean getItemsToSell) {
		return (getItemsToSell ? this.waresToSell : this.waresToBuy);
	}

	@Override
	public RupeeTradeList<RupeeTrade> getCustomizedRupeeTrades(EntityPlayer player, boolean getItemsToSell) {
		if (!this.villager.worldObj.isRemote && !this.hasRupeeTrades()) {
			this.populateRupeeTradeLists(player);
		}
		this.customizeRupeeTradesForPlayer(player);
		return this.getRupeeTrades(getItemsToSell);
	}

	@Override
	public void setRupeeTrades(RupeeTradeList<RupeeTrade> trades, boolean getItemsToSell) {
		if (getItemsToSell) {
			this.waresToSell = trades;
		} else {
			this.waresToBuy = trades;
		}
	}

	@Override
	public void useRupeeTrade(RupeeTrade trade, boolean getItemsToSell) {
		trade.incrementTimesUsed();
		this.villager.livingSoundTime = -this.villager.getTalkInterval();
		this.villager.playSound("mob.villager.yes", 1.0F, this.getSoundPitch());
		// Purchasing expensive items or using the last of a trade triggers a refresh
		if (trade.isDisabled() || (getItemsToSell && trade.getPrice() > 49) || this.incrementChuTrades(trade)) {
			this.refreshTimer = 40;
			this.refreshTrades = true;
			this.lastCustomer = (this.getRupeeCustomer() == null ? null : this.getRupeeCustomer().getCommandSenderName());
		}
	}

	/**
	 * Increments corresponding Jelly Potion trade's max uses while container still open
	 * @param trade Trade to check; nothing will be done if it's not a Chu Jelly trade
	 * @return true if max uses was incremented
	 */
	private boolean incrementChuTrades(RupeeTrade trade) {
		if (trade.getTimesUsed() % 15 == 0 && this.isChuTrader()) {
			RupeeTrade potionTrade = this.getChuPotionTrade(trade);
			if (potionTrade != null) {
				RupeeTrade match = this.waresToSell.getTrade(potionTrade, RupeeTrade.SIMPLE);
				if (match != null) {
					match.increaseMaxUses(1);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the corresponding potion RupeeTrade for any chu jelly trade 
	 * @param trade any trade
	 * @return null if the trade parameter did not contain a chu jelly, or the RupeeTrade for the potion
	 */
	private RupeeTrade getChuPotionTrade(RupeeTrade trade) {
		ItemStack stack = trade.getTradeItem();
		if (stack.getItem() == ZSSItems.jellyChu) {
			ChuType type = ((ItemChuJelly) stack.getItem()).getType(stack);
			Item potion = ItemChuJelly.POTION_MAP.get(type);
			if (potion instanceof ItemZeldaPotion) {
				ItemStack potionStack = new ItemStack(potion);
				int price = RupeeValueRegistry.getRupeeValue(potionStack, ((ItemZeldaPotion) potion).getDefaultRupeeValue(potionStack));
				return new RupeeTrade(potionStack, price, 1, true, false);
			}
			ZSSMain.logger.error("Unrecognized chu jelly type " + type.name() + " from stack " + stack);
		}
		return null;
	}

	/** Returns whether this villager deals at all in Chu Jellies */
	public boolean isChuTrader() {
		if (this.villager.isChild() || !EnumVillager.LIBRARIAN.is(this.villager)) {
			return false;
		}
		return this.villager.hasCustomNameTag() && this.villager.getCustomNameTag().contains("Doc");
	}

	/**
	 * Copied from EntityLiving since it is not accessible in this context
	 */
	protected float getSoundPitch() {
		return this.villager.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.5F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F;
	}

	@Override
	public Result onInteract(EntityPlayer player) {
		// IMerchant interface should open unless the player is sneaking
		if (this.villager.isChild() || !player.isSneaking()) {
			return Result.DENY;
		} else if (this.villager.getCustomer() != null) {
			if (!player.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
			}
			return Result.DENY;
		}
		if (!this.hasRupeeTrades()) {
			if (!this.villager.worldObj.isRemote) {
				this.populateRupeeTradeLists(player);
			}
		}
		if (this.getRupeeCustomer() == null && !this.villager.worldObj.isRemote) {
			this.customizeRupeeTradesForPlayer(player);
		}
		return Result.DEFAULT;
	}

	@Override
	public boolean wasInteractionHandled(Result result) {
		return result == Result.ALLOW;
	}

	/**
	 * Called from {@link IRupeeMerchant#onInteract(EntityPlayer) onInteract} when either of the
	 * merchant's rupee trading lists is null or empty.
	 */
	protected void populateRupeeTradeLists(EntityPlayer player) {
		ResourceLocation location = VanillaRupeeMerchant.getDefaultTradeLocation(this.villager);
		if (location != null) {
			ZSSMain.logger.info("Attempting to populate villager rupee trades: " + location.toString());
			RupeeMerchantHelper.setDefaultTrades(this, location);
		}
	}

	/**
	 * Called from {@link IRupeeMerchant#onInteract(EntityPlayer) onInteract} to modify the trade lists based on the current player
	 */
	protected void customizeRupeeTradesForPlayer(EntityPlayer player) {
		boolean flag; // flag for calls to #addOrRemoveTrade
		// Customize items the merchant will purchase
		if (this.waresToBuy == null) {
			this.waresToBuy = new RupeeTradeList<RupeeTrade>(RupeeTradeList.WILL_BUY);
		}
		// Customize items the merchant will sell
		if (this.waresToSell == null) {
			this.waresToSell = new RupeeTradeList<RupeeTrade>(RupeeTradeList.FOR_SALE);
		}
		if (EnumVillager.BUTCHER.is(this.villager)) {
			flag = ZSSQuests.get(player).hasCompleted(QuestWhipTrader.class);
			this.waresToSell.addOrRemoveTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.whip, 1, WhipType.WHIP_SHORT.ordinal()), -1), flag);
		}
		if (EnumVillager.PRIEST.is(this.villager)) {
			// Add or remove magic arrow trades based on config settings and player achievements
			int level = 0;
			if (Config.areArrowTradesEnabled()) {
				if (PlayerUtils.hasAchievement(player, ZSSAchievements.fairyBowMax)) {
					level = 3;
				} else {
					level = (PlayerUtils.hasAchievement(player, ZSSAchievements.fairyBow) ? 2 : 1);
				}
			}
			this.waresToSell.addOrRemoveTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.arrowFire), -1), RupeeTrade.SIMPLE, level > 1);
			this.waresToSell.addOrRemoveTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.arrowIce), -1), RupeeTrade.SIMPLE, level > 1);
			this.waresToSell.addOrRemoveTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.arrowSilver), -1), RupeeTrade.SIMPLE, level > 2);
			this.waresToSell.addOrRemoveTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.arrowLight), -1), RupeeTrade.SIMPLE, level > 2);
		}
	}

	/**
	 * Call during each of the villager's update ticks to check if trades need to be refreshed
	 */
	public void onUpdate() {
		if (this.refreshTimer > 0 && this.getRupeeCustomer() == null) {
			--this.refreshTimer;
			if (this.refreshTimer < 1) {
				if (this.refreshTrades) {
					this.refreshTradingList();
					this.refreshTrades = false;
					Village village = DirtyEntityAccessor.getVillageObject(this.villager);
					if (village != null && this.lastCustomer != null) {
						this.villager.worldObj.setEntityState(this.villager, (byte) 14);
						village.setReputationForPlayer(this.lastCustomer, 1);
					}
				}
				this.villager.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
			}
		}
	}

	/**
	 * Called each time the trading list is refreshed. The default implementation
	 * increases the max trade uses of any disabled recipes, just as vanilla does.
	 */
	protected void refreshTradingList() {
		if (this.waresToBuy != null) {
			RupeeMerchantHelper.refreshTradingList(this.waresToBuy, this.rand);
		}
		if (this.waresToSell != null) {
			RupeeMerchantHelper.refreshTradingList(this.waresToSell, this.rand);
		}
		if (this.isChuTrader()) {
			this.refreshChuTrades();
		}
	}

	/**
	 * Adds new chu potion trades
	 */
	private void refreshChuTrades() {
		List<RupeeTrade> jellies = Lists.<RupeeTrade>newArrayList();
		if (this.waresToBuy != null) {
			for (Iterator<RupeeTrade> iterator = this.waresToBuy.iterator(); iterator.hasNext();) {
				RupeeTrade trade = iterator.next();
				if (trade.getTimesUsed() < 15) {
					continue;
				}
				RupeeTrade potionTrade = this.getChuPotionTrade(trade);
				if (potionTrade != null) {
					RupeeTrade match = this.waresToSell.getTrade(potionTrade, RupeeTrade.SIMPLE);
					if (match == null) {
						int n = (trade.getTimesUsed() / 15) - 1;
						ZSSMain.logger.info(String.format("Adding %d uses for trade used %d times"), n, trade.getTimesUsed());
						potionTrade.increaseMaxUses(n);
						jellies.add(potionTrade);
					}
				}
			}
		}
		if (!jellies.isEmpty()) {
			if (this.waresToSell == null) {
				this.waresToSell = new RupeeTradeList(RupeeTradeList.FOR_SALE);
			}
			this.waresToSell.addAll(jellies);
		}
	}

	/**
	 * Call this manually to write any rupee trades this villager has to the tag compound
	 */
	public void writeToNBT(NBTTagCompound compound) {
		if (this.waresToBuy != null) {
			compound.setTag(RupeeTradeList.WILL_BUY, this.waresToBuy.writeToNBT());
		}
		if (this.waresToSell != null) {
			compound.setTag(RupeeTradeList.FOR_SALE, this.waresToSell.writeToNBT());
		}
	}

	/**
	 * Call this manually to read any rupee trades this villager has from the tag compound
	 */
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey(RupeeTradeList.WILL_BUY, Constants.NBT.TAG_COMPOUND)) {
			this.waresToBuy = new RupeeTradeList<RupeeTrade>(compound.getCompoundTag(RupeeTradeList.WILL_BUY));
		}
		if (compound.hasKey(RupeeTradeList.FOR_SALE, Constants.NBT.TAG_COMPOUND)) {
			this.waresToSell = new RupeeTradeList<RupeeTrade>(compound.getCompoundTag(RupeeTradeList.FOR_SALE));
		}
	}
}

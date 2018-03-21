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

package zeldaswordskills.entity.npc;

import java.util.Iterator;
import java.util.ListIterator;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.INpcVillager;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.api.item.RupeeValueRegistry;
import zeldaswordskills.entity.player.quests.IQuest;
import zeldaswordskills.entity.player.quests.QuestBombBagTrade;
import zeldaswordskills.entity.player.quests.QuestBombTrades;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Barnes sells regular bombs by default unless otherwise configured.
 * 
 * Water and Fire bombs can be achieved by bringing him an appropriate item, in no particular order.
 * 
 * Bomb backs can be unlocked by buying the single version of the bomb X number of times.
 * 
 * A single bomb bag trade can be unlocked by talking to him with a live bomb in your hand.
 * 
 * An unlimited bomb bag trade can be unlocked after unlocking all bomb and bomb pack trades.
 * 
 * All unlockable trades are on a per player basis and can be disabled via the config.
 *
 */
public class EntityNpcBarnes extends EntityNpcMerchantBase implements INpcVillager, IRupeeMerchant
{
	public static final ResourceLocation DEFAULT_RUPEE_TRADES = new ResourceLocation(ModInfo.ID, "npc/barnes");

	// Remove merchant recipes after giving some time to convert old NPC's trades
	private static final RupeeTrade BOMB_STANDARD = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()), 8);
	private static final RupeeTrade BOMB_STANDARD_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_STANDARD.ordinal()), 20);
	private static final RupeeTrade BOMB_WATER = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 12);
	private static final RupeeTrade BOMB_WATER_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_WATER.ordinal()), 30);
	private static final RupeeTrade BOMB_FIRE = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()), 16);
	private static final RupeeTrade BOMB_FIRE_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_FIRE.ordinal()), 40);
	private static final RupeeTrade BOMB_SEEDS = new RupeeTrade(new ItemStack(ZSSItems.bombFlowerSeed), 4);

	/** List of RupeeTrades this merchant is willing to buy */
	protected RupeeTradeList<RupeeTrade> waresToBuy;

	/** List of RupeeTrades this merchant has for sale */
	protected RupeeTradeList<RupeeTrade> waresToSell;

	/** The rupee merchant's current customer */
	protected EntityPlayer rupeeCustomer;

	/** The rupee merchant's last customer, used for adding to that player's reputation */
	protected String lastRupeeCustomer;

	/** True when the merchant has added some new wares */
	protected boolean hasNewWares;

	public EntityNpcBarnes(World world) {
		super(world);
	}

	@Override
	protected String getNameTagOnSpawn() {
		return "Barnes";
	}

	@Override
	protected String getLivingSound() {
		return Sounds.VILLAGER_HAGGLE;
	}

	@Override
	protected String getHurtSound() {
		return Sounds.VILLAGER_HIT;
	}

	@Override
	protected String getDeathSound() {
		return Sounds.VILLAGER_DEATH;
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
		player.openGui(ZSSMain.instance, gui_id, player.worldObj, this.getEntityId(), 0, 0);
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
		if (!this.worldObj.isRemote && !this.hasRupeeTrades()) {
			this.populateRupeeTradeLists(player);
		}
		this.customizeRupeeTradesForPlayer(player);
		return this.getRupeeTrades(getItemsToSell);
	}

	/**
	 * Called from {@link IRupeeMerchant#onInteract(EntityPlayer) onInteract} to modify the trade lists based on the current player
	 */
	protected void customizeRupeeTradesForPlayer(EntityPlayer player) {
		// Customize items the merchant will purchase
		if (this.waresToBuy == null) {
			this.waresToBuy = new RupeeTradeList<RupeeTrade>(RupeeTradeList.WILL_BUY);
		}
		// Customize items the merchant will sell
		if (this.waresToSell == null) {
			this.waresToSell = new RupeeTradeList<RupeeTrade>(RupeeTradeList.FOR_SALE);
		}
		// Customize items the merchant will sell if bomb trading quest is enabled
		if (Config.enableBarnesTradeSequence()) {
			// Barnes always sells standard bombs when trade sequence is enabled
			this.waresToSell.addOrUpdateTrade(RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()), -1));
			QuestBombTrades quest = (QuestBombTrades) ZSSQuests.get(player).add(new QuestBombTrades());
			for (BombType type : BombType.values()) {
				ItemStack stack = new ItemStack(ZSSItems.bomb, 1, type.ordinal());
				RupeeTrade trade = RupeeValueRegistry.getRupeeTrade(stack, -1);
				trade.setTimesUsed(quest.getTradeUses(type));
				this.waresToSell.addOrRemoveTrade(trade, RupeeTrade.DEFAULT_QTY, quest.isTradeUnlocked(type, false));
				// Pack of 5 bombs for the price of 4
				stack.stackSize = 5;
				trade = new RupeeTrade(stack, trade.getPrice() * 4);
				this.waresToSell.addOrRemoveTrade(trade, RupeeTrade.DEFAULT_QTY, quest.isTradeUnlocked(type, true));
			}
			// Add or remove single-use bomb bag trade
			this.addOrRemoveBombBagTrade(player);
			// Add or remove unlimited use bomb bag trade
			RupeeTrade trade = RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.bombBag), -1);
			// TODO && Config.enableBombBagTrade()
			this.waresToSell.addOrRemoveTrade(trade, RupeeTrade.DEFAULT_MAX_USES, quest.isComplete(player));
		}
	}

	private void addOrRemoveBombBagTrade(EntityPlayer player) {
		IQuest quest = ZSSQuests.get(player).get(QuestBombBagTrade.class);
		boolean flag = (quest != null && !quest.isComplete(player));
		ItemStack stack = new ItemStack(ZSSItems.bombBag);
		RupeeTrade trade = new RupeeTrade(stack, RupeeValueRegistry.getRupeeValue(stack, -1), 1, true, false);
		this.waresToSell.addOrRemoveTrade(trade, RupeeTrade.DEFAULT_MAX_USES, flag);
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
		this.livingSoundTime = -this.getTalkInterval();
		this.playSound("mob.villager.yes", this.getSoundVolume(), this.getSoundPitch());
		// Refresh every 5 trade uses or when a trade runs out of stock
		if (trade.isDisabled() || trade.getTimesUsed() % 5 == 0) {
			EntityPlayer customer = this.getRupeeCustomer();
			if (trade.getTradeItem().getItem() == ZSSItems.bombBag && customer != null) {
				IQuest quest = ZSSQuests.get(customer).get(QuestBombBagTrade.class);
				if (quest != null && !quest.isComplete(customer)) {
					quest.complete(customer);
				}
			}
			this.refreshTimer = 40;
			this.refreshTrades = true;
			this.lastRupeeCustomer = (customer == null ? null : customer.getCommandSenderName());
		}
	}

	@Override
	public Result onInteract(EntityPlayer player) {
		// Check for quest updates and stop interaction if something changed
		IQuest quest = ZSSQuests.get(player).add(new QuestBombTrades());
		if (quest.update(player, this)) {
			return Result.DENY;
		}
		if (!this.worldObj.isRemote) {
			// Ensure trade list populated
			if (!this.hasRupeeTrades()) {
				this.populateRupeeTradeLists(player);
			}
			// Customize rupee trades for the potential customer if not already trading
			if (this.getRupeeCustomer() == null && this.getCustomer() == null) {
				// TODO no point doing this here since customization occurs when opening the GUI
				// this.getCustomizedRupeeTrades(player, true);
				//this.customizeRupeeTradesForPlayer(player);
			}
		}
		return Result.DEFAULT;
		/*
		ItemStack stack = player.getHeldItem();
		// TODO this greeting is now getting played twice...
		String chat = "chat.zss.npc.barnes.greeting";
		// Vanilla IMerchants require player to be holding a rupee to access the interface
		boolean openGui = true; // (stack != null && stack.getItem() == ZSSItems.rupee);
		// TODO change to updating quest status; use quests to determine what additional items Barnes has when trading
		if (this.hasNewWares) {
			chat = "chat.zss.npc.barnes.trade.new";
			this.hasNewWares = false;
		} else if (stack != null) {
			if (stack.getItem() == ZSSItems.bombFlowerSeed && this.waresToBuy != null) {
				if (this.waresToBuy.addTrade(BOMB_SEEDS)) {
					chat = "chat.zss.npc.barnes.trade.bombseeds.new";
				} else {
					chat = "chat.zss.npc.barnes.trade.bombseeds.old";
				}
			} else if (this.waresToSell == null) {
				// null trade list, nothing to do
			} else if (stack.getItem() == Items.fish) {
				RupeeTrade trade = RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 12);
				if (trade != null && this.waresToSell.addTrade(trade)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.water";
				}
			} else if (stack.getItem() == Items.magma_cream) {
				RupeeTrade trade = RupeeValueRegistry.getRupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()), 16);
				if (trade != null && this.waresToSell.addTrade(trade)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.fire";
				}
			} else if (!this.waresToSell.containsTrade(BOMB_WATER)) {
				chat = "chat.zss.npc.barnes.material.water";
			} else if (!this.waresToSell.containsTrade(BOMB_FIRE)) {
				chat = "chat.zss.npc.barnes.material.fire";
			}
		}
		if (!this.worldObj.isRemote) {
			PlayerUtils.sendTranslatedChat(player, chat);
			if (openGui) {
				return Result.ALLOW;
			}
		}
		return Result.DENY;
		 */
	}

	@Override
	public boolean wasInteractionHandled(EntityPlayer player, Result result) {
		if (result == Result.ALLOW) {
			if (!this.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.greeting");
			}
			return true;
		}
		return false;
	}

	/**
	 * Called from {@link IRupeeMerchant#onInteract(EntityPlayer) onInteract} when either of the
	 * merchant's rupee trading lists is null or empty.
	 */
	protected void populateRupeeTradeLists(EntityPlayer player) {
		RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES, this.rand);
		this.removeOldTrades();
	}

	/**
	 * Temporary method to remove old MerchantRecipe trades that have been replaced by per-player rupee trades
	 */
	private void removeOldTrades() {
		if (this.trades == null || this.waresToSell == null) {
			return;
		}
		ItemStack emerald = new ItemStack(Items.emerald, 8);
		for (BombType bomb : BombType.values()) {
			MerchantRecipe trade = new MerchantRecipe(emerald, new ItemStack(ZSSItems.bomb, 1, bomb.ordinal()));
			MerchantRecipeHelper.removeTrade(this.trades, trade, false, true);
		}
		MerchantRecipeHelper.removeTrade(this.trades, new MerchantRecipe(emerald, new ItemStack(ZSSItems.bombBag)), false, true);
	}

	@Override
	protected void populateTradingList() {
		// no default merchant trades
	}

	@Override
	protected void refreshTradingList() {
		super.refreshTradingList();
		if (this.waresToBuy != null) {
			this.refreshTradingList(this.waresToBuy);
		}
		if (this.waresToSell != null) {
			this.refreshTradingList(this.waresToSell);
		}
	}

	private void refreshTradingList(RupeeTradeList<RupeeTrade> trades) {
		for (Iterator<RupeeTrade> iterator = trades.iterator(); iterator.hasNext();) {
			RupeeTrade trade = iterator.next();
			if (trade.isDisabled()) {
				if (trade.allowRefresh()) {
					trade.increaseMaxUses(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
				} else if (trade.removeWhenDisabled()) {
					iterator.remove();
				}
			}
		}
	}

	@Override
	protected void updateTradingList() {
		if (this.waresToSell == null) {
			return;
		}
		// TODO move to #useRupeeTrade and complete quest stages for current player there

		// Add bomb pack trades after enough of the singles are purchased
		for (ListIterator<RupeeTrade> iterator = this.waresToSell.listIterator(); iterator.hasNext();) {
			RupeeTrade trade = iterator.next();
			// Predicate<RupeeTrade> predicate = new RupeeTradeStackPredicate(trade, true, false, true);
			// TODO config for # times used before pack added?
			if (trade.getTimesUsed() < 3) {
				// need to use more to add bomb pack
			} else if (trade.matches(BOMB_STANDARD, RupeeTrade.DEFAULT_QTY)) {
				ZSSMain.logger.info("Standard bomb trade used more than 3 times");
				// TODO retrieve current pricing from registry? or use price of existing trade?
				ItemStack stack = new ItemStack(ZSSItems.bomb, 5, BombType.BOMB_STANDARD.ordinal());
				RupeeTrade newTrade = new RupeeTrade(stack, trade.getPrice() * 4);
				if (!this.waresToSell.containsTrade(newTrade, RupeeTrade.DEFAULT_QTY)) {
					ZSSMain.logger.info("Added bomb pack trade");
					iterator.add(newTrade);
					this.hasNewWares = true;
				} else {
					ZSSMain.logger.info("Already had bomb pack trade");
				}
			} else if (trade.matches(BOMB_WATER, RupeeTrade.DEFAULT_QTY)) {
				ZSSMain.logger.info("Water bomb trade used more than 3 times");
				if (!this.waresToSell.containsTrade(BOMB_WATER_PACK, RupeeTrade.DEFAULT_QTY)) {
					ZSSMain.logger.info("Added bomb pack trade");
					iterator.add(BOMB_WATER_PACK.copy());
					this.hasNewWares = true;
				} else {
					ZSSMain.logger.info("Already had bomb pack trade");
				}
			} else if (trade.matches(BOMB_FIRE, RupeeTrade.DEFAULT_QTY)) {
				ZSSMain.logger.info("Fire bomb trade used more than 3 times");
				if (!this.waresToSell.containsTrade(BOMB_FIRE_PACK, RupeeTrade.DEFAULT_QTY)) {
					ZSSMain.logger.info("Added bomb pack trade");
					iterator.add(BOMB_FIRE_PACK.copy());
					this.hasNewWares = true;
				} else {
					ZSSMain.logger.info("Already had bomb pack trade");
				}
			}
		}
	}

	@Override
	public boolean interact(EntityPlayer player) {
		// TODO this is displaying on client side when opening rupee trading interface directly
		// ZSSMain.logger.info("Interacting with Barnes as a vanilla IMerchant");
		if (!this.isEntityAlive()) {
			return false;
		} else if (this.getCustomer() != null) {
			if (!this.worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
			}
		} else if (!this.worldObj.isRemote) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.greeting");
			this.displayTradingGuiFor(player);
		}
		return true;
	}

	@Override
	public Result canInteractConvert(EntityPlayer player, EntityVillager villager) {
		if (player.worldObj.isRemote || villager.getClass() != EntityVillager.class || villager.isChild()) {
			return Result.DEFAULT;
		} else if (PlayerUtils.consumeHeldItem(player, Items.gunpowder, 1)) {
			return Result.ALLOW;
		}
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.hmph");
		return Result.DENY;
	}

	@Override
	public Result canLeftClickConvert(EntityPlayer player, EntityVillager villager) {
		return Result.DEFAULT;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		// this.setDead();
	}

	@Override
	public void onConverted(EntityPlayer player) {
		this.populateTradingList();
		RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES, this.rand);
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.barnes.open");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (this.waresToBuy != null) {
			compound.setTag(RupeeTradeList.WILL_BUY, this.waresToBuy.writeToNBT());
		}
		if (this.waresToSell != null) {
			compound.setTag(RupeeTradeList.FOR_SALE, this.waresToSell.writeToNBT());
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		if (compound.hasKey(RupeeTradeList.WILL_BUY, Constants.NBT.TAG_COMPOUND)) {
			this.waresToBuy = new RupeeTradeList<RupeeTrade>(compound.getCompoundTag(RupeeTradeList.WILL_BUY));
		}
		if (compound.hasKey(RupeeTradeList.FOR_SALE, Constants.NBT.TAG_COMPOUND)) {
			this.waresToSell = new RupeeTradeList<RupeeTrade>(compound.getCompoundTag(RupeeTradeList.FOR_SALE));
		}
	}
}

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
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

public class EntityNpcBarnes extends EntityNpcMerchantBase implements INpcVillager, IRupeeMerchant
{
	public static final ResourceLocation DEFAULT_RUPEE_TRADES = new ResourceLocation(ModInfo.ID, "npc/barnes");

	// Remove merchant recipes after giving some time to convert old NPC's trades
	private static final MerchantRecipe standardBomb = new MerchantRecipe(new ItemStack(Items.emerald, 8), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()));
	private static final MerchantRecipe waterBomb = new MerchantRecipe(new ItemStack(Items.emerald, 12), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()));
	private static final MerchantRecipe fireBomb = new MerchantRecipe(new ItemStack(Items.emerald, 16), new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()));
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
		return this.customer;
	}

	@Override
	public void setRupeeCustomer(EntityPlayer player) {
		this.customer = player;
	}

	@Override
	public void openRupeeGui(EntityPlayer player, boolean getItemsToSell) {
		int gui_id = (getItemsToSell ? GuiHandler.GUI_RUPEE_SHOP : GuiHandler.GUI_RUPEE_SALES);
		player.openGui(ZSSMain.instance, gui_id, player.worldObj, this.getEntityId(), 0, 0);
	}

	@Override
	public RupeeTradeList<RupeeTrade> getRupeeTrades(boolean getItemsToSell) {
		return (getItemsToSell ? this.waresToSell : this.waresToBuy);
	}

	@Override
	public RupeeTradeList<RupeeTrade> getCustomizedRupeeTrades(EntityPlayer player, boolean getItemsToSell) {
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
		this.livingSoundTime = -this.getTalkInterval();
		this.playSound("mob.villager.yes", this.getSoundVolume(), this.getSoundPitch());
		// Refresh every 5 trade uses or when a trade runs out of stock
		if (trade.isDisabled() || trade.getTimesUsed() % 5 == 0) {
			this.refreshTimer = 40;
			this.refreshTrades = true;
			this.lastCustomer = (this.customer == null ? null : this.customer.getCommandSenderName());
		}
	}

	@Override
	public Result onInteract(EntityPlayer player) {
		if (!this.isEntityAlive() || !player.isSneaking()) {
			return Result.DENY;
		} else if (this.getRupeeCustomer() != null) {
			if (!worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.merchant.busy");
			}
			return Result.DENY;
		}
		if (this.waresToBuy == null || this.waresToBuy.isEmpty() || this.waresToSell == null || this.waresToSell.isEmpty()) {
			if (!this.worldObj.isRemote) {
				this.populateRupeeTradeLists(player);
			}
		}
		ItemStack stack = player.getHeldItem();
		// TODO this greeting is now getting played twice...
		String chat = "chat.zss.npc.barnes.greeting";
		// Vanilla IMerchants require player to be holding a rupee to access the interface
		boolean openGui = true; // (stack != null && stack.getItem() == ZSSItems.rupee);
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
		RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES, this.rand);
		this.transferTrades();
	}

	/**
	 * Temporary method to transfer MerchantRecipe trades over to RupeeTrades
	 */
	private void transferTrades() {
		if (this.trades == null || this.waresToSell == null) {
			return;
		}
		// Transfer old MerchantRecipes to RupeeTrades
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.standardBomb, false, true)) {
			ItemStack stack = new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal());
			Integer price = RupeeValueRegistry.getRupeeValue(stack);
			if (price != null) {
				this.waresToSell.add(new RupeeTrade(stack, price));
				stack.stackSize = 5;
				this.waresToSell.add(new RupeeTrade(stack, price * 4));
			}
		}
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.waterBomb, false, true)) {
			ItemStack stack = new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal());
			Integer price = RupeeValueRegistry.getRupeeValue(stack);
			if (price != null) {
				this.waresToSell.add(new RupeeTrade(stack, price));
				stack.stackSize = 5;
				this.waresToSell.add(new RupeeTrade(stack, price * 4));
			}
		}
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.fireBomb, false, true)) {
			ItemStack stack = new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal());
			Integer price = RupeeValueRegistry.getRupeeValue(stack);
			if (price != null) {
				this.waresToSell.add(new RupeeTrade(stack, price));
				stack.stackSize = 5;
				this.waresToSell.add(new RupeeTrade(stack, price * 4));
			}
		}
		if (Config.enableTradeBombBag()) {
			MerchantRecipe bag = new MerchantRecipe(new ItemStack(Items.emerald, Config.getBombBagPrice()), new ItemStack(ZSSItems.bombBag));
			if (MerchantRecipeHelper.removeTrade(this.trades, bag, false, true)) {
				this.addBombBagTrade();
			}
		}
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

	/**
	 * Returns true if a Bomb Bag trade was added (must be enabled in Config)
	 */
	public boolean addBombBagTrade() {
		if (Config.enableTradeBombBag() && this.waresToSell != null) {
			RupeeTrade bag = new RupeeTrade(new ItemStack(ZSSItems.bombBag), Config.getBombBagPrice(), 1);
			if (!this.waresToSell.containsTrade(bag, RupeeTrade.SIMPLE)) {
				this.waresToSell.add(0, bag);
				return true;
			}
		}
		return false;
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

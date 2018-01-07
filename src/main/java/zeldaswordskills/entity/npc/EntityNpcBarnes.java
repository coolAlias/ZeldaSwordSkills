/**
    Copyright (C) <2015> <coolAlias>

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
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.INpcVillager;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
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
	private static final MerchantRecipe bombSeeds = new MerchantRecipe(new ItemStack(ZSSItems.bombFlowerSeed), new ItemStack(Items.emerald, 4));
	private static final RupeeTrade BOMB_STANDARD = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()), 8);
	private static final RupeeTrade BOMB_STANDARD_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_STANDARD.ordinal()), 20);
	private static final RupeeTrade BOMB_WATER = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_WATER.ordinal()), 12);
	private static final RupeeTrade BOMB_WATER_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_WATER.ordinal()), 30);
	private static final RupeeTrade BOMB_FIRE = new RupeeTrade(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_FIRE.ordinal()), 16);
	private static final RupeeTrade BOMB_FIRE_PACK = new RupeeTrade(new ItemStack(ZSSItems.bomb, 3, BombType.BOMB_FIRE.ordinal()), 40);
	private static final RupeeTrade BOMB_SEEDS = new RupeeTrade(new ItemStack(ZSSItems.bombFlowerSeed), 4);

	private static final MerchantRecipe GUNPOWDER = new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(Items.gunpowder));

	/** List of RupeeTrades this merchant is willing to buy */
	protected RupeeTradeList waresToBuy;

	/** List of RupeeTrades this merchant has for sale */
	protected RupeeTradeList waresToSell;

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
	public RupeeTradeList getRupeeTrades(boolean getItemsToSell) {
		return (getItemsToSell ? this.waresToSell : this.waresToBuy);
	}

	@Override
	public RupeeTradeList getCustomizedRupeeTrades(EntityPlayer player, boolean getItemsToSell) {
		return this.getRupeeTrades(getItemsToSell);
	}

	@Override
	public void setRupeeTrades(RupeeTradeList trades, boolean getItemsToSell) {
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
		// TODO change back to 5 after testing
		if (trade.isDisabled() || trade.getTimesUsed() % 2 == 0) {
			this.refreshTimer = 40;
			this.refreshTrades = true;
			this.lastCustomer = (this.customer == null ? null : this.customer.getCommandSenderName());
		}
	}

	@Override
	public Result onInteract(EntityPlayer player) {
		if (!this.isEntityAlive() || player.isSneaking()) {
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
		boolean openGui = true;
		if (this.hasNewWares) {
			chat = "chat.zss.npc.barnes.trade.new";
			this.hasNewWares = false;
		} else if (stack != null) {
			if (stack.getItem() == ZSSItems.bombFlowerSeed && this.waresToBuy != null) {
				if (this.waresToBuy.addOrUpdateTrade(BOMB_SEEDS)) {
					chat = "chat.zss.npc.barnes.trade.bombseeds.new";
				} else {
					chat = "chat.zss.npc.barnes.trade.bombseeds.old";
				}
			} else if (this.waresToSell == null) {
				// null trade list, nothing to do
			} else if (stack.getItem() == Items.fish) {
				if (this.waresToSell.addOrUpdateTrade(BOMB_WATER)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.water";
					openGui = false;
				}
			} else if (stack.getItem() == Items.magma_cream) {
				if (this.waresToSell.addOrUpdateTrade(BOMB_FIRE)) {
					--stack.stackSize;
					chat = "chat.zss.npc.barnes.trade.fire";
					openGui = false;
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
		RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES);
		this.transferTrades();
		/*
		// TODO pull initial list from JSON @ "assets/rupee_trades/npc_barnes"
		if (getItemsToSell) {
			this.waresToSell = new RupeeTradeList(RupeeTradeList.FOR_SALE);
			this.waresToSell.add(BOMB_STANDARD);
			this.waresToSell.add(BOMB_STANDARD_PACK);
			this.wares.add(BOMB_WATER);
			this.wares.add(BOMB_FIRE);
			this.wares.add(new RupeeTrade(new ItemStack(Items.arrow, 10), 20));
			this.wares.add(new RupeeTrade(new ItemStack(ZSSItems.shieldDeku), 100));
			this.wares.add(new RupeeTrade(new ItemStack(ZSSItems.shieldHylian), 1000));
			this.addBombBagTrade();
			//this.transferTrades();
		} else {
			this.waresToBuy = new RupeeTradeList(RupeeTradeList.WILL_BUY);
			this.waresToBuy.add(BOMB_SEEDS);
			// testing item with subtypes:
			ItemStack stack = new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal());
			((ItemBomb) stack.getItem()).setDefaultNBT(stack);
			this.waresToBuy.add(new RupeeTrade(stack, 5));
			// testing item with subtypes using wildcard value:
			this.waresToBuy.add(new RupeeTrade(new ItemStack(ZSSItems.jellyChu, 3, OreDictionary.WILDCARD_VALUE), 10));
			// testing item without subtypes:
			this.waresToBuy.add(new RupeeTrade(new ItemStack(Items.stone_shovel), 8));
			// testing item without subtypes and using wildcard value:
			this.waresToBuy.add(new RupeeTrade(new ItemStack(Items.stone_axe, 1, OreDictionary.WILDCARD_VALUE), 5));
			// testing item with NBT data:
			this.waresToBuy.add(new RupeeTrade(Items.enchanted_book.getEnchantedItemStack(new EnchantmentData(Enchantment.fortune, 2)), 100));
		}
		//RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES);
		 */
	}

	/**
	 * Temporary method to transfer MerchantRecipe trades over to RupeeTrades
	 */
	private void transferTrades() {
		if (this.trades == null || this.waresToSell == null) {
			return;
		}
		// Transfer old MerchantRecipes to RupeeTrades
		// TODO it occurs to me that having a static RupeeTrade instance will cause them to
		//		globally increment the number of times used...
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.standardBomb, false, true)) {
			this.waresToSell.add(BOMB_STANDARD);
			this.waresToSell.add(BOMB_STANDARD_PACK);
		}
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.waterBomb, false, true)) {
			this.waresToSell.add(BOMB_WATER);
			this.waresToSell.add(BOMB_WATER_PACK);
		}
		if (MerchantRecipeHelper.removeTrade(this.trades, EntityNpcBarnes.fireBomb, false, true)) {
			this.waresToSell.add(BOMB_FIRE);
			this.waresToSell.add(BOMB_FIRE_PACK);
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
		MerchantRecipeList newTrades = new MerchantRecipeList();
		newTrades.add(GUNPOWDER);
		if (this.trades == null) {
			this.trades = newTrades;
		} else {
			// Add any previous trades after bomb-related trades
			for (int i = 0; i < this.trades.size(); ++i) {
				MerchantRecipeHelper.addToListWithCheck(newTrades, (MerchantRecipe) this.trades.get(i));
			}
			this.trades = newTrades;
		}
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

	private void refreshTradingList(RupeeTradeList trades) {
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
		// Barnes gives special deals on packs of bombs from time to time
		// Could be limited, adding single use trade (or +1 to existing trade) at a time
		// After a certain point, could add the unlimited version
		// Not very practical to update entries due to the nature of iteration, though...
		for (ListIterator<RupeeTrade> iterator = this.waresToSell.listIterator(); iterator.hasNext();) {
			RupeeTrade trade = iterator.next();
			if (trade.getTimesUsed() < 3) {
				// need to use more to add bomb pack
			} else if (RupeeTrade.DEFAULT.compare(trade, BOMB_STANDARD) == 0) {
				if (!this.waresToSell.containsTrade(BOMB_STANDARD_PACK)) {
					iterator.add(BOMB_STANDARD_PACK);
					this.hasNewWares = true;
				}
			} else if (RupeeTrade.DEFAULT.compare(trade, BOMB_WATER) == 0) {
				if (!this.waresToSell.containsTrade(BOMB_WATER_PACK)) {
					iterator.add(BOMB_WATER_PACK);
					this.hasNewWares = true;
				}
			} else if (RupeeTrade.DEFAULT.compare(trade, BOMB_FIRE) == 0) {
				System.out.println("Fire bomb trade used more than 3 times");
				if (!this.waresToSell.containsTrade(BOMB_FIRE_PACK)) {
					iterator.add(BOMB_FIRE_PACK);
					this.hasNewWares = true;
					System.out.println("Added fire bomb pack trade");
				} else {
					System.out.println("Already have fire bomb pack trade");
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
		RupeeMerchantHelper.setDefaultTrades(this, DEFAULT_RUPEE_TRADES);
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
			this.waresToBuy = new RupeeTradeList(compound.getCompoundTag(RupeeTradeList.WILL_BUY));
		}
		if (compound.hasKey(RupeeTradeList.FOR_SALE, Constants.NBT.TAG_COMPOUND)) {
			this.waresToSell = new RupeeTradeList(compound.getCompoundTag(RupeeTradeList.FOR_SALE));
			/*
			// TODO issue with this is that it will prevent special cases such as quest rewards
			//		from persisting, unless we re-add them each time the player interacts
			//		which is probably how it has to be done - see Biggoron's Sword for example
			if (!Config.enableTradeBombBag()) {
				RupeeTrade bag = new RupeeTrade(new ItemStack(ZSSItems.bombBag), 1, 1);
				this.waresToSell.removeTrade(bag, RupeeTrade.SIMPLE, true);
			}
			 */
		}
	}
}

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

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.handler.GuiHandler;

/**
 * 
 * Base class for non-villager NPCs that run a rupee-based shop.
 * 
 */
public abstract class EntityNpcRupeeMerchant extends EntityNpcBase implements IRupeeMerchant
{
	/** List of RupeeTrades this merchant is willing to buy */
	protected RupeeTradeList<RupeeTrade> waresToBuy;

	/** List of RupeeTrades this merchant has for sale */
	protected RupeeTradeList<RupeeTrade> waresToSell;

	/** The merchant's current customer */
	protected EntityPlayer customer;

	/** The merchant's last customer to use a trade, used for adding to that player's reputation */
	protected String lastCustomer;

	/** Set to true when trades will gain more uses, e.g. when a trade is used for the first time */
	protected boolean refreshTrades;

	/** Time remaining until trades are refreshed */
	protected int refreshTimer;

	public EntityNpcRupeeMerchant(World world) {
		super(world);
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
		// Purchasing expensive items or using the last of a trade triggers a refresh
		if (trade.isDisabled() || (getItemsToSell && trade.getPrice() > 49)) {
			this.refreshTimer = 40;
			this.refreshTrades = true;
			this.lastCustomer = (this.customer == null ? null : this.customer.getCommandSenderName());
		}
	}

	@Override
	public Result onInteract(EntityPlayer player) {
		if (this.isChild()) {
			return Result.DENY;
		}
		if (!this.worldObj.isRemote) {
			if (!this.hasRupeeTrades()) {
				this.populateRupeeTradeLists(player);
			}
			if (this.getRupeeCustomer() == null) {
				this.customizeRupeeTradesForPlayer(player);
			}
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
	protected abstract void populateRupeeTradeLists(EntityPlayer player);

	/**
	 * Called from {@link IRupeeMerchant#onInteract(EntityPlayer) onInteract} and 
	 * {@link IRupeeMerchant#getCustomizedRupeeTrades(EntityPlayer, boolean)} to
	 * modify the trade lists based on the current player.
	 */
	protected abstract void customizeRupeeTradesForPlayer(EntityPlayer player);

	/**
	 * Called after the trading list is refreshed to add new trades. Vanilla
	 * villagers would also call this while populating the list for the first time.
	 */
	protected abstract void updateTradingList();

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
	}

	/**
	 * Vanilla behavior:
	 * Called from InventoryMerchant#resetRecipeAndSlots when any of the input slots
	 * changes; if nothing is in either slot, the villager plays the "no" sound, or
	 * "yes" if there is a stack in one of them regardless of it it matches a trade.
	 * 
	 * TODO how should this work for rupee merchants?
	 * 		only input slot is when player is selling
	 * 
	 * @param stack
	 */
	// @Override
	public void func_110297_a_(ItemStack stack) {
		if (!this.worldObj.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
			this.livingSoundTime = -this.getTalkInterval();
			this.playSound((stack == null ? "mob.villager.no" : "mob.villager.yes"), this.getSoundVolume(), this.getSoundPitch());
		}
	}

	@Override
	protected void updateAITasks() {
		if (this.getRupeeCustomer() == null && this.refreshTimer > 0) {
			--this.refreshTimer;
			if (this.refreshTimer <= 0) {
				if (this.refreshTrades) {
					this.refreshTradingList();
					this.updateTradingList();
					this.refreshTrades = false;
					if (this.villageObj != null && this.lastCustomer != null) {
						this.worldObj.setEntityState(this, (byte) 14);
						this.villageObj.setReputationForPlayer(this.lastCustomer, 1);
					}
				}
				this.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
			}
		}
		super.updateAITasks();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		if (this.waresToBuy != null) {
			compound.setTag(RupeeTradeList.WILL_BUY, this.waresToSell.writeToNBT());
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

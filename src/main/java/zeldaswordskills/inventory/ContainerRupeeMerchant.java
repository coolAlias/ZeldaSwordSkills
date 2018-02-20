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

package zeldaswordskills.inventory;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackDamagePredicate;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackItemPredicate;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackMinQtyPredicate;
import zeldaswordskills.api.item.ItemStackPredicate.ItemStackNbtPredicate;
import zeldaswordskills.api.item.ItemStackPredicate.WildCardPredicate;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerWallet;

public abstract class ContainerRupeeMerchant extends AbstractContainer
{
	/** Player inventory starts after 1 purchase slot plus 4 slots for wares */
	public static final int WARES_START = 1;
	public static final int WARES_END = 4;
	protected static final int INV_START = 5;
	protected static final int INV_END = INV_START + 26;
	protected static final int HOTBAR_START = INV_END + 1;
	protected static final int HOTBAR_END = HOTBAR_START + 8;

	/** Whether to display the toggle shop mode button */
	public boolean showTab;

	protected final IRupeeMerchant merchant;

	/** Current shop mode - i.e. whether merchant is selling (player is buying) or buying (player is selling) items */
	protected final boolean getItemsToSell;

	protected final IInventory waresInv;

	/** Active slot that receives purchased items or that player puts items for sale in */
	protected final SlotRupeeMerchant tradeSlot;

	/** Index of rupee trade to display in the first slot position (for scrolling) */
	protected int currentIndex;

	/** Used when toggling between interfaces to avoid dropping items in {@link #onContainerClosed(EntityPlayer)} */
	public boolean toggling;

	public ContainerRupeeMerchant(EntityPlayer player, IRupeeMerchant merchant, boolean getItemsToSell) {
		this.merchant = merchant;
		this.getItemsToSell = getItemsToSell;
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(getItemsToSell);
		RupeeTradeList<RupeeTrade> other = this.merchant.getRupeeTrades(!getItemsToSell);
		this.showTab = (other != null && !other.isEmpty());
		this.tradeSlot = new SlotRupeeMerchant(new InventoryBasic("", true, 1), 0, 152, 62);
		if (!player.worldObj.isRemote) {
			ItemStack stack = ZSSPlayerInfo.get(player).getRupeeContainerStack();
			if (stack != null) {
				this.tradeSlot.inventory.setInventorySlotContents(0, stack);
				ZSSPlayerInfo.get(player).setRupeeContainerStack(null);
			}
		}
		this.addSlotToContainer(this.tradeSlot);
		this.waresInv = new InventoryBasic("", true, 4);
		for (int i = 0; i < this.waresInv.getSizeInventory(); ++i) {
			if (trades != null && i < trades.size()) {
				this.waresInv.setInventorySlotContents(i, trades.get(i).getTradeItem());
			} else {
				this.waresInv.setInventorySlotContents(i, null);
			}
			int x = 67 + (26 * i);
			int y = 12;
			this.addSlotToContainer(new SlotRupeeWares(this.waresInv, i, x, y));
		}
		this.addPlayerInventory(player, true);
	}

	public IRupeeMerchant getMerchant() {
		return this.merchant;
	}

	@Override
	public void addCraftingToCrafters(ICrafting player) {
		super.addCraftingToCrafters(player);
		player.sendProgressBarUpdate(this, 0, this.showTab ? 1 : 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int index, int value) {
		if (index == 0) {
			this.showTab = (value > 0);
		}
	}

	/**
	 * Uses the RupeeTrade at the selected index provided the player meets any requirements
	 * @param slotIndex Selected slot index, between {@value #WARES_START} and {@value #WARES_END}
	 */
	public abstract void useTrade(EntityPlayer player, int slotIndex);

	/**
	 * Returns a RupeeTrade matching the ItemStack in the trade slot or null if none match;
	 * uses {@link #getTradeSelector(ItemStack)} to determine matches.
	 * @param slotIndex The selected slot index, if any, is given priority match-making
	 */
	public RupeeTrade findMatchingTrade(int slotIndex) {
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		if (trades == null || !this.tradeSlot.getHasStack()) {
			return null;
		}
		Predicate<ItemStack> selector = this.getTradeSelector(this.tradeSlot.getStack());
		if (selector == null) {
			return null;
		}
		// Check selected index for match first
		int index = this.convertSlotIndex(slotIndex);
		if (index > -1 && index < trades.size()) {
			RupeeTrade trade = trades.get(index);
			if (trade != null && selector.apply(trade.getTradeItem())) {
				return trade;
			}
		}
		// Iterate through entire trade list if selected index did not match
		for (RupeeTrade trade : trades) {
			if (selector.apply(trade.getTradeItem())) {
				return trade;
			}
		}
		return null;
	}

	/**
	 * Return the predicate used to find a RupeeTrade corresponding to the stack placed in the trade slot.
	 * @param stack Typically whatever stack the player has placed in the trade slot
	 * @return
	 */
	public Predicate<ItemStack> getTradeSelector(ItemStack stack) {
		return null;
	}

	/**
	 * Converts selected slot index to that of its corresponding trade list entry using {@link #currentIndex}
	 * @param slotIndex Selected slot index, either -1 or between {@value #WARES_START} and {@value #WARES_END}
	 * @return -1 or the validated RupeeTrade index
	 */
	public int convertSlotIndex(int slotIndex) {
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		if (trades == null || slotIndex < WARES_START || slotIndex > WARES_END) {
			return -1;
		}
		return Math.min(this.currentIndex + slotIndex - WARES_START, trades.size() - 1);
	}

	/**
	 * Set's {@link #currentIndex} according to the value provided and refreshes the inventory contents
	 * @param index The value will be clamped between 0 and the largest factor of 4 less than the size of the current rupee trade list
	 * @return the current index value
	 */
	public int setCurrentIndex(int index) {
		RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
		int prev = this.currentIndex;
		int max = (trades == null ? 0 : trades.size() - 4);
		max += (max > 0 ? 4 - (max % 4) : 0);
		this.currentIndex = MathHelper.clamp_int(index, 0, max);
		// Refresh inventory contents if current index has changed
		if (max > 0 && this.currentIndex != prev) {
			for (int i = 0; i < this.waresInv.getSizeInventory(); ++i) {
				ItemStack stack = null;
				if (this.currentIndex + i < trades.size()) {
					stack = trades.get(this.currentIndex + i).getTradeItem();
				}
				this.waresInv.setInventorySlotContents(i, stack);
			}
		}
		return this.currentIndex;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.merchant.getRupeeCustomer() == player;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		if (this.toggling) {
			if (this.tradeSlot.getHasStack()) {
				ZSSPlayerInfo.get(player).setRupeeContainerStack(this.tradeSlot.getStack());
			}
		} else {
			super.onContainerClosed(player);
			if (this.tradeSlot.getHasStack()) {
				player.dropPlayerItemWithRandomChoice(this.tradeSlot.getStack(), false);
				this.tradeSlot.inventory.setInventorySlotContents(0, null);
			}
			this.merchant.setRupeeCustomer(null);
			ZSSPlayerInfo.get(player).setRupeeContainerStack(null); // redundant, but just in case
		}
	}

	/**
	 * Transfers items via shift-click between regular inventory and hotbar and
	 * between the purchase slot (even when selling) and inventory.
	 */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack stack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			stack = itemstack1.copy();
			if (slotIndex == 0) {
				if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, true)) {
					return null;
				}
				slot.onSlotChange(itemstack1, stack);
			} else if (slotIndex >= INV_START && slotIndex < HOTBAR_START) {
				if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
					return null;
				}
			} else if (slotIndex >= HOTBAR_START && slotIndex < HOTBAR_END + 1 && !this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
				return null;
			}
			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
			if (itemstack1.stackSize == stack.stackSize) {
				return null;
			}
			slot.onPickupFromSlot(player, itemstack1);
		}
		return stack;
	}

	public static class Shop extends ContainerRupeeMerchant
	{
		public Shop(EntityPlayer player, IRupeeMerchant merchant) {
			super(player, merchant, true);
		}

		@Override
		public void useTrade(EntityPlayer player, int slotIndex) {
			int index = this.convertSlotIndex(slotIndex);
			RupeeTradeList<RupeeTrade> trades = this.merchant.getRupeeTrades(this.getItemsToSell);
			if (trades == null || index < 0 || index > trades.size() - 1) {
				return; // nothing to do
			}
			RupeeTrade trade = trades.get(index);
			if (trade == null) {
				// nothing to do
			} else if (trade.isDisabled()) {
				// trade can not currently be used
			} else if (!player.worldObj.isRemote) {
				ItemStack stack = trade.getTradeItem();
				ZSSPlayerWallet wallet = ZSSPlayerWallet.get(player);
				this.tradeSlot.enabled = true;
				if (stack != null && wallet != null && wallet.getRupees() >= trade.getPrice() && this.mergeItemStack(stack, 0, WARES_START, false)) {
					wallet.spendRupees(trade.getPrice());
					this.merchant.useRupeeTrade(trade, true);
				}
				this.tradeSlot.enabled = false;
			}
		}
	}

	public static class Sales extends ContainerRupeeMerchant
	{
		public Sales(EntityPlayer player, IRupeeMerchant merchant) {
			super(player, merchant, false);
			this.tradeSlot.enabled = true;
		}

		@Override
		public Predicate<ItemStack> getTradeSelector(ItemStack stack) {
			List<Predicate<ItemStack>> predicates = Lists.newArrayList();
			predicates.add(Predicates.<ItemStack>notNull());
			predicates.add(new ItemStackItemPredicate(stack));
			// Require matching damage unless RupeeTrade uses wildcard value, but not if item being sold uses it
			predicates.add(Predicates.or(new WildCardPredicate(), new ItemStackDamagePredicate(stack)));
			predicates.add(new ItemStackMinQtyPredicate(stack));
			predicates.add(new ItemStackNbtPredicate(stack));
			return Predicates.and(predicates);
		}

		@Override
		public void useTrade(EntityPlayer player, int slotIndex) {
			ItemStack stack = this.tradeSlot.getStack();
			RupeeTrade trade = this.findMatchingTrade(slotIndex);
			if (stack == null || trade == null) {
				// nothing to do
			} else if (trade.isDisabled()) {
				// trade can not currently be used
			} else if (!player.worldObj.isRemote) {
				ItemStack match = trade.getTradeItem();
				ZSSPlayerWallet wallet = ZSSPlayerWallet.get(player);
				while (stack != null && stack.stackSize >= match.stackSize && wallet.addRupees(trade.getPrice(), false)) {
					// IInventory#decrStackSize returns a stack of up to the size requested, rather
					// than the decremented stack itself, so decrease stack size directly
					stack.stackSize -= match.stackSize;
					if (stack.stackSize < 1) {
						stack = null;
					}
					this.merchant.useRupeeTrade(trade, false);
				}
				// Update the slot's inventory contents
				this.tradeSlot.putStack(stack);
				// Force wallet to sync
				wallet.sync();
			}
		}
	}

	public static class SlotRupeeMerchant extends Slot
	{
		/** Set this to true before calling #mergeStackInSlot, then set back to false */
		public boolean enabled;
		public SlotRupeeMerchant(IInventory inv, int index, int x, int y) {
			super(inv, index, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return this.enabled;
		}
	}

	public static class SlotRupeeWares extends Slot
	{
		public SlotRupeeWares(IInventory inv, int index, int x, int y) {
			super(inv, index, x, y);
		}

		@Override
		public boolean isItemValid(ItemStack stack) {
			return false;
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return false;
		}
	}
}

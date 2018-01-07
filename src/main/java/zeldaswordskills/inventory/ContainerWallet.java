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

import java.util.EnumMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.item.ItemRupee;
import zeldaswordskills.item.ItemRupee.Rupee;
import zeldaswordskills.item.ZSSItems;

/**
 * 
 * Wallet interface allows player to add and remove rupees from their wallet.
 * 
 */
public class ContainerWallet extends AbstractContainer
{
	private static final int INV_START = ItemRupee.Rupee.values().length;
	private static final int INV_END = INV_START + 26;
	private static final int HOTBAR_START = INV_END + 1;
	private static final int HOTBAR_END = HOTBAR_START + 8;

	private final InventoryBasic inventory;

	private final EntityPlayer player;

	private final ZSSPlayerWallet wallet;

	public ContainerWallet(EntityPlayer player) {
		this.player = player;
		this.wallet = ZSSPlayerWallet.get(player);
		int rupees = this.wallet.getRupees();
		this.inventory = new InventoryWallet(player);
		// Add player's rupees to wallet inventory
		EnumMap<Rupee, Integer> stackSizes = ItemRupee.Rupee.getRupeeStackSizes(rupees);
		for (ItemRupee.Rupee rupee : ItemRupee.Rupee.values()) {
			ItemStack stack = new ItemStack(ZSSItems.rupee, stackSizes.get(rupee), rupee.ordinal());
			this.inventory.setInventorySlotContents(rupee.ordinal(), (stack.stackSize > 0 ? stack : null));
			addSlotToContainer(new SlotWallet(this.inventory, this.wallet, stack, rupee.ordinal(), 26 + rupee.ordinal() * 18, 53));
		}
		addPlayerInventory(player, true);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player == this.player;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack stack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			stack = itemstack1.copy();
			if (slotIndex < INV_START) {
				if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, true)) {
					return null;
				}
				slot.onSlotChange(itemstack1, stack);
			} else if (itemstack1.getItem() instanceof ItemRupee) {
				if (!this.mergeItemStack(itemstack1, itemstack1.getItemDamage(), itemstack1.getItemDamage()+1, false)) {
					return null;
				}
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
}

class SlotWallet extends Slot
{
	private final ZSSPlayerWallet wallet;
	private final ItemStack renderStack;
	public SlotWallet(IInventory inv, ZSSPlayerWallet wallet, ItemStack stack, int index, int x, int y) {
		super(inv, index, x, y);
		this.wallet = wallet;
		this.renderStack = stack.copy();
		// Customize item damage to flag 'empty' version
		this.renderStack.setItemDamage(stack.getItemDamage() + ItemRupee.Rupee.values().length);
	}

	@Override
	public int getSlotStackLimit() {
		int value = ItemRupee.Rupee.byDamage(this.getSlotIndex()).value;
		int remaining = this.wallet.getCapacity() - this.wallet.getRupees();
		int limit = (int)(remaining / value);
		ItemStack stack = this.getStack();
		if (stack != null) {
			// TODO compare slot rupee value to stack rupee value and adjust accordingly
			limit += stack.stackSize;
		}
		return Math.min(limit, super.getSlotStackLimit());
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemRupee)) {
			return false;
		}
		return ItemRupee.Rupee.byDamage(stack.getItemDamage()).ordinal() == this.getSlotIndex();
	}

	@Override
	public void putStack(ItemStack stack) {
		// TODO impossible without an ItemStack-sensitive getSlotStackLimit unless overriding Container#slotClick
		//		in which case, may not need to override this method
		if (stack != null) {
			ItemStack current = this.getStack();
			if (current != null && current.getItemDamage() != stack.getItemDamage()) {
				// TODO parse
			}
		}
		super.putStack(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getBackgroundIconIndex() {
		return this.renderStack.getIconIndex();
	}
}

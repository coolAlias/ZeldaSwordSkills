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

package zeldaswordskills.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.item.ItemPendant;

public class ContainerPedestal extends Container
{
	private TileEntityPedestal pedestal;
	
	/** Slot indices; Pedestal has 3 slots (0, 1, 2) */
	private static final int INV_START = 3, INV_END = INV_START+26,
	HOTBAR_START = INV_END+1, HOTBAR_END = HOTBAR_START+8;

	public ContainerPedestal(InventoryPlayer inv, TileEntityPedestal pedestal) {
		this.pedestal = pedestal;
		addSlotToContainer(new SlotPedestal(pedestal, 0, 80, 19));
		addSlotToContainer(new SlotPedestal(pedestal, 1, 49, 50));
		addSlotToContainer(new SlotPedestal(pedestal, 2, 111, 50));
		
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; ++i) {
			addSlotToContainer(new Slot(inv, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return pedestal.isUseableByPlayer(player);
	}
	
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotIndex < INV_START) {
				if (!mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, true)) {
					return null;
				}
				slot.onSlotChange(itemstack1, itemstack);
			} else if (itemstack1.getItem() instanceof ItemPendant) {
				if (!mergeItemStack(itemstack1, itemstack1.getItemDamage(), itemstack1.getItemDamage()+1, false)) {
					return null;
				}
			} else if (slotIndex >= INV_START && slotIndex < HOTBAR_START) {
				if (!mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
					return null;
				}
			} else if (slotIndex >= HOTBAR_START && slotIndex < HOTBAR_END + 1 && !mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.stackSize == itemstack.stackSize) {
				return null;
			}

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}
	
	@Override
	public ItemStack slotClick(int slot, int par2, int par3, EntityPlayer player) {
		if (slot < INV_START && pedestal.worldObj.getBlockMetadata(pedestal.xCoord, pedestal.yCoord, pedestal.zCoord) == 0x8) {
			return null;
		} else {
			return super.slotClick(slot, par2, par3, player);
		}
	}
	
	/**
	 * Vanilla method fails to account for stacks of size one, as well as whether stack
	 * is valid for slot
	 */
	@Override
	protected boolean mergeItemStack(ItemStack itemstack, int start, int end, boolean backwards)
	{
		boolean flag1 = false;
		int k = (backwards ? end - 1 : start);
		Slot slot;
		ItemStack itemstack1;
		if (itemstack.isStackable()) {
			while (itemstack.stackSize > 0 && (!backwards && k < end || backwards && k >= start))
			{
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();

				if (!slot.isItemValid(itemstack)) {
					k += (backwards ? -1 : 1);
					continue;
				}

				if (itemstack1 != null && itemstack1.itemID == itemstack.itemID && (!itemstack.getHasSubtypes() || itemstack.getItemDamage() == itemstack1.getItemDamage()) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1))
				{
					int l = itemstack1.stackSize + itemstack.stackSize;
					if (l <= itemstack.getMaxStackSize() && l <= slot.getSlotStackLimit()) {
						itemstack.stackSize = 0;
						itemstack1.stackSize = l;
						pedestal.onInventoryChanged();
						flag1 = true;
					} else if (itemstack1.stackSize < itemstack.getMaxStackSize() && l < slot.getSlotStackLimit()) {
						itemstack.stackSize -= itemstack.getMaxStackSize() - itemstack1.stackSize;
						itemstack1.stackSize = itemstack.getMaxStackSize();
						pedestal.onInventoryChanged();
						flag1 = true;
					}
				}
				k += (backwards ? -1 : 1);
			}
		}
		if (itemstack.stackSize > 0) {
			k = (backwards ? end - 1 : start);
			while (!backwards && k < end || backwards && k >= start) {
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();
				if (!slot.isItemValid(itemstack)) {
					k += (backwards ? -1 : 1);
					continue;
				}
				if (itemstack1 == null) {
					int l = itemstack.stackSize;

					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(itemstack.copy());
						itemstack.stackSize = 0;
						pedestal.onInventoryChanged();
						flag1 = true;
						break;
					} else {
						putStackInSlot(k, new ItemStack(itemstack.getItem(), slot.getSlotStackLimit(), itemstack.getItemDamage()));
						itemstack.stackSize -= slot.getSlotStackLimit();
						pedestal.onInventoryChanged();
						flag1 = true;
					}
				}
				k += (backwards ? -1 : 1);
			}
		}
		return flag1;
	}
}

class SlotPedestal extends Slot {

	public SlotPedestal(IInventory inv, int index, int x, int y) {
		super(inv, index, x, y);
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemPendant && stack.getItemDamage() == slotNumber;
    }
}

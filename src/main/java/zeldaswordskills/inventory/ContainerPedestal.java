/**
    Copyright (C) <2017> <coolAlias>

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
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.item.ItemPendant;

public class ContainerPedestal extends AbstractContainer
{
	/** Slot indices; Pedestal has 3 slots (0, 1, 2) */
	private static final int INV_START = 3;
	private static final int INV_END = INV_START+26;
	private static final int HOTBAR_START = INV_END+1;
	private static final int HOTBAR_END = HOTBAR_START+8;

	private final TileEntityPedestal pedestal;

	public ContainerPedestal(EntityPlayer player, TileEntityPedestal pedestal) {
		this.pedestal = pedestal;
		addSlotToContainer(new SlotPedestal(pedestal, 0, 80, 19));
		addSlotToContainer(new SlotPedestal(pedestal, 1, 49, 50));
		addSlotToContainer(new SlotPedestal(pedestal, 2, 111, 50));
		addPlayerInventory(player, true);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return pedestal.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack stack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotIndex);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			stack = itemstack1.copy();
			if (slotIndex < INV_START) {
				if (!mergeItemStack(itemstack1, INV_START, HOTBAR_END+1, true)) {
					return null;
				}
				slot.onSlotChange(itemstack1, stack);
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
			if (itemstack1.stackSize == stack.stackSize) {
				return null;
			}
			slot.onPickupFromSlot(player, itemstack1);
		}
		return stack;
	}

	@Override
	public ItemStack slotClick(int slot, int par2, int par3, EntityPlayer player) {
		if (slot < INV_START && pedestal.getWorldObj().getBlockMetadata(pedestal.xCoord, pedestal.yCoord, pedestal.zCoord) == 0x8) {
			return null;
		} else {
			return super.slotClick(slot, par2, par3, player);
		}
	}
}

class SlotPedestal extends Slot
{
	private final TileEntityPedestal pedestal;

	public SlotPedestal(TileEntityPedestal pedestal, int index, int x, int y) {
		super(pedestal, index, x, y);
		this.pedestal = pedestal;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemPendant && stack.getItemDamage() == slotNumber;
	}

	@Override
	public void onSlotChanged() {
		this.pedestal.markDirty();
	}
}

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
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public abstract class AbstractContainer extends Container
{
	/**
	 * Adds the player's inventory slots to this container in the default positions
	 * @param addHotBar true to also add the player's hot bar slots
	 */
	protected void addPlayerInventory(EntityPlayer player, boolean addHotBar) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		if (addHotBar) {
			for (int i = 0; i < 9; ++i) {
				addSlotToContainer(new Slot(player.inventory, i, 8 + i * 18, 142));
			}
		}
	}

	/**
	 * Vanilla method fails to account for stacks of size one, as well as
	 * whether the stack is valid for the slot
	 */
	@Override
	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean backwards) {
		boolean flag1 = false;
		int k = (backwards ? end - 1 : start);
		Slot slot;
		ItemStack itemstack1;
		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!backwards && k < end || backwards && k >= start)) {
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();
				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}
				boolean sameItem = (itemstack1 != null && itemstack1.getItem() == stack.getItem());
				boolean sameDamage = (itemstack1 != null && (!stack.getHasSubtypes() || stack.getItemDamage() == itemstack1.getItemDamage()));
				if (sameItem && sameDamage && ItemStack.areItemStackTagsEqual(stack, itemstack1)) {
					int l = itemstack1.stackSize + stack.stackSize;
					int slotLimit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					if (l <= slotLimit) {
						stack.stackSize = 0;
						itemstack1.stackSize = l;
						slot.onSlotChanged();
						flag1 = true;
					} else if (itemstack1.stackSize < slotLimit) {
						stack.stackSize -= slotLimit - itemstack1.stackSize;
						itemstack1.stackSize = slotLimit;
						slot.onSlotChanged();
						flag1 = true;
					}
				}
				k += (backwards ? -1 : 1);
			}
		}
		if (stack.stackSize > 0) {
			k = (backwards ? end - 1 : start);
			while (!backwards && k < end || backwards && k >= start) {
				slot = (Slot) inventorySlots.get(k);
				itemstack1 = slot.getStack();
				if (!slot.isItemValid(stack)) {
					k += (backwards ? -1 : 1);
					continue;
				}
				if (itemstack1 == null) {
					int l = stack.stackSize;
					if (l <= slot.getSlotStackLimit()) {
						slot.putStack(stack.copy());
						stack.stackSize = 0;
						slot.onSlotChanged();
						flag1 = true;
						break;
					} else {
						putStackInSlot(k, new ItemStack(stack.getItem(), slot.getSlotStackLimit(), stack.getItemDamage()));
						stack.stackSize -= slot.getSlotStackLimit();
						slot.onSlotChanged();
						flag1 = true;
					}
				}
				k += (backwards ? -1 : 1);
			}
		}
		return flag1;
	}
}

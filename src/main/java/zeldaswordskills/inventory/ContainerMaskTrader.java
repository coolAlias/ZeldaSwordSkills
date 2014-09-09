/**
    Copyright (C) <2014> <coolAlias>

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
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.server.BorrowMaskPacket;

public class ContainerMaskTrader extends Container
{
	private final InventoryMaskTrader inv;
	/** Set to true when mask is borrowed, closing the screen */
	private boolean maskBorrowed = false;

	public ContainerMaskTrader() {
		inv = new InventoryMaskTrader();
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			addSlotToContainer(new Slot(inv, i, (i > 5 ? 116 : 8) + (i % 3) * 18, (i % 6 > 2 ? 142 : 124)));
		}
		addSlotToContainer(new Slot(new InventoryBasic("", true, 1), 0, 80, 124));
	}

	/** Returns true if there is a valid stack in the borrow slot */
	public boolean canBorrow() {
		return ((Slot) inventorySlots.get(inv.getSizeInventory())).getHasStack();
	}

	/** Sends borrow mask packet to player and closes screen */
	public void borrowMask() {
		ItemStack mask = ((Slot) inventorySlots.get(inv.getSizeInventory())).getStack();
		PacketDispatcher.sendToServer(new BorrowMaskPacket(mask));
		maskBorrowed = true;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return !maskBorrowed && inv.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		return null;
	}

	@Override
	public ItemStack slotClick(int slotIndex, int button, int par3, EntityPlayer player) {
		if (slotIndex >= 0 && slotIndex < inv.getSizeInventory()) {
			Slot slot = (Slot) inventorySlots.get(slotIndex);
			if (slot != null && slot.getHasStack()) {
				((Slot) inventorySlots.get(inv.getSizeInventory())).inventory.setInventorySlotContents(0, slot.getStack().copy());
			}
		}
		return null;
	}
}

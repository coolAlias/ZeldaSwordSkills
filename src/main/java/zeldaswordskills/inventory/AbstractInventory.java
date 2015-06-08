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
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

/**
 * 
 * Class aiming to minimize amount of code needed for an inventory.
 * MarkDirty() is called where appropriate, but not implemented (usually only TileEntities need it).
 * Default displays an empty string as the translated (i.e. non-custom) name.
 *
 */
public abstract class AbstractInventory implements IInventory
{
	/** The inventory slots need to be initialized during construction */
	protected ItemStack[] inventory;

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if(stack.stackSize > amount) {
				stack = stack.splitStack(amount);
				markDirty();
			} else {
				setInventorySlotContents(slot, null);
			}
		}

		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		ItemStack stack = getStackInSlot(slot);
		setInventorySlotContents(slot, null);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		inventory[slot] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
		markDirty();
	}

	@Override
	public void markDirty() {}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < inventory.length; ++i) {
			inventory[i] = null;
		}
	}

	/**
	 * Return unlocalized name here, or pre-translated and return true for hasCustomName()
	 */
	@Override
	public String getCommandSenderName() {
		return "";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public IChatComponent getDisplayName() {
		return (IChatComponent)(hasCustomName() ? new ChatComponentText(getCommandSenderName()) : new ChatComponentTranslation(getCommandSenderName()));
	}

	/**
	 * NBT key used to set and retrieve the NBTTagCompound containing this inventory
	 */
	protected abstract String getNbtKey();

	/**
	 * Writes this inventory to NBT; must be called manually
	 * Fails silently if {@link #getNbtKey} returns null or an empty string
	 */
	public void writeToNBT(NBTTagCompound compound) {
		String key = getNbtKey();
		if (key == null || key.equals("")) {
			return;
		}
		NBTTagList items = new NBTTagList();
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null) {
				NBTTagCompound item = new NBTTagCompound();
				item.setByte("Slot", (byte) i);
				getStackInSlot(i).writeToNBT(item);
				items.appendTag(item);
			}
		}
		compound.setTag(key, items);
	}

	/**
	 * Loads this inventory from NBT; must be called manually
	 * Fails silently if {@link #getNbtKey} returns null or an empty string
	 */
	public void readFromNBT(NBTTagCompound compound) {
		String key = getNbtKey();
		if (key == null || key.equals("")) {
			return;
		}
		NBTTagList items = compound.getTagList(key, compound.getId());
		for (int i = 0; i < items.tagCount(); ++i) {
			NBTTagCompound item = items.getCompoundTagAt(i);
			byte slot = item.getByte("Slot");
			if (slot >= 0 && slot < getSizeInventory()) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(item);
			}
		}
	}
}

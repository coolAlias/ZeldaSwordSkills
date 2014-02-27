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

package zeldaswordskills.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.network.PlaySoundPacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * 
 * A collection of utility methods related to the player
 *
 */
public class PlayerUtils
{
	/** Returns true if the player is currently holding a sword-type weapon */
	public static boolean isHoldingSword(EntityPlayer player) {
		return (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemSword);
	}
	
	/** Returns true if the player is currently holding a Zelda-specific sword */
	public static boolean isHoldingZeldaSword(EntityPlayer player) {
		return (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemZeldaSword);
	}
	
	/** Returns true if the player is currently holding a Master sword */
	public static boolean isHoldingMasterSword(EntityPlayer player) {
		return (isHoldingZeldaSword(player) && ((ItemZeldaSword) player.getHeldItem().getItem()).isMasterSword());
	}
	
	/**
	 * Returns true if the player has any type of master sword somewhere in the inventory
	 */
	public static boolean hasMasterSword(EntityPlayer player) {
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null && stack.getItem() instanceof ItemZeldaSword && ((ItemZeldaSword) stack.getItem()).isMasterSword()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the player has the Item somewhere in the inventory,
	 * ignoring the stack's damage value
	 */
	public static boolean hasItem(EntityPlayer player, Item item) {
		return hasItem(player, item, -1);
	}
	
	/**
	 * Returns true if the player has the Item somewhere in the inventory, with
	 * optional metadata for subtyped items
	 * @param meta use -1 to ignore the stack's damage value
	 */
	public static boolean hasItem(EntityPlayer player, Item item, int meta) {
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null && stack.getItem() == item) {
				return meta == -1 || stack.getItemDamage() == meta;
			}
		}
		return false;
	}
	
	/** Returns the difference between player's max and current health */
	public static float getHealthMissing(EntityPlayer player) {
		return (player.getMaxHealth() - player.getHealth());
	}
	
	/**
	 * Adds the stack to the player's inventory or, failing that, drops it as an EntityItem
	 */
	public static void addItemToInventory(EntityPlayer player, ItemStack stack) {
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropPlayerItem(stack);
		}
	}
	
	/**
	 * Metadata-sensitive version of consumeInventoryItem
	 */
	public static boolean consumeInventoryItem(EntityPlayer player, int id, int meta) {
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null && stack.itemID == id && stack.getItemDamage() == meta) {
				--stack.stackSize;
				if (stack.stackSize == 0) {
					player.inventory.setInventorySlotContents(i, null);
				}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the provided ItemStack was consumed
	 * The method is stackSize sensitive, consuming the exact amount as the argument
	 */
	public static boolean consumeInventoryItems(EntityPlayer player, ItemStack stack) {
		int required = stack.stackSize;
		for (int i = 0; i < player.inventory.getSizeInventory() && required > 0; ++i) {
			ItemStack invStack = player.inventory.getStackInSlot(i);
			if (invStack != null && invStack.getItem() == stack.getItem() && invStack.getItemDamage() == stack.getItemDamage()) {
				if (invStack.stackSize < required) {
					required -= invStack.stackSize;
					player.inventory.setInventorySlotContents(i, null);
				} else {
					player.inventory.setInventorySlotContents(i, invStack.splitStack(required));
					required = 0;
					break;
				}
			}
		}
		if (required > 0) {
			player.inventory.addItemStackToInventory(new ItemStack(stack.getItem(), stack.stackSize - required, stack.getItemDamage()));
		}
		
		return required == 0;
	}
	
	/**
	 * Sends a packet to the client to play a sound on the client side only, or
	 * sends a packet to the server to play a sound on the server for all to hear
	 */
	public static void playSound(EntityPlayer player, String sound, float volume, float pitch) {
		if (player.worldObj.isRemote) {
			PacketDispatcher.sendPacketToServer(new PlaySoundPacket(sound, volume, pitch, player).makePacket());
		} else {
			PacketDispatcher.sendPacketToPlayer(new PlaySoundPacket(sound, volume, pitch).makePacket(), (Player) player);
		}
	}
}

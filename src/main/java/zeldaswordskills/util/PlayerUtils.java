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

package zeldaswordskills.util;

import java.util.UUID;

import mods.battlegear2.api.core.IBattlePlayer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.IWeapon;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;

/**
 * 
 * A collection of utility methods related to the player
 *
 */
public class PlayerUtils
{
	/** Copy of Item#field_111210_e which is the UUID used to store weapon damage modifiers */
	public static final UUID itemDamageUUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

	/**
	 * Returns whether the player is blocking with the currently held item, accounting for possibility of Battlegear2 offhand shield use
	 */
	public static boolean isBlocking(EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && player.isUsingItem() && stack.getItem().getItemUseAction(stack) == EnumAction.block) {
			return true;
		} else if (ZSSMain.isBG2Enabled) {
			return ((IBattlePlayer) player).isBattlemode() && ((IBattlePlayer) player).isBlockingWithShield();
		}
		return false;
	}

	/**
	 * Returns true if the player is holding any type of item which grants a damage bonus,
	 * including from tools such as axes, shovels, etc.
	 */
	public static boolean isHoldingWeapon(EntityPlayer player) {
		AttributeModifier itemDamage = player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.attackDamage).getModifier(itemDamageUUID);
		return itemDamage != null && itemDamage.getAmount() > 0;
	}

	/**
	 * Returns true if the item is a sword: i.e. if it is an {@link ItemSword},
	 * an {@link IWeapon} (returns {@link IWeapon#isSword(ItemStack)}),
	 * or registered to the {@link WeaponRegistry} as a sword
	 */
	public static boolean isSword(ItemStack stack) {
		if (stack == null) {
			return false;
		} else if (stack.getItem() instanceof IWeapon) {
			return ((IWeapon) stack.getItem()).isSword(stack);
		}
		return WeaponRegistry.INSTANCE.isSword(stack.getItem());
	}

	/**
	 * Returns true if the item is any kind of weapon: a {@link #isSword(ItemStack) sword},
	 * an {@link IWeapon}, or registered to the {@link WeaponRegistry} as a weapon
	 */
	public static boolean isWeapon(ItemStack stack) {
		if (stack == null) {
			return false;
		} else if (stack.getItem() instanceof IWeapon) {
			return ((IWeapon) stack.getItem()).isWeapon(stack);
		}
		return (isSword(stack) || WeaponRegistry.INSTANCE.isWeapon(stack.getItem()));
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
	 * Subtype sensitive version of {@link #hasItem(EntityPlayer, Item) hasItem},
	 * checks Item and damage values, ignoring stack size and NBT.
	 */
	public static boolean hasItem(EntityPlayer player, ItemStack stack) {
		return hasItem(player, stack.getItem(), stack.getItemDamage());
	}

	/**
	 * Returns true if the player has the Item somewhere in the inventory, with
	 * optional metadata for subtyped items
	 * @param meta use -1 to ignore the stack's damage value
	 */
	public static boolean hasItem(EntityPlayer player, Item item, int meta) {
		for (ItemStack stack : player.inventory.mainInventory) {
			if (stack != null && stack.getItem() == item) {
				if (meta == -1 || stack.getItemDamage() == meta) {
					return true;
				}
			}
		}
		return false;
	}

	/** Returns the difference between player's max and current health */
	public static float getHealthMissing(EntityPlayer player) {
		return player.capabilities.isCreativeMode ? 0.0F : (player.getMaxHealth() - player.getHealth());
	}

	/**
	 * Adds the stack to the player's inventory or, failing that, drops it as an EntityItem
	 */
	public static void addItemToInventory(EntityPlayer player, ItemStack stack) {
		if (!player.inventory.addItemStackToInventory(stack)) {
			player.dropPlayerItemWithRandomChoice(stack, false);
		}
	}

	/**
	 * Attempts to consume the amount given from the player's held item stack only;
	 * does not check stack damage. In Creative, acts like hasItem (does not consume).
	 */
	public static boolean consumeHeldItem(EntityPlayer player, Item item, int amount) {
		return consumeHeldItem(player, item, -1, amount);
	}

	/**
	 * Attempts to consume the amount given from the player's held item stack only.
	 * In Creative, acts like hasItem (does not consume).
	 * @param damage Required stack damage to match, or -1 to not check damage
	 */
	public static boolean consumeHeldItem(EntityPlayer player, Item item, int damage, int amount) {
		if (amount < 1) {
			return false;
		}
		ItemStack stack = player.getHeldItem();
		if (stack == null || stack.getItem() != item || stack.stackSize < amount) {
			return false;
		} else if (damage > -1 && stack.getItemDamage() != damage) {
			return false;
		} else if (player.capabilities.isCreativeMode) {
			return true;
		} else {
			stack.stackSize -= amount;
			if (stack.stackSize < 1) {
				stack = null;
				player.setCurrentItemOrArmor(0, null);
			}
			return true;
		}
	}

	/**
	 * Returns true if the required number of item were removed from the player's inventory;
	 * if the entire quantity is not present, then no items are removed.
	 * In Creative, acts like hasItem (does not consume).
	 */
	public static boolean consumeInventoryItem(EntityPlayer player, Item item, int required) {
		return consumeInventoryItem(player, item, 0, required);
	}

	/**
	 * Calls {@link #consumeInventoryItem} with the stack's item and damage value
	 */
	public static boolean consumeInventoryItem(EntityPlayer player, ItemStack stack, int required) {
		return consumeInventoryItem(player, stack.getItem(), stack.getItemDamage(), required);
	}

	/**
	 * A metadata-sensitive version of {@link InventoryPlayer#consumeInventoryItem(int)}
	 * In Creative, acts like hasItem (does not consume).
	 * @param item	The type of item to consume
	 * @param meta	The required damage value of the stack
	 * @param required	The number of such items to consume
	 * @return	True if the entire amount was consumed; if this is not possible, no items are consumed and it returns false
	 */
	public static boolean consumeInventoryItem(EntityPlayer player, Item item, int meta, int required) {
		if (required < 1) {
			return false;
		}
		// decremented until it reaches zero, meaning the entire required amount was consumed
		int consumed = required;
		for (int i = 0; i < player.inventory.getSizeInventory() && consumed > 0; ++i) {
			ItemStack invStack = player.inventory.getStackInSlot(i);
			if (invStack != null && invStack.getItem() == item && invStack.getItemDamage() == meta) {
				if (invStack.stackSize <= consumed) {
					consumed -= invStack.stackSize;
					if (!player.capabilities.isCreativeMode) {
						player.inventory.setInventorySlotContents(i, null);
					}
				} else {
					if (!player.capabilities.isCreativeMode) {
						invStack = invStack.splitStack(invStack.stackSize - consumed);
						player.inventory.setInventorySlotContents(i, invStack);
					}
					consumed = 0;
				}
			}
		}
		if (consumed > 0 && !player.capabilities.isCreativeMode) {
			player.inventory.addItemStackToInventory(new ItemStack(item, required - consumed, meta));
		}

		return consumed == 0;
	}

	/** Sends a pre-translated chat message to the player */
	public static void sendChat(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentText(message));
	}

	/** Sends a translated chat message to the player */
	public static void sendTranslatedChat(EntityPlayer player, String message) {
		player.addChatMessage(new ChatComponentTranslation(message));
	}

	/** Sends a formatted, translated chat message to the player */
	public static void sendFormattedChat(EntityPlayer player, String message, Object... args) {
		player.addChatMessage(new ChatComponentTranslation(message, args));
	}

	/**
	 * Sends a packet to the client to play a sound on the client side only, or
	 * sends a packet to the server to play a sound on the server for all to hear.
	 * To avoid playing a sound twice, only call the method from one side or the other, not both.
	 */
	public static void playSound(EntityPlayer player, String sound, float volume, float pitch) {
		if (player.worldObj.isRemote) {
			PacketDispatcher.sendToServer(new PlaySoundPacket(sound, volume, pitch, player));
		} else if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new PlaySoundPacket(sound, volume, pitch), (EntityPlayerMP) player);
		}
	}

	/**
	 * Plays a sound at the player's position with randomized volume and pitch.
	 * Sends a packet to the client to play a sound on the client side only, or
	 * sends a packet to the server to play a sound on the server for all to hear.
	 * 
	 * To avoid playing a sound twice, only call the method from one side or the
	 * other, not both. To play a sound directly on the server, use
	 * {@link WorldUtils#playSoundAtEntity} instead.
	 * 
	 * @param f		Volume: nextFloat() * f + add
	 * @param add	Pitch: 1.0F / (nextFloat() * f + add)
	 */
	public static void playRandomizedSound(EntityPlayer player, String sound, float f, float add) {
		float volume = player.worldObj.rand.nextFloat() * f + add;
		float pitch = 1.0F / (player.worldObj.rand.nextFloat() * f + add);
		playSound(player, sound, volume, pitch);
	}
}

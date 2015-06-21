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

package zeldaswordskills.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 
 * For items that can cycle back and forth between different modes via key bind.
 *
 */
public interface ICyclableItem {

	/**
	 * Sets the item's current mode to the next available
	 */
	void nextItemMode(ItemStack stack, EntityPlayer player);

	/**
	 * Sets the item's current mode to the previous one
	 */
	void prevItemMode(ItemStack stack, EntityPlayer player);

	/**
	 * Return integer representation of the current mode (used to send current mode to the client)
	 */
	int getCurrentMode(ItemStack stack, EntityPlayer player);

	/**
	 * Set the current mode based on its integer representation (used when synchronizing the client side)
	 */
	void setCurrentMode(ItemStack stack, EntityPlayer player, int mode);

	/**
	 * Return an ItemStack to render in the HUD overlay representing the current mode, or null not to render
	 */
	ItemStack getRenderStackForMode(ItemStack stack, EntityPlayer player);

}

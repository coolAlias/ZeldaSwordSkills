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

package zeldaswordskills.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 
 * Interface for Items that need special handling when picked up by a player
 *
 */
public interface IHandlePickup {
	
	/**
	 * This method is called when the item is picked up; if the stack's size is
	 * less than the original size after the method is called, the event will be
	 * canceled, the pickup sound will be played, and a pickup notification will
	 * be sent to nearby players
	 * @return returning false will cancel the event without picking up the item
	 */
	public boolean onPickupItem(ItemStack stack, EntityPlayer player);

}

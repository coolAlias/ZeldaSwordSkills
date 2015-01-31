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

package zeldaswordskills.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * 
 * Any Item that implements this interface will be able to interact with Sacred Flames
 *
 */
public interface ISacredFlame {

	/**
	 * Called when a player activates (right clicks) a Sacred Flame Block.
	 * Called whether or not the flame is active, and is called on both client and server.
	 * @param stack the player's currently held item that was used to click the flames
	 * @param type the flame's type: BlockSacredFlame.DIN, FARORE, or NAYRU
	 * @param isActive true if the flame is currently active, i.e. has not been extinguished
	 * @return true to extinguish the sacred flame block (based on Config settings may permanently destroy the block)
	 */
	public boolean onActivatedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive);

	/**
	 * Called when a player left clicks a Sacred Flame Block.
	 * Called whether or not the flame is active, and is called on both client and server.
	 * @param stack the player's currently held item that was used to click the flames
	 * @param type the flame's type: BlockSacredFlame.DIN, FARORE, or NAYRU
	 * @param isActive true if the flame is currently active, i.e. has not been extinguished
	 * @return true to extinguish the sacred flame block (based on Config settings may permanently destroy the block)
	 */
	public boolean onClickedSacredFlame(ItemStack stack, World world, EntityPlayer player, int type, boolean isActive);

}

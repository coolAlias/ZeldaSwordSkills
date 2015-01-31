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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;

/**
 * 
 * Interface for any item that can be upgraded at a fairy spawner. This allows for
 * a slight delay between the time the item is tossed and the time it is processed.
 *
 */
public interface IFairyUpgrade {

	/**
	 * Called from a fairy spawner during a scheduled update to process any upgrade or other
	 * effect that should occur; this method is only called if hasFairyUpgrade returns true
	 */
	public void handleFairyUpgrade(EntityItem item, EntityPlayer player, TileEntityDungeonCore core);

	/**
	 * Return true if this object should call handleFairyUpgrade;
	 * useful if different objects of the same class are handled differently
	 * @param stack used to differentiate based on NBT or item damage
	 */
	public boolean hasFairyUpgrade(ItemStack stack);

}

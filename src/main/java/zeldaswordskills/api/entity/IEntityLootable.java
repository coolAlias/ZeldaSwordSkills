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

package zeldaswordskills.api.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.block.IWhipBlock.WhipType;

/**
 * 
 * For entities that need greater control over whip looting outcomes.
 *
 */
public interface IEntityLootable {

	/**
	 * @param player	The whip-wielding thief attempting to steal
	 * @param whip		Type of whip being used to steal the item
	 * @return			The chance that an item will be looted
	 */
	public float getLootableChance(EntityPlayer player, WhipType whip);

	/**
	 * @param player	The whip-wielding thief attempting to steal
	 * @param whip		Type of whip being used to steal the item
	 * @return			The ItemStack that the thief will receive, possibly NULL
	 */
	public ItemStack getEntityLoot(EntityPlayer player, WhipType whip);

	/**
	 * Called after the thief has tried to steal an item to determine if
	 * the entity's stolen item NBT flag should be set.
	 * @param player			The thief that stole the item!
	 * @param wasItemStolen		Whether an item was truly stolen or not
	 * @return					True to set the NBT flag, preventing any further stealing attempts
	 */
	public boolean onLootStolen(EntityPlayer player, boolean wasItemStolen);

}

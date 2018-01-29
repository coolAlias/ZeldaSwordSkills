/**
    Copyright (C) <2018> <coolAlias>

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

import net.minecraft.item.ItemStack;

/**
 * 
 * Interface for ammunition that has a minimum 'level' requirement for the bow or other tool.
 *
 */
public interface ISpecialAmmunition
{
	/**
	 * Minimum bow (or other tool) level required to use this ammunition
	 * @param stack the ammunition stack 
	 * @return if larger than the tool's maximum level, the ammunition may not be usable at all
	 */
	int getRequiredLevelForAmmo(ItemStack stack);

}

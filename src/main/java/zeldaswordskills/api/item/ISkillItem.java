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

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import zeldaswordskills.item.WeaponRegistry;

/**
 * 
 * Allows the item to count as a weapon for skills that require the player to be
 * wielding some kind of weapon; note that some skills specifically require a
 * sword, in which case the item must either extend the vanilla {@link ItemSword}
 * directly or implement {@link ISword}.
 * 
 * For items that do not use NBT or stack damage, consider registering them via
 * the {@link WeaponRegistry} using FML's Inter-Mod Communications.
 *
 */
public interface ISkillItem {

	/**
	 * Return true if the ItemStack is considered a weapon for the purpose of skill use
	 */
	boolean isWeapon(ItemStack stack);

}

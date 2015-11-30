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
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.LeapingBlow;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.skills.sword.RisingCut;
import zeldaswordskills.skills.sword.SwordBeam;

/**
 * 
 * Items implementing this interface are always considered weapons and thus will
 * be compatible with most of the {@link SkillBase skills} by default.
 * 
 * If a skill's activation requires blocking, the item must be able to block or
 * it will not be able to activate such skills.
 * 
 * Some skills may only be performed while wielding a {@link #isSword sword}; these are:
 * {@link LeapingBlow}, {@link MortalDraw}, {@link RisingCut}, and {@link SwordBeam}.
 * 
 * For items that do not use NBT or stack damage, consider registering them as weapons
 * or as swords via the {@link WeaponRegistry} using FML's Inter-Mod Communications.
 *
 */
public interface IWeapon {

	/**
	 * Return true if the ItemStack is considered a sword
	 */
	boolean isSword(ItemStack stack);

	/**
	 * Return true if the ItemStack is considered a weapon
	 * (should return true if {@link #isSword} returns true)
	 */
	boolean isWeapon(ItemStack stack);

}

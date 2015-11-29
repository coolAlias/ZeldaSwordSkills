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
import zeldaswordskills.item.WeaponRegistry;
import zeldaswordskills.skills.sword.LeapingBlow;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.skills.sword.RisingCut;
import zeldaswordskills.skills.sword.SwordBeam;

/**
 * 
 * For the purpose of using certain skills, this item will be considered a sword.
 * 
 * For items that do not use NBT or stack damage, consider registering them via
 * the {@link WeaponRegistry} using FML's Inter-Mod Communications.
 * 
 * Skills which require a sword are:
 * {@link LeapingBlow}, {@link MortalDraw}, {@link RisingCut}, and {@link SwordBeam}
 *
 */
public interface ISword {

	/**
	 * Return true if the ItemStack is considered a sword
	 */
	boolean isSword(ItemStack stack);

}

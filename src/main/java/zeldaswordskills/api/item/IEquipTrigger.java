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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 
 * For items equipped in the player's armor slots that need to have
 * something happen when first equipped and/or unequipped.
 * <br><br>
 * Note that since multiple equipped items may provide the same or similar
 * modifiers (specifically Buffs), any time one is unequipped and removes
 * its modifier, it could negate a bonus (or penalty) granted by another.
 * <br><br>
 * To get around this, any time an equipped slot changes state, ALL of the
 * slots will first be "unequipped" to remove all modifiers, then re-equipped.
 * <br><br>
 * Unfortunately, this does not re-apply modifiers from any other source, so
 * if a player had e.g. Buff.EVADE_UP from something other than an IEquipTrigger
 * item and unequipped the Pegasus Boots, they would lose their evasion buff.
 *
 */
public interface IEquipTrigger
{
	/**
	 * Called when the stack is first equipped on the player, as well as each time any
	 * armor slot changes to reapply any changes after first calling onArmorUnequipped.
	 * @param equipSlot The armor equip slot index for e.g. {@link EntityPlayer#getCurrentArmor(int)} 
	 */
	void onArmorEquipped(ItemStack stack, EntityPlayer player, int equipSlot);

	/**
	 * Called when the armor is unequipped, destroyed, or otherwise removed,
	 * as well as each time any armor slot changes.
	 * @param equipSlot The armor equip slot index for e.g. {@link EntityPlayer#getCurrentArmor(int)}
	 */
	void onArmorUnequipped(ItemStack stack, EntityPlayer player, int equipSlot);

}

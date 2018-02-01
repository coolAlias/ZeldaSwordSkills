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
import zeldaswordskills.api.item.ItemMode.ItemModeEntity;

/**
 * 
 * Use this class for any item that uses {@link ISpecialAmmunition} level
 * requirements when cycling modes, such as the Hero's Bow.
 *
 */
public class ItemModeSpecialAmmo<T> extends ItemModeEntity<T>
{
	/**
	 * Returns the next available arrow index whose required level does not exceed that given
	 * @param index the current index, usually as retrieved from {@link ICyclableItem#getCurrentMode(ItemStack, EntityPlayer)}
	 * @param level the max arrow level allowed, usually as retrieved from the Hero's Bow
	 */
	public int next(int index, int level) {
		return cycle(index, level, true);
	}

	/**
	 * Returns the previous available arrow index whose required level does not exceed that given
	 * @param index the current index, usually as retrieved from {@link ICyclableItem#getCurrentMode(ItemStack, EntityPlayer)}
	 * @param level the max arrow level allowed, usually as retrieved from the Hero's Bow
	 */
	public int prev(int index, int level) {
		return cycle(index, level, false);
	}

	private int cycle(int index, int level, boolean next) {
		int current = index;
		ItemStack stack;
		int req = 0;
		do {
			index = (next ? this.next(index) : this.prev(index));
			stack = this.getStack(index);
			if (stack != null && stack.getItem() instanceof ISpecialAmmunition) {
				req = ((ISpecialAmmunition) stack.getItem()).getRequiredLevelForAmmo(stack);
			} else {
				req = 0;
			}
		} while (index != current && req > level);
		return index;
	}
}

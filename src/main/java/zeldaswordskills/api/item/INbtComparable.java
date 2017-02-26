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
 * Allows Item implementations to, in specific cases, determine whether ItemStack NBT tags
 * are to be considered equivalent, rather than using {@link ItemStack#areItemStackTagsEqual}.
 * 
 * Cases that call this interface's method(s) should make that clear to potential callers. 
 *
 */
public interface INbtComparable {

	/**
	 * Will be called in some cases instead of {@link ItemStack#areItemStackTagsEqual} to determine if
	 * two ItemStacks' NBTTagCompounds are considered "equivalent" without necessarily being equal.
	 * <br>Each stack should contain the same Item and at least one should have a non-null tag compound.
	 * @return True if the two stacks' nbt tags are considered equivalent
	 */
	boolean areTagsEquivalent(ItemStack a, ItemStack b);

}

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

package zeldaswordskills.api.item;

/**
 * 
 * Interface for items, usually weapons, that cannot be swung at the normal
 * vanilla spam-happy speed. After left-clicking with such an item, all further
 * left-click actions will be prevented until the attack timer returns to zero.
 * 
 * Alternatively, a config option can disable the attack timer for vanilla items,
 * such that after using an ISwingSpeed item, the player is still able to attack
 * by switching to a non-ISwingSpeed item.
 *
 */
public interface ISwingSpeed {
	
	/**
	 * The time for which all left-clicks will be prevented after swinging this item;
	 * may be configured to allow non-ISwingSpeed items to ignore the timer, in which
	 * case only ISwingSpeed items will be affected
	 */
	public int getSwingSpeed();

}

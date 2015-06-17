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

package zeldaswordskills.entity.ai;

public interface IWallPerch {
	/**
	 * Returns whether the entity is 'perched', i.e. it should not move
	 */
	boolean isPerched();

	/**
	 * Return true if the entity's current location is a suitable perch
	 */
	boolean canPerch();

	/**
	 * Set the entity's 'perched' status, such that {@link #isPerched()} should now return 'isPerched'
	 */
	void setPerched(boolean isPerched);

}

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

package zeldaswordskills.util;

import net.minecraft.util.MovingObjectPosition;

/**
 * 
 * Based on documentation for {@link MovingObjectPosition#sideHit}
 * 
 * However, based on my (coolAlias) observations, the documentation is incorrect.
 * Observed values are:
 * 2 = NORTH (face of block), 3 = SOUTH, 4 = WEST, 5 = EAST, 0 = BOTTOM, 1 = TOP
 * 
 * @author Hunternif
 *
 */
public final class SideHit {
	public static final int NONE = -1;
	public static final int BOTTOM = 0;
	public static final int TOP = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	public static final int NORTH = 4;
	public static final int SOUTH = 5;
}

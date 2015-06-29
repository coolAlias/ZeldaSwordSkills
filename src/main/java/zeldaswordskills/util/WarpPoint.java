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

import net.minecraft.nbt.NBTTagCompound;

/**
 * 
 * Simple utility class to store all data needed to teleport somewhere
 *
 */
public class WarpPoint
{
	/** Warp dimension */
	public final int dimensionId;
	
	/** Warp coordinates */
	public final int x, y, z;

	public WarpPoint(int dimensionId, int x, int y, int z) {
		this.dimensionId = dimensionId;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Returns an NBTTagCompound containing this warp point
	 */
	public NBTTagCompound writeToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("WarpPointData", new int[]{dimensionId, x, y, z});
		return tag;
	}

	/**
	 * Creates a new warp point from the NBTTagCompound
	 * @param tag	Must be the same tag format as returned by {@link #writeToNBT()} 
	 */
	public static WarpPoint readFromNBT(NBTTagCompound tag) {
		// Id 11 is TAG_Int_Array
		if (tag.hasKey("WarpPointData") && tag.getTag("WarpPointData").getId() == 11) {
			int[] data = tag.getIntArray("WarpPointData");
			return new WarpPoint(data[0], data[1], data[2], data[3]);
		}
		return null;
	}
}

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
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.util.Constants;

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
	public final BlockPos pos;

	public WarpPoint(int dimensionId, BlockPos pos) {
		this.dimensionId = dimensionId;
		this.pos = pos;
	}

	@Override
	public String toString() {
		return String.format("[%d/%d/%d in dimension %d]", pos.getX(), pos.getY(), pos.getZ(), dimensionId);
	}

	/**
	 * Returns an NBTTagCompound containing this warp point
	 */
	public NBTTagCompound writeToNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setIntArray("WarpPointData", new int[]{dimensionId, pos.getX(), pos.getY(), pos.getZ()});
		return tag;
	}

	/**
	 * Creates a new warp point from the NBTTagCompound
	 * @param tag	Must be the same tag format as returned by {@link #writeToNBT()} 
	 */
	public static WarpPoint readFromNBT(NBTTagCompound tag) {
		if (tag.hasKey("WarpPointData") && tag.getTag("WarpPointData").getId() == Constants.NBT.TAG_INT_ARRAY) {
			int[] data = tag.getIntArray("WarpPointData");
			return new WarpPoint(data[0], new BlockPos(data[1], data[2], data[3]));
		}
		return null;
	}
}

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

package zeldaswordskills.world.gen.structure;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;

/**
 * 
 * Stores information about generated Boss rooms
 *
 */
public class RoomGenData extends WorldSavedData
{
	private NBTTagCompound roomData = new NBTTagCompound();

	public RoomGenData(String tagName) {
		super(tagName);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		roomData = compound.getCompoundTag("ZSSDungeons");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setTag("ZSSDungeons", roomData);
	}

	/**
	 * Sets chunkX and chunkZ in the compound and adds it to room data with a unique identifier
	 */
	public void addRoomTag(NBTTagCompound compound, int chunkX, int chunkZ) {
		String s = createTagName(chunkX, chunkZ);
		compound.setInteger("chunkX", chunkX);
		compound.setInteger("chunkZ", chunkZ);
		this.roomData.setTag(s, compound);
	}

	/** Creates a unique tag name based on the chunk coordinates */
	public String createTagName(int chunkX, int chunkZ) {
		return "[" + chunkX + "," + chunkZ + "]";
	}

	public NBTTagCompound getRoomData() {
		return roomData;
	}
}

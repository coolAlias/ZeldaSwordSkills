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

package zeldaswordskills.network.bidirectional;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import zeldaswordskills.network.CustomPacket;
import zeldaswordskills.util.WorldUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Packet to begin or stop playing a record at a specific location.
 * 
 * When received on the server, it broadcasts to all nearby players, including the original player.
 * 
 * When received on the client, it begins or stops the record playing at the position given.
 *
 */
public class PlayRecordPacket extends CustomPacket
{
	private String record;
	private int x, y, z;

	public PlayRecordPacket() {}

	/**
	 * Plays or stops a record at the entity's position
	 * @param record	Send NULL to stop any currently playing record at the position
	 */
	public PlayRecordPacket(String record, Entity entity) {
		this(record, MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
	}

	/**
	 * Plays or stops a record at the x/y/z coordinates
	 * @param record	Send NULL to stop any currently playing record at the position
	 */
	public PlayRecordPacket(String record, int x, int y, int z) {
		this.record = record;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		record = (buffer.readByte() > 0 ? buffer.readUTF() : null);
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		buffer.writeByte(record == null ? (byte) 0 : (byte) 1);
		if (record != null) {
			buffer.writeUTF(record);
		}
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		if (side.isClient()) {
			player.worldObj.playRecord(record, x, y, z);
		} else {
			WorldUtils.sendPacketToAllAround(this.makePacket(), player.worldObj, player, 64.0D);
		}
	}
}

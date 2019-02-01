/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.bidirectional;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.network.AbstractMessage;
import zeldaswordskills.network.PacketDispatcher;

/**
 * 
 * Packet to begin or stop playing a record at a specific location.
 * 
 * When received on the server, it broadcasts to all nearby players, including the original player.
 * 
 * When received on the client, it begins or stops the record playing at the position given.
 *
 */
public class PlayRecordPacket extends AbstractMessage<PlayRecordPacket>
{
	private String record;
	private BlockPos pos;

	public PlayRecordPacket() {}

	/**
	 * Plays or stops a record at the entity's position
	 * @param record	Send NULL to stop any currently playing record at the position
	 */
	public PlayRecordPacket(String record, Entity entity) {
		this(record, new BlockPos(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)));
	}

	/**
	 * Plays or stops a record at the x/y/z coordinates
	 * @param record	Send NULL to stop any currently playing record at the position
	 */
	public PlayRecordPacket(String record, BlockPos pos) {
		this.record = record;
		this.pos = pos;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		record = (buffer.readByte() > 0 ? ByteBufUtils.readUTF8String(buffer) : null);
		this.pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeByte(record == null ? (byte) 0 : (byte) 1);
		if (record != null) {
			ByteBufUtils.writeUTF8String(buffer, record);
		}
		buffer.writeInt(this.pos.getX());
		buffer.writeInt(this.pos.getY());
		buffer.writeInt(this.pos.getZ());
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (side.isClient()) {
			player.worldObj.playRecord(pos, record);
		} else {
			PacketDispatcher.sendToAllAround(this, player, 64.0D);
		}
	}
}

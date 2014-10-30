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

package zeldaswordskills.network.packet.bidirectional;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import zeldaswordskills.network.PacketDispatcher;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Packet to begin or stop playing a record at a specific location.
 * 
 * When received on the server, it broadcasts to all nearby players, including the original player.
 * 
 * When received on the client, it begins or stops the record playing at the position given.
 *
 */
public class PlayRecordPacket implements IMessage
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
	public void fromBytes(ByteBuf buffer) {
		record = (buffer.readByte() > 0 ? ByteBufUtils.readUTF8String(buffer) : null);
		x = buffer.readInt();
		y = buffer.readInt();
		z = buffer.readInt();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(record == null ? (byte) 0 : (byte) 1);
		if (record != null) {
			ByteBufUtils.writeUTF8String(buffer, record);
		}
		buffer.writeInt(x);
		buffer.writeInt(y);
		buffer.writeInt(z);
	}

	public static class Handler extends AbstractBiMessageHandler<PlayRecordPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, PlayRecordPacket message, MessageContext ctx) {
			player.worldObj.playRecord(message.record, message.x, message.y, message.z);
			return null;
		}

		@Override
		public IMessage handleServerMessage(EntityPlayer player, PlayRecordPacket message, MessageContext ctx) {
			PacketDispatcher.sendToAllAround(message, player, 64.0D);
			return null;
		}
	}
}

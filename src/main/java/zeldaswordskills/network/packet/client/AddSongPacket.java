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

package zeldaswordskills.network.packet.client;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.SongNote;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Sent to client to sync songs when learned
 * 
 * Sent to server only for Scarecrow's Song, when notes set from GUI
 *
 */
public class AddSongPacket implements IMessage
{
	private ZeldaSong song;

	private List<SongNote> notes;

	public AddSongPacket() {}

	/**
	 * Sync song learned to client
	 */
	public AddSongPacket(ZeldaSong song) {
		this(song, null);
	}

	/**
	 * Sync Scarecrow's Song notes to server and back to client
	 */
	public AddSongPacket(ZeldaSong song, List<SongNote> notes) {
		this.song = song;
		this.notes = notes;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		song = ZeldaSong.values()[buffer.readByte() % ZeldaSong.values().length];
		int n = buffer.readByte();
		notes = (n > 0 ? new ArrayList<SongNote>() : null);
		for (int i = 0; i < n; ++i) {
			notes.add(SongNote.values()[buffer.readByte() % SongNote.values().length]);
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte((byte) song.ordinal());
		int n = (notes == null ? 0 : notes.size());
		buffer.writeByte((byte) n);
		for (int i = 0; i < n; ++i) {
			buffer.writeByte((byte) notes.get(i).ordinal());
		}
	}

	public static class Handler implements IMessageHandler<AddSongPacket, IMessage> {
		@Override
		public IMessage onMessage(AddSongPacket message, MessageContext ctx) {
			EntityPlayer player = ZSSMain.proxy.getPlayerEntity(ctx);
			// Only time this packet should be received by server is for learning the Scarecrow's Song
			if (!player.worldObj.isRemote && message.song != ZeldaSong.SCARECROW_SONG) {
				LogHelper.warning("AddSongPacket received on server with invalid song: " + message.song);
			} else {
				ZSSPlayerSongs.get(player).addNewSong(message.song, message.notes);
			}
			return null;
		}
	}
}

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
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.network.AbstractMessage;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.SongNote;

/**
 * 
 * Sent to client to sync songs when learned or removed
 * 
 * Sent to server when song learned from GUI; for Scarecrow's Song, notes are also sent
 *
 */
public class LearnSongPacket extends AbstractMessage<LearnSongPacket>
{
	private AbstractZeldaSong song;

	private List<SongNote> notes;

	private boolean remove;

	private boolean reset;

	public LearnSongPacket() {}

	/**
	 * Sync song learned to client or server
	 */
	public LearnSongPacket(AbstractZeldaSong song) {
		this(song, null);
	}

	/**
	 * Sync Scarecrow's Song notes to server and back to client
	 */
	public LearnSongPacket(AbstractZeldaSong song, List<SongNote> notes) {
		this.song = song;
		this.notes = notes;
	}

	/**
	 * Send notice to remove a specific song on the client side
	 */
	public LearnSongPacket(AbstractZeldaSong song, boolean remove) {
		this.song = song;
		this.remove = remove;
	}

	/**
	 * Send notice to remove all songs on the client side
	 */
	public LearnSongPacket(boolean reset) {
		this.reset = reset;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		reset = buffer.readBoolean();
		if (reset) {
			return; // done processing this packet
		}
		remove = buffer.readBoolean();
		String s = ByteBufUtils.readUTF8String(buffer);
		song = ZeldaSongs.getSongByName(s);
		if (song == null) {
			ZSSMain.logger.error("Invalid song name '" + s + "' read from packet!");
		}
		int n = buffer.readByte();
		notes = (n > 0 ? new ArrayList<SongNote>() : null);
		for (int i = 0; i < n; ++i) {
			notes.add(SongNote.values()[buffer.readByte() % SongNote.values().length]);
		}
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeBoolean(reset);
		if (reset) {
			return; // done
		}
		buffer.writeBoolean(remove);
		ByteBufUtils.writeUTF8String(buffer, song.getUnlocalizedName());
		int n = (notes == null ? 0 : notes.size());
		buffer.writeByte((byte) n);
		for (int i = 0; i < n; ++i) {
			buffer.writeByte((byte) notes.get(i).ordinal());
		}
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if ((reset || remove) && side.isServer()) {
			((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Sent invalid packet to server!");
			return;
		}
		if (reset) {
			ZSSPlayerSongs.get(player).resetKnownSongs();
		} else if (song != null) {
			if (remove) {
				ZSSPlayerSongs.get(player).removeSong(song);
			} else {
				ZSSPlayerSongs.get(player).learnSong(song, notes);
			}
		}
	}
}

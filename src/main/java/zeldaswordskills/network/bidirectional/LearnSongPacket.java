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

package zeldaswordskills.network.bidirectional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.AbstractMessage;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.SongNote;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Sent to client to sync songs when learned
 * 
 * Sent to server when song learned from GUI; for Scarecrow's Song, notes are also sent
 *
 */
public class LearnSongPacket extends AbstractMessage<LearnSongPacket>
{
	private ZeldaSong song;

	private List<SongNote> notes;

	public LearnSongPacket() {}

	/**
	 * Sync song learned to client or server
	 */
	public LearnSongPacket(ZeldaSong song) {
		this(song, null);
	}

	/**
	 * Sync Scarecrow's Song notes to server and back to client
	 */
	public LearnSongPacket(ZeldaSong song, List<SongNote> notes) {
		this.song = song;
		this.notes = notes;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		song = ZeldaSong.values()[buffer.readByte() % ZeldaSong.values().length];
		int n = buffer.readByte();
		notes = (n > 0 ? new ArrayList<SongNote>() : null);
		for (int i = 0; i < n; ++i) {
			notes.add(SongNote.values()[buffer.readByte() % SongNote.values().length]);
		}
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeByte((byte) song.ordinal());
		int n = (notes == null ? 0 : notes.size());
		buffer.writeByte((byte) n);
		for (int i = 0; i < n; ++i) {
			buffer.writeByte((byte) notes.get(i).ordinal());
		}
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ZSSPlayerSongs.get(player).learnSong(song, notes);
	}
}

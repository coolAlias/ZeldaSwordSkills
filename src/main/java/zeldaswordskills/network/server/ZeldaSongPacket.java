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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Sent to the server after a song has successfully completed playing to
 * perform the actual effects of the song.
 *
 */
public class ZeldaSongPacket extends CustomServerPacket
{
	private AbstractZeldaSong song;

	public ZeldaSongPacket() {}

	public ZeldaSongPacket(AbstractZeldaSong song) {
		this.song = song;
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		String s = buffer.readUTF();
		song = ZeldaSongs.getSongByName(s);
		if (song == null) {
			LogHelper.severe("Invalid song name '" + s + "' read from packet!");
		}
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		buffer.writeUTF(song.getUnlocalizedName());
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		if (song != null) {
			song.performSongEffects(player);
		}
	}
}

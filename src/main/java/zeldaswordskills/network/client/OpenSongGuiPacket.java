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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * This packet is sent from CommandGrantSong to open up the song-learning
 * GUI instead of granting the song directly.
 *
 */
public class OpenSongGuiPacket extends CustomClientPacket
{
	private AbstractZeldaSong song;

	public OpenSongGuiPacket() {}

	public OpenSongGuiPacket(AbstractZeldaSong song) {
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
		if (song != null && Minecraft.getMinecraft().inGameHasFocus) {
			ZSSPlayerSongs.get(player).songToLearn = song;
			player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
		}
	}
}

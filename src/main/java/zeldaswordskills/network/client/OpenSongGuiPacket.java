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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;

/**
 * 
 * This packet is sent from CommandGrantSong to open up the song-learning
 * GUI instead of granting the song directly.
 *
 */
public class OpenSongGuiPacket extends AbstractClientMessage<OpenSongGuiPacket>
{
	private AbstractZeldaSong song;

	public OpenSongGuiPacket() {}

	public OpenSongGuiPacket(AbstractZeldaSong song) {
		this.song = song;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		String s = ByteBufUtils.readUTF8String(buffer);
		song = ZeldaSongs.getSongByName(s);
		if (song == null) {
			ZSSMain.logger.error("Invalid song name '" + s + "' read from packet!");
		}
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		ByteBufUtils.writeUTF8String(buffer, song.getUnlocalizedName());
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (song != null && Minecraft.getMinecraft().inGameHasFocus) {
			ZSSPlayerSongs.get(player).songToLearn = song;
			player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, 0, 0, 0);
		}
	}
}

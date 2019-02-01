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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;

/**
 * 
 * Sent to the server after a song has successfully completed playing to
 * perform the actual effects of the song.
 *
 */
public class ZeldaSongPacket extends AbstractServerMessage<ZeldaSongPacket>
{
	private AbstractZeldaSong song;

	public ZeldaSongPacket() {}

	public ZeldaSongPacket(AbstractZeldaSong song) {
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
		if (song != null) {
			song.performSongEffects(player);
		}
	}
}

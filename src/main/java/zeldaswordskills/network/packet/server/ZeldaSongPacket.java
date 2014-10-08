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

package zeldaswordskills.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ref.ZeldaSong;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ZeldaSongPacket implements IMessage
{
	private ZeldaSong song;

	public ZeldaSongPacket() {}

	public ZeldaSongPacket(ZeldaSong song) {
		this.song = song;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.song = ZeldaSong.values()[buffer.readInt() % ZeldaSong.values().length];
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(song.ordinal());
	}

	public static class Handler extends AbstractServerMessageHandler<ZeldaSongPacket> {
		@Override
		public IMessage handleServerMessage(EntityPlayer player, ZeldaSongPacket message, MessageContext ctx) {
			// make sure player is holding an instrument to play a song
			// maybe also check if the GUI is open or not
			// ItemStack held = player.getHeldItem();
			//if (held != null && held.getItem() instanceof ItemInstrument) {
			// maybe do something with the instrument here
			message.song.playSong(player);
			//}
			return null;
		}
	}
}

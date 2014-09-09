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
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Send from the server to unpress a key (or all keys) on the client
 *
 */
public class UnpressKeyPacket implements IMessage
{
	/** Values for Left, Right, and Middle Mouse Buttons */
	public static final int LMB = -100, RMB = -99, MMB = -98;
	private int keyCode;

	public UnpressKeyPacket() {}

	public UnpressKeyPacket(int keyCode) {
		this.keyCode = keyCode;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		keyCode = buffer.readInt();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(keyCode);
	}

	public static class Handler extends AbstractClientMessageHandler<UnpressKeyPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, UnpressKeyPacket message, MessageContext ctx) {
			// KeyBinding kb = (KeyBinding) KeyBinding.hash.lookup(keyCode);
			if (message.keyCode != 0) { // kb != null
				KeyBinding.setKeyBindState(message.keyCode, false);
			} else {
				KeyBinding.unPressAllKeys();
			}
			return null;
		}
	}
}

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
import zeldaswordskills.ZSSMain;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Sets the player's attack time on either the client or the server
 *
 */
public class AttackTimePacket implements IMessage
{
	private int attackTime;

	public AttackTimePacket() {}

	public AttackTimePacket(int attackTime) {
		this.attackTime = attackTime;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.attackTime = buffer.readInt();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeInt(attackTime);
	}

	public static class Handler implements IMessageHandler<AttackTimePacket, IMessage> {
		@Override
		public IMessage onMessage(AttackTimePacket message, MessageContext ctx) {
			ZSSMain.proxy.getPlayerEntity(ctx).attackTime = message.attackTime;
			return null;
		}
	}
}

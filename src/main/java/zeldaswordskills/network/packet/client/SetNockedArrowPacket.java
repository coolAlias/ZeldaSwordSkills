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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.ZSSPlayerInfo;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Updates the currently nocked arrow on the client, since DataWatcher
 * is apparently unable to handle NULL values.
 * 
 * Required for the Hero's Bow to fire without the graphical glitch
 * caused by writing to NBT.
 *
 */
public class SetNockedArrowPacket implements IMessage
{
	private ItemStack arrowStack;

	public SetNockedArrowPacket() {}

	public SetNockedArrowPacket(ItemStack arrowStack) {
		this.arrowStack = arrowStack;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(arrowStack == null ? (byte) 0 : (byte) 1);
		if (arrowStack != null) {
			ByteBufUtils.writeItemStack(buffer, arrowStack);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		arrowStack = (buffer.readByte() > 0 ? ByteBufUtils.readItemStack(buffer) : null);
	}

	public static class Handler extends AbstractClientMessageHandler<SetNockedArrowPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, SetNockedArrowPacket message, MessageContext ctx) {
			if (message.arrowStack != null) {
				ZSSPlayerInfo.get(player).setBorrowedMask(message.arrowStack.getItem());
			}
			return null;
		}
	}
}

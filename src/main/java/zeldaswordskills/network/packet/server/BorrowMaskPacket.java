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
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Sent from the Mask Trader's GUI with the mask the player borrowed.
 *
 */
public class BorrowMaskPacket implements IMessage
{
	private ItemStack mask;

	public BorrowMaskPacket() {}

	public BorrowMaskPacket(ItemStack mask) {
		this.mask = mask;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(mask == null ? (byte) 0 : (byte) 1);
		if (mask != null) {
			ByteBufUtils.writeItemStack(buffer, mask);
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		mask = (buffer.readByte() > 0 ? ByteBufUtils.readItemStack(buffer) : null);
	}

	public static class Handler extends AbstractServerMessageHandler<BorrowMaskPacket> {
		@Override
		public IMessage handleServerMessage(EntityPlayer player, BorrowMaskPacket message, MessageContext ctx) {
			if (message.mask != null) {
				PlayerUtils.addItemToInventory(player, message.mask);
				ZSSPlayerInfo.get(player).setBorrowedMask(message.mask.getItem());
			}
			return null;
		}
	}
}

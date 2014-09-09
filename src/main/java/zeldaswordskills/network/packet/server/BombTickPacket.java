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
import zeldaswordskills.item.ItemBomb;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class BombTickPacket implements IMessage {

	public BombTickPacket() {}

	@Override
	public void toBytes(ByteBuf buffer) {}

	@Override
	public void fromBytes(ByteBuf buffer) {}

	public static class Handler extends AbstractServerMessageHandler<BombTickPacket> {
		@Override
		public IMessage handleServerMessage(EntityPlayer player, BombTickPacket message, MessageContext ctx) {
			ItemStack held = player.getHeldItem();
			if (held != null && held.getItem() instanceof ItemBomb) {
				((ItemBomb) held.getItem()).tickBomb(held, player.worldObj, player);
			}
			return null;
		}
	}
}

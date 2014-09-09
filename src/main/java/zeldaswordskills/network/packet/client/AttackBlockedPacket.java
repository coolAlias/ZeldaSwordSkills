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
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerInfo;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class AttackBlockedPacket implements IMessage
{
	/** Stores the shield ItemStack that was used to block */
	private NBTTagCompound compound;

	public AttackBlockedPacket() {}

	public AttackBlockedPacket(ItemStack shield) {
		compound = new NBTTagCompound();
		shield.writeToNBT(compound);
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, compound);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		compound = ByteBufUtils.readTag(buffer);
	}

	public static class Handler extends AbstractClientMessageHandler<AttackBlockedPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, AttackBlockedPacket message, MessageContext ctx) {
			ItemStack shield = ItemStack.loadItemStackFromNBT(message.compound);
			ZSSPlayerInfo.get(player).onAttackBlocked(shield, 0.0F);
			return null;
		}
	}
}

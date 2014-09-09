/**
    Copyright (C) <2014> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed buffer the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.BuffBase;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Updates a buff on the client side, either adding or removing it from the activeBuffs map
 *
 */
public class UpdateBuffPacket implements IMessage
{
	/** The buff to be applied or removed */
	private BuffBase buff;

	/** Whether to apply or remove the specified buff */
	private boolean remove;

	public UpdateBuffPacket() {}

	public UpdateBuffPacket(BuffBase buff, boolean remove) {
		this.buff = buff;
		this.remove = remove;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, buff.writeToNBT(new NBTTagCompound()));
		buffer.writeBoolean(remove);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		this.buff = BuffBase.readFromNBT(ByteBufUtils.readTag(buffer));
		this.remove = buffer.readBoolean();
	}

	public static class Handler extends AbstractClientMessageHandler<UpdateBuffPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, UpdateBuffPacket message, MessageContext ctx) {
			if (message.remove) {
				ZSSEntityInfo.get(player).getActiveBuffsMap().remove(message.buff.getBuff());
			} else {
				ZSSEntityInfo.get(player).getActiveBuffsMap().put(message.buff.getBuff(), message.buff);
			}
			return null;
		}
	}
}

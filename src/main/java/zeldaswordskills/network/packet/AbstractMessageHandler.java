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

package zeldaswordskills.network.packet;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Base message handler class using SimpleNetworkWrapper / IMessage framework;
 * its sole purpose is to call function based on side and pass a player object.
 * 
 */
public abstract class AbstractMessageHandler<T extends IMessage> implements IMessageHandler <T, IMessage>
{
	/**
	 * Handle a message received on the client side
	 * @return a message to send back to the Server, or null if no reply is necessary
	 */
	@SideOnly(Side.CLIENT)
	public abstract IMessage handleClientMessage(EntityPlayer player, T msg, MessageContext ctx);

	/**
	 * Handle a message received on the server side
	 * @return a message to send back to the Client, or null if no reply is necessary
	 */
	public abstract IMessage handleServerMessage(EntityPlayer player, T msg, MessageContext ctx);

	@Override
	public IMessage onMessage(T msg, MessageContext ctx) {
		EntityPlayer player = ZSSMain.proxy.getPlayerEntity(ctx);
		if (player == null) {
			LogHelper.severe("Unable to process " + msg.getClass().getSimpleName() + " on " + ctx.side.name() + ": player was NULL");
			return null;
		}
		if (ctx.side.isClient()) {
			return handleClientMessage(player, msg, ctx);
		} else {
			return handleServerMessage(player, msg, ctx);
		}
	}
}

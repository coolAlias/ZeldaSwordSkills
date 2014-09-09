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
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ZSSItems;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Getting around derpiness of Minecraft: client-side world.getMaterial returns
 * incorrect value and is unable to detect liquids until player is submerged,
 * regardless of position parameters passed.
 * 
 * This packet should be received on the client side when the player is standing
 * in liquid wearing Heavy Boots; use to increase velocity.
 *
 */
public class InLiquidPacket implements IMessage
{
	/** If this is true, the magnitude of velocity increase will be higher*/
	private boolean inLava;

	public InLiquidPacket() {}

	public InLiquidPacket(boolean inLava) {
		this.inLava = inLava;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeBoolean(inLava);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		inLava = buffer.readBoolean();
	}

	public static class Handler extends AbstractClientMessageHandler<InLiquidPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, InLiquidPacket message, MessageContext ctx) {
			if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS) != null && player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsHeavy) {
				double d = (message.inLava ? 1.75D : 1.125D);
				if (player.onGround) {
					player.motionX *= d;
					player.motionZ *= d;
				} else if (player.motionY < 0 && !Minecraft.getMinecraft().gameSettings.keyBindJump.getIsKeyPressed()) {
					player.motionY *= 1.5;
					if (player.motionY < -0.35D) {
						player.motionY = -0.35D;
					}
				}
			}
			return null;
		}
	}
}

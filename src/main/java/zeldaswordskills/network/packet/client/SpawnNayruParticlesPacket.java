/**
    Copyright (C) <2014> <coolAlias>

    @author original credits go to diesieben07

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
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Spawns the particles for Naryu's Love on client worlds
 *
 */
public class SpawnNayruParticlesPacket implements IMessage
{
	/** Affected player's position */
	private double x, y, z;

	/** Affected player's height and width */
	private float h, w;

	public SpawnNayruParticlesPacket() {}

	public SpawnNayruParticlesPacket(EntityPlayer player) {
		x = player.posX;
		y = player.posY;
		z = player.posZ;
		h = player.height;
		w = player.width;
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
		buffer.writeFloat(h);
		buffer.writeFloat(w);
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
		h = buffer.readFloat();
		w = buffer.readFloat();
	}

	public static class Handler extends AbstractClientMessageHandler<SpawnNayruParticlesPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, SpawnNayruParticlesPacket message, MessageContext ctx) {
			for (int i = 0; i < 3; ++i) {
				double d0 = player.worldObj.rand.nextGaussian() * 0.02D;
				double d1 = player.worldObj.rand.nextGaussian() * 0.02D;
				double d2 = player.worldObj.rand.nextGaussian() * 0.02D;
				player.worldObj.spawnParticle("magicCrit",
						message.x + (player.worldObj.rand.nextFloat() * message.w * 2.0F) -	message.w,
						message.y + (player.worldObj.rand.nextFloat() * message.h),
						message.z + (player.worldObj.rand.nextFloat() * message.w * 2.0F) - message.w, d0, d1, d2);
			}
			return null;
		}
	}
}

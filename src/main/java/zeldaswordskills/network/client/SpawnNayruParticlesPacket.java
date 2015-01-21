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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Spawns the particles for Naryu's Love on client worlds
 *
 */
public class SpawnNayruParticlesPacket extends AbstractClientMessage
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
	protected void read(PacketBuffer buffer) throws IOException {
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
		h = buffer.readFloat();
		w = buffer.readFloat();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
		buffer.writeFloat(h);
		buffer.writeFloat(w);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		for (int i = 0; i < 3; ++i) {
			double d0 = player.worldObj.rand.nextGaussian() * 0.02D;
			double d1 = player.worldObj.rand.nextGaussian() * 0.02D;
			double d2 = player.worldObj.rand.nextGaussian() * 0.02D;
			player.worldObj.spawnParticle("magicCrit",
					x + (player.worldObj.rand.nextFloat() * w * 2.0F) -	w,
					y + (player.worldObj.rand.nextFloat() * h),
					z + (player.worldObj.rand.nextFloat() * w * 2.0F) - w, d0, d1, d2);
		}
	}
}

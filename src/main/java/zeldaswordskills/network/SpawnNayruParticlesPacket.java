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

package zeldaswordskills.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Spawns the particles for Naryu's Love on client worlds
 *
 */
public class SpawnNayruParticlesPacket extends CustomPacket
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
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
		out.writeFloat(h);
		out.writeFloat(w);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
		h = in.readFloat();
		w = in.readFloat();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			for (int i = 0; i < 3; ++i) {
				double d0 = player.worldObj.rand.nextGaussian() * 0.02D;
				double d1 = player.worldObj.rand.nextGaussian() * 0.02D;
				double d2 = player.worldObj.rand.nextGaussian() * 0.02D;
				player.worldObj.spawnParticle("magicCrit", x + (double)(player.worldObj.rand.nextFloat() * w * 2.0F) -
						(double) w, y + (double)(player.worldObj.rand.nextFloat() * h),
						z + (double)(player.worldObj.rand.nextFloat() * w * 2.0F) - (double) w, d0, d1, d2);
			}
		} else {
			throw new ProtocolException("Particle packets may only be sent to clients");
		}
	}
}

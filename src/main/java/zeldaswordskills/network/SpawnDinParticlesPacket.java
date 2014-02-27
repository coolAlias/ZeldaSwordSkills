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

package zeldaswordskills.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.item.ItemSpiritCrystal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Specific packet due to the high number of particles to spawn and large radius
 *
 */
public class SpawnDinParticlesPacket extends CustomPacket
{
	/** Center of AoE */
	private double x, y, z;
	/** Radius in which to spawn the particles */
	private float r;

	public SpawnDinParticlesPacket() {}
	
	public SpawnDinParticlesPacket(EntityPlayer player, float radius) {
		this(player.posX, player.posY, player.posZ, radius);
	}
	
	public SpawnDinParticlesPacket(double posX, double posY, double posZ, float radius) {
		x = posX;
		y = posY;
		z = posZ;
		r = radius;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
		out.writeFloat(r);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
		r = in.readFloat();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			ItemSpiritCrystal.spawnDinParticles(player.worldObj, x, y, z, r);
		} else {
			throw new ProtocolException("Particle packets may only be sent to clients");
		}
	}
}

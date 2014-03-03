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
import net.minecraft.item.Item;
import zeldaswordskills.item.ISpawnParticles;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Packet that calls a specific ISpawnParticles method in the Item class, allowing
 * each Item to handle its own particle algorithm individually yet spawn them in
 * all client worlds
 *
 */
public class ISpawnParticlesPacket extends CustomPacket
{
	/** The Item class which will spawn the particles; must implement ISpawnParticles */
	private Item item;
	/** Center of AoE */
	private double x, y, z;
	/** Radius in which to spawn the particles */
	private float r;

	public ISpawnParticlesPacket() {}
	
	public ISpawnParticlesPacket(EntityPlayer player, Item item, float radius) {
		this(item, player.posX, player.posY, player.posZ, radius);
	}
	
	public ISpawnParticlesPacket(Item item, double posX, double posY, double posZ, float radius) {
		this.item = item;
		x = posX;
		y = posY;
		z = posZ;
		r = radius;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(item.itemID);
		out.writeDouble(x);
		out.writeDouble(y);
		out.writeDouble(z);
		out.writeFloat(r);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		item = Item.itemsList[in.readInt()];
		x = in.readDouble();
		y = in.readDouble();
		z = in.readDouble();
		r = in.readFloat();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			if (item instanceof ISpawnParticles) {
				((ISpawnParticles) item).spawnParticles(player.worldObj, x, y, z, r);
			}
		} else {
			throw new ProtocolException("Particle packets may only be sent to clients");
		}
	}
}

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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Vec3;
import zeldaswordskills.item.ISpawnParticles;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Packet that calls a specific ISpawnParticles method in the Item class, allowing
 * each Item to handle its own particle algorithm individually yet spawn them in
 * all client worlds
 *
 */
public class PacketISpawnParticles extends AbstractClientMessage<PacketISpawnParticles>
{
	/** The Item class which will spawn the particles; must implement ISpawnParticles */
	private Item item;

	/** Center of AoE */
	private double x, y, z;

	/** Radius buffer which to spawn the particles */
	private float r;

	/** Storage for the normalized look vector of the original player */
	private double lookX, lookY, lookZ;

	public PacketISpawnParticles() {}

	public PacketISpawnParticles(EntityPlayer player, Item item, float radius) {
		this.item = item;
		x = player.posX;
		y = player.posY;
		z = player.posZ;
		r = radius;
		lookX = player.getLookVec().xCoord;
		lookY = player.getLookVec().yCoord;
		lookZ = player.getLookVec().zCoord;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		item = Item.getItemById(buffer.readInt());
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
		r = buffer.readFloat();
		lookX = buffer.readDouble();
		lookY = buffer.readDouble();
		lookZ = buffer.readDouble();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(Item.getIdFromItem(item));
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
		buffer.writeFloat(r);
		buffer.writeDouble(lookX);
		buffer.writeDouble(lookY);
		buffer.writeDouble(lookZ);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (item instanceof ISpawnParticles) {
			Vec3 vec3 = Vec3.createVectorHelper(lookX, lookY, lookZ);
			((ISpawnParticles) item).spawnParticles(player.worldObj, x, y, z, r, vec3);
		}
	}
}

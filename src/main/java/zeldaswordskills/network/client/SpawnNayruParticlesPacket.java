/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

/**
 * 
 * Spawns the particles for Naryu's Love on client worlds
 *
 */
public class SpawnNayruParticlesPacket extends AbstractClientMessage<SpawnNayruParticlesPacket>
{
	/** Affected entity's entityId */
	private int entityId;

	/** Affected entity's position */
	private double x, y, z;

	/** Affected entity's height and width */
	private float h, w;

	public SpawnNayruParticlesPacket() {}

	public SpawnNayruParticlesPacket(Entity entity) {
		entityId = entity.getEntityId();
		x = entity.posX;
		y = entity.posY;
		z = entity.posZ;
		h = entity.height;
		w = entity.width;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		entityId = buffer.readInt();
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
		h = buffer.readFloat();
		w = buffer.readFloat();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(entityId);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
		buffer.writeFloat(h);
		buffer.writeFloat(w);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		Entity entity = player.worldObj.getEntityByID(entityId);
		SpawnNayruParticlesPacket.spawnParticles(entity, x, y, z, h, w);
	}

	@SideOnly(Side.CLIENT)
	public static void spawnParticles(Entity entity, double x, double y, double z, float h, float w) {
		for (int i = 0; i < 3; ++i) {
			double d0 = entity.worldObj.rand.nextGaussian() * 0.02D;
			double d1 = entity.worldObj.rand.nextGaussian() * 0.02D;
			double d2 = entity.worldObj.rand.nextGaussian() * 0.02D;
			entity.worldObj.spawnParticle(EnumParticleTypes.CRIT_MAGIC,
					x + (entity.worldObj.rand.nextFloat() * w * 2.0F) -	w,
					y + (entity.worldObj.rand.nextFloat() * h),
					z + (entity.worldObj.rand.nextFloat() * w * 2.0F) - w, d0, d1, d2);
		}
	}
}

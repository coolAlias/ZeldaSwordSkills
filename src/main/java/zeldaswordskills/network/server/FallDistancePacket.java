/**
    Copyright (C) <2015> <coolAlias>

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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Updates fall distance from client side, such as while swinging with the whip.
 *
 */
public class FallDistancePacket extends CustomServerPacket
{
	/** This entity's fall distance will be modified */
	private int entityId;

	/** The amount to modify the fall distance, either negative or positive; 0.0F exactly sets fall distance to zero */
	private float fallMod;

	public FallDistancePacket() {}

	public FallDistancePacket(Entity entity, float fallMod) {
		this.entityId = entity.entityId;
		this.fallMod = fallMod;
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		entityId = in.readInt();
		fallMod = in.readFloat();
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(entityId);
		out.writeFloat(fallMod);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		Entity entity = (entityId == player.entityId ? player : player.worldObj.getEntityByID(entityId));
		if (entity != null) {
			if (fallMod == 0.0F) {
				entity.fallDistance = 0.0F;
			} else {
				entity.fallDistance += fallMod;
			}
		}
	}
}

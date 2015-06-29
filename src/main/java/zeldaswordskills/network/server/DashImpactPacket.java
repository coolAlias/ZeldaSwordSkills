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
import net.minecraft.util.EnumMovingObjectType;
import net.minecraft.util.MovingObjectPosition;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Informs the server about that the player has impacted either a block or an entity
 * while using Dash, and calls {@link Dash#onImpact}, thereby handling and terminating
 * the server side instance of the skill.
 * 
 * This is a workaround for the server having difficulties detecting impacts, at least
 * from the viewpoint of the player; whereas the client impact detection reflects perfectly
 * what the user would expect based on what they see, the server fails to detect entity
 * collisions and usually collides with blocks that the player is standing on, which is
 * weird, considering it works fine for entities like arrows. Hm.
 * 
 * Also need to send the player's motionX and motionZ, as the server values are typically zero.
 *
 */
public class DashImpactPacket extends CustomServerPacket
{
	/** Stores the player's motionX and motionZ, since the server values are unreliable */
	private double motionX, motionZ;

	/** Stores the type of hit, as a byte (0: None 1: BLOCK 2: ENTITY) */
	private byte hitType;

	/** Stores the entity's ID until it can be retrieved from the world during handling */
	private int entityId;

	public DashImpactPacket() {}

	/**
	 * Creates dash packet with given moving object position
	 * @param mop Must not be null
	 */
	public DashImpactPacket(EntityPlayer player, MovingObjectPosition mop) {
		this.motionX = player.motionX;
		this.motionZ = player.motionZ;
		this.hitType = (mop != null ? (byte) mop.typeOfHit.ordinal() : (byte) 0);
		if (this.hitType == EnumMovingObjectType.ENTITY.ordinal()) {
			this.entityId = mop.entityHit.entityId;
		}
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeDouble(motionX);
		out.writeDouble(motionZ);
		out.writeByte(hitType);
		if (hitType == EnumMovingObjectType.ENTITY.ordinal()) {
			out.writeInt(entityId);
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		motionX = in.readDouble();
		motionZ = in.readDouble();
		hitType = in.readByte();
		if (hitType == EnumMovingObjectType.ENTITY.ordinal()) {
			entityId = in.readInt();
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		Dash dash = (Dash) ZSSPlayerSkills.get(player).getActiveSkill(SkillBase.dash);
		if (dash != null && dash.isActive()) {
			MovingObjectPosition mop = null;
			if (hitType == EnumMovingObjectType.ENTITY.ordinal()) {
				Entity entityHit = player.worldObj.getEntityByID(entityId);
				if (entityHit != null) {
					mop = new MovingObjectPosition(entityHit);
				} else {
					LogHelper.warning("Could not retrieve valid entity for MovingObjectPosition while handling Dash Packet!");
				}
			}
			player.motionX = motionX;
			player.motionZ = motionZ;
			dash.onImpact(player.worldObj, player, mop);
		}
	}
}

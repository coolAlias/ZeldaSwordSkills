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

package zeldaswordskills.network.packet.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

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
public class DashImpactPacket implements IMessage
{
	/** Stores the player's motionX and motionZ, since the server values are unreliable */
	//private double motionX, motionZ;

	/** The moving object position as determined by the client */
	//private MovingObjectPosition mop;

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
		//this.motionX = player.motionX;
		//this.motionZ = player.motionZ;
		this.hitType = (mop != null ? (byte) mop.typeOfHit.ordinal() : (byte) 0);
		if (this.hitType == MovingObjectType.ENTITY.ordinal()) {
			this.entityId = mop.entityHit.getEntityId();
		}
		//this.mop = mop;
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		//motionX = buffer.readDouble();
		//motionZ = buffer.readDouble();
		hitType = buffer.readByte();
		if (hitType == MovingObjectType.ENTITY.ordinal()) {
			entityId = buffer.readInt();
		}
		/*
			// Don't need a valid MOP for block hits, only entities
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			int side = buffer.readInt();
		}
		 */
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		//buffer.writeDouble(motionX);
		//buffer.writeDouble(motionZ);
		buffer.writeByte(hitType);
		if (hitType == MovingObjectType.ENTITY.ordinal()) {
			buffer.writeInt(entityId);
		}
		/*
		if (mop != null) {
			// None of this data is needed for Dash to process the impact
			buffer.writeInt(mop.blockX);
			buffer.writeInt(mop.blockY);
			buffer.writeInt(mop.blockZ);
			buffer.writeInt(mop.sideHit);
			buffer.writeDouble(mop.hitVec.xCoord);
			buffer.writeDouble(mop.hitVec.yCoord);
			buffer.writeDouble(mop.hitVec.zCoord);
		*/
	}

	public static class Handler extends AbstractServerMessageHandler<DashImpactPacket> {
		@Override
		public IMessage handleServerMessage(EntityPlayer player, DashImpactPacket message, MessageContext ctx) {
			Dash dash = (Dash) ZSSPlayerSkills.get(player).getActiveSkill(SkillBase.dash);
			if (dash != null && dash.isActive()) {
				MovingObjectPosition mop = null;
				if (message.hitType == MovingObjectType.ENTITY.ordinal()) {
					Entity entityHit = player.worldObj.getEntityByID(message.entityId);
					if (entityHit != null) {
						mop = new MovingObjectPosition(entityHit);
					} else {
						LogHelper.warning("Could not retrieve valid entity for MovingObjectPosition while handling Dash Packet!");
					}
				}
				//player.motionX = message.motionX;
				//player.motionZ = message.motionZ;
				dash.onImpact(player.worldObj, player, mop);
			} //else {
				// this happens rarely, e.g. when impacting on the very last client active tick
				// LogHelper.warning("Dash skill was either null or not active when receiving Dash Packet!"); 
			//}
			return null;
		}
	}
}

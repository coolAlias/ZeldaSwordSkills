/**
    Copyright (C) <2015> <coolAlias>

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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.Dash;
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
public class DashImpactPacket extends AbstractServerMessage<DashImpactPacket>
{
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
		this.hitType = (byte) mop.typeOfHit.ordinal();
		if (this.hitType == MovingObjectType.ENTITY.ordinal()) {
			this.entityId = mop.entityHit.getEntityId();
		}
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		hitType = buffer.readByte();
		if (hitType == MovingObjectType.ENTITY.ordinal()) {
			entityId = buffer.readInt();
		}
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeByte(hitType);
		if (hitType == MovingObjectType.ENTITY.ordinal()) {
			buffer.writeInt(entityId);
		}
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		Dash dash = (Dash) ZSSPlayerSkills.get(player).getActiveSkill(SkillBase.dash);
		if (dash != null && dash.isActive()) {
			MovingObjectPosition mop = null;
			if (hitType == MovingObjectType.ENTITY.ordinal()) {
				Entity entityHit = player.worldObj.getEntityByID(entityId);
				if (entityHit != null) {
					mop = new MovingObjectPosition(entityHit);
				} else {
					ZSSMain.logger.warn("Could not retrieve valid entity for MovingObjectPosition while handling Dash Packet!");
				}
			}
			dash.onImpact(player.worldObj, player, mop);
		}
	}
}

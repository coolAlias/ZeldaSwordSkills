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
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * This packet updates the server with the current target for currently active ILockOnTarget skill.
 *
 */
public class TargetIdPacket implements IMessage
{
	/** Id of ILockOnTarget skill */
	private byte skillId;

	/** Current target from ILockOnTarget skill */
	private Entity entity;

	/** Entity Id of above entity */
	private int entityId;

	/** True if current target entity is null */
	boolean isNull = false;

	public TargetIdPacket() {}

	/**
	 * Constructs packet that will update the provided skill's current target on the server
	 * @throws IllegalArgumentException if SkillBase is not an instance of ILockOnTarget
	 */
	public TargetIdPacket(SkillBase skill) throws IllegalArgumentException {
		if (skill instanceof ILockOnTarget) {
			this.skillId = skill.getId();
			this.entity = ((ILockOnTarget) skill).getCurrentTarget();
		} else {
			throw new IllegalArgumentException("Parameter 'skill' must be an instance of ILockOnTarget while constructing TargetIdPacket");
		}
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		if (buffer.readByte() == 1) {
			this.skillId = buffer.readByte();
			this.entityId = buffer.readInt();
		} else {
			this.isNull = true;
		}
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		if (entity != null) {
			buffer.writeByte((byte) 1);
			buffer.writeByte(skillId);
			buffer.writeInt(entity.getEntityId());
		} else {
			buffer.writeByte((byte) 0);
		}
	}

	public static class Handler extends AbstractServerMessageHandler<TargetIdPacket> {
		@Override
		public IMessage handleServerMessage(EntityPlayer player, TargetIdPacket message, MessageContext ctx) {
			ILockOnTarget skill = ZSSPlayerSkills.get(player).getTargetingSkill();
			if (skill != null) {
				if (message.isNull) {
					skill.setCurrentTarget(null);
				} else {
					Entity entity = player.worldObj.getEntityByID(message.entityId);
					skill.setCurrentTarget(entity);
					if (entity == null) { // For some reason the target id is sometimes incorrect or out of date
						LogHelper.warning("Invalid target; entity with id " + message.entityId + " is null");
					}
				}
			}
			return null;
		}
	}
}

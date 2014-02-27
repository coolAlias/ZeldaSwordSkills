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
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * This packet updates the server with the current target for currently active ILockOnTarget skill.
 *
 */
public class TargetIdPacket extends CustomPacket
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
			this.skillId = skill.id;
			this.entity = ((ILockOnTarget) skill).getCurrentTarget();
		} else {
			throw new IllegalArgumentException("Parameter 'skill' must be an instance of ILockOnTarget while constructing TargetIdPacket");
		}
	}
	
	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		if (entity != null) {
			out.writeByte((byte) 1);
			out.writeByte(skillId);
			out.writeInt(entity.entityId);
		} else {
			out.writeByte((byte) 0);
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		if (in.readByte() == 1) {
			this.skillId = in.readByte();
			this.entityId = in.readInt();
		} else {
			this.isNull = true;
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			ILockOnTarget skill = ZSSPlayerInfo.get(player).getTargetingSkill();
			if (skill != null) {
				if (!this.isNull) {
					Entity entity = player.worldObj.getEntityByID(this.entityId);
					if (entity != null) {
						skill.setCurrentTarget(side, entity);
					} else {
						// For some reason the target id is sometimes incorrect or out of date
						skill.setCurrentTarget(side, (Entity) null);
						LogHelper.log(Level.WARNING,"Invalid target; entity with id " + this.entityId + " is null");
					}
				} else {
					skill.setCurrentTarget(side, (Entity) null);
				}
			} else {
				throw new ProtocolException("Target Id Packet can only be sent to players with active ILockOnTarget skill");
			}
		} else {
			throw new ProtocolException("Target Id Packet may only be sent to the server");
		}
	}
}

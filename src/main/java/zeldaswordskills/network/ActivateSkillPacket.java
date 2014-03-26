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
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.skills.SkillBase;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Attempts to activate a skill for player. When activated on the server, a packet is automatically
 * sent to the client, so skills shouldn't be manually activated client side.
 *
 */
public class ActivateSkillPacket extends CustomPacket
{
	/** If true, calls triggerSkill(), otherwise uses activateSkill() */
	private boolean wasTriggered = false;
	
	/** Skill to activate */
	private SkillBase skill;
	
	public ActivateSkillPacket() {}

	public ActivateSkillPacket(SkillBase skill) {
		this(skill, false);
	}

	public ActivateSkillPacket(SkillBase skill, boolean wasTriggered) {
		this.wasTriggered = wasTriggered;
		this.skill = skill;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeBoolean(this.wasTriggered);
		out.writeByte(skill.getId());
	}

	@Override
	public void process(ByteArrayDataInput in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		if (ZSSPlayerInfo.get(player) != null) {
			if (in.readBoolean()) {
				ZSSPlayerInfo.get(player).triggerSkill(player.worldObj, in.readByte());
			} else {
				ZSSPlayerInfo.get(player).activateSkill(player.worldObj, in.readByte());
			}
		} else {
			throw new ProtocolException("No Skills section");
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {}
}

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

package zeldaswordskills.network.bidirectional;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.CustomPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Send from the client when activating a skill (such as from {@link SkillActive#keyPressed});
 * if the server determines that it is allowed, the skill will be activated and this packet
 * sent back to the client to activate the skill on the client side as well.
 * 
 * See {@link SkillActive#activate} and {@link SkillActive#trigger}.
 * 
 */
public class ActivateSkillPacket extends CustomPacket
{
	/** If true, calls {@link ZSSPlayerInfo#triggerSkill}, otherwise uses {@link ZSSPlayerInfo#activateSkill} */
	private boolean wasTriggered = false;
	
	private byte skillId;
	
	public ActivateSkillPacket() {}

	public ActivateSkillPacket(SkillBase skill) {
		this(skill, false);
	}

	public ActivateSkillPacket(SkillBase skill, boolean wasTriggered) {
		this.wasTriggered = wasTriggered;
		this.skillId = skill.getId();
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeBoolean(this.wasTriggered);
		out.writeByte(skillId);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		wasTriggered = in.readBoolean();
		skillId = in.readByte();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (ZSSPlayerSkills.get(player) != null) {
			if (wasTriggered) {
				ZSSPlayerSkills.get(player).triggerSkill(player.worldObj, skillId);
			} else {
				ZSSPlayerSkills.get(player).activateSkill(player.worldObj, skillId);
			}
		} else {
			throw new ProtocolException("No skills section");
		}
	}
}

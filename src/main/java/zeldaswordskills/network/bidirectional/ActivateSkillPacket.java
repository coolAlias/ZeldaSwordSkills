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
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.network.AbstractMessage;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
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
public class ActivateSkillPacket extends AbstractMessage<ActivateSkillPacket>
{
	/** If true, calls {@link ZSSPlayerSkills#triggerSkill}, otherwise uses {@link ZSSPlayerSkills#activateSkill} */
	private boolean wasTriggered = false;

	/** Skill to activate */
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
	protected void read(PacketBuffer buffer) throws IOException {
		wasTriggered = buffer.readBoolean();
		skillId = buffer.readByte();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeBoolean(wasTriggered);
		buffer.writeByte(skillId);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		// handled identically on both sides
		if (wasTriggered) {
			ZSSPlayerSkills.get(player).triggerSkill(player.worldObj, skillId);
		} else {
			ZSSPlayerSkills.get(player).activateSkill(player.worldObj, skillId);
		}
	}
}

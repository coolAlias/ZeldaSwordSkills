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
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Send to either side to {@link SkillActive#deactivate deactivate} a skill.
 *
 */
public class DeactivateSkillPacket extends CustomPacket
{
	/** Skill to deactivate */
	private byte skillId;

	public DeactivateSkillPacket() {}

	public DeactivateSkillPacket(SkillActive skill) {
		this.skillId = skill.getId();
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeByte(skillId);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		skillId = in.readByte();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (ZSSPlayerInfo.get(player) != null) {
			SkillBase skill = ZSSPlayerInfo.get(player).getPlayerSkill(skillId);
			if (skill instanceof SkillActive) {
				((SkillActive) skill).deactivate(player);
			} else {
				LogHelper.warning("Error processing DeactivateSkillPacket for " + player + "; skill with ID " + skillId + " was not valid for this player.");
			}
		}
	}
}

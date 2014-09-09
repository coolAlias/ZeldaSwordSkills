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

package zeldaswordskills.network.packet.bidirectional;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Send to either side to {@link SkillActive#deactivate deactivate} a skill.
 *
 */
public class DeactivateSkillPacket implements IMessage
{
	/** Skill to deactivate */
	private byte skillId;

	public DeactivateSkillPacket() {}

	public DeactivateSkillPacket(SkillActive skill) {
		this.skillId = skill.getId();
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		skillId = buffer.readByte();
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		buffer.writeByte(skillId);
	}

	public static class Handler implements IMessageHandler<DeactivateSkillPacket, IMessage> {
		@Override
		public IMessage onMessage(DeactivateSkillPacket message, MessageContext ctx) {
			EntityPlayer player = ZSSMain.proxy.getPlayerEntity(ctx);
			if (ZSSPlayerSkills.get(player) != null) {
				SkillBase skill = ZSSPlayerSkills.get(player).getPlayerSkill(message.skillId);
				if (skill instanceof SkillActive) {
					((SkillActive) skill).deactivate(player);
				} else {
					LogHelper.warning("Error processing DeactivateSkillPacket for " + player + "; skill with ID " + message.skillId + " was not valid for this player.");
				}
			}
			return null;
		}
	}
}

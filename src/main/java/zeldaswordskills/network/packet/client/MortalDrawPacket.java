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

package zeldaswordskills.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.client.ZSSClientEvents;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.MortalDraw;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Sent to client upon successful draw, notifying player to attack current target
 *
 */
public class MortalDrawPacket implements IMessage {

	public MortalDrawPacket() {}

	@Override
	public void fromBytes(ByteBuf buffer) {}

	@Override
	public void toBytes(ByteBuf buffer) {}

	public static class Handler extends AbstractClientMessageHandler<MortalDrawPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, MortalDrawPacket message, MessageContext ctx) {
			ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
			if (skills.hasSkill(SkillBase.mortalDraw)) {
				((MortalDraw) skills.getPlayerSkill(SkillBase.mortalDraw)).drawSword(player, null);
				ILockOnTarget skill = skills.getTargetingSkill();
				if (skill instanceof ICombo) {
					ZSSClientEvents.performComboAttack(Minecraft.getMinecraft(), skill);
				}
			}
			return null;
		}
	}
}

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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * This packet simply informs the server when an attack combo should be ended prematurely.
 * If a combo ends on the server side, the Combo class' own endCombo method should be used
 * directly instead of sending a packet.
 *
 */
public class EndComboPacket extends AbstractServerMessage<EndComboPacket>
{
	/** Id of skill that implements ICombo */
	private byte id;

	public EndComboPacket() {}

	public EndComboPacket(SkillBase skill) {
		this.id = skill.getId();
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		id = buffer.readByte();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeByte(id);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (SkillBase.getSkill(id) instanceof ICombo) {
			ICombo skill = (ICombo) ZSSPlayerSkills.get(player).getPlayerSkill(id);
			if (skill != null) {
				if (skill.isComboInProgress()) {
					skill.getCombo().endCombo(player);
				} else {
					skill.setCombo(null);
				}
			}
		}
	}
}

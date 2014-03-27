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
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.SkillBase;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * This packet simply informs the server when an attack combo should be ended prematurely.
 * If a combo ends on the server side, the Combo class' own endCombo method should be used
 * directly instead of sending a packet.
 *
 */
public class EndComboPacket extends CustomPacket
{
	/** Id of skill that implements ICombo */
	private byte id;

	public EndComboPacket() {}
	
	public EndComboPacket(SkillBase skill) {
		this.id = skill.getId();
	}
	
	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeByte(this.id);
	}
	
	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		this.id = in.readByte();
	}
	
	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			if (SkillBase.getSkill(this.id) instanceof ICombo) {
				ICombo skill = (ICombo) ZSSPlayerInfo.get(player).getPlayerSkill(this.id);
				if (skill != null) {
					if (skill.isComboInProgress()) {
						skill.getCombo().endCombo(player);
					} else {
						skill.setCombo(null);
					}
				}
			} else {
				throw new ProtocolException("Skill with id " + this.id + " is not a member of ICombo; unable to process EndComboPacket");
			}
		} else {
			throw new ProtocolException("End combo packet should only be sent to server");
		}
	}
}

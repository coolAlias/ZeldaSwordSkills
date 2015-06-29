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
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.skills.sword.SpinAttack;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Packet to notify server that player is trying to add one more spin to SpinAttack.
 * 
 * It does not require any data to be sent other than the packet itself.
 *
 */
public class RefreshSpinPacket extends CustomServerPacket {

	public RefreshSpinPacket() {}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		SkillActive skill = ZSSPlayerSkills.get(player).getActiveSkill(SkillBase.spinAttack);
		if (skill instanceof SpinAttack && skill.isActive()) {
			((SpinAttack) skill).refreshServerSpin(player);
		}
	}
}

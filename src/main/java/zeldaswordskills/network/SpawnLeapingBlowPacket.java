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
import zeldaswordskills.skills.sword.LeapingBlow;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class SpawnLeapingBlowPacket extends CustomPacket
{
	/** Whether the player was wielding a Master Sword at the time activated */
	private boolean isMaster;

	public SpawnLeapingBlowPacket() {}
	
	public SpawnLeapingBlowPacket(boolean isMaster) {
		this.isMaster = isMaster;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeBoolean(isMaster);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		isMaster = in.readBoolean();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			if (ZSSPlayerInfo.get(player) != null) {
				if (ZSSPlayerInfo.get(player).hasSkill(SkillBase.leapingBlow)) {
					((LeapingBlow) ZSSPlayerInfo.get(player).getPlayerSkill(SkillBase.leapingBlow)).spawnLeapingBlowEntity(player.worldObj, player, isMaster);
				}
			} else {
				throw new ProtocolException("ZSSPlayerInfo is null while handling Spawn Leaping Blow Packet");
			}
		} else {
			throw new ProtocolException("Leaping Blow entity can only be spawned on the server");
		}
	}
}

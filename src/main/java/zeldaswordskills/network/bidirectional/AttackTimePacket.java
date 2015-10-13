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
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.AbstractMessage;

/**
 * 
 * Sets the player's attack time on either the client or the server
 *
 */
public class AttackTimePacket extends AbstractMessage<AttackTimePacket>
{
	private int attackTime;

	public AttackTimePacket() {}

	public AttackTimePacket(int attackTime) {
		this.attackTime = attackTime;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.attackTime = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(attackTime);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ZSSPlayerInfo.get(player).setAttackTime(attackTime);
	}
}

/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

public class SyncCurrentMagicPacket extends AbstractClientMessage<SyncCurrentMagicPacket>
{
	private float mp;

	private boolean firstJoin;

	public SyncCurrentMagicPacket() {}

	public SyncCurrentMagicPacket(EntityPlayer player) {
		this(player, false);
	}

	/**
	 * @param firstJoin True when player first joins world to ignore max MP restriction when setting current MP
	 */
	public SyncCurrentMagicPacket(EntityPlayer player, boolean firstJoin) {
		this.mp = ZSSPlayerInfo.get(player).getCurrentMagic();
		this.firstJoin = firstJoin;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		mp = buffer.readFloat();
		firstJoin = buffer.readBoolean();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeFloat(mp);
		buffer.writeBoolean(firstJoin);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (firstJoin) {
			ZSSPlayerInfo.get(player).setInitialMagic(mp);
		} else {
			ZSSPlayerInfo.get(player).setCurrentMagic(mp);
		}
	}
}

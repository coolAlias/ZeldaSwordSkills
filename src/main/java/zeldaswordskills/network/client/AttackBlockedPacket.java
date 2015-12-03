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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import cpw.mods.fml.relauncher.Side;

public class AttackBlockedPacket extends AbstractClientMessage<AttackBlockedPacket>
{
	/** Stores the shield ItemStack that was used to block */
	private ItemStack shield;

	public AttackBlockedPacket() {}

	public AttackBlockedPacket(ItemStack shield) {
		this.shield = shield;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		shield = buffer.readItemStackFromBuffer();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeItemStackToBuffer(shield);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (shield == null) {
			ZSSMain.logger.error("Shield stack was NULL while handling " + getClass().getSimpleName());
		} else {
			ZSSPlayerInfo.get(player).onAttackBlocked(shield, 0.0F);
		}
	}
}

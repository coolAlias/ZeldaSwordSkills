/**
    Copyright (C) <2018> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed buffer the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.server;

import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.inventory.ContainerRupeeMerchant;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

/**
 * 
 * Set the current starting trade index for {@link ContainerRupeeMerchant#setSlotIndex}
 *
 */
public class RupeeMerchantIndexPacket extends AbstractServerMessage<RupeeMerchantIndexPacket>
{
	private int index;

	public RupeeMerchantIndexPacket() {}

	public RupeeMerchantIndexPacket(int index) {
		this.index = index;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.index = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(this.index);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (player.openContainer instanceof ContainerRupeeMerchant) {
			((ContainerRupeeMerchant) player.openContainer).setCurrentIndex(this.index);
		}
	}
}

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
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.inventory.ContainerRupeeMerchant;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

/**
 * 
 * Handles request to toggle currently open rupee merchant GUI between buying and selling
 *
 */
public class RupeeMerchantTogglePacket extends AbstractServerMessage<RupeeMerchantTogglePacket>
{
	private boolean getItemsToSell;

	public RupeeMerchantTogglePacket() {}

	public RupeeMerchantTogglePacket(boolean getItemsToSell) {
		this.getItemsToSell = getItemsToSell;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.getItemsToSell = buffer.readBoolean();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeBoolean(this.getItemsToSell);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (player.openContainer instanceof ContainerRupeeMerchant) {
			ContainerRupeeMerchant container = (ContainerRupeeMerchant) player.openContainer;
			container.toggling = true;
			RupeeMerchantHelper.openRupeeMerchantGui(container.getMerchant(), player, !this.getItemsToSell);
			container.toggling = false; // in case original container is still open
		}
	}
}

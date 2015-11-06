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
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Sent from the Mask Trader's GUI with the mask the player borrowed.
 *
 */
public class BorrowMaskPacket extends AbstractServerMessage<BorrowMaskPacket>
{
	private ItemStack mask;

	public BorrowMaskPacket() {}

	public BorrowMaskPacket(ItemStack mask) {
		this.mask = mask;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		mask = buffer.readItemStackFromBuffer();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeItemStackToBuffer(mask); // can handle NULL
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (mask != null) {
			PlayerUtils.addItemToInventory(player, mask);
			ZSSQuests.get(player).setBorrowedMask(mask.getItem());
		}
	}
}

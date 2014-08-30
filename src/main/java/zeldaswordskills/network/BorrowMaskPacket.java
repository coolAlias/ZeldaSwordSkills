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
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.util.PlayerUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Sent from the Mask Trader's GUI with the mask the player borrowed.
 *
 */
public class BorrowMaskPacket extends CustomPacket
{
	private ItemStack mask;

	public BorrowMaskPacket() {}

	public BorrowMaskPacket(ItemStack mask) {
		this.mask = mask;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeByte(mask == null ? (byte) 0 : (byte) 1);
		if (mask != null) {
			Packet.writeItemStack(mask, out);
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		mask = (in.readByte() > 0 ? Packet.readItemStack(in) : null);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			if (mask != null) {
				PlayerUtils.addItemToInventory(player, mask);
				ZSSPlayerInfo.get(player).setBorrowedMask(mask.getItem());
			}
		}
	}
}

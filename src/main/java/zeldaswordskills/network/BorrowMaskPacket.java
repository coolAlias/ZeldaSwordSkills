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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.util.PlayerUtils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BorrowMaskPacket extends CustomPacket
{
	private NBTTagCompound item;

	public BorrowMaskPacket() {}
	
	public BorrowMaskPacket(ItemStack mask) {
		item = new NBTTagCompound();
		mask.writeToNBT(item);
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		CompressedStreamTools.write(item, out);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		item = CompressedStreamTools.read(in);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			try {
				ItemStack mask = ItemStack.loadItemStackFromNBT(item);
				PlayerUtils.addItemToInventory(player, mask);
				ZSSPlayerInfo.get(player).setBorrowedMask(mask.getItem());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

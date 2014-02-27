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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import zeldaswordskills.block.tileentity.TileEntityPedestal;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Updates sword pedestal's sword and orientation on the client
 *
 */
public class SwordPedestalPacket extends CustomPacket
{
	/** Tile entity's coordinates */
	private int posX, posY, posZ;

	/** Sword itemstack (may be null) */
	private ItemStack sword = null;

	/** Orientation (0 or 1) */
	private byte orientation = 0;

	public SwordPedestalPacket() {}

	public SwordPedestalPacket(int x, int y, int z, ItemStack stack, byte o) {
		posX = x;
		posY = y;
		posZ = z;
		sword = stack;
		orientation = o;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(posX);
		out.writeInt(posY);
		out.writeInt(posZ);
		out.writeBoolean(sword != null);
		if (sword != null) {
			writeNBTTagCompound(sword.writeToNBT(new NBTTagCompound()), out);
			out.writeByte(orientation);
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		posX = in.readInt();
		posY = in.readInt();
		posZ = in.readInt();
		if (in.readBoolean()) {
			sword = ItemStack.loadItemStackFromNBT(readNBTTagCompound(in));
			orientation = in.readByte();
		}
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			TileEntity te = player.worldObj.getBlockTileEntity(posX, posY, posZ);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).setSword(sword, orientation);
			}
		} else {
			throw new ProtocolException("Sword pedestal packet may only be sent to the client");
		}
	}
}

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
import net.minecraft.tileentity.TileEntity;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class SetGossipStoneMessagePacket extends CustomServerPacket
{
	private int x, y, z;
	private String message;

	public SetGossipStoneMessagePacket() {}

	public SetGossipStoneMessagePacket(TileEntityGossipStone te) {
		this.x = te.xCoord;
		this.y = te.yCoord;
		this.z = te.zCoord;
		this.message = te.getMessage();
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
		this.message = buffer.readUTF();
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
		buffer.writeUTF(this.message);
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		TileEntity te = player.worldObj.getBlockTileEntity(this.x, this.y, this.z);
		if (te instanceof TileEntityGossipStone) {
			((TileEntityGossipStone) te).setMessage(this.message);
		}
	}
}

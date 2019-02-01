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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

public class SetGossipStoneMessagePacket extends AbstractServerMessage<SetGossipStoneMessagePacket>
{
	private BlockPos pos;
	private String message;

	public SetGossipStoneMessagePacket() {}

	public SetGossipStoneMessagePacket(TileEntityGossipStone te) {
		this.pos = te.getPos();
		this.message = te.getMessage();
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.pos = BlockPos.fromLong(buffer.readLong());
		this.message = ByteBufUtils.readUTF8String(buffer);
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeLong(this.pos.toLong());
		ByteBufUtils.writeUTF8String(buffer, this.message);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		TileEntity te = player.worldObj.getTileEntity(this.pos);
		if (te instanceof TileEntityGossipStone) {
			((TileEntityGossipStone) te).setMessage(this.message);
		}
	}
}

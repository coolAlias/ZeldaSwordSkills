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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.api.item.IDynamicItemBlock;
import zeldaswordskills.api.item.ILiftBlock;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

/**
 * 
 * Block render colors are available client side only, but is needed server side to store in NBT.
 *
 */
public class HeldBlockColorPacket extends AbstractServerMessage<HeldBlockColorPacket>
{
	private int color;

	public HeldBlockColorPacket() {}

	public HeldBlockColorPacket(int color) {
		this.color = color;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		color = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(color);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && (stack.getItem() instanceof ILiftBlock || stack.getItem() instanceof IDynamicItemBlock)) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			stack.getTagCompound().setInteger("blockColor", color);
		}
	}
}

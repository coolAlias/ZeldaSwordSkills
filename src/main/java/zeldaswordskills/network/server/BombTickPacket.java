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
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BombTickPacket extends CustomServerPacket {

	/** Index of inventory slot containing the ticking bomb */
	private int slot;

	public BombTickPacket() {}

	public BombTickPacket(int slot) {
		this.slot = slot;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(slot);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		slot = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		ItemStack bomb = player.inventory.getStackInSlot(slot);
		if (bomb != null && bomb.getItem() instanceof ItemBomb) {
			((ItemBomb) bomb.getItem()).tickBomb(bomb, player.worldObj, player, slot);
		}
	}
}

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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class SetItemModePacket extends CustomClientPacket
{
	private int mode;

	public SetItemModePacket() {}

	public SetItemModePacket(int mode) {
		this.mode = mode;
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		mode = buffer.readInt();
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		buffer.writeInt(mode);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ICyclableItem)) {
			LogHelper.warning("Received SetItemModePacket with invalid held item " + stack);
		} else {
			((ICyclableItem) stack.getItem()).setCurrentMode(stack, player, mode);
		}
	}
}

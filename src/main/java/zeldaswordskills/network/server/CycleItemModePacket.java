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
import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;
import zeldaswordskills.network.client.SetItemModePacket;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class CycleItemModePacket extends CustomServerPacket
{
	private boolean next;

	public CycleItemModePacket() {}

	/**
	 * @param next True to cycle to the next item mode, or false to cycle to the previous mode
	 */
	public CycleItemModePacket(boolean next) {
		this.next = next;
	}

	@Override
	public void read(ByteArrayDataInput buffer) throws IOException {
		next = buffer.readBoolean();
	}

	@Override
	public void write(ByteArrayDataOutput buffer) throws IOException {
		buffer.writeBoolean(next);
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ICyclableItem)) {
			LogHelper.warning("Received CycleItemModePacket with invalid held item " + stack);
			return;
		}
		if (next) {
			((ICyclableItem) stack.getItem()).nextItemMode(stack, player);
		} else {
			((ICyclableItem) stack.getItem()).prevItemMode(stack, player);
		}
		if (player instanceof Player) {
			PacketDispatcher.sendPacketToPlayer(new SetItemModePacket(((ICyclableItem) stack.getItem()).getCurrentMode(stack, player)).makePacket(), (Player) player);
		}
	}
}

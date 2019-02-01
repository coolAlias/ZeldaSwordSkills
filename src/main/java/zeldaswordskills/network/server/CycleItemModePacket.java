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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.ICyclableItem;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SetItemModePacket;

public class CycleItemModePacket extends AbstractServerMessage<CycleItemModePacket>
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
	protected void read(PacketBuffer buffer) throws IOException {
		next = buffer.readBoolean();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeBoolean(next);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ICyclableItem)) {
			ZSSMain.logger.warn("Received CycleItemModePacket with invalid held item " + stack);
			return;
		}
		if (next) {
			((ICyclableItem) stack.getItem()).nextItemMode(stack, player);
		} else {
			((ICyclableItem) stack.getItem()).prevItemMode(stack, player);
		}
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SetItemModePacket(((ICyclableItem) stack.getItem()).getCurrentMode(stack, player)), (EntityPlayerMP) player);
		}
	}
}

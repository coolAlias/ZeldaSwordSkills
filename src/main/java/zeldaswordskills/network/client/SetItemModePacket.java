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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.ICyclableItem;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

public class SetItemModePacket extends AbstractClientMessage<SetItemModePacket>
{
	private int mode;

	public SetItemModePacket() {}

	public SetItemModePacket(int mode) {
		this.mode = mode;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		mode = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(mode);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ICyclableItem)) {
			ZSSMain.logger.warn("Received SetItemModePacket with invalid held item " + stack);
		} else {
			((ICyclableItem) stack.getItem()).setCurrentMode(stack, player, mode);
		}
	}
}

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
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

/**
 * 
 * Updates the currently nocked arrow on the client, since DataWatcher
 * is apparently unable to handle NULL values.
 * 
 * Required for the Hero's Bow to fire without the graphical glitch
 * caused by writing to NBT.
 *
 */
public class SetNockedArrowPacket extends AbstractClientMessage<SetNockedArrowPacket>
{
	private ItemStack arrowStack;

	public SetNockedArrowPacket() {}

	public SetNockedArrowPacket(ItemStack arrowStack) {
		this.arrowStack = arrowStack;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		arrowStack = buffer.readItemStackFromBuffer();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeItemStackToBuffer(arrowStack);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ZSSPlayerInfo.get(player).setNockedArrow(arrowStack);
	}
}

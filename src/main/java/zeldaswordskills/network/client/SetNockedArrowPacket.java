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
import net.minecraft.network.packet.Packet;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Updates the currently nocked arrow on the client, since DataWatcher
 * is apparently unable to handle NULL values.
 * 
 * Required for the Hero's Bow to fire without the graphical glitch
 * caused by writing to NBT.
 *
 */
public class SetNockedArrowPacket extends CustomClientPacket
{
	private ItemStack arrowStack;

	public SetNockedArrowPacket() {}

	public SetNockedArrowPacket(ItemStack arrowStack) {
		this.arrowStack = arrowStack;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeByte(arrowStack == null ? (byte) 0 : (byte) 1);
		if (arrowStack != null) {
			Packet.writeItemStack(arrowStack, out);
		}
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		arrowStack = (in.readByte() > 0 ? Packet.readItemStack(in) : null);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		ZSSPlayerInfo.get(player).setNockedArrow(arrowStack);
	}
}

/**
    Copyright (C) <2014> <coolAlias>

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
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Hack to prevent bombs ticking while a GUI screen is open by not ticking
 * automatically on the server and instead relying on this packet (client
 * knows if a gui is open, but server does not).
 *
 */
public class BombTickPacket extends AbstractServerMessage {

	public BombTickPacket() {}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ItemStack held = player.getHeldItem();
		if (held != null && held.getItem() instanceof ItemBomb) {
			((ItemBomb) held.getItem()).tickBomb(held, player.worldObj, player);
		}
	}
}

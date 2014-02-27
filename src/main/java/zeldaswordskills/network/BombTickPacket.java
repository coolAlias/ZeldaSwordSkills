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

package zeldaswordskills.network;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.item.ItemBomb;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BombTickPacket extends CustomPacket {

	public BombTickPacket() {}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {}

	@Override
	public void process(ByteArrayDataInput in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		if (side.isServer()) {
			ItemStack held = player.getHeldItem();
			if (held != null && held.getItem() instanceof ItemBomb) {
				((ItemBomb) held.getItem()).tickBomb(held, player.worldObj, player);
			}
		} else {
			throw new ProtocolException("BombTickPacket can only be sent to the server");
		}
	}
}

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

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Used for skills that activate client-side only and need to add exhaustion to the player.
 *
 */
public class AddExhaustionPacket extends CustomPacket
{
	private float amount;

	public AddExhaustionPacket() {}

	public AddExhaustionPacket(float amount) { this.amount = amount; }

	@Override
	public void write(ByteArrayDataOutput out) throws IOException { out.writeFloat(amount);}

	@Override
	public void read(ByteArrayDataInput in) throws IOException { this.amount = in.readFloat(); }

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isServer()) {
			player.addExhaustion(amount);
		} else {
			throw new ProtocolException("AddExhaustionPacket can only be sent to the server");
		}
	}
}

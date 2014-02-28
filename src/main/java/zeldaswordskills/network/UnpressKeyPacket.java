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

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Send from the server to unpress a key (or all keys) on the client
 *
 */
public class UnpressKeyPacket extends CustomPacket
{
	/** Values for Left, Right, and Middle Mouse Buttons */
	public static final int LMB = -100, RMB = -99, MMB = -98;
	private int keyCode;

	public UnpressKeyPacket() {}
	
	public UnpressKeyPacket(int keyCode) {
		this.keyCode = keyCode;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeInt(keyCode);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		keyCode = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			KeyBinding kb = (KeyBinding) KeyBinding.hash.lookup(keyCode);
			if (kb != null) {
				System.out.println("Unpressing one key: " + kb.keyDescription);
				KeyBinding.setKeyBindState(keyCode, false);
			} else {
				System.out.println("Unpressing all keys");
				KeyBinding.unPressAllKeys();
			}
		}
	}
}

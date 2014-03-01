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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ZSSItems;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Getting around derpiness of Minecraft: client-side world.getMaterial returns
 * incorrect value and is unable to detect liquids until player is submerged,
 * regardless of position parameters passed.
 * 
 * This packet should be received on the client side when the player is standing
 * in liquid wearing Heavy Boots; use to increase velocity.
 *
 */
public class InLiquidPacket extends CustomPacket
{
	/** If this is true, the magnitude of velocity increase will be higher*/
	private boolean inLava;

	public InLiquidPacket() {}

	public InLiquidPacket(boolean inLava) {
		this.inLava = inLava;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeBoolean(inLava);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		inLava = in.readBoolean();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			if (player.getCurrentArmor(ArmorIndex.WORN_BOOTS) != null && player.getCurrentArmor(ArmorIndex.WORN_BOOTS).getItem() == ZSSItems.bootsHeavy) {
				double d = (inLava ? 1.75D : 1.125D);
				if (player.onGround) {
					player.motionX *= d;
					player.motionZ *= d;
				} else if (player.motionY < 0 && !Minecraft.getMinecraft().gameSettings.keyBindJump.pressed) {
					player.motionY *= 1.5;
					if (player.motionY < -0.35D) {
						player.motionY = -0.35D;
					}
				}
			}
		} else {
			throw new ProtocolException("InLiquid packet may only be sent to the client");
		}
	}
}

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
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;
import cpw.mods.fml.relauncher.Side;

public class OpenGuiPacket extends AbstractServerMessage<OpenGuiPacket>
{
	/** ID of the gui to open; see {@link GuiHandler} for list of valid IDs */
	private int id;

	public OpenGuiPacket() {}

	public OpenGuiPacket(int id) {
		this.id = id;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		id = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(id);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		player.openGui(ZSSMain.instance, id, player.worldObj, (int) player.posX, (int) player.posY, (int) player.posZ);
	}
}

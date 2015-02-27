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
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import cpw.mods.fml.relauncher.Side;

public class OpenGossipStoneEditorPacket extends AbstractClientMessage<OpenGossipStoneEditorPacket>
{
	private int x, y, z;

	public OpenGossipStoneEditorPacket() {}

	/**
	 * Constructor taking just the coordinates - TileEntity validated when opening GUI
	 */
	public OpenGossipStoneEditorPacket(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.x = buffer.readInt();
		this.y = buffer.readInt();
		this.z = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(this.x);
		buffer.writeInt(this.y);
		buffer.writeInt(this.z);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		// TileEntity checked in IGuiHandler, so no need to do so here
		player.openGui(ZSSMain.instance, GuiHandler.GUI_EDIT_GOSSIP_STONE, player.worldObj, x, y, z);
	}
}

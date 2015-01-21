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

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Synchronizes all PlayerInfo data on the client
 *
 */
public class SyncPlayerInfoPacket extends AbstractClientMessage
{
	/** NBTTagCompound used to store and transfer the Player's Info */
	private NBTTagCompound compound;

	/** Whether skills should validate; only false when skills reset */
	private boolean validate = true;

	public SyncPlayerInfoPacket() {}

	public SyncPlayerInfoPacket(ZSSPlayerInfo info) {
		compound = new NBTTagCompound();
		info.saveNBTData(compound);
	}

	/**
	 * Sets validate to false for reset skills packets
	 */
	public SyncPlayerInfoPacket setReset() {
		validate = false;
		return this;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		compound = buffer.readNBTTagCompoundFromBuffer();
		validate = buffer.readBoolean();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeNBTTagCompoundToBuffer(compound);
		buffer.writeBoolean(validate);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
		info.loadNBTData(compound);
		if (validate) {
			info.getPlayerSkills().validateSkills();
		}
	}
}

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
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerInfo;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Synchronizes all ZSSPlayerInfo data on the client
 *
 */
public class SyncPlayerInfoPacket extends CustomPacket
{
	/** NBTTagCompound used to store and transfer the Player's Info */
	private NBTTagCompound compound;

	public SyncPlayerInfoPacket() {}

	public SyncPlayerInfoPacket(ZSSPlayerInfo info) {
		compound = new NBTTagCompound();
		info.saveNBTData(compound);
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		writeNBTTagCompound(compound, out);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		compound = readNBTTagCompound(in);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			if (info != null) {
				info.loadNBTData(compound);
				info.validateSkills();
			} else {
				throw new ProtocolException("No Skills section");
			}
		} else {
			throw new ProtocolException("Cannot send SyncPlayerInfoPacket to the server!");
		}
	}
}

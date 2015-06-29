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
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Updates a buff on the client side, either adding or removing it from the activeBuffs map
 *
 */
public class UpdateBuffPacket extends CustomClientPacket
{
	/** The buff to be applied or removed */
	private BuffBase buff;

	/** Whether to apply or remove the specified buff */
	private boolean remove;

	public UpdateBuffPacket() {}

	public UpdateBuffPacket(BuffBase buff, boolean remove) {
		this.buff = buff;
		this.remove = remove;
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		writeNBTTagCompound(buff.writeToNBT(new NBTTagCompound()), out);
		out.writeBoolean(remove);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		this.buff = BuffBase.readFromNBT(readNBTTagCompound(in));
		this.remove = in.readBoolean();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (remove) {
			ZSSEntityInfo.get(player).getActiveBuffsMap().remove(buff.getBuff());
		} else {
			ZSSEntityInfo.get(player).getActiveBuffsMap().put(buff.getBuff(), buff);
		}
	}
}

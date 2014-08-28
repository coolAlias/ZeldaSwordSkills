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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Packet responsible for keeping attack Combos synchronized between client and server.
 *
 */
public class UpdateComboPacket extends CustomPacket
{
	/** Stores data of combo to be updated */
	private NBTTagCompound compound;
	
	public UpdateComboPacket() {}
	
	public UpdateComboPacket(Combo combo) {
		compound = combo.writeToNBT();
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		CompressedStreamTools.write(compound, out);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		compound = CompressedStreamTools.read(in);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if (side.isClient()) {
			Combo combo = Combo.readFromNBT(compound);
			try {
				ICombo skill = (ICombo) ZSSPlayerInfo.get(player).getPlayerSkill(combo.getSkill());
				if (skill != null) {
					combo.getEntityFromWorld(player.worldObj);
					skill.setCombo(combo);
				}
			} catch (ClassCastException e) {
				LogHelper.severe("Class Cast Exception from invalid Combo skill id of " + combo.getSkill());
			}
		} else {
			throw new ProtocolException("Update Combo Packet should only be sent from server to client");
		}
	}
}

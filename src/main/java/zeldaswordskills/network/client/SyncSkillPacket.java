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
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.network.CustomPacket.CustomClientPacket;
import zeldaswordskills.skills.SkillBase;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Synchronizes the client-side version of a skill with the server-side data.
 *
 */
public class SyncSkillPacket extends CustomClientPacket
{
	/** The ID of the skill to update */
	private byte id;
	/** Stores the skill's data */
	private NBTTagCompound compound;

	public SyncSkillPacket() {}

	public SyncSkillPacket(SkillBase skill) {
		id = skill.getId();
		compound = new NBTTagCompound();
		skill.writeToNBT(compound);
	}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {
		out.writeByte(id);
		CompressedStreamTools.write(compound, out);
	}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {
		id = in.readByte();
		compound = CompressedStreamTools.read(in);
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		ZSSPlayerSkills.get(player).syncClientSideSkill(id, compound);
	}
}

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
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

public class SyncQuestsPacket extends AbstractClientMessage<SyncQuestsPacket>
{
	private NBTTagCompound compound;

	public SyncQuestsPacket() {}

	public SyncQuestsPacket(ZSSQuests quests) {
		compound = new NBTTagCompound();
		quests.writeToNBT(compound, true);
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		compound = buffer.readNBTTagCompoundFromBuffer();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeNBTTagCompoundToBuffer(compound);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ZSSQuests.get(player).loadNBTData(compound);
	}
}

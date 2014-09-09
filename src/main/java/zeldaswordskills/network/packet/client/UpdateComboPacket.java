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

package zeldaswordskills.network.packet.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * 
 * Packet responsible for keeping attack Combos synchronized between server and client.
 *
 */
public class UpdateComboPacket implements IMessage
{
	/** Stores data of combo to be updated */
	private NBTTagCompound compound;

	public UpdateComboPacket() {}

	public UpdateComboPacket(Combo combo) {
		compound = combo.writeToNBT();
	}

	@Override
	public void fromBytes(ByteBuf buffer) {
		compound = ByteBufUtils.readTag(buffer);
	}

	@Override
	public void toBytes(ByteBuf buffer) {
		ByteBufUtils.writeTag(buffer, compound);
	}

	public static class Handler extends AbstractClientMessageHandler<UpdateComboPacket> {
		@Override
		public IMessage handleClientMessage(EntityPlayer player, UpdateComboPacket message, MessageContext ctx) {
			Combo combo = Combo.readFromNBT(message.compound);
			try {
				ICombo skill = (ICombo) ZSSPlayerSkills.get(player).getPlayerSkill(combo.getSkill());
				if (skill != null) {
					combo.getEntityFromWorld(player.worldObj);
					skill.setCombo(combo);
				}
			} catch (ClassCastException e) {
				LogHelper.warning("Class Cast Exception from invalid Combo skill id of " + combo.getSkill());
			}
			return null;
		}
	}
}

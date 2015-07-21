/**
    Copyright (C) <2015> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed buffer the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

/**
 * 
 * Updates a buff on the client side, either adding or removing it from the activeBuffs map
 *
 */
public class UpdateBuffPacket extends AbstractClientMessage<UpdateBuffPacket>
{
	/** The buff to be applied or removed */
	private BuffBase buff;

	/** Whether to apply or remove the specified buff */
	private boolean remove;

	/** ID of entity to update, or -1 for the player */
	private int entityId;

	public UpdateBuffPacket() {}

	/**
	 * Constructs update packet for a player entity
	 */
	public UpdateBuffPacket(BuffBase buff, boolean remove) {
		this(buff, null, remove);
	}

	/**
	 * Constructs update packet for any EntityLivingBase
	 */
	public UpdateBuffPacket(BuffBase buff, EntityLivingBase entity, boolean remove) {
		this.buff = buff;
		this.remove = remove;
		this.entityId = (entity == null ? -1 : entity.getEntityId());
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.buff = BuffBase.readFromNBT(buffer.readNBTTagCompoundFromBuffer());
		this.remove = buffer.readBoolean();
		this.entityId = buffer.readInt();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeNBTTagCompoundToBuffer(buff.writeToNBT(new NBTTagCompound()));
		buffer.writeBoolean(remove);
		buffer.writeInt(entityId);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		Entity entity = (entityId < 0 ? player : player.worldObj.getEntityByID(entityId));
		if (!(entity instanceof EntityLivingBase)) {
			return;
		}
		if (remove) {
			ZSSEntityInfo.get((EntityLivingBase) entity).removeBuff(buff.getBuff());
		} else {
			ZSSEntityInfo.get((EntityLivingBase) entity).applyBuff(buff);
		}
	}
}

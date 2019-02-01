/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.client;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.item.ISpawnParticles;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

/**
 * 
 * Packet that calls a specific ISpawnParticles method in the Item class, allowing
 * each Item to handle its own particle algorithm individually yet spawn them in
 * all client worlds
 *
 */
public class PacketISpawnParticles extends AbstractClientMessage<PacketISpawnParticles>
{
	/** The ItemStack spawning the particles; Item must implement ISpawnParticles */
	private ItemStack stack;

	/** The name of the player that caused the particles to be spawned */
	private String commandSenderName;

	/** Radius buffer which to spawn the particles */
	private float r;

	public PacketISpawnParticles() {}

	public PacketISpawnParticles(EntityPlayer player, float radius) {
		this.commandSenderName = player.getName();
		this.stack = player.getHeldItem();
		r = radius;
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		commandSenderName = ByteBufUtils.readUTF8String(buffer);
		stack = ByteBufUtils.readItemStack(buffer);
		r = buffer.readFloat();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		ByteBufUtils.writeUTF8String(buffer, commandSenderName);
		ByteBufUtils.writeItemStack(buffer, stack);
		buffer.writeFloat(r);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		EntityPlayer commandSender = player.worldObj.getPlayerEntityByName(commandSenderName);
		if (commandSender != null && stack != null && stack.getItem() instanceof ISpawnParticles) {
			((ISpawnParticles) stack.getItem()).spawnParticles(player.worldObj, commandSender, stack, player.posX, player.posY, player.posZ, r);
		}
	}
}

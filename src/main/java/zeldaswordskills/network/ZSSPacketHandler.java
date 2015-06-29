/**
    Copyright (C) <2015> <coolAlias>
    
    @author original credits go to Hunternif and possibly diesieben07 

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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import zeldaswordskills.network.CustomPacket.ProtocolException;
import zeldaswordskills.util.LogHelper;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class ZSSPacketHandler implements IPacketHandler {

	public ZSSPacketHandler() {}

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		try {
			EntityPlayer entityPlayer = (EntityPlayer) player;
			ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
			// Assuming your packetId is between 0 (inclusive) and 256 (exclusive).
			int packetId = in.readUnsignedByte();
			CustomPacket customPacket = CustomPacket.constructPacket(packetId);
			try {
				customPacket.process(in, entityPlayer, entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
			} catch (IOException e) {
				throw new ProtocolException(e);
			}
		} catch (ProtocolException e) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Protocol Exception!");
				LogHelper.warning(String.format("Player %s caused a Protocol Exception and was kicked.", ((EntityPlayer)player).username) + e.toString());
			} else {
				LogHelper.severe(e.toString());
			}
		} catch (InstantiationException e) {
			throw new RuntimeException("Unexpected InstantiationException during Packet construction!", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Unexpected IllegalAccessException during Packet construction!", e);
		}
	}
}

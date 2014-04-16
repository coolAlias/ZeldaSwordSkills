/**
    Copyright (C) <2014> <coolAlias>
    
    @author original credits go to diesieben07

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
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.LogHelper;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public abstract class CustomPacket {
	private static final BiMap<Integer, Class<? extends CustomPacket>> idMap;

	static {
		ImmutableBiMap.Builder<Integer, Class<? extends CustomPacket>> builder = ImmutableBiMap.builder();
		int i = 0;
		builder.put(Integer.valueOf(i++), ActivateSkillPacket.class);
		builder.put(Integer.valueOf(i++), AddExhaustionPacket.class);
		builder.put(Integer.valueOf(i++), AttackBlockedPacket.class);
		builder.put(Integer.valueOf(i++), BombTickPacket.class);
		builder.put(Integer.valueOf(i++), BorrowMaskPacket.class);
		builder.put(Integer.valueOf(i++), EndComboPacket.class);
		builder.put(Integer.valueOf(i++), GetBombPacket.class);
		builder.put(Integer.valueOf(i++), InLiquidPacket.class);
		builder.put(Integer.valueOf(i++), MortalDrawPacket.class);
		builder.put(Integer.valueOf(i++), PacketISpawnParticles.class);
		builder.put(Integer.valueOf(i++), PlaySoundPacket.class);
		builder.put(Integer.valueOf(i++), SpawnLeapingBlowPacket.class);
		builder.put(Integer.valueOf(i++), SpawnNayruParticlesPacket.class);
		builder.put(Integer.valueOf(i++), SyncEntityInfoPacket.class);
		builder.put(Integer.valueOf(i++), SyncPlayerInfoPacket.class);
		builder.put(Integer.valueOf(i++), SyncSkillPacket.class);
		builder.put(Integer.valueOf(i++), TargetIdPacket.class);
		builder.put(Integer.valueOf(i++), UnpressKeyPacket.class);
		builder.put(Integer.valueOf(i++), UpdateBuffPacket.class);
		builder.put(Integer.valueOf(i++), UpdateComboPacket.class);
		idMap = builder.build();
	}

	public static CustomPacket constructPacket(int packetId)
			throws ProtocolException, InstantiationException, IllegalAccessException {
		Class<? extends CustomPacket> clazz = idMap.get(Integer.valueOf(packetId));
		if (clazz == null) {
			throw new ProtocolException("Unknown Packet Id!");
		} else {
			return clazz.newInstance();
		}
	}
	
	public static class ProtocolException extends Exception {
		/** Automatically generated serial version UID */
		private static final long serialVersionUID = -7850212538983273200L;
		public ProtocolException() {
		}
		public ProtocolException(String message, Throwable cause) {
			super(message, cause);
		}
		public ProtocolException(String message) {
			super(message);
		}
		public ProtocolException(Throwable cause) {
			super(cause);
		}
	}

	public final int getPacketId() {
		if (idMap.inverse().containsKey(getClass())) {
			return idMap.inverse().get(getClass()).intValue();
		} else {
			throw new RuntimeException("Packet " + getClass().getSimpleName() + " is missing a mapping!");
		}
	}
	
	public final Packet makePacket() {
		ByteArrayDataOutput dataOut = ByteStreams.newDataOutput();
		try {
			write(dataOut);
		} catch (IOException e) {
			LogHelper.log(Level.WARNING, "Error writing packet: " + e.toString());
		}
		byte[] data = dataOut.toByteArray();
		ByteArrayDataOutput packetOut = ByteStreams.newDataOutput();
		packetOut.writeByte(getPacketId());
		packetOut.write(data);
		return PacketDispatcher.getPacket(ModInfo.CHANNEL, packetOut.toByteArray());
	}

	public abstract void write(ByteArrayDataOutput out) throws IOException;
	
	public abstract void read(ByteArrayDataInput in) throws IOException;
	
	public abstract void execute(EntityPlayer player, Side side) throws ProtocolException;
	
	public void process(ByteArrayDataInput in, EntityPlayer player, Side side)
			throws IOException, ProtocolException {
		read(in);
		execute(player, side);
	}
	
	/**
	 * Adapted to a public version from Minecraft's Packet class method
	 */
	public static void writeNBTTagCompound(NBTTagCompound compound, ByteArrayDataOutput out) throws IOException {
		if (compound == null) {
			out.writeShort(-1);
		} else {
			byte[] abyte = CompressedStreamTools.compress(compound);
			out.writeShort((short)abyte.length);
			out.write(abyte);
		}
	}

	/**
	 * Adapted from Minecraft's Packet class method
	 */
	public static NBTTagCompound readNBTTagCompound(ByteArrayDataInput in) throws IOException {
		short short1 = in.readShort();

		if (short1 < 0) {
			return null;
		} else {
			byte[] abyte = new byte[short1];
			in.readFully(abyte);
			return CompressedStreamTools.decompress(abyte);
		}
	}
}
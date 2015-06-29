/**
    Copyright (C) <2015> <coolAlias>

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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.bidirectional.DeactivateSkillPacket;
import zeldaswordskills.network.bidirectional.LearnSongPacket;
import zeldaswordskills.network.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.network.client.AttackBlockedPacket;
import zeldaswordskills.network.client.InLiquidPacket;
import zeldaswordskills.network.client.MortalDrawPacket;
import zeldaswordskills.network.client.OpenGossipStoneEditorPacket;
import zeldaswordskills.network.client.OpenSongGuiPacket;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.network.client.SetItemModePacket;
import zeldaswordskills.network.client.SetNockedArrowPacket;
import zeldaswordskills.network.client.SpawnNayruParticlesPacket;
import zeldaswordskills.network.client.SyncConfigPacket;
import zeldaswordskills.network.client.SyncEntityInfoPacket;
import zeldaswordskills.network.client.SyncPlayerInfoPacket;
import zeldaswordskills.network.client.SyncSkillPacket;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.network.client.UpdateBuffPacket;
import zeldaswordskills.network.client.UpdateComboPacket;
import zeldaswordskills.network.server.AddExhaustionPacket;
import zeldaswordskills.network.server.BombTickPacket;
import zeldaswordskills.network.server.BorrowMaskPacket;
import zeldaswordskills.network.server.CycleItemModePacket;
import zeldaswordskills.network.server.DashImpactPacket;
import zeldaswordskills.network.server.EndComboPacket;
import zeldaswordskills.network.server.FallDistancePacket;
import zeldaswordskills.network.server.GetBombPacket;
import zeldaswordskills.network.server.OpenGuiPacket;
import zeldaswordskills.network.server.RefreshSpinPacket;
import zeldaswordskills.network.server.SetGossipStoneMessagePacket;
import zeldaswordskills.network.server.TargetIdPacket;
import zeldaswordskills.network.server.ZeldaSongPacket;
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
		// Bidirectional packets
		builder.put(Integer.valueOf(i++), ActivateSkillPacket.class);
		builder.put(Integer.valueOf(i++), DeactivateSkillPacket.class);
		builder.put(Integer.valueOf(i++), LearnSongPacket.class);
		builder.put(Integer.valueOf(i++), PlayRecordPacket.class);
		builder.put(Integer.valueOf(i++), PlaySoundPacket.class);

		// Packets handled on CLIENT
		builder.put(Integer.valueOf(i++), AttackBlockedPacket.class);
		builder.put(Integer.valueOf(i++), InLiquidPacket.class);
		builder.put(Integer.valueOf(i++), MortalDrawPacket.class);
		builder.put(Integer.valueOf(i++), OpenGossipStoneEditorPacket.class);
		builder.put(Integer.valueOf(i++), OpenSongGuiPacket.class);
		builder.put(Integer.valueOf(i++), PacketISpawnParticles.class);
		builder.put(Integer.valueOf(i++), SetItemModePacket.class);
		builder.put(Integer.valueOf(i++), SetNockedArrowPacket.class);
		builder.put(Integer.valueOf(i++), SpawnNayruParticlesPacket.class);
		builder.put(Integer.valueOf(i++), SyncConfigPacket.class);
		builder.put(Integer.valueOf(i++), SyncEntityInfoPacket.class);
		builder.put(Integer.valueOf(i++), SyncPlayerInfoPacket.class);
		builder.put(Integer.valueOf(i++), SyncSkillPacket.class);
		builder.put(Integer.valueOf(i++), UnpressKeyPacket.class);
		builder.put(Integer.valueOf(i++), UpdateBuffPacket.class);
		builder.put(Integer.valueOf(i++), UpdateComboPacket.class);

		// Packets handled on SERVER
		builder.put(Integer.valueOf(i++), AddExhaustionPacket.class);
		builder.put(Integer.valueOf(i++), BombTickPacket.class);
		builder.put(Integer.valueOf(i++), BorrowMaskPacket.class);
		builder.put(Integer.valueOf(i++), CycleItemModePacket.class);
		builder.put(Integer.valueOf(i++), DashImpactPacket.class);
		builder.put(Integer.valueOf(i++), EndComboPacket.class);
		builder.put(Integer.valueOf(i++), FallDistancePacket.class);
		builder.put(Integer.valueOf(i++), GetBombPacket.class);
		builder.put(Integer.valueOf(i++), OpenGuiPacket.class);
		builder.put(Integer.valueOf(i++), RefreshSpinPacket.class);
		builder.put(Integer.valueOf(i++), SetGossipStoneMessagePacket.class);
		builder.put(Integer.valueOf(i++), TargetIdPacket.class);
		builder.put(Integer.valueOf(i++), ZeldaSongPacket.class);
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
			LogHelper.warning("Error writing packet: " + e.toString());
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

	/**
	 * If message is sent to the wrong side, an exception will be thrown during handling
	 * @return True if the message is allowed to be handled on the given side
	 */
	protected boolean isValidOnSide(Side side) {
		return true;
	}

	/**
	 * Ensures the packet is valid on the given side, then reads and executes the packet
	 */
	public final void process(ByteArrayDataInput in, EntityPlayer player, Side side) throws IOException, ProtocolException {
		if (!isValidOnSide(side)) {
			throw new ProtocolException(getClass().getSimpleName() + " may only be sent to the " + (side.isClient() ? "SERVER" : "CLIENT"));
		}
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

	/**
	 * Packet that is only valid when sent to the CLIENT
	 */
	public static abstract class CustomClientPacket extends CustomPacket {
		@Override
		protected boolean isValidOnSide(Side side) {
			return side.isClient();
		}
	}

	/**
	 * Packet that is only valid when sent to the SERVER
	 */
	public static abstract class CustomServerPacket extends CustomPacket {
		@Override
		protected boolean isValidOnSide(Side side) {
			return side.isServer();
		}
	}
}
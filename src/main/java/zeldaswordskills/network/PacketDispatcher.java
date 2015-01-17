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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.bidirectional.AttackTimePacket;
import zeldaswordskills.network.bidirectional.DeactivateSkillPacket;
import zeldaswordskills.network.bidirectional.LearnSongPacket;
import zeldaswordskills.network.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.network.client.AttackBlockedPacket;
import zeldaswordskills.network.client.InLiquidPacket;
import zeldaswordskills.network.client.MortalDrawPacket;
import zeldaswordskills.network.client.PacketISpawnParticles;
import zeldaswordskills.network.client.SetNockedArrowPacket;
import zeldaswordskills.network.client.SpawnNayruParticlesPacket;
import zeldaswordskills.network.client.SyncEntityInfoPacket;
import zeldaswordskills.network.client.SyncPlayerInfoPacket;
import zeldaswordskills.network.client.SyncSkillPacket;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.network.client.UpdateBuffPacket;
import zeldaswordskills.network.client.UpdateComboPacket;
import zeldaswordskills.network.server.AddExhaustionPacket;
import zeldaswordskills.network.server.BombTickPacket;
import zeldaswordskills.network.server.BorrowMaskPacket;
import zeldaswordskills.network.server.DashImpactPacket;
import zeldaswordskills.network.server.EndComboPacket;
import zeldaswordskills.network.server.FallDistancePacket;
import zeldaswordskills.network.server.GetBombPacket;
import zeldaswordskills.network.server.OpenGuiPacket;
import zeldaswordskills.network.server.RefreshSpinPacket;
import zeldaswordskills.network.server.TargetIdPacket;
import zeldaswordskills.network.server.ZeldaSongPacket;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Houses the SimpleNetworkWrapper instance and provides wrapper methods for sending packets.
 *
 */
public class PacketDispatcher
{
	private static byte packetId = 0;

	private static final SimpleNetworkWrapper dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.CHANNEL);

	/**
	 *  Registers all packets and handlers - call this during {link FMLPreInitializationEvent}
	 */
	public static final void preInit() {
		// Bidirectional packets
		registerMessage(ActivateSkillPacket.class);
		registerMessage(AttackTimePacket.class);
		registerMessage(DeactivateSkillPacket.class);
		registerMessage(LearnSongPacket.class);
		registerMessage(PlayRecordPacket.class);
		registerMessage(PlaySoundPacket.class);

		// Packets handled on CLIENT
		registerMessage(AttackBlockedPacket.class, Side.CLIENT);
		registerMessage(InLiquidPacket.class, Side.CLIENT);
		registerMessage(MortalDrawPacket.class, Side.CLIENT);
		registerMessage(PacketISpawnParticles.class, Side.CLIENT);
		registerMessage(SetNockedArrowPacket.class, Side.CLIENT);
		registerMessage(SpawnNayruParticlesPacket.class, Side.CLIENT);
		registerMessage(SyncEntityInfoPacket.class, Side.CLIENT);
		registerMessage(SyncPlayerInfoPacket.class, Side.CLIENT);
		registerMessage(SyncSkillPacket.class, Side.CLIENT);
		registerMessage(UnpressKeyPacket.class, Side.CLIENT);
		registerMessage(UpdateBuffPacket.class, Side.CLIENT);
		registerMessage(UpdateComboPacket.class, Side.CLIENT);

		// Packets handled on SERVER
		registerMessage(AddExhaustionPacket.class, Side.SERVER);
		registerMessage(BombTickPacket.class, Side.SERVER);
		registerMessage(BorrowMaskPacket.class, Side.SERVER);
		registerMessage(DashImpactPacket.class, Side.SERVER);
		registerMessage(EndComboPacket.class, Side.SERVER);
		registerMessage(FallDistancePacket.class, Side.SERVER);
		registerMessage(GetBombPacket.class, Side.SERVER);
		registerMessage(OpenGuiPacket.class, Side.SERVER);
		registerMessage(RefreshSpinPacket.class, Side.SERVER);
		registerMessage(TargetIdPacket.class, Side.SERVER);
		registerMessage(ZeldaSongPacket.class, Side.SERVER);
	}

	/**
	 * Registers an AbstractMessage to one side
	 */
	private static final <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz, Side side) {
		PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, side);
	}

	/**
	 * Registers an AbstractMessage to both sides (bidirectional message)
	 */
	private static final <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz) {
		PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId, Side.CLIENT);
		PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
	}

	/**
	 * Send this message to the specified player.
	 * See {@link SimpleNetworkWrapper#sendTo(IMessage, EntityPlayerMP)}
	 */
	public static final void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
	}

	/**
	 * Send this message to everyone.
	 * See {@link SimpleNetworkWrapper#sendToAll(IMessage)}
	 */
	public static void sendToAll(IMessage message) {
		PacketDispatcher.dispatcher.sendToAll(message);
	}

	/**
	 * Send this message to everyone within a certain range of a point.
	 * See {@link SimpleNetworkWrapper#sendToDimension(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		PacketDispatcher.dispatcher.sendToAllAround(message, point);
	}

	/**
	 * Sends a message to everyone within a certain range of the coordinates in the same dimension.
	 */
	public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range) {
		PacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}

	/**
	 * Sends a message to everyone within a certain range of the player provided.
	 */
	public static final void sendToAllAround(IMessage message, EntityPlayer player, double range) {
		PacketDispatcher.sendToAllAround(message, player.worldObj.provider.dimensionId, player.posX, player.posY, player.posZ, range);
	}

	/**
	 * Send this message to everyone within the supplied dimension.
	 * See {@link SimpleNetworkWrapper#sendToDimension(IMessage, int)}
	 */
	public static final void sendToDimension(IMessage message, int dimensionId) {
		PacketDispatcher.dispatcher.sendToDimension(message, dimensionId);
	}

	/**
	 * Send this message to the server.
	 * See {@link SimpleNetworkWrapper#sendToServer(IMessage)}
	 */
	public static final void sendToServer(IMessage message) {
		PacketDispatcher.dispatcher.sendToServer(message);
	}
}

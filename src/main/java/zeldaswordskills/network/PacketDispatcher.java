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
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.packet.AbstractMessageHandler;
import zeldaswordskills.network.packet.bidirectional.AbstractBiMessageHandler;
import zeldaswordskills.network.packet.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.packet.bidirectional.AttackTimePacket;
import zeldaswordskills.network.packet.bidirectional.DeactivateSkillPacket;
import zeldaswordskills.network.packet.bidirectional.PlaySoundPacket;
import zeldaswordskills.network.packet.client.AbstractClientMessageHandler;
import zeldaswordskills.network.packet.client.AttackBlockedPacket;
import zeldaswordskills.network.packet.client.InLiquidPacket;
import zeldaswordskills.network.packet.client.MortalDrawPacket;
import zeldaswordskills.network.packet.client.PacketISpawnParticles;
import zeldaswordskills.network.packet.client.SetNockedArrowPacket;
import zeldaswordskills.network.packet.client.SpawnNayruParticlesPacket;
import zeldaswordskills.network.packet.client.SyncEntityInfoPacket;
import zeldaswordskills.network.packet.client.SyncPlayerInfoPacket;
import zeldaswordskills.network.packet.client.SyncSkillPacket;
import zeldaswordskills.network.packet.client.UnpressKeyPacket;
import zeldaswordskills.network.packet.client.UpdateBuffPacket;
import zeldaswordskills.network.packet.client.UpdateComboPacket;
import zeldaswordskills.network.packet.server.AbstractServerMessageHandler;
import zeldaswordskills.network.packet.server.AddExhaustionPacket;
import zeldaswordskills.network.packet.server.BombTickPacket;
import zeldaswordskills.network.packet.server.BorrowMaskPacket;
import zeldaswordskills.network.packet.server.DashImpactPacket;
import zeldaswordskills.network.packet.server.EndComboPacket;
import zeldaswordskills.network.packet.server.GetBombPacket;
import zeldaswordskills.network.packet.server.OpenGuiPacket;
import zeldaswordskills.network.packet.server.RefreshSpinPacket;
import zeldaswordskills.network.packet.server.TargetIdPacket;
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
	 *  Registers all packets and handlers - call this during {@code FMLPreInitializationEvent}
	 */
	public static final void initialize() {
		// Bi-directional packets (with side-specific handlers)
		registerMessage(PlaySoundPacket.Handler.class, PlaySoundPacket.class);

		// Bi-directional packets using standard IMessageHandler implementation (handled identically on both sides)
		registerBiMessage(ActivateSkillPacket.Handler.class, ActivateSkillPacket.class);
		registerBiMessage(AttackTimePacket.Handler.class, AttackTimePacket.class);
		registerBiMessage(DeactivateSkillPacket.Handler.class, DeactivateSkillPacket.class);

		// Packets handled on CLIENT
		registerMessage(AttackBlockedPacket.Handler.class, AttackBlockedPacket.class);
		registerMessage(InLiquidPacket.Handler.class, InLiquidPacket.class);
		registerMessage(MortalDrawPacket.Handler.class, MortalDrawPacket.class);
		registerMessage(PacketISpawnParticles.Handler.class, PacketISpawnParticles.class);
		registerMessage(SetNockedArrowPacket.Handler.class, SetNockedArrowPacket.class);
		registerMessage(SpawnNayruParticlesPacket.Handler.class, SpawnNayruParticlesPacket.class);
		registerMessage(SyncEntityInfoPacket.Handler.class, SyncEntityInfoPacket.class);
		registerMessage(SyncPlayerInfoPacket.Handler.class, SyncPlayerInfoPacket.class);
		registerMessage(SyncSkillPacket.Handler.class, SyncSkillPacket.class);
		registerMessage(UnpressKeyPacket.Handler.class, UnpressKeyPacket.class);
		registerMessage(UpdateBuffPacket.Handler.class, UpdateBuffPacket.class);
		registerMessage(UpdateComboPacket.Handler.class, UpdateComboPacket.class);

		// Packets handled on SERVER
		registerMessage(AddExhaustionPacket.Handler.class, AddExhaustionPacket.class);
		registerMessage(BombTickPacket.Handler.class, BombTickPacket.class);
		registerMessage(BorrowMaskPacket.Handler.class, BorrowMaskPacket.class);
		registerMessage(DashImpactPacket.Handler.class, DashImpactPacket.class);
		registerMessage(EndComboPacket.Handler.class, EndComboPacket.class);
		registerMessage(GetBombPacket.Handler.class, GetBombPacket.class);
		registerMessage(OpenGuiPacket.Handler.class, OpenGuiPacket.class);
		registerMessage(RefreshSpinPacket.Handler.class, RefreshSpinPacket.class);
		registerMessage(TargetIdPacket.Handler.class, TargetIdPacket.class);
	}

	/**
	 * Registers a message and message handler on the designated side;
	 * used for standard IMessage + IMessageHandler implementations
	 */
	private static final <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> handlerClass, Class<REQ> messageClass, Side side) {
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}
	
	/**
	 * Registers a message and message handler on both sides; used mainly
	 * for standard IMessage + IMessageHandler implementations and ideal
	 * for messages that are handled identically on either side
	 */
	private static final <REQ extends IMessage, REPLY extends IMessage> void registerBiMessage(Class<? extends IMessageHandler<REQ, REPLY>> handlerClass, Class<REQ> messageClass) {
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId, Side.CLIENT);
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, Side.SERVER);
	}

	/**
	 * Registers a message and message handler, automatically determining Side(s) based on the handler class
	 * @param handlerClass	Must extend one of {@link AbstractClientMessageHandler}, {@link AbstractServerMessageHandler}, or {@link AbstractBiMessageHandler}
	 */
	private static final <REQ extends IMessage> void registerMessage(Class<? extends AbstractMessageHandler<REQ>> handlerClass, Class<REQ> messageClass) {
		if (AbstractClientMessageHandler.class.isAssignableFrom(handlerClass)) {
			registerMessage(handlerClass, messageClass, Side.CLIENT);
		} else if (AbstractServerMessageHandler.class.isAssignableFrom(handlerClass)) {
			registerMessage(handlerClass, messageClass, Side.SERVER);
		} else if (AbstractBiMessageHandler.class.isAssignableFrom(handlerClass)) {
			registerBiMessage(handlerClass, messageClass);
		} else {
			throw new IllegalArgumentException("Cannot determine on which Side(s) to register " + handlerClass.getName() + " - unrecognized handler class!");
		}
	}

	/**
	 * Send this message to the specified player.
	 * See {@link SimpleNetworkWrapper#sendTo(IMessage, EntityPlayerMP)}
	 */
	public static final void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
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

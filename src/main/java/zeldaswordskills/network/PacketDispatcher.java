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

package zeldaswordskills.network;

import java.util.Collection;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.network.bidirectional.ActivateSkillPacket;
import zeldaswordskills.network.bidirectional.AttackTimePacket;
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
import zeldaswordskills.network.server.HeldBlockColorPacket;
import zeldaswordskills.network.server.OpenGuiPacket;
import zeldaswordskills.network.server.RefreshSpinPacket;
import zeldaswordskills.network.server.SetGossipStoneMessagePacket;
import zeldaswordskills.network.server.TargetIdPacket;
import zeldaswordskills.network.server.ZeldaSongPacket;
import zeldaswordskills.ref.ModInfo;

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
	 *  Registers all packets and handlers - call this during {@link FMLPreInitializationEvent}
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
		registerMessage(AttackBlockedPacket.class);
		registerMessage(InLiquidPacket.class);
		registerMessage(MortalDrawPacket.class);
		registerMessage(OpenGossipStoneEditorPacket.class);
		registerMessage(OpenSongGuiPacket.class);
		registerMessage(PacketISpawnParticles.class);
		registerMessage(SetItemModePacket.class);
		registerMessage(SetNockedArrowPacket.class);
		registerMessage(SpawnNayruParticlesPacket.class);
		registerMessage(SyncConfigPacket.class);
		registerMessage(SyncEntityInfoPacket.class);
		registerMessage(SyncPlayerInfoPacket.class);
		registerMessage(SyncSkillPacket.class);
		registerMessage(UnpressKeyPacket.class);
		registerMessage(UpdateBuffPacket.class);
		registerMessage(UpdateComboPacket.class);

		// Packets handled on SERVER
		registerMessage(AddExhaustionPacket.class);
		registerMessage(BombTickPacket.class);
		registerMessage(BorrowMaskPacket.class);
		registerMessage(CycleItemModePacket.class);
		registerMessage(DashImpactPacket.class);
		registerMessage(EndComboPacket.class);
		registerMessage(FallDistancePacket.class);
		registerMessage(GetBombPacket.class);
		registerMessage(HeldBlockColorPacket.class);
		registerMessage(OpenGuiPacket.class);
		registerMessage(RefreshSpinPacket.class);
		registerMessage(SetGossipStoneMessagePacket.class);
		registerMessage(TargetIdPacket.class);
		registerMessage(ZeldaSongPacket.class);
	}

	/**
	 * Registers an {@link AbstractMessage} to the appropriate side(s)
	 */
	private static final <T extends AbstractMessage<T> & IMessageHandler<T, IMessage>> void registerMessage(Class<T> clazz) {
		if (AbstractMessage.AbstractClientMessage.class.isAssignableFrom(clazz)) {
			PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.CLIENT);
		} else if (AbstractMessage.AbstractServerMessage.class.isAssignableFrom(clazz)) {
			PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
		} else {
			PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId, Side.CLIENT);
			PacketDispatcher.dispatcher.registerMessage(clazz, clazz, packetId++, Side.SERVER);
		}
	}

	/**
	 * Send this message to the specified player's client-side counterpart.
	 * See {@link SimpleNetworkWrapper#sendTo(IMessage, EntityPlayerMP)}
	 */
	public static final void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
	}

	/**
	 * Sends this message to players provided. SERVER->CLIENT only.
	 */
	public static void sendToPlayers(IMessage message, Collection<EntityPlayer> players) {
		for (EntityPlayer player : players) {
			if (player instanceof EntityPlayerMP) {
				PacketDispatcher.dispatcher.sendTo(message, (EntityPlayerMP) player);
			}
		}
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
	 * See {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static final void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
		PacketDispatcher.dispatcher.sendToAllAround(message, point);
	}

	/**
	 * Sends a message to everyone within a certain range of the coordinates in the same dimension.
	 * Shortcut to {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static final void sendToAllAround(IMessage message, int dimension, double x, double y, double z, double range) {
		PacketDispatcher.sendToAllAround(message, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
	}

	/**
	 * Sends a message to everyone within a certain range of the entity provided.
	 * Shortcut to {@link SimpleNetworkWrapper#sendToAllAround(IMessage, NetworkRegistry.TargetPoint)}
	 */
	public static final void sendToAllAround(IMessage message, Entity entity, double range) {
		PacketDispatcher.sendToAllAround(message, entity.worldObj.provider.getDimensionId(), entity.posX, entity.posY, entity.posZ, range);
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

	/**
	 * Sends a vanilla Packet to a player. SERVER->CLIENT only.
	 */
	public static void sendTo(Packet packet, EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
		}
	}

	/**
	 * Sends a vanilla Packet to players provided. SERVER->CLIENT only.
	 */
	public static void sendToPlayers(Packet packet, Collection<EntityPlayer> players) {
		for (EntityPlayer player : players) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
			}
		}
	}

	/**
	 * Sends a vanilla Packet to all players in the list except for the one player. SERVER->CLIENT only.
	 */
	public static void sendToPlayersExcept(Packet packet, EntityPlayer player, Collection<EntityPlayer> players) {
		for (EntityPlayer p : players) {
			if (p != player && p instanceof EntityPlayerMP) {
				((EntityPlayerMP) p).playerNetServerHandler.sendPacket(packet);
			}
		}
	}

	/**
	 * Sends a vanilla Packet to all players in the same dimension. SERVER->CLIENT only.
	 */
	public static void sendToAll(Packet packet, World world) {
		if (world instanceof WorldServer) {
			for (Object o : ((WorldServer) world).playerEntities) {
				if (o instanceof EntityPlayerMP) {
					((EntityPlayerMP) o).playerNetServerHandler.sendPacket(packet);
				}
			}
		}
	}

	/**
	 * Sends a vanilla Packet to all players within the given range of an entity. SERVER->CLIENT only.
	 */
	public static void sendToAllAround(Packet packet, Entity entity, int range) {
		int rangeSq = (range * range);
		if (entity.worldObj instanceof WorldServer) {
			for (Object o : ((WorldServer) entity.worldObj).playerEntities) {
				if (o instanceof EntityPlayerMP) {
					EntityPlayerMP player = (EntityPlayerMP) o;
					if (player.getDistanceSqToEntity(entity) <= rangeSq) {
						((EntityPlayerMP) o).playerNetServerHandler.sendPacket(packet);
					}
				}
			}
		}
	}
}

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

package zeldaswordskills.songs;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.WarpPoint;
import zeldaswordskills.world.TeleporterNoPortal;

/**
 * Class for songs which teleport the player.
 * Current implementation is for {@link BlockWarpStone}, but the class has methods
 * which may be overridden to alter requirements and outcomes.
 */
public class ZeldaSongWarp extends AbstractZeldaSong {

	public ZeldaSongWarp(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	/**
	 * Returns the point to which the player will teleport when this song is played
	 */
	protected WarpPoint getWarpPoint(EntityPlayer player) {
		WarpPoint warp = ZSSPlayerSongs.get(player).getWarpPoint(this);
		return (warp == null ? Config.getDefaultWarpPoint(this) : warp);
	}

	/**
	 * Return true to allow inter-dimensional travel
	 */
	protected boolean canCrossDimensions() {
		return true;
	}

	/**
	 * Return true if the block at the target position is a valid spot to teleport
	 */
	protected boolean isBlockValid(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return (state.getBlock() instanceof BlockWarpStone && ((BlockWarpStone.EnumWarpSong) state.getValue(BlockWarpStone.WARP_SONG)).getWarpSong() == this);
	}

	@Override
	protected boolean hasEffect(EntityPlayer player, ItemStack instrument, int power) {
		return power > 4;
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		WarpPoint warp = getWarpPoint(player);
		int dimension = player.worldObj.provider.getDimensionId();
		if (warp == null) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.warp.null");
		} else if (!canCrossDimensions() && dimension != warp.dimensionId) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.warp.dimension");
		} else if (dimension == 1 && warp.dimensionId != 1) { // can't teleport from the end to other dimensions
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.warp.end");
		} else {
			if (player.ridingEntity != null) {
				player.mountEntity(null);
			}
			double dx = player.posX;
			double dy = player.posY;
			double dz = player.posZ;
			if (dimension != warp.dimensionId) {
				((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, warp.dimensionId, new TeleporterNoPortal((WorldServer) player.worldObj));
			}
			boolean noBlock = true; // true if warp block not found
			boolean noAir = false; // true if new position is not suitable
			if (isBlockValid(player.worldObj, warp.pos)) {
				noBlock = false;
				if (!EntityAITeleport.teleportTo(player.worldObj, player, (double) warp.pos.getX() + 0.5D, warp.pos.getY() + 1, (double) warp.pos.getZ() + 0.5D, null, true, false)) {
					noAir = true;
				}
			}
			// set back to original dimension and position if new position invalid
			if (noBlock || noAir) {
				if (dimension != warp.dimensionId) {
					((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, dimension, new TeleporterNoPortal((WorldServer) player.worldObj));
				}
				player.setPositionAndUpdate(dx, dy, dz);
				PlayerUtils.sendTranslatedChat(player, noAir ? "chat.zss.song.warp.blocked" : "chat.zss.song.warp.missing");
			} else {
				PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
			}
		}
	}
}

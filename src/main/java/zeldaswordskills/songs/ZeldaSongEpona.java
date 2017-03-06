/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.songs;

import java.util.List;

import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;

/**
 * Epona's song tames nearby horses and, if played on a powerful instrument,
 * teleports the last ridden horse to the player so long as the horse is in
 * the same dimension.
 */
public class ZeldaSongEpona extends AbstractZeldaSong {

	public ZeldaSongEpona(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		List<EntityHorse> horses = player.worldObj.getEntitiesWithinAABB(EntityHorse.class, player.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D));
		for (EntityHorse horse : horses) {
			if (!horse.isTame()) {
				horse.setTamedBy(player);
				// 18 is flag for heart particles
				player.worldObj.setEntityState(horse, (byte) 18);
			}
		}
		ZSSPlayerSongs songs = ZSSPlayerSongs.get(player);
		// Check for cows for Lon Lon Milk (Fairy Ocarina can be used if you don't want to summon Epona)
		List<EntityCow> cows = player.worldObj.getEntitiesWithinAABB(EntityCow.class, player.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D));
		for (EntityCow cow : cows) {
			songs.addLonLonCow(cow);
		}
		if (power < 5) {
			return; // only maximum power instruments can teleport Epona
		}
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.getEntityBoundingBox().maxY);
		int z = MathHelper.floor_double(player.posZ);
		if (!player.worldObj.provider.isSurfaceWorld()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.epona.dimension");
		} else if (!player.worldObj.canBlockSeeSky(new BlockPos(x, y, z))) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.epona.sky");
		} else {
			EntityHorse epona = songs.getLastHorseRidden();
			if (epona == null) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.song.epona.missing");
			} else {
				// TODO check for clear space where horse should spawn?
				if (epona.riddenByEntity != null) {
					epona.riddenByEntity.mountEntity(null);
				}
				if (epona.getLeashed()) {
					epona.clearLeashed(true, true);
				}
				Vec3 vec3 = player.getLookVec();
				epona.setPosition(player.posX + (vec3.xCoord * 2.0D), player.posY + 1, player.posZ + (vec3.zCoord * 2.0D));
				S18PacketEntityTeleport packet = new S18PacketEntityTeleport(epona);
				PacketDispatcher.sendTo(packet, player); // send to ocarina player first, maybe it will be faster 
				PacketDispatcher.sendToPlayersExcept(packet, player, ((WorldServer) player.worldObj).getEntityTracker().getTrackingPlayers(epona));
				epona.makeHorseRearWithSound();
				songs.setHorseRidden(epona); // sets last chunk coordinates in case player doesn't mount
			}
		}
	}
}

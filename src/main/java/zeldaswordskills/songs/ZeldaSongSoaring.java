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

package zeldaswordskills.songs;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.util.SongNote;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * Sends the player to their spawn coordinates (either their bed or the world spawn point)
 */
public class ZeldaSongSoaring extends AbstractZeldaSong {

	public ZeldaSongSoaring(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	@Override
	protected boolean hasEffect(EntityPlayer player, ItemStack instrument, int power) {
		// Not usable in the Nether or the End, mainly due to unpredictable results
		return power > 4 && Math.abs(player.dimension) != 1;
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		ChunkCoordinates cc = player.getBedLocation(player.dimension);
		if (cc != null) {
			cc = EntityPlayer.verifyRespawnCoordinates(player.worldObj, cc, player.isSpawnForced(player.dimension));
		}
		if (cc == null) {
			cc = player.worldObj.getSpawnPoint();
		}
		if (cc != null) {
			if (player.ridingEntity != null) {
				player.mountEntity(null);
			}
			player.setPosition((double) cc.posX + 0.5D, (double) cc.posY + 0.1D, (double) cc.posZ + 0.5D);
			while (!player.worldObj.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()) {
				player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
			}
			player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
			PacketDispatcher.sendPacketToPlayer(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F).makePacket(), (Player) player);
		}
	}
}

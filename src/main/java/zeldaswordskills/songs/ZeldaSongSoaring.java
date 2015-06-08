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
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.SongNote;

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
		BlockPos pos = player.getBedLocation(player.dimension);
		if (pos != null) {
			pos = EntityPlayer.getBedSpawnLocation(player.worldObj, pos, player.isSpawnForced(player.dimension));
		}
		if (pos == null) {
			pos = player.worldObj.getSpawnPoint();
		}
		if (pos != null) {
			if (player.ridingEntity != null) {
				player.mountEntity(null);
			}
			player.setPosition((double) pos.getX() + 0.5D, (double) pos.getY() + 0.1D, (double) pos.getZ() + 0.5D);
			while (!player.worldObj.getCollidingBoundingBoxes(player, player.getEntityBoundingBox()).isEmpty()) {
				player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
			}
			player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
			PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
		}
	}
}

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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.SongNote;

/**
 * Playable once per day to cure the player of all ailments and restore full health.
 */
public class ZeldaSongHealing extends AbstractZeldaSong {

	public ZeldaSongHealing(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		if (power > 4 && ZSSPlayerSongs.get(player).canHealFromSong()) {
			PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
			player.curePotionEffects(new ItemStack(Items.milk_bucket));
			player.heal(player.getMaxHealth());
			ZSSPlayerSongs.get(player).setNextHealTime();
		}
	}
}

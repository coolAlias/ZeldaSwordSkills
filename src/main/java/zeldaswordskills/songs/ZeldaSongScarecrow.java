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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * 
 * The Scarecrow Song is the only song with notes determined by the player.
 * 
 */
public final class ZeldaSongScarecrow extends AbstractZeldaSong {

	public ZeldaSongScarecrow() {
		super("scarecrow", 160);
	}

	@Override
	public boolean canLearn(EntityPlayer player) {
		// TODO would be nice if the code from ZSSPlayerSongs#learnSong could be handled here
		return true;
	}

	@Override
	public boolean canLearnFromCommand() {
		return false;
	}

	@Override
	public boolean canLearnFromInscription(World world, int x, int y, int z, Block block, int meta) {
		return false;
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
		// TODO
		PacketDispatcher.sendPacketToPlayer(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F).makePacket(), (Player) player);
	}
}

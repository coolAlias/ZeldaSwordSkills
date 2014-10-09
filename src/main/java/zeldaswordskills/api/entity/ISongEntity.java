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

package zeldaswordskills.api.entity;

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.ref.ZeldaSong;

/**
 * 
 * Entities that react to {@link ZeldaSong}s should use this interface.
 *
 */
public interface ISongEntity {

	/**
	 * Called when a song is played nearby; typical range is about 8 blocks.
	 * Note that it is only called on the server side.
	 * @param player	Player who played the song
	 * @param song		Song that was played
	 */
	public void onSongPlayed(EntityPlayer player, ZeldaSong song);

}

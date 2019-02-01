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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.util.SongNote;

/**
 * Songs with no particular effect in and of themselves, but may have special
 * effects defined by {@link ISongBlock} and {@link ISongEntity} classes.
 */
public final class ZeldaSongNoEffect extends AbstractZeldaSong {

	public ZeldaSongNoEffect(String unlocalizedName, int minDuration, SongNote... notes) {
		super(unlocalizedName, minDuration, notes);
	}

	@Override
	protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {}

}

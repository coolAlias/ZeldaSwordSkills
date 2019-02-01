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

package zeldaswordskills.api.block;

import zeldaswordskills.api.entity.CustomExplosion;

/**
 * Flags blocks that may be affected by {@link CustomExplosion custom explosions}
 * even in Adventure Mode or when the explosion is targeting a specific block.
 * <br><br>
 * Non-griefing explosions do not affect any blocks, even IExplodable ones.
 *
 */
public interface IExplodable {

}

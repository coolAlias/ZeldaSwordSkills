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

package zeldaswordskills.api.entity;

/**
 * 
 * Use these in conjunction with IEntityBomb and the static createExplosion methods
 * to automatically apply certain characteristics to custom explosions.
 * 
 * If different traits are required, create a CustomExplosion object and change its
 * variables as needed, rather than using the static methods.
 *
 */
public enum BombType {
	/** Applies vanilla explosion rules */
	BOMB_STANDARD("standard"),
	/** Ignores water when determining which blocks to destroy; less effective in the Nether */
	BOMB_WATER("water"),
	/** Ignores lava when determining which blocks to destroy */
	BOMB_FIRE("fire");

	/** The unlocalized name of the bomb type, e.g. 'standard', 'fire', etc. */
	public final String unlocalizedName;
	
	private BombType(String name) {
		this.unlocalizedName = name;
	}
}

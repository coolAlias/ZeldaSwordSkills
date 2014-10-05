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

/**
 * 
 * For entities specially affected by Light Arrows
 *
 */
public interface IEntityEvil {

	/**
	 * Return true if this entity may be instantly killed by Light Arrows.
	 * If false, {@link #getLightArrowDamage} will be inflicted instead.
	 */
	public boolean isLightArrowFatal();

	/**
	 * Return the amount of damage to inflict when {@link #isLightArrowFatal} returns false
	 * @param amount	Initial damage amount that would be dealt
	 * @return			Amount of damage to inflict
	 */
	public float getLightArrowDamage(float amount);

}

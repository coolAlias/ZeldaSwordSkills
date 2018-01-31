/**
    Copyright (C) <2018> <coolAlias>

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

import net.minecraft.entity.projectile.EntityArrow;

/**
 * 
 * For entities specially affected by e.g. Light and Silver Arrows
 *
 */
public interface IEntityEvil {

	/**
	 * Return true if this entity may be instantly killed by the arrow entity.
	 * If false, {@link #getLightArrowDamage} will be inflicted instead.
	 * @param arrow The arrow entity inflicting damage
	 */
	boolean isLightArrowFatal(EntityArrow arrow);

	/**
	 * Return the amount of damage to inflict when {@link #isLightArrowFatal} returns false
	 * @param arrow The arrow entity inflicting damage
	 * @param amount Initial damage amount that would be dealt
	 * @return Amount of damage to inflict
	 */
	float getLightArrowDamage(EntityArrow arrow, float amount);

}

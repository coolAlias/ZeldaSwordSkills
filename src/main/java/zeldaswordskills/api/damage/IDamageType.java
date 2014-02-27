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

package zeldaswordskills.api.damage;

import java.util.Set;

/**
 * 
 * Interface for DamageSources that have a custom EnumDamageType
 * 
 * Note that damage resistances (and weaknesses) are cumulative, so if a DamageSource
 * has multiple damage descriptors, it's possible for the amount to be reduced or
 * augmented once for each type.
 * 
 * For example, if the damage was both fire and magic, an entity with resistances
 * to both would reduce the damage by both resistance amounts.
 *
 */
public interface IDamageType {

	/**
	 * Returns a list of all custom enumerated damage types associated with this DamageSource
	 */
	public Set<EnumDamageType> getEnumDamageTypes();
	
}

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

package zeldaswordskills.util;

import net.minecraft.entity.EnumCreatureType;

/**
 * 
 * Helper class containing data needed to add spawn rates for an entity
 *
 */
public class SpawnableEntityData
{
	/** The creature type used to determine spawning behavior */
	public final EnumCreatureType creatureType;

	/** Minimum number of entities to spawn in a group */
	public final int min;

	/** Maximum number of entities that may spawn in a group */
	public final int max;

	/** Spawn rate for this entity */
	public final int spawnRate;

	public SpawnableEntityData(EnumCreatureType creatureType, int min, int max, int defaultSpawnRate) {
		this.creatureType = creatureType;
		this.min = min;
		this.max = max;
		this.spawnRate = defaultSpawnRate;
	}
}

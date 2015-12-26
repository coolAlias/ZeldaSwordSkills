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

import net.minecraft.entity.Entity;
import zeldaswordskills.entity.ZSSEntityInfo;

/**
 * 
 * Use this interface if the IEntityBomb is also ingestible, i.e. it can be eaten.
 * 
 * Note that bombs ingested using {@link ZSSEntityInfo#onBombIngested} do not persist;
 * i.e. if an entity ingests a bomb and then the entity is re-loaded from NBT, it
 * will no longer have a bomb in its belly.
 *
 */
public interface IEntityBombIngestible extends IEntityBomb {

	/**
	 * The amount of damage the explosion will cause, or 0 for default explosion damage.
	 * Note that the entity which ingested the bomb will die by default, but it may
	 * implement {@link IEntityBombEater} to define custom behavior.
	 * @param entity The entity which ingested the bomb
	 */
	float getExplosionDamage(Entity entity);

	/**
	 * Sets the damage the explosion will cause
	 * @param damage Note that 0.0F will result in the default vanilla explosion damage calculations
	 */
	IEntityBomb setExplosionDamage(float damage);

	/**
	 * The radius of the resulting explosion
	 * @param entity The entity which ingested the bomb
	 */
	float getExplosionRadius(Entity entity);

	IEntityBomb setExplosionRadius(float radius);

	/**
	 * The number of ticks before the ingested bomb explodes
	 * @param entity The entity which ingested the bomb
	 */
	int getFuseTime(Entity entity);

	IEntityBomb setFuseTime(int time);

}

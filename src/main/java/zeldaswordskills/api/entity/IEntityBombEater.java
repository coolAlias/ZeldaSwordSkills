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

import cpw.mods.fml.common.eventhandler.Event.Result;
import zeldaswordskills.entity.ZSSEntityInfo;

/**
 * 
 * For entities with specific behavior when an ingested {@link IEntityBombIngestible}
 * explodes.
 *
 */
public interface IEntityBombEater {
	/**
	 * Method called from {@link EntityBomb} when it collides with a bomb-eating entity
	 * @return
	 * Result.DEFAULT to ingest the bomb using the default {@link ZSSEntityInfo#onBombIngested}
	 * Result.ALLOW if the bomb was ingested using custom handling
	 * Result.DENY to prevent the bomb from being ingested
	 */
	public Result ingestBomb(IEntityBombIngestible bomb);

	/**
	 * This method is called when an ingested bomb is about to explode inside the
	 * entity, provided that it was ingested using {@link ZSSEntityInfo#onBombIngested}.
	 * Any non-standard implementations should occur here, returning FALSE if no further
	 * processing is needed.
	 * @param bomb Contains information about BombType, blast radius, damage, etc.
	 * @return TRUE to continue with standard processing, i.e. check if the ingested
	 * 			bomb should explode and whether it is fatal
	 */
	public boolean onBombIndigestion(IEntityBombIngestible bomb);

	/**
	 * Return TRUE to cause the standard explosion of ingested bombs, or FALSE for no explosion
	 */
	public boolean doesIngestedBombExplode(IEntityBombIngestible bomb);

	/**
	 * Return TRUE if the ingested bomb is fatal to this entity
	 */
	public boolean isIngestedBombFatal(IEntityBombIngestible bomb);

}

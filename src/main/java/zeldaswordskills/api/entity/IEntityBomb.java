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

import zeldaswordskills.api.block.IExplodable;


/**
 * 
 * Interface for any entity that can cause an explosion involving BombType. This
 * should be used in conjunction with the CustomExplosions class methods for
 * causing explosions.
 *
 */
public interface IEntityBomb {
	
	/** Get this bomb's type */
	public BombType getType();
	
	/** Factor by which affected entity's motion will be multiplied */
	public float getMotionFactor();

	/**
	 * Factor by which to modify the radius of block destruction:
	 * below 1.0F restricts the radius; above 1.0F expands the radius
	 * Maximum radius * destruction factor is capped at 16.0F, which is mighty big.
	 */
	public float getDestructionFactor();

	/**
	 * Return false to restrict this bomb's destruction in Adventure Mode,
	 * allowing it to affect only {@link IExplodable} blocks
	 */
	public boolean canGriefAdventureMode();

}

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

package zeldaswordskills.entity.ai;

import net.minecraft.util.MovingObjectPosition;
import zeldaswordskills.entity.projectile.EntityMeleeTracker;

/**
 * 
 * Interface for entities using {@link EntityMeleeTracker} to determine if their
 * attacks have struck a target.
 *
 */
public interface IMeleeAttacker {

	/**
	 * Called when the projectile tracker strikes an object of some kind; it's up
	 * to each implementation to determine if the target is valid, in range, etc.
	 * @param flag	A custom flag optionally set by IMeleeAttacker when spawning the {@link EntityMeleeTracker}
	 */
	public void onMeleeImpact(MovingObjectPosition mop, int flag);

	/**
	 * Called after the projectile tracker expires without impacting anything.
	 * @param flag	A custom flag optionally set by IMeleeAttacker when spawning the {@link EntityMeleeTracker}
	 */
	public void onMeleeMiss(int flag);

}

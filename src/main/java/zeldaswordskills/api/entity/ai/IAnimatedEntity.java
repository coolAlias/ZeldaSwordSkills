/**
    Copyright (C) <2016> <coolAlias>

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

package zeldaswordskills.api.entity.ai;

import java.util.List;

public interface IAnimatedEntity {

	/**
	 * Return a collection containing all currently active actions for this entity
	 */
	List<EntityAction> getActiveActions();

	/**
	 * Return the speed at which to perform the current action
	 * @param action_id Id of the action requesting the speed value
	 */
	float getActionSpeed(int action_id);

	/**
	 * Return the current number of ticks for which the action has been active
	 * @param action_id Id of the action requesting the timer
	 */
	int getActionTime(int action_id);

}

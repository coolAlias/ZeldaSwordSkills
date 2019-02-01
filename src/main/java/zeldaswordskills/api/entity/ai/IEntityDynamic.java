/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.api.entity.ai;

/**
 * 
 * Contains methods specifically for interacting with {@link IEntityDynamicAI} types
 * of AI, such as the default {@link EntityAIDynamic} implementation.
 *
 */
public interface IEntityDynamic extends IAnimatedEntity {

	/**
	 * Return true if the current action is allowed to begin and/or continue
	 */
	boolean canExecute(int action_id, IEntityDynamicAI ai);

	/**
	 * Called from the {@link IEntityDynamicAI} when the action first begins
	 * @param action_id Usually the same as retrieved from the AI
	 * @param ai AI instance provided for ease of access to AI fields and methods
	 */
	void beginAction(int action_id, IEntityDynamicAI ai);

	/**
	 * Called from the {@link IEntityDynamicAI} once the action has fully completed
	 * @param action_id Usually the same as retrieved from the AI
	 * @param ai AI instance provided for ease of access to AI fields and methods
	 */
	void endAction(int action_id, IEntityDynamicAI ai);

	/**
	 * Called from the {@link IEntityDynamicAI} when the (usually delayed) action should be performed
	 * @param action_id Usually the same as retrieved from the AI
	 * @param ai AI instance provided for ease of access to AI fields and methods
	 */
	void performAction(int action_id, IEntityDynamicAI ai);

}

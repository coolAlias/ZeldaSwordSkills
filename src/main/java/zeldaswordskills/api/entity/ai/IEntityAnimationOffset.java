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

public interface IEntityAnimationOffset {

	/**
	 * Return an integer offset for animations that use the entity's ticksExisted
	 * so that not all entities perform the animation in sync.
	 * @param action_id Usually the {@link EntityAction} id, but could be anything passed by the model
	 */
	int getTicksExistedOffset(int action_id);

}

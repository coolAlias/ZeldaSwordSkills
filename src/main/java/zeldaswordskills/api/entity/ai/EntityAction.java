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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

/**
 * 
 * Wrapper for entity actions; expected to be used in combination with entities
 * implementing either {@link IAnimatedEntity} or {@link IEntityDynamic}.
 *
 */
public class EntityAction
{
	/** Action ID, e.g. for use as the bit flag sent to {@link EntityLivingBase#handleHealthUpdate} */
	public final int id;

	/** Base duration of this action */
	public final int duration;

	/** Default frame on which any special action should occur */
	public final int action_frame;

	/**
	 * @param id See {@link #id}; this is used for the hash code and should be unique within the expected action group
	 * @param duration See {@link #duration}
	 * @param action_frame See {@link #action_frame}
	 */
	public EntityAction(int id, int duration, int action_frame) {
		this.id = id;
		this.duration = duration;
		this.action_frame = action_frame;
	}

	/**
	 * Returns the actual frame on which the action should be performed based on the given speed
	 */
	public int getActionFrame(float speed) {
		return MathHelper.ceiling_float_int((float) action_frame / speed);
	}

	/**
	 * Returns the total duration of this action based on the given speed
	 */
	public int getDuration(float speed) {
		return MathHelper.ceiling_float_int((float) duration / speed);
	}

	@Override
	public int hashCode() {
		return 31 + id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		return ((EntityAction) obj).id == this.id;
	}
}

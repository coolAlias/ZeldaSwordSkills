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

package zeldaswordskills.api.client.animation;

/**
 * 
 * Provides a base for all other animation classes.
 *
 */
public abstract class AnimationBase implements IAnimation
{
	protected boolean allowInvert = true;
	protected boolean allowMultiplier = true;
	protected boolean allowOffset = true;
	protected boolean allowSpeed = true;

	/**
	 * Sets whether this animation may be inverted by the 'invert' parameter of {@link IAnimation#apply}.
	 * Note that it is up to each concrete implementation to respect this.
	 */
	public AnimationBase setAllowInversion(boolean allow) {
		this.allowInvert = allow;
		return this;
	}

	/**
	 * Sets whether this animation's magnitude may be multiplied by the 'multiplier' from {@link IAnimation#apply}.
	 * Note that it is up to each concrete implementation to respect this.
	 */
	public AnimationBase setAllowMultiplier(boolean allow) {
		this.allowMultiplier = allow;
		return this;
	}

	/**
	 * Sets whether this animation may be offset by the 'offset' amount from {@link IAnimation#apply}.
	 * Note that it is up to each concrete implementation to respect this.
	 */
	public AnimationBase setAllowOffset(boolean allow) {
		this.allowOffset = allow;
		return this;
	}

	/**
	 * Sets whether this animation's speed may be multiplied by the 'speed' parameter from {@link IAnimation#apply}.
	 * Note that it is up to each concrete implementation to respect this.
	 */
	public AnimationBase setAllowSpeed(boolean allow) {
		this.allowSpeed = allow;
		return this;
	}
}

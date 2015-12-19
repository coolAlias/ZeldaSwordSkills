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

package zeldaswordskills.api.client.animation;

import java.util.Collection;

import javax.annotation.Nullable;

public interface IAnimation
{
	/**
	 * Applies the animation based on the current frame and partial tick. Note that some
	 * of the parameters' effects are entirely up to the animation implementation.
	 * @param frame The current animation frame
	 * @param partialTick Partial progress towards the next frame
	 * @param speed Dynamic value describing the animation speed
	 * @param multiplier Dynamic value that may affect the magnitude of the animation
	 * @param offset Dynamic value that may change where the animation begins and ends
	 * @param invert True to invert this animation if possible
	 */
	void apply(int frame, float partialTick, float speed, float multiplier, float offset, boolean invert);

	/**
	 * Return true if the animation should be applied this frame
	 * @param speed Same speed that will be passed to {@link #apply}
	 */
	boolean shouldApply(int frame, float partialTick, float speed);

	/**
	 * 
	 * Class containing a few helper methods to handle IAnimations
	 *
	 */
	public static class Helper
	{
		/**
		 * Applies a collection of animation targets to their respective model pieces; see {@link IAnimation#apply}
		 * @return true if the collection is not null and at least one of the animations was applied this frame
		 */
		public static boolean applyAnimation(@Nullable Collection<IAnimation> animations, int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
			boolean flag = false;
			if (animations != null) {
				for (IAnimation animation : animations) {
					if (animation.shouldApply(frame, partialTick, speed)) {
						animation.apply(frame, partialTick, speed, multiplier, offset, invert);
						flag = true;
					}
				}
			}
			return flag;
		}

		/**
		 * Applies a single animation target to its respective model piece(s); see {@link IAnimation#apply}
		 * @param animation must not be null
		 * @return true if the animation was applied
		 */
		public static boolean applyAnimation(IAnimation animation, int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
			if (animation.shouldApply(frame, partialTick, speed)) {
				animation.apply(frame, partialTick, speed, multiplier, offset, invert);
				return true;
			}
			return false;
		}
	}
}

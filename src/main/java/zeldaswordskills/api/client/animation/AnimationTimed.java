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

import net.minecraft.util.MathHelper;

/**
 * 
 * An animation that plays only after a certain frame has been reached.
 * 
 * Note that the animation is expected to be applied even after the endFrame
 * has passed, with further animations applied on top of it.
 * 
 * As such, the order in which timed animations are applied is very important;
 * generally, animations for an individual model piece should be applied ordered
 * first by start frame, and from there possibly by the RotationAxis, if any,
 * on which the animation will be applied.
 *
 */
public abstract class AnimationTimed extends AnimationBase
{
	protected final float startFrame;
	protected final float endFrame;
	protected boolean strict;
	protected IProgressType progressType = IProgressType.LINEAR;

	/**
	 * Constructs a timed animation object whose timing can be modified dynamically by the speed parameter
	 * @param startFrame  The frame on which this animation should begin to be applied
	 * @param endFrame    The frame on which this animation should be fully applied
	 */
	public AnimationTimed(int startFrame, int endFrame) {
		this.startFrame = startFrame;
		this.endFrame = endFrame;
	}

	/**
	 * Sets this animation to 'strict' mode: animation will only be applied within the target frames
	 */
	public AnimationTimed setStrict() {
		this.strict = true;
		return this;
	}

	/**
	 * Sets the animation's progress type; see {@link IProgressType}
	 */
	public AnimationTimed setProgressType(IProgressType type) {
		this.progressType = type;
		return this;
	}

	@Override
	public boolean shouldApply(int frame, float partialTick, float speed) {
		if (this.strict && (float) frame > (this.endFrame / speed)) {
			return false;
		}
		return (float) frame >= (this.startFrame / speed);
	}

	/**
	 * Returns the animation's progress towards completion, clamped between 0.0F and 1.0F
	 */
	protected final float getProgress(int frame, float partialTick, float speed) {
		float start = (this.startFrame / speed);
		float end = (this.endFrame / speed);
		float f = (float) frame - start;
		return this.progressType.getProgress(MathHelper.clamp_float((f + partialTick) / (end - start), 0.0F, 1.0F));
	}
}

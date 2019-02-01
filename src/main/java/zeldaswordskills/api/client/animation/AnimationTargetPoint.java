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

import net.minecraft.client.model.ModelRenderer;

/**
 * 
 * Animation that modifies the model part's rotation point (i.e. base model position).
 *
 */
public class AnimationTargetPoint extends AnimationTimed
{
	protected final ModelRenderer part;
	protected final RotationAxis axis;
	protected final float targetPosition;

	/**
	 * Constructs an animation with the rotation point offset relative to the model part.
	 * See {@link #AnimationTargetPoint(ModelRenderer, RotationAxis, float, int, int, boolean) AnimationTargetPoint}.
	 */
	public AnimationTargetPoint(ModelRenderer part, RotationAxis axis, float targetPosition, int startFrame, int endFrame) {
		this(part, axis, targetPosition, startFrame, endFrame, true);
	}

	/**
	 * Constructs an animation with the rotation point offset either absolute or relative.
	 * Also see {@link AnimationTimed#AnimationTimed(int, int) AnimationTimed}.
	 * @param part           Reference to ModelRenderer piece from the model class on which to apply rotation
	 * @param axis           The {@link RotationAxis} on which the offset will be applied
	 * @param targetPosition Target rotation point expected by the end of the animation, usually relative to the original position
	 * @param relative       True to add the model part's original rotation point value to the targetOffset
	 */
	public AnimationTargetPoint(ModelRenderer part, RotationAxis axis, float targetPosition, int startFrame, int endFrame, boolean relative) {
		super(startFrame, endFrame);
		this.part = part;
		this.axis = axis;
		this.targetPosition = (relative ? this.getCurrentAmount() + targetPosition : targetPosition);
	}

	@Override
	public void apply(int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
		invert &= this.allowInvert;
		multiplier = (this.allowMultiplier ? multiplier : 1.0F);
		offset = (this.allowOffset ? offset : 0.0F);
		speed = (this.allowSpeed ? speed : 1.0F);
		float progress = getProgress(frame, partialTick, speed);
		this.apply(getOffsetAmount(progress, multiplier, offset, invert));
	}

	/**
	 * Apply the amount to the appropriate axis, part, and field (e.g. rotation point or offset)
	 */
	protected void apply(float amount) {
		this.axis.addRotationPoint(this.part, amount);
	}

	/**
	 * Returns the amount to add to the rotation point based on the current value and {@link #getProgress(int, float, float) progress}
	 */
	protected float getOffsetAmount(float progress, float multiplier, float offset, boolean invert) {
		float target = (invert ? -this.targetPosition : this.targetPosition);
		target = (target + (invert ? -offset : offset)) - this.getCurrentAmount();
		return multiplier * progress * target;
	}

	/**
	 * Return the current value (e.g. rotation point or offset) for the part / axis combo
	 */
	protected float getCurrentAmount() {
		return this.axis.getRotationPoint(this.part);
	}
}

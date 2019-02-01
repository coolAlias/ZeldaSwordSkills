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
 * Animation that modifies the model part's rotation angle on the specified axis.
 *
 */
public class AnimationTargetAngle extends AnimationTimed
{
	protected final ModelRenderer part;
	protected final RotationAxis axis;
	protected final float targetAngle;

	/**
	 * Constructs an animation target with the target angle defined in either radians or degrees.
	 * Also see {@link AnimationTimed#AnimationTimed(int, int) AnimationTimed}.
	 * @param part        Reference to ModelRenderer piece from the model class on which to apply rotation
	 * @param axis        The {@link RotationAxis} to which rotation will be applied
	 * @param targetAngle Target angle expected to be in radians, not degrees
	 * @param convert     Pass true to convert the targetAngle from degrees to radians
	 */
	public AnimationTargetAngle(ModelRenderer part, RotationAxis axis, float targetAngle, int startFrame, int endFrame, boolean convert) {
		super(startFrame, endFrame);
		this.part = part;
		this.axis = axis;
		this.targetAngle = (convert ? (float) Math.toRadians(targetAngle) : targetAngle);
	}

	/**
	 * Constructs an animation target with the angle defined in radians
	 * See {@link #AnimationTargetAngle(ModelRenderer, RotationAxis, float, int, int, boolean) AnimationTargetAngle}
	 */
	public AnimationTargetAngle(ModelRenderer part, RotationAxis axis, float targetAngle, int startFrame, int endFrame) {
		this(part, axis, targetAngle, startFrame, endFrame, false);
	}

	@Override
	public void apply(int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
		invert &= this.allowInvert;
		multiplier = (this.allowMultiplier ? multiplier : 1.0F);
		offset = (this.allowOffset ? offset : 0.0F);
		speed = (this.allowSpeed ? speed : 1.0F);
		float progress = getProgress(frame, partialTick, speed);
		this.axis.addRotation(this.part, getRotationAmount(progress, multiplier, offset, invert));
	}

	/**
	 * Returns the amount to add to the rotation angle based on the current value and {@link #getProgress(int, float, float) progress}
	 */
	protected float getRotationAmount(float progress, float multiplier, float offset, boolean invert) {
		float angle = (invert ? -this.targetAngle : this.targetAngle);
		angle = (angle + (invert ? -offset : offset)) - this.axis.getRotation(this.part);
		return multiplier * progress * angle;
	}
}

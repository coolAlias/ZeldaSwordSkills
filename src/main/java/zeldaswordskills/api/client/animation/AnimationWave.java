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
 * Animates a series of parts using the sine or cosine wave function.
 * 
 * Note that the 'frame' parameter is usually based on the entity's ticksExisted field.
 *
 */
public abstract class AnimationWave extends AnimationBase
{
	/** Base rotation amount that will be applied per frame if magnitude is 1.0F, equivalent to 22.5 degrees and chosen for no particular reason */
	protected static final float BASE = (float)(Math.PI / 8.0D);
	protected final RotationAxis axis;
	protected final float baseSpeed;
	protected final float magnitude;
	protected final float baseOffset;
	protected final ModelRenderer[] parts;

	/**
	 * Constructs an animation describing a wave-like motion for the array of model parts
	 * @param axis      The {@link RotationAxis} to which rotation will be applied
	 * @param baseSpeed The default speed at which the animation will play
	 * @param magnitude The magnitude of the wave - larger numbers give a bigger motion (suggest values between 0 to 1)
	 * @param frequency Describes how 'in sync' the various parts' rotations are applied;
	 *                  higher values give more 'waviness' to the motion (suggest values between 0 to 1)
	 * @param parts     List of parts involved in the wave
	 */
	public AnimationWave(RotationAxis axis, float speed, float magnitude, float frequency, ModelRenderer... parts) {
		this.axis = axis;
		this.baseSpeed = speed;
		this.magnitude = magnitude;
		this.parts = parts;
		this.baseOffset = (frequency > 0 ? (float)(Math.PI) / ((float) parts.length * frequency) : 0.0F);
	}

	@Override
	public boolean shouldApply(int frame, float partialTick, float speed) {
		return true;
	}

	@Override
	public final void apply(int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
		invert &= this.allowInvert;
		multiplier = (this.allowMultiplier ? multiplier : 1.0F);
		offset = (this.allowOffset ? offset : 0.0F);
		speed = (this.allowSpeed ? speed : 1.0F);
		float progress = speed * (frame + partialTick);
		for (int i = 0; i < this.parts.length; ++i) {
			applyRotationToPart(i, progress, speed, multiplier, offset, invert);
		}
	}

	/**
	 * Apply rotation to the part for the given index; parameters have already
	 * been adjusted based on the {@link AnimationBase} settings.
	 * @param index    Index of the current part
	 * @param progress Total animation progress
	 */
	protected abstract void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert);

	/**
	 * Returns the amount the part should move this frame using Sine
	 * @param progress total frame progress, i.e. the current frame count plus the partialTick
	 * @param offset   amount to offset the wave, if any
	 * @param speed    speed to play the animation 
	 */
	protected float getMotionSin(float progress, float offset, float speed) {
		float motion = (float) Math.sin(progress * speed * this.baseSpeed + (offset * this.baseOffset));
		return this.magnitude * AnimationWave.BASE * motion;
	}

	/**
	 * Returns the amount the part should move this frame using Cosine
	 * @param progress total frame progress, i.e. the current frame count plus the partialTick
	 * @param offset   amount to offset the wave, if any
	 * @param speed    speed to play the animation 
	 */
	protected float getMotionCos(float progress, float offset, float speed) {
		float motion = (float) Math.cos(progress * speed * this.baseSpeed + (offset * this.baseOffset));
		return this.magnitude * AnimationWave.BASE * motion;
	}

	public static class AnimationWaveSin extends AnimationWave
	{
		public AnimationWaveSin(RotationAxis axis, float speed, float magnitude, float frequency, ModelRenderer... parts) {
			super(axis, speed, magnitude, frequency, parts);
		}

		@Override
		protected void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert) {
			multiplier = (invert ? -multiplier : multiplier);
			float motion = this.getMotionSin(progress, index * offset, speed);
			this.axis.addRotation(this.parts[index], multiplier * motion);
		}
	}

	public static class AnimationWaveCos extends AnimationWave
	{
		public AnimationWaveCos(RotationAxis axis, float speed, float magnitude, float frequency, ModelRenderer... parts) {
			super(axis, speed, magnitude, frequency, parts);
		}

		@Override
		protected void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert) {
			multiplier = (invert ? -multiplier : multiplier);
			float motion = this.getMotionCos(progress, index * offset, speed);
			this.axis.addRotation(this.parts[index], multiplier * motion);
		}
	}
}

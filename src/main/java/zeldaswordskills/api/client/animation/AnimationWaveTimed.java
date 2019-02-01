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
 * Applies the sine or cosine wave animation only within the given time frame.
 *
 */
public abstract class AnimationWaveTimed extends AnimationWave
{
	protected final float startFrame;
	protected final float endFrame;

	/**
	 * Also see {@link AnimationWave#AnimationWave(RotationAxis, float, float, float, ModelRenderer...) AnimationWave}
	 * @param startFrame  The frame on which this animation should begin to be applied
	 * @param endFrame    The frame on which this animation should be fully applied
	 */
	public AnimationWaveTimed(RotationAxis axis, int startFrame, int endFrame, float speed, float magnitude, float frequency, ModelRenderer... parts) {
		super(axis, speed, magnitude, frequency, parts);
		this.startFrame = startFrame;
		this.endFrame = endFrame;
	}

	@Override
	public boolean shouldApply(int frame, float partialTick, float speed) {
		return (float) frame >= (this.startFrame / speed) && (float) frame <= (this.endFrame / speed);
	}

	public static class AnimationWaveTimedSin extends AnimationWaveTimed
	{
		public AnimationWaveTimedSin(RotationAxis axis, int startFrame, int endFrame, float speed, float magnitude, float frequency, ModelRenderer... parts) {
			super(axis, startFrame, endFrame, speed, magnitude, frequency, parts);
		}

		@Override
		protected void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert) {
			multiplier = (invert ? -multiplier : multiplier);
			float motion = this.getMotionSin(progress, index * offset, speed);
			this.axis.addRotation(this.parts[index], multiplier * motion);
		}
	}

	public static class AnimationWaveTimedCos extends AnimationWaveTimed
	{
		public AnimationWaveTimedCos(RotationAxis axis, int startFrame, int endFrame, float speed, float magnitude, float frequency, ModelRenderer... parts) {
			super(axis, startFrame, endFrame, speed, magnitude, frequency, parts);
		}

		@Override
		protected void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert) {
			multiplier = (invert ? -multiplier : multiplier);
			float motion = this.getMotionCos(progress, index * offset, speed);
			this.axis.addRotation(this.parts[index], multiplier * motion);
		}
	}
}

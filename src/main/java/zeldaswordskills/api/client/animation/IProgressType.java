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
 * Type describes how (mathematically) an animation progresses from start to finish.
 * Made it an interface instead of an enum to allow for adding custom progress types.
 *
 */
public interface IProgressType
{
	/**
	 * Return the progress towards completion, usually as a value progressing from 0.0F to 1.0F
	 * @param x Value between 0.0F and 1.0F representing current linear progress towards completion
	 */
	float getProgress(float x);

	//===================== IMPLEMENTATIONS =====================//
	/** Linear progress: y = x */
	public static final IProgressType LINEAR = new IProgressType() {
		@Override
		public float getProgress(float x) {
			return x;
		}
	};

	/** Square root of x; see {@link IProgressType.ProgressTypeRoot ProgressTypeRoot} */
	public static final IProgressType SQRT = new IProgressType.ProgressTypeRoot(2);

	/** Cubic root of x; see {@link IProgressType.ProgressTypeRoot ProgressTypeRoot} */
	public static final IProgressType ROOT_CUBE = new IProgressType.ProgressTypeRoot(3);

	/** x squared; see {@link IProgressType.ProgressTypeExponential ProgressTypeExponential} */
	public static final IProgressType SQUARED = new IProgressType.ProgressTypeExponential(2);

	/** x cubed; see {@link IProgressType.ProgressTypeExponential ProgressTypeExponential} */
	public static final IProgressType CUBED = new IProgressType.ProgressTypeExponential(3);

	/** x to the power of 4; see {@link IProgressType.ProgressTypeExponential ProgressTypeExponential} */
	public static final IProgressType POWER_4 = new IProgressType.ProgressTypeExponential(4);

	/**
	 * Exponential progress (n expected to be positive).
	 * Progresses slowly initially and speeds up as the animation nears completion.
	 */
	public static class ProgressTypeExponential implements IProgressType {
		private final int n;
		public ProgressTypeExponential(int n) {
			this.n = n;
		}
		@Override
		public float getProgress(float x) {
			return (float) Math.pow(x, n);
		}
	}

	/**
	 * Progress based on taking the root n of x; returns 0.0F if x <= 0.0F (n expected to be positive).
	 * Progresses quickly initially and slows down as the animation nears completion.
	 */
	public static class ProgressTypeRoot implements IProgressType {
		private final int n;
		public ProgressTypeRoot(int n) {
			this.n = n;
		}
		@Override
		public float getProgress(float x) {
			switch (n) {
			case 1: return x;
			case 2: return (float) Math.sqrt(x);
			case 3: return (float) Math.cbrt(x);
			}
			// More complex roots not supported natively; calculate using natural log
			return (x <= 0.0F ? 0.0F : (float) Math.exp(Math.log(x) / (double) n));
		}
	}
}

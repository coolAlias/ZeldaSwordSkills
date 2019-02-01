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
 * Animates a series of parts such that they move in a circular motion around two axes
 * 
 */
public class AnimationCircle extends AnimationWave
{
	protected final RotationAxis axis2;
	protected final boolean clockwise;

	/**
	 * See {@link AnimationWave#AnimationWave(RotationAxis, float, float, float, ModelRenderer...) AnimationWave}
	 * @param axis2     Second axis on which to apply rotation
	 * @param clockwise True for a clockwise circular motion, false for counterclockwise
	 */
	public AnimationCircle(RotationAxis axis1, RotationAxis axis2, float speed, float magnitude, float frequency, boolean clockwise, ModelRenderer... parts) {
		super(axis1, speed, magnitude, frequency, parts);
		this.axis2 = axis2;
		this.clockwise = clockwise;
	}

	@Override
	protected void applyRotationToPart(int index, float progress, float speed, float multiplier, float offset, boolean invert) {
		float sin = getMotionSin(progress, index * offset, speed);
		float cos = getMotionCos(progress, index * offset, speed);
		invert ^= this.clockwise;
		this.axis.addRotation(this.parts[index], multiplier * (invert ? sin : cos));
		this.axis2.addRotation(this.parts[index], multiplier * (invert ? cos : sin));
	}
}

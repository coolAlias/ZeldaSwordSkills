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
 * Animation target to toggle a model part's rendering visibility during the time frame
 *
 */
public class AnimationVisible extends AnimationTimed
{
	protected final ModelRenderer part;
	private final boolean base;
	private final boolean toggle;

	/**
	 * Toggle a part's visibility; also see {@link AnimationTimed#AnimationTimed(int, int) AnimationTimed}
	 * @param part   Reference to ModelRenderer piece from the model class whose visibility will be toggled
	 * @param base   Default value for {@link ModelRenderer#isHidden}
	 * @param toggle Value for {@link ModelRenderer#isHidden} while within the animation frames
	 */
	public AnimationVisible(ModelRenderer part, int startFrame, int endFrame, boolean base, boolean toggle) {
		super(startFrame, endFrame);
		this.part = part;
		this.base = base;
		this.toggle = toggle;
	}

	@Override
	public void apply(int frame, float partialTick, float speed, float multiplier, float offset, boolean invert) {
		speed = (this.allowSpeed ? speed : 1.0F);
		float end = (this.endFrame / speed);
		float progress = (float) frame + partialTick;
		this.part.isHidden = (progress < end ? this.toggle : this.base);
	}
}

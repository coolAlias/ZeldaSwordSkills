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

import net.minecraft.client.model.ModelRenderer;

/**
 * 
 * Animation that modifies the model part's offset amount (i.e. rendering position) on the specified axis.
 *
 */
public class AnimationTargetOffset extends AnimationTargetPoint
{
	public AnimationTargetOffset(ModelRenderer part, RotationAxis axis, float targetPosition, int startFrame, int endFrame) {
		super(part, axis, targetPosition, startFrame, endFrame);
	}

	public AnimationTargetOffset(ModelRenderer part, RotationAxis axis, float targetPosition, int startFrame, int endFrame, boolean relative) {
		super(part, axis, targetPosition, startFrame, endFrame, relative);
	}

	@Override
	protected void apply(float amount) {
		this.axis.addOffset(this.part, amount);
	}

	@Override
	protected float getCurrentAmount() {
		return this.axis.getOffset(this.part);
	}
}

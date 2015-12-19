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

package zeldaswordskills.api.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

/**
 * 
 * Use this instead of ModelRenderer to easily reset a model to its initial pose.
 *
 */
public class SmartModelRenderer extends ModelRenderer
{
	private float initAngleX, initAngleY, initAngleZ;
	private boolean initHidden;

	public SmartModelRenderer(ModelBase model) {
		super(model);
	}

	public SmartModelRenderer(ModelBase model, String name) {
		super(model, name);
	}

	public SmartModelRenderer(ModelBase model, int textureX, int textureY) {
		super(model, textureX, textureY);
	}

	/**
	 * Sets and memorizes the initial rotation angles for this model part
	 */
	public void setInitialPose(float x, float y, float z) {
		this.rotateAngleX = this.initAngleX = x;
		this.rotateAngleY = this.initAngleY = y;
		this.rotateAngleZ = this.initAngleZ = z;
		this.initHidden = this.isHidden;
	}

	/**
	 * Resets this model part to its initial pose
	 */
	public void resetPose() {
		this.rotateAngleX = this.initAngleX;
		this.rotateAngleY = this.initAngleY;
		this.rotateAngleZ = this.initAngleZ;
		this.isHidden = this.initHidden;
	}
}

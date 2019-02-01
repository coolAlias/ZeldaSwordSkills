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

package zeldaswordskills.api.client.model;

import net.minecraft.client.model.ModelBase;

/**
 * 
 * Additionally saves the model part's offset values so long as
 * {@link #setInitialPose} is called after setting the values. 
 *
 */
public class SmartOffsetModelRenderer extends SmartModelRenderer
{
	private float initOffsetX, initOffsetY, initOffsetZ;

	public SmartOffsetModelRenderer(ModelBase model) {
		super(model);
	}

	public SmartOffsetModelRenderer(ModelBase model, String name) {
		super(model, name);
	}

	public SmartOffsetModelRenderer(ModelBase model, int textureX, int textureY) {
		super(model, textureX, textureY);
	}

	@Override
	public void setInitialPose(float x, float y, float z) {
		super.setInitialPose(x, y, z);
		this.initOffsetX = this.offsetX;
		this.initOffsetY = this.offsetY;
		this.initOffsetZ = this.offsetZ;
	}

	@Override
	public void resetPose() {
		super.resetPose();
		this.offsetX = this.initOffsetX;
		this.offsetY = this.initOffsetY;
		this.offsetZ = this.initOffsetZ;
	}
}

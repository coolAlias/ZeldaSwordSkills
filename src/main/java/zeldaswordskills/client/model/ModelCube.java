/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * 
 * @author original credit to Hunternif; modified by coolAlias
 * 
 * Model for a cube. Both the cube's dimensions and texture size can be set
 *
 */
public class ModelCube extends ModelBase
{
	/** Vanilla model + renderer */
	private ModelRenderer cube;

	/**
	 * Model cube of given size using standard 16x16 texture
	 */
	public ModelCube(int size) {
		this(size, 16, 16, 0, 0);
	}

	/**
	 * Model cube of given size with custom texture height and width
	 */
	public ModelCube(int size, int textureX, int textureY) {
		this(size, textureX, textureY, 0, 0);
	}

	/**
	 * Model cube of given size with custom texture height, width, and offsets U and V (untested)
	 */
	public ModelCube(int size, int textureX, int textureY, int textureU, int textureV) {
		cube = new ModelRenderer(this, textureU, textureV);
		cube.addBox(-size, -size, -size, size * 2, size * 2, size * 2);
		cube.setRotationPoint(0F, 0F, 0F);
		cube.setTextureSize(textureX, textureY);
		cube.mirror = true;
		cube.rotateAngleY = 0.4371034F;
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		cube.render(f5);
	}

	public void render(Entity entity) {
		cube.render(0.0625f);
	}

	@Override
	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
}

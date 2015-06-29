/**
    Copyright (C) <2015> <coolAlias>

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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author credits to HoopAWolf for the model
 *
 */
@SideOnly(Side.CLIENT)
public class ModelCeramicJar extends ModelBase
{
	private ModelRenderer Shape1;
	private ModelRenderer Shape2;
	private ModelRenderer Shape3;
	private ModelRenderer Shape4;
	private ModelRenderer Shape5;
	private ModelRenderer Shape6;
	private ModelRenderer Shape7;
	private ModelRenderer Shape8;
	private ModelRenderer Shape9;
	private ModelRenderer Shape10;
	private ModelRenderer Shape11;
	private ModelRenderer Shape12;
	private ModelRenderer Shape13;
	private ModelRenderer Shape14;

	public ModelCeramicJar() {
		textureWidth = 64;
		textureHeight = 64;

		Shape1 = new ModelRenderer(this, 0, 0);
		Shape1.addBox(0F, 0F, 0F, 1, 7, 5);
		Shape1.setRotationPoint(2.5F, 15.7F, -2.5F);
		Shape1.setTextureSize(64, 32);
		Shape1.mirror = true;
		setRotation(Shape1, 0F, 0F, 0F);
		Shape2 = new ModelRenderer(this, 0, 51);
		Shape2.addBox(0F, 0F, 0F, 6, 1, 6);
		Shape2.setRotationPoint(-3F, 23F, -3F);
		Shape2.setTextureSize(64, 32);
		Shape2.mirror = true;
		setRotation(Shape2, 0F, 0F, 0F);
		Shape3 = new ModelRenderer(this, 19, 32);
		Shape3.addBox(0F, 0F, 0F, 1, 2, 5);
		Shape3.setRotationPoint(1.5F, 14F, -2.5F);
		Shape3.setTextureSize(64, 32);
		Shape3.mirror = true;
		setRotation(Shape3, 0F, 0F, 0F);
		Shape4 = new ModelRenderer(this, 28, 0);
		Shape4.addBox(0F, 0F, 0F, 7, 7, 1);
		Shape4.setRotationPoint(-3.5F, 15.7F, -3.5F);
		Shape4.setTextureSize(64, 32);
		Shape4.mirror = true;
		setRotation(Shape4, 0F, 0F, 0F);
		Shape5 = new ModelRenderer(this, 28, 0);
		Shape5.addBox(0F, 0F, 0F, 7, 7, 1);
		Shape5.setRotationPoint(-3.5F, 15.7F, 2.5F);
		Shape5.setTextureSize(64, 32);
		Shape5.mirror = true;
		setRotation(Shape5, 0F, 0F, 0F);
		Shape6 = new ModelRenderer(this, 0, 0);
		Shape6.addBox(0F, 0F, 0F, 1, 7, 5);
		Shape6.setRotationPoint(-3.5F, 15.7F, -2.5F);
		Shape6.setTextureSize(64, 32);
		Shape6.mirror = true;
		setRotation(Shape6, 0F, 0F, 0F);
		Shape7 = new ModelRenderer(this, 0, 41);
		Shape7.addBox(0F, 0F, 0F, 7, 1, 7);
		Shape7.setRotationPoint(-3.5F, 22.7F, -3.5F);
		Shape7.setTextureSize(64, 32);
		Shape7.mirror = true;
		setRotation(Shape7, 0F, 0F, 0F);
		Shape8 = new ModelRenderer(this, 19, 24);
		Shape8.addBox(0F, 0F, 0F, 1, 2, 5);
		Shape8.setRotationPoint(-2.5F, 14F, -2.5F);
		Shape8.setTextureSize(64, 32);
		Shape8.mirror = true;
		setRotation(Shape8, 0F, 0F, 0F);
		Shape9 = new ModelRenderer(this, 0, 16);
		Shape9.addBox(0F, 0F, 0F, 2, 1, 2);
		Shape9.setRotationPoint(1F, 13F, -1F);
		Shape9.setTextureSize(64, 32);
		Shape9.mirror = true;
		setRotation(Shape9, 0F, 0F, 0F);
		Shape10 = new ModelRenderer(this, 19, 20);
		Shape10.addBox(0F, 0F, 0F, 3, 2, 1);
		Shape10.setRotationPoint(-1.5F, 14F, 1.5F);
		Shape10.setTextureSize(64, 32);
		Shape10.mirror = true;
		setRotation(Shape10, 0F, 0F, 0F);
		Shape11 = new ModelRenderer(this, 19, 15);
		Shape11.addBox(0F, 0F, 0F, 3, 2, 1);
		Shape11.setRotationPoint(-1.5F, 14F, -2.5F);
		Shape11.setTextureSize(64, 32);
		Shape11.mirror = true;
		setRotation(Shape11, 0F, 0F, 0F);
		Shape12 = new ModelRenderer(this, 0, 26);
		Shape12.addBox(0F, 0F, 0F, 6, 1, 2);
		Shape12.setRotationPoint(-3F, 13F, -3F);
		Shape12.setTextureSize(64, 32);
		Shape12.mirror = true;
		setRotation(Shape12, 0F, 0F, 0F);
		Shape13 = new ModelRenderer(this, 0, 32);
		Shape13.addBox(0F, 0F, 0F, 6, 1, 2);
		Shape13.setRotationPoint(-3F, 13F, 1F);
		Shape13.setTextureSize(64, 32);
		Shape13.mirror = true;
		setRotation(Shape13, 0F, 0F, 0F);
		Shape14 = new ModelRenderer(this, 0, 20);
		Shape14.addBox(0F, 0F, 0F, 2, 1, 2);
		Shape14.setRotationPoint(-3F, 13F, -1F);
		Shape14.setTextureSize(64, 32);
		Shape14.mirror = true;
		setRotation(Shape14, 0F, 0F, 0F);
	}
	
	public void renderAll() {
		float f = 0.0625F;
		Shape1.render(f);
		Shape2.render(f);
		Shape3.render(f);
		Shape4.render(f);
		Shape5.render(f);
		Shape6.render(f);
		Shape7.render(f);
		Shape8.render(f);
		Shape9.render(f);
		Shape10.render(f);
		Shape11.render(f);
		Shape12.render(f);
		Shape13.render(f);
		Shape14.render(f);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		Shape1.render(f5);
		Shape2.render(f5);
		Shape3.render(f5);
		Shape4.render(f5);
		Shape5.render(f5);
		Shape6.render(f5);
		Shape7.render(f5);
		Shape8.render(f5);
		Shape9.render(f5);
		Shape10.render(f5);
		Shape11.render(f5);
		Shape12.render(f5);
		Shape13.render(f5);
		Shape14.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}
}

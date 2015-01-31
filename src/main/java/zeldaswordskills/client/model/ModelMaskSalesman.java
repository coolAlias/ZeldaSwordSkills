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
import net.minecraft.util.MathHelper;

/**
 * 
 * @author metroidisendless; refactored by coolAlias
 *
 */
public class ModelMaskSalesman extends ModelBase
{
	private ModelRenderer head;
	private ModelRenderer body;
	private ModelRenderer armRightUpper;
	private ModelRenderer armLeftUpper;
	private ModelRenderer armRightLower;
	private ModelRenderer armLeftLower;
	private ModelRenderer legRightUpper;
	private ModelRenderer legLeftUpper;
	private ModelRenderer body2;
	private ModelRenderer body3;
	private ModelRenderer body4;
	private ModelRenderer body5;
	private ModelRenderer body6;
	private ModelRenderer body7;
	private ModelRenderer body8;
	private ModelRenderer body9;
	private ModelRenderer body10;

	public ModelMaskSalesman() {
		textureWidth = 128;
		textureHeight = 128;
		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4F, -7.5F, -5F, 8, 8, 8);
		head.setRotationPoint(0F, 1F, -1F);
		head.setTextureSize(textureWidth, textureHeight);
		setRotation(head, 0F, 0F, 0F);
		body = new ModelRenderer(this, 16, 16);
		body.addBox(-4F, 0F, -2F, 8, 6, 4);
		body.setRotationPoint(0F, 1.5F, -0.6F);
		body.setTextureSize(textureWidth, textureHeight);
		setRotation(body, 0.1047198F, 0F, 0F);

		armRightUpper = new ModelRenderer(this, 40, 16);
		armRightUpper.addBox(-3F, -2F, -2F, 4, 6, 4);
		armRightUpper.setRotationPoint(-5F, 4F, 0F);
		armRightUpper.setTextureSize(textureWidth, textureHeight);
		setRotation(armRightUpper, -0.0872665F, 0F, 0F);
		armRightLower = new ModelRenderer(this, 40, 26);
		armRightLower.addBox(-3F, 1F, -6F, 4, 6, 4);
		armRightLower.setRotationPoint(0F, 5F, 3.7F);
		armRightLower.setTextureSize(textureWidth, textureHeight);
		setRotation(armRightLower, -0.8901179F, 0F, 0F);
		armRightUpper.addChild(armRightLower);

		armLeftUpper = new ModelRenderer(this, 40, 16);
		armLeftUpper.addBox(-1F, -1F, -2F, 4, 6, 4);
		armLeftUpper.setRotationPoint(5F, 3F, 0F);
		armLeftUpper.setTextureSize(textureWidth, textureHeight);
		armLeftUpper.mirror = true;
		setRotation(armLeftUpper, -0.0872665F, 0F, 0F);
		armLeftLower = new ModelRenderer(this, 40, 26);
		armLeftLower.addBox(-3F, 1F, -6F, 4, 6, 4);
		armLeftLower.setRotationPoint(2F, 6F, 3.5F);
		armLeftLower.setTextureSize(textureWidth, textureHeight);
		armLeftLower.mirror = true;
		setRotation(armLeftLower, -0.8901179F, 0F, 0F);
		armLeftUpper.addChild(armLeftLower);

		legRightUpper = new ModelRenderer(this, 0, 16);
		legRightUpper.addBox(-2F, 0F, -2F, 4, 12, 4);
		legRightUpper.setRotationPoint(-2F, 12F, 0F);
		legRightUpper.setTextureSize(textureWidth, textureHeight);
		setRotation(legRightUpper, 0F, 0F, 0F);
		legLeftUpper = new ModelRenderer(this, 0, 16);
		legLeftUpper.addBox(-2F, 0F, -2F, 4, 12, 4);
		legLeftUpper.setRotationPoint(2F, 12F, 0F);
		legLeftUpper.setTextureSize(textureWidth, textureHeight);
		legLeftUpper.mirror = true;
		setRotation(legLeftUpper, 0F, 0F, 0F);

		body2 = new ModelRenderer(this, 16, 26);
		body2.addBox(-4F, 0F, -2F, 8, 6, 4);
		body2.setRotationPoint(0F, 7F, 0F);
		body2.setTextureSize(textureWidth, textureHeight);
		setRotation(body2, 0F, 0F, 0F);
		body3 = new ModelRenderer(this, 0, 36);
		body3.addBox(-4F, 0F, 2F, 12, 14, 7);
		body3.setRotationPoint(-2F, -3F, 0F);
		body3.setTextureSize(textureWidth, textureHeight);
		setRotation(body3, 0F, 0F, 0F);
		body4 = new ModelRenderer(this, 0, 57);
		body4.addBox(-6F, 0F, 0F, 14, 5, 6);
		body4.setRotationPoint(-1F, -8F, 2F);
		body4.setTextureSize(textureWidth, textureHeight);
		setRotation(body4, 0F, 0F, 0F);
		body5 = new ModelRenderer(this, 32, 0);
		body5.addBox(0F, 0F, 0F, 6, 6, 1);
		body5.setRotationPoint(-10F, -8F, 2F);
		body5.setTextureSize(textureWidth, textureHeight);
		setRotation(body5, 0F, 0.418879F, 0F);
		body6 = new ModelRenderer(this, 46, 0);
		body6.addBox(0F, 0F, 0F, 1, 6, 6);
		body6.setRotationPoint(6F, 0F, 2.5F);
		body6.setTextureSize(textureWidth, textureHeight);
		setRotation(body6, 0F, 0F, 0F);
		body7 = new ModelRenderer(this, 60, 0);
		body7.addBox(0F, 0F, 0F, 1, 6, 6);
		body7.setRotationPoint(7F, 7F, 2.5F);
		body7.setTextureSize(textureWidth, textureHeight);
		setRotation(body7, 0F, 0F, 0.2443461F);
		body8 = new ModelRenderer(this, 32, 7);
		body8.addBox(0F, 0F, 0F, 6, 6, 1);
		body8.setRotationPoint(0F, 0F, 9F);
		body8.setTextureSize(textureWidth, textureHeight);
		setRotation(body8, 0F, 0F, 0F);
		body9 = new ModelRenderer(this, 74, 0);
		body9.addBox(0F, 0F, 0F, 1, 6, 6);
		body9.setRotationPoint(-7F, -3F, 2.5F);
		body9.setTextureSize(textureWidth, textureHeight);
		setRotation(body9, 0F, 0F, 0F);
		body10 = new ModelRenderer(this, 88, 0);
		body10.addBox(0F, 5F, 0F, 6, 6, 1);
		body10.setRotationPoint(-8F, 0F, 8F);
		body10.setTextureSize(textureWidth, textureHeight);
		setRotation(body10, 0F, -0.4537856F, 0F);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		head.render(f5);
		body.render(f5);
		armRightUpper.render(f5);
		armLeftUpper.render(f5);
		legRightUpper.render(f5);
		legLeftUpper.render(f5);
		body2.render(f5);
		body3.render(f5);
		body4.render(f5);
		body5.render(f5);
		body6.render(f5);
		body7.render(f5);
		body8.render(f5);
		body9.render(f5);
		body10.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) {
		super.setRotationAngles(f1, f2, f3, f4, f5, f6, entity);
		head.rotateAngleY = f4 / (180F / (float)Math.PI);
		head.rotateAngleX = f5 / (180F / (float)Math.PI);
		armRightUpper.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f2 * 0.5F;
		armLeftUpper.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F;
		armRightUpper.rotateAngleZ = 0.0F;
		armLeftUpper.rotateAngleZ = 0.0F;
		legRightUpper.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 1.4F * f2;
		legLeftUpper.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 1.4F * f2;
		legRightUpper.rotateAngleY = 0.0F;
		legLeftUpper.rotateAngleY = 0.0F;
	}
}

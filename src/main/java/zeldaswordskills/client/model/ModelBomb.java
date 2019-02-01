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

package zeldaswordskills.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author credits go to HoopAWolf for this model
 *
 */
@SideOnly(Side.CLIENT)
public class ModelBomb extends ModelBase
{
	private ModelRenderer shape5;
	private ModelRenderer shape1;
	private ModelRenderer shape2;
	private ModelRenderer shape3;
	private ModelRenderer shape4;
	private ModelRenderer shape6;
	private ModelRenderer shape7;
	private ModelRenderer shape8;
	private ModelRenderer shape9;
	private ModelRenderer shape10;
	private ModelRenderer shape11;
	private ModelRenderer shape12;
	private ModelRenderer shape13;
	private ModelRenderer shape14;
	private ModelRenderer shape15;

	public ModelBomb() {
		textureWidth = 64;
		textureHeight = 32;

		shape5 = new ModelRenderer(this, 0, 0);
		shape5.addBox(-3F, -1F, -4F, 8, 8, 8);
		shape5.setRotationPoint(-1F, 13F, 0F);
		shape5.setTextureSize(64, 32);
		shape5.mirror = true;
		setRotation(shape5, 0F, 0F, 0F);
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.addBox(-2F, -2F, -4F, 6, 10, 8);
		shape1.setRotationPoint(-1F, 13F, 0F);
		shape1.setTextureSize(64, 32);
		shape1.mirror = true;
		setRotation(shape1, 0F, 0F, 0F);
		shape2 = new ModelRenderer(this, 0, 0);
		shape2.addBox(-3F, -2F, -3F, 8, 10, 6);
		shape2.setRotationPoint(-1F, 13F, 0F);
		shape2.setTextureSize(64, 32);
		shape2.mirror = true;
		setRotation(shape2, 0F, 0F, 0F);
		shape3 = new ModelRenderer(this, 0, 0);
		shape3.addBox(-2F, -1F, -5F, 6, 8, 10);
		shape3.setRotationPoint(-1F, 13F, 0F);
		shape3.setTextureSize(64, 32);
		shape3.mirror = true;
		setRotation(shape3, 0F, 0F, 0F);
		shape4 = new ModelRenderer(this, 0, 0);
		shape4.addBox(-3F, 0F, -5F, 8, 6, 10);
		shape4.setRotationPoint(-1F, 13F, 0F);
		shape4.setTextureSize(64, 32);
		shape4.mirror = true;
		setRotation(shape4, 0F, 0F, 0F);
		shape6 = new ModelRenderer(this, 0, 0);
		shape6.addBox(-4F, -1F, -3F, 10, 8, 6);
		shape6.setRotationPoint(-1F, 13F, 0F);
		shape6.setTextureSize(64, 32);
		shape6.mirror = true;
		setRotation(shape6, 0F, 0F, 0F);
		shape7 = new ModelRenderer(this, 0, 0);
		shape7.addBox(-4F, 0F, -4F, 10, 6, 8);
		shape7.setRotationPoint(-1F, 13F, 0F);
		shape7.setTextureSize(64, 32);
		shape7.mirror = true;
		setRotation(shape7, 0F, 0F, 0F);
		shape8 = new ModelRenderer(this, 41, 0);
		shape8.addBox(-1F, -3F, -3F, 4, 12, 6);
		shape8.setRotationPoint(-1F, 13F, 0F);
		shape8.setTextureSize(64, 32);
		shape8.mirror = true;
		setRotation(shape8, 0F, 0F, 0F);
		shape9 = new ModelRenderer(this, 28, 16);
		shape9.addBox(-2F, -3F, -2F, 6, 12, 4);
		shape9.setRotationPoint(-1F, 13F, 0F);
		shape9.setTextureSize(64, 32);
		shape9.mirror = true;
		setRotation(shape9, 0F, 0F, 0F);
		shape10 = new ModelRenderer(this, 0, 0);
		shape10.addBox(-5F, 0F, -2F, 12, 6, 4);
		shape10.setRotationPoint(-1F, 13F, 0F);
		shape10.setTextureSize(64, 32);
		shape10.mirror = true;
		setRotation(shape10, 0F, 0F, 0F);
		shape11 = new ModelRenderer(this, 0, 0);
		shape11.addBox(-5F, 1F, -3F, 12, 4, 6);
		shape11.setRotationPoint(-1F, 13F, 0F);
		shape11.setTextureSize(64, 32);
		shape11.mirror = true;
		setRotation(shape11, 0F, 0F, 0F);
		shape12 = new ModelRenderer(this, 0, 0);
		shape12.addBox(-1F, 0F, -6F, 4, 6, 12);
		shape12.setRotationPoint(-1F, 13F, 0F);
		shape12.setTextureSize(64, 32);
		shape12.mirror = true;
		setRotation(shape12, 0F, 0F, 0F);
		shape13 = new ModelRenderer(this, 0, 0);
		shape13.addBox(-2F, 1F, -6F, 6, 4, 12);
		shape13.setRotationPoint(-1F, 13F, 0F);
		shape13.setTextureSize(64, 32);
		shape13.mirror = true;
		setRotation(shape13, 0F, 0F, 0F);
		shape14 = new ModelRenderer(this, 0, 27);
		shape14.addBox(0F, 0F, 0F, 4, 4, 0);
		shape14.setRotationPoint(1F, 6F, 0F);
		shape14.setTextureSize(64, 32);
		shape14.mirror = true;
		setRotation(shape14, 0F, 0F, 0.2792527F);
		shape15 = new ModelRenderer(this, 0, 20);
		shape15.addBox(0F, 0F, 0F, 4, 1, 4);
		shape15.setRotationPoint(-2F, 9F, -2F);
		shape15.setTextureSize(64, 32);
		shape15.mirror = true;
		setRotation(shape15, 0F, 0F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		shape5.render(f5);
		shape1.render(f5);
		shape2.render(f5);
		shape3.render(f5);
		shape4.render(f5);
		shape6.render(f5);
		shape7.render(f5);
		shape8.render(f5);
		shape9.render(f5);
		shape10.render(f5);
		shape11.render(f5);
		shape12.render(f5);
		shape13.render(f5);
		shape14.render(f5);
		shape15.render(f5);
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

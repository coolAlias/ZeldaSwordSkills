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
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author credits go to Jones7789 for the original model and texture
 *
 */
@SideOnly(Side.CLIENT)
public class ModelOctorok extends ModelBase
{
	private ModelRenderer shape1;
	private ModelRenderer shape2;
	private ModelRenderer shape3;
	private ModelRenderer shape4;
	private ModelRenderer shape5;
	private ModelRenderer shape6;
	private ModelRenderer shape7;

	/** The octorok's tentacles */
	private ModelRenderer[] tentacles;

	public ModelOctorok() {
		textureWidth = 128;
		textureHeight = 64;
		shape1 = new ModelRenderer(this, 0, 0);
		shape1.addBox(0F, 0F, 0F, 7, 4, 6);
		shape1.setRotationPoint(-3F, 11F, -1F);
		shape1.setTextureSize(128, 64);
		shape1.mirror = true;
		setRotation(shape1, 0F, 0F, 0F);
		shape2 = new ModelRenderer(this, 0, 41);
		shape2.addBox(0F, 0F, 0F, 12, 2, 12);
		shape2.setRotationPoint(-6F, 10F, -4F);
		shape2.setTextureSize(128, 64);
		shape2.mirror = true;
		setRotation(shape2, 0F, 0F, 0F);
		shape3 = new ModelRenderer(this, 22, 21);
		shape3.addBox(0F, 0F, 0F, 10, 5, 10);
		shape3.setRotationPoint(-5F, 5F, -3F);
		shape3.setTextureSize(128, 64);
		shape3.mirror = true;
		setRotation(shape3, 0F, 0F, 0F);
		shape4 = new ModelRenderer(this, 30, 0);
		shape4.addBox(0F, 0F, 0F, 8, 5, 8);
		shape4.setRotationPoint(-4F, 0F, -2F);
		shape4.setTextureSize(128, 64);
		shape4.mirror = true;
		setRotation(shape4, 0F, 0F, 0F);
		shape5 = new ModelRenderer(this, 88, 0);
		shape5.addBox(0F, 0F, 0F, 4, 4, 1);
		shape5.setRotationPoint(-5F, -1F, -3F);
		shape5.setTextureSize(128, 64);
		shape5.mirror = true;
		setRotation(shape5, 0F, 0F, 0F);
		shape6 = new ModelRenderer(this, 88, 0);
		shape6.addBox(0F, 0F, 0F, 4, 4, 1);
		shape6.setRotationPoint(1F, -1F, -3F);
		shape6.setTextureSize(128, 64);
		shape6.mirror = true;
		setRotation(shape6, 0F, 0F, 0F);
		shape7 = new ModelRenderer(this, 0, 31);
		shape7.addBox(0F, 0F, 0F, 5, 5, 3);
		shape7.setRotationPoint(-2.5F, 7F, -6F);
		shape7.setTextureSize(128, 64);
		shape7.mirror = true;
		setRotation(shape7, 0F, 0F, 0F);

		tentacles = new ModelRenderer[6];
		for (int i = 0; i < tentacles.length; ++i) {
			tentacles[i] = new ModelRenderer(this, 67, 0);
			double d0 = (double) i * Math.PI * 2.0D / (double) tentacles.length;
			float f = (float) Math.cos(d0) * 2.35F - 0.4F;
			float f1 = (float) Math.sin(d0) * 2.35F + 1.25F;
			tentacles[i].addBox(0.0F, 0.0F, 0.0F, 3, 13, 3);
			tentacles[i].rotationPointX = f;
			tentacles[i].rotationPointZ = f1;
			tentacles[i].rotationPointY = (float)(13);
			d0 = (double) i * Math.PI * -2.0D / (double) tentacles.length + (Math.PI / 2D);
			tentacles[i].rotateAngleY = (float) d0;
		}
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		shape1.render(f5);
		shape2.render(f5);
		shape3.render(f5);
		shape4.render(f5);
		shape5.render(f5);
		shape6.render(f5);
		shape7.render(f5);
		for (int i = 0; i < tentacles.length; ++i) {
			tentacles[i].render(f5);
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		for (int i = 0; i < tentacles.length; ++i) {
			tentacles[i].rotateAngleX = f2;
		}
	}
}

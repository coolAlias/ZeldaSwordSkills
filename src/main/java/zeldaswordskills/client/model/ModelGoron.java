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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author Model and texture courtesy of RazzleberryFox; re-coded by coolAlias
 *
 */
@SideOnly(Side.CLIENT)
public class ModelGoron extends ModelBase {
	private ModelRenderer head;
	private ModelRenderer sumo1;
	private ModelRenderer sumo2;
	private ModelRenderer sumo3;
	private ModelRenderer rightArm;
	private ModelRenderer leftArm;
	private ModelRenderer rightLeg;
	private ModelRenderer leftLeg;
	private ModelRenderer body;
	private ModelRenderer boobs;
	private ModelRenderer tum1;
	private ModelRenderer tum2;
	private ModelRenderer tum3;
	private ModelRenderer back1;
	private ModelRenderer back2;
	private ModelRenderer back3;
	private ModelRenderer back4;
	private ModelRenderer[][] rockArrays;

	public ModelGoron() {
		int i;
		float x, y, z;
		float rot = 0.7853982F; // Rotation used for rocks
		textureWidth = 128;
		textureHeight = 64;

		head = new ModelRenderer(this, 0, 0);
		head.addBox(-4F, -8F, -4F, 8, 8, 8);
		head.setRotationPoint(0F, 3F, 0F);
		head.setTextureSize(textureWidth, textureHeight);
		head.mirror = true;
		setRotation(head, 0F, 0F, 0F);
		sumo1 = new ModelRenderer(this, 63, 0);
		sumo1.addBox(-6F, 0F, -5F, 7, 4, 7);
		sumo1.setRotationPoint(2.5F, 14.5F, 1.5F);
		sumo1.setTextureSize(textureWidth, textureHeight);
		sumo1.mirror = true;
		setRotation(sumo1, 0F, 0F, 0F);
		sumo2 = new ModelRenderer(this, 71, 40);
		sumo2.addBox(-6F, 0F, -5F, 3, 3, 5);
		sumo2.setRotationPoint(1.5F, 15F, 2.5F);
		sumo2.setTextureSize(textureWidth, textureHeight);
		sumo2.mirror = true;
		setRotation(sumo2, 0F, 0F, 0F);
		sumo3 = new ModelRenderer(this, 71, 40);
		sumo3.addBox(-6F, 0F, -5F, 3, 3, 5);
		sumo3.setRotationPoint(7.5F, 15F, 2.5F);
		sumo3.setTextureSize(textureWidth, textureHeight);
		sumo3.mirror = true;
		setRotation(sumo3, 0F, 0F, 0F);
		rightArm = new ModelRenderer(this, 108, 0);
		rightArm.addBox(-3F, -2F, -2F, 4, 16, 5);
		rightArm.setRotationPoint(-7F, 5F, -1F);
		rightArm.setTextureSize(textureWidth, textureHeight);
		rightArm.mirror = true;
		setRotation(rightArm, 0F, 0F, 0F);
		leftArm = new ModelRenderer(this, 108, 21);
		leftArm.addBox(-1F, -2F, -2F, 4, 16, 5);
		leftArm.setRotationPoint(7F, 5F, -1F);
		leftArm.setTextureSize(textureWidth, textureHeight);
		leftArm.mirror = true;
		setRotation(leftArm, 0F, 0F, 0F);
		rightLeg = new ModelRenderer(this, 91, 0);
		rightLeg.addBox(-2F, 0F, -2F, 4, 9, 4);
		rightLeg.setRotationPoint(-2F, 15F, 0F);
		rightLeg.setTextureSize(textureWidth, textureHeight);
		rightLeg.mirror = true;
		setRotation(rightLeg, 0F, 0F, 0F);
		leftLeg = new ModelRenderer(this, 91, 13);
		leftLeg.addBox(-2F, 0F, -2F, 4, 9, 4);
		leftLeg.setRotationPoint(2F, 15F, 0F);
		leftLeg.setTextureSize(textureWidth, textureHeight);
		leftLeg.mirror = true;
		setRotation(leftLeg, 0F, 0F, 0F);
		body = new ModelRenderer(this, 0, 16);
		body.addBox(-6F, 0F, -5F, 12, 12, 10);
		body.setRotationPoint(0F, 3F, 0F);
		body.setTextureSize(textureWidth, textureHeight);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);
		boobs = new ModelRenderer(this, 45, 38);
		boobs.addBox(-6F, 0F, -5F, 10, 4, 1);
		boobs.setRotationPoint(1F, 3.5F, -0.5F);
		boobs.setTextureSize(textureWidth, textureHeight);
		boobs.mirror = true;
		setRotation(boobs, 0F, 0F, 0F);
		tum1 = new ModelRenderer(this, 0, 38);
		tum1.addBox(-6F, 0F, -5F, 10, 7, 2);
		tum1.setRotationPoint(1F, 7.5F, -1F);
		tum1.setTextureSize(textureWidth, textureHeight);
		tum1.mirror = true;
		setRotation(tum1, 0F, 0F, 0F);
		tum2 = new ModelRenderer(this, 0, 47);
		tum2.addBox(-6F, 0F, -5F, 8, 6, 1);
		tum2.setRotationPoint(2F, 8F, -1.5F);
		tum2.setTextureSize(textureWidth, textureHeight);
		tum2.mirror = true;
		setRotation(tum2, 0F, 0F, 0F);
		tum3 = new ModelRenderer(this, 0, 54);
		tum3.addBox(-6F, 0F, -5F, 7, 5, 1);
		tum3.setRotationPoint(2.5F, 8.5F, -2F);
		tum3.setTextureSize(textureWidth, textureHeight);
		tum3.mirror = true;
		setRotation(tum3, 0F, 0F, 0F);
		back1 = new ModelRenderer(this, 65, 16);
		back1.addBox(-6F, 0F, -5F, 10, 11, 1);
		back1.setRotationPoint(1F, 3.5F, 10F);
		back1.setTextureSize(textureWidth, textureHeight);
		back1.mirror = true;
		setRotation(back1, 0F, 0F, 0F);
		back2 = new ModelRenderer(this, 65, 16);
		back2.addBox(-6F, 0F, -5F, 9, 10, 1);
		back2.setRotationPoint(1.5F, 4F, 11F);
		back2.setTextureSize(textureWidth, textureHeight);
		back2.mirror = true;
		setRotation(back2, 0F, 0F, 0F);
		back3 = new ModelRenderer(this, 67, 18);
		back3.addBox(-6F, 0F, -5F, 8, 9, 1);
		back3.setRotationPoint(2F, 4.5F, 12F);
		back3.setTextureSize(textureWidth, textureHeight);
		back3.mirror = true;
		setRotation(back3, 0F, 0F, 0F);
		back4 = new ModelRenderer(this, 67, 16);
		back4.addBox(-6F, 0F, -5F, 7, 6, 1);
		back4.setRotationPoint(2F, 6F, 12F);
		back4.setTextureSize(textureWidth, textureHeight);
		back4.mirror = true;
		setRotation(back4, 0F, 0F, 0F);
		rockArrays = new ModelRenderer[12][];
		rockArrays[0] = new ModelRenderer[8];
		for (i = 0; i < rockArrays[0].length; ++i) {
			rockArrays[0][i] = new ModelRenderer(this, 52, 16);
			rockArrays[0][i].addBox(-1F, -1F, -1F, 1, 1, 1);
			x = (i % 2 == 1 ? 1 : -1) * (i == 0 ? 0.0F : (i == 1 ? 0.3F : i < 4 ? 1F : 1.5F));
			z = i < 2 ? -2.7F : (i < 4 ? -2.5F : (i < 6 ? -1.5F : 3.5F));
			rockArrays[0][i].setRotationPoint(x, -4F, z);
			rockArrays[0][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[0][i].mirror = true;
			setRotation(rockArrays[0][i], 0F, 0F, rot);
		}
		rockArrays[1] = new ModelRenderer[6];
		for (i = 0; i < rockArrays[1].length; ++i) {
			rockArrays[1][i] = new ModelRenderer(this, 50, 16);
			rockArrays[1][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = 2F * (i % 3 - 1);
			rockArrays[1][i].setRotationPoint(x, (i < 3 ? 3F : 15F), 5F);
			rockArrays[1][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[1][i].mirror = true;
			setRotation(rockArrays[1][i], 0F, rot, 0F);
		}
		rockArrays[2] = new ModelRenderer[6];
		for (i = 0; i < rockArrays[2].length; ++i) {
			rockArrays[2][i] = new ModelRenderer(this, 50, 16);
			rockArrays[2][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = 3F * (i % 3 - 1);
			rockArrays[2][i].setRotationPoint(x, (i < 3 ? 7F : 11F), 8F);
			rockArrays[2][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[2][i].mirror = true;
			setRotation(rockArrays[2][i], 0F, rot, 0F);
		}
		rockArrays[3] = new ModelRenderer[6];
		for (i = 0; i < rockArrays[3].length; ++i) {
			rockArrays[3][i] = new ModelRenderer(this, 50, 16);
			rockArrays[3][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = (i % 2 == 1 ? 1 : -1) * 4.5F;
			z = (i / 2 == 1 ? 7F : 6F);
			rockArrays[3][i].setRotationPoint(x, 5F + (4F * (i / 2)), z);
			rockArrays[3][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[3][i].mirror = true;
			setRotation(rockArrays[3][i], rot, (i % 2 == 1 ? rot : -rot), 0F);
		}
		rockArrays[4] = new ModelRenderer[8];
		for (i = 0; i < rockArrays[4].length; ++i) {
			rockArrays[4][i] = new ModelRenderer(this, 50, 16);
			rockArrays[4][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = (i % 2 == 0 ? -1F : 1F);
			z = (float)((i / 2) - 1);
			rockArrays[4][i].setRotationPoint(x, -4.5F, z);
			rockArrays[4][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[4][i].mirror = true;
			setRotation(rockArrays[4][i], rot, 0F, 0F);
		}
		rockArrays[5] = new ModelRenderer[10];
		for (i = 0; i < rockArrays[5].length; ++i) {
			rockArrays[5][i] = new ModelRenderer(this, 50, 16);
			rockArrays[5][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = -3F + ((float) (i % 5) * 1.5F);
			rockArrays[5][i].setRotationPoint(x, (i < 5 ? 5F : 13F), 7F);
			rockArrays[5][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[5][i].mirror = true;
			boolean flag = ((i % 5) % 2 == 0);
			setRotation(rockArrays[5][i], (flag ? rot : 0F), (flag ? 0F : rot), 0F);
		}
		rockArrays[6] = new ModelRenderer[3];
		for (i = 0; i < rockArrays[6].length; ++i) {
			rockArrays[6][i] = new ModelRenderer(this, 50, 16);
			rockArrays[6][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			rockArrays[6][i].setRotationPoint(-2.5F + ((float) i * 2.5F), 9F, 9F);
			rockArrays[6][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[6][i].mirror = true;
			setRotation(rockArrays[6][i], rot, 0F, 0F);
		}
		rockArrays[7] = new ModelRenderer[4];
		for (i = 0; i < rockArrays[7].length; ++i) {
			rockArrays[7][i] = new ModelRenderer(this, 50, 16);
			rockArrays[7][i].addBox(-1F, -1F, -1F, 2, 2, 2);
			x = (i % 2 == 1 ? 1 : -1) * (i < 2 ? 1F : 1.3F);
			rockArrays[7][i].setRotationPoint(x, (i < 2 ? 2F : 9F), (i < 2 ? 4F : 8F));
			rockArrays[7][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[7][i].mirror = true;
			setRotation(rockArrays[7][i], 0F, rot, 0F);
		}
		rockArrays[8] = new ModelRenderer[3];
		for (i = 0; i < rockArrays[8].length; ++i) {
			rockArrays[8][i] = new ModelRenderer(this, 50, 16);
			rockArrays[8][i].addBox(-1F, -2F, -1F, 2, 2, 2);
			rockArrays[8][i].setRotationPoint(-0.7F, -4F, (i == 0 ? -1.5F : (i == 1 ? -0.5F : 1.5F)));
			rockArrays[8][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[8][i].mirror = true;
			setRotation(rockArrays[8][i], 0F, 0F, rot);
		}
		rockArrays[9] = new ModelRenderer[4];
		for (i = 0; i < rockArrays[9].length; ++i) {
			rockArrays[9][i] = new ModelRenderer(this, 50, 16);
			rockArrays[9][i].addBox(-1F, -2F, -1F, 2, 2, 2);
			rockArrays[9][i].setRotationPoint((i < 2 ? -2.8F : 1.8F), -3.4F, (i % 2 == 1 ? 1.5F : -1.2F));
			rockArrays[9][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[9][i].mirror = true;
			setRotation(rockArrays[9][i], 0F, 0F, rot);
		}
		rockArrays[10] = new ModelRenderer[4];
		for (i = 0; i < rockArrays[10].length; ++i) {
			rockArrays[10][i] = new ModelRenderer(this, 50, 16);
			rockArrays[10][i].addBox(-2F, -1F, -1F, 2, 2, 2);
			z = (i == 0 ? -2.5F : (i == 1 ? -1F : (i == 2 ? 1F : 3F)));
			rockArrays[10][i].setRotationPoint(1F, -4.5F, z);
			rockArrays[10][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[10][i].mirror = true;
			setRotation(rockArrays[10][i], rot, 0F, 0F);
		}
		rockArrays[11] = new ModelRenderer[7];
		for (i = 0; i < rockArrays[11].length; ++i) {
			rockArrays[11][i] = new ModelRenderer(this, 50, 16);
			rockArrays[11][i].addBox(-2F, -1F, -1F, 2, 2, 2);
			x = (float)((i + 1) % 3);
			y = -(float)(((i % 3) + 1) / 2) - ((i / 3) * 2F);
			rockArrays[11][i].setRotationPoint(x, y, 4F);
			rockArrays[11][i].setTextureSize(textureWidth, textureHeight);
			rockArrays[11][i].mirror = true;
			setRotation(rockArrays[11][i], rot, 0F, 0F);
		}
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		head.render(f5);
		sumo1.render(f5);
		sumo2.render(f5);
		sumo3.render(f5);
		rightArm.render(f5);
		leftArm.render(f5);
		rightLeg.render(f5);
		leftLeg.render(f5);
		body.render(f5);
		boobs.render(f5);
		tum1.render(f5);
		tum2.render(f5);
		tum3.render(f5);
		back1.render(f5);
		back2.render(f5);
		back3.render(f5);
		back4.render(f5);
		for (ModelRenderer[] rocks : rockArrays) {
			for (ModelRenderer r : rocks) {
				r.render(f5);
			}
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	private float getSwingAmount(float f, float max) {
		return (Math.abs(f % max - max * 0.5F) - max * 0.25F) / (max * 0.25F);
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float par2, float par3, float par4) {
		int i = entity.attackTime;
		if (i > 0) {
			rightArm.rotateAngleX = -2.0F + 1.5F * getSwingAmount((float) i - par4, 10.0F);
			leftArm.rotateAngleX = -2.0F + 1.5F * getSwingAmount((float) i - par4, 10.0F);
		}
	}

	@Override
	public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) {
		super.setRotationAngles(f1, f2, f3, f4, f5, f6, entity);
		head.rotateAngleY = f4 / (180F / (float) Math.PI);
		//head.rotateAngleX = f5 / (180F / (float) Math.PI);

		if (!(entity instanceof EntityLivingBase) || ((EntityLivingBase) entity).attackTime == 0) {
			rightArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f2 * 0.5F;
			leftArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F;
		}
		rightArm.rotateAngleZ = 0.0F;
		leftArm.rotateAngleZ = 0.0F;
		rightLeg.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 1.4F * f2;
		leftLeg.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 1.4F * f2;
		rightLeg.rotateAngleY = 0.0F;
		leftLeg.rotateAngleY = 0.0F;
	}
}

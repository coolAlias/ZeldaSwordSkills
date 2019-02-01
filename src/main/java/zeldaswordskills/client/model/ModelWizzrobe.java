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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.mobs.EntityWizzrobe;

@SideOnly(Side.CLIENT)
public class ModelWizzrobe extends ModelBase
{
	private ModelRenderer hat;
	private ModelRenderer head;
	private ModelRenderer body;
	private ModelRenderer rightArm;
	private ModelRenderer leftArm;
	private ModelRenderer robe;

	/** Set to Wizzrobe's cast time each time a spell begins casting to mark the first tick value */
	private int maxCastTick;

	/** Flag set or unset at peak of motion, signaling spell to render or not */
	public boolean atPeak;

	/** Array of rotations around x-axis for arms */
	private static final float rotX[] = {-0.5F, -1.35F, -2.0F, -2.5F};

	/** Array of rotations around z-axis for arms; right arm should use negative value */
	private static final float rotZ[] = {0.1F, 0.15F, 0.2F, 0.25F};

	public ModelWizzrobe() {
		this(0.0F, 0.0F);
	}

	public ModelWizzrobe(float f1, float f2) {
		textureWidth = 64;
		textureHeight = 128;
		head = new ModelRenderer(this, 0, 0).setTextureSize(textureWidth, textureHeight);
		head.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, f1);
		head.setRotationPoint(0.0F, 0.0F + f2, 0.0F);
		body = new ModelRenderer(this, 0, 16).setTextureSize(textureWidth, textureHeight);
		body.addBox(-4.0F, 0.0F, -2.0F, 8, 8, 4, f1);
		body.setRotationPoint(0.0F, 0.0F + f2, 0.0F);
		rightArm = new ModelRenderer(this, 32, 10).setTextureSize(textureWidth, textureHeight);
		rightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, f1);
		rightArm.setRotationPoint(-5.0F, 2.0F + f2, 0.0F);
		leftArm = new ModelRenderer(this, 32, 10).setTextureSize(textureWidth, textureHeight);
		leftArm.mirror = true;
		leftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, f1);
		leftArm.setRotationPoint(5.0F, 2.0F + f2, 0.0F);
		// rotation points are set in relation to the parent piece,
		// whereas non-child pieces are set with absolute positions
		robe = new ModelRenderer(this, 32, 0).setTextureSize(textureWidth, textureHeight);
		robe.addBox(-4.0F, 0.0F, -3.0F, 8, 4, 6, f1);
		// thus, upper robe's y is 8, because that's the size of the body
		robe.setRotationPoint(0.0F, 8.0F + f2, 0.0F);
		setRotation(robe, 0.0F, 0.0F, 0.0F);
		body.addChild(robe);
		ModelRenderer robeMid = new ModelRenderer(this, 0, 28).setTextureSize(textureWidth, textureHeight);
		robeMid.addBox(-5.0F, 0.0F, -4.0F, 10, 6, 8, f1);
		// but the mid-robe is 4, because it is a child of the upper, and the upper is only 4 tall
		robeMid.setRotationPoint(0.0F, 4.0F + f2, 0.0F);
		setRotation(robeMid, 0.0F, 0.0F, 0.0F);
		robe.addChild(robeMid);
		ModelRenderer robeLower = new ModelRenderer(this, 0, 42).setTextureSize(textureWidth, textureHeight);
		robeLower.addBox(-6.0F, 0.0F, -5.0F, 12, 6, 10, f1);
		robeLower.setRotationPoint(0.0F, 6.0F + f2, 0.0F);
		setRotation(robeLower, 0.0F, 0.0F, 0.0F);
		robeMid.addChild(robeLower);
		hat = (new ModelRenderer(this, 24, 58)).setTextureSize(textureWidth, textureHeight);
		hat.setRotationPoint(-5.0F, -10.03125F, -5.0F);
		hat.addBox(0.0F, 0.0F, 0.0F, 10, 2, 10);
		head.addChild(hat);
		ModelRenderer hatLayer1 = (new ModelRenderer(this, 36, 31)).setTextureSize(textureWidth, textureHeight);
		hatLayer1.setRotationPoint(1.75F, -4.0F, 2.0F);
		hatLayer1.addBox(0.0F, 0.0F, 0.0F, 7, 4, 7);
		hatLayer1.rotateAngleX = -0.05235988F;
		hatLayer1.rotateAngleZ = 0.02617994F;
		hat.addChild(hatLayer1);
		ModelRenderer hatLayer2 = (new ModelRenderer(this, 48, 14)).setTextureSize(textureWidth, textureHeight);
		hatLayer2.setRotationPoint(1.75F, -4.0F, 2.0F);
		hatLayer2.addBox(0.0F, 0.0F, 0.0F, 4, 4, 4);
		hatLayer2.rotateAngleX = -0.10471976F;
		hatLayer2.rotateAngleZ = 0.05235988F;
		hatLayer1.addChild(hatLayer2);
		ModelRenderer hatLayer3 = (new ModelRenderer(this, 26, 18)).setTextureSize(textureWidth, textureHeight);
		hatLayer3.setRotationPoint(1.75F, -2.0F, 2.0F);
		hatLayer3.addBox(0.0F, 0.0F, 0.0F, 1, 2, 1, 0.25F);
		hatLayer3.rotateAngleX = -0.20943952F;
		hatLayer3.rotateAngleZ = 0.10471976F;
		hatLayer2.addChild(hatLayer3);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		head.render(f5);
		body.render(f5);
		rightArm.render(f5);
		leftArm.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float par2, float par3, float par4) {
		setLivingAnimations((EntityWizzrobe) entity, par2, par3, par4);
	}

	private void setLivingAnimations(EntityWizzrobe entity, float par2, float par3, float par4) {
		int castTime = entity.getCurrentCastingTime();
		if (castTime > 0) {
			maxCastTick = entity.getMaxCastingTime() - 1;
			int i = 0;
			// 3 ticks up, 3 ticks down, rest held even
			if (castTime == 1 || castTime == maxCastTick) {
				i = 0;
			} else if (castTime == 2 || castTime == (maxCastTick - 1)) {
				i = 1;
			} else if (castTime == 3 || castTime == (maxCastTick - 2)) {
				i = 2;
				atPeak = false;
			} else {
				i = 3;
				atPeak = true;
			}
			rightArm.rotateAngleX = rotX[i];
			leftArm.rotateAngleX = rotX[i];
			rightArm.rotateAngleZ = -rotZ[i];
			leftArm.rotateAngleZ = rotZ[i];
		} else {
			rightArm.rotateAngleZ = 0.0F;
			leftArm.rotateAngleZ = 0.0F;
		}
	}

	@Override
	public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) {
		super.setRotationAngles(f1, f2, f3, f4, f5, f6, entity);
		head.rotateAngleY = f4 / (180F / (float) Math.PI);
		if (((EntityWizzrobe) entity).getCurrentCastingTime() == 0) {
			rightArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f2 * 0.5F;
			leftArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F;
			atPeak = false;
		}
	}
}

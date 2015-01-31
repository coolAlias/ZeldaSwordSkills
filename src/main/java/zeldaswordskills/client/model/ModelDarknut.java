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
import zeldaswordskills.entity.mobs.EntityDarknut;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * @author Model concept by TheRedMajora, re-coded and animated by coolAlias
 *
 */
@SideOnly(Side.CLIENT)
public class ModelDarknut extends ModelBase implements IModelBiped
{
	private ModelRenderer head;
	private ModelRenderer body;
	private ModelRenderer leftArm;
	private ModelRenderer rightArm;
	private ModelRenderer rightLeg;
	private ModelRenderer leftLeg;
	private ModelRenderer shoulderPlateRight;
	private ModelRenderer shoulderPlateLeft;
	private ModelRenderer upperArmor;
	private ModelRenderer lowerArmor;
	private ModelRenderer helmRightPlate;
	private ModelRenderer helmTopPlate;
	private ModelRenderer helmBackPlate;
	private ModelRenderer helmLeftPlate;
	private ModelRenderer cheekGuardLeft;
	private ModelRenderer noseGuard;
	private ModelRenderer cheekGuardRight;
	private ModelRenderer helmVisor;
	private ModelRenderer hornHolderLeft;
	private ModelRenderer hornLeft;
	private ModelRenderer hornRight;
	private ModelRenderer hornHolderRight;
	private ModelRenderer cape;

	/** Whether the model should be rendered holding an item in the left hand, and if that item is a block. */
	private int heldItemLeft;

	/** Whether the model should be rendered holding an item in the right hand, and if that item is a block. */
	private int heldItemRight;

	/** Regular attack animation, right arm rotation Z */
	private static final float[] attackRotZ = { 0.2F, 0.45F, 0.75F, 1.0F, 0.75F, 0.45F, 0.2F, 0.0F, -0.2F};

	private static final float[] powRotXR = { -2.5F, -1.5F, -1.0F, -0.5F, -0.4F };
	private static final float[] powRotYR = { 0F, -0.1F, -0.2F, -0.2F, -0.2F };
	private static final float[] powRotZR = { 0F, 0F, 0F, -0.25F, -0.5F };
	private static final float[] powRotXL = { -2.0F, -1.4F, -1.0F, -0.6F, -0.4F };
	private static final float[] powRotYL = { 1.0F, 0.9F, 0.8F, 0.5F, 0.2F };
	private static final float[] powRotZL = { 0.0F, 0.15F, 0.3F, 0.45F, 0.5F };

	public ModelDarknut() {
		textureWidth = 64;
		textureHeight = 64;
		head = new ModelRenderer(this, 0, 0);
		head.setTextureSize(textureWidth, textureHeight);
		head.addBox(-4F, -8F, -4F, 8, 8, 8);
		head.setRotationPoint(0F, 0F, 0F);
		setRotation(head, 0F, 0F, 0F);
		body = new ModelRenderer(this, 16, 16);
		body.setTextureSize(textureWidth, textureHeight);
		body.addBox(-4F, 0F, -2F, 8, 12, 4);
		body.setRotationPoint(0F, 0F, 0F);
		setRotation(body, 0F, 0F, 0F);
		rightArm = new ModelRenderer(this, 40, 16);
		rightArm.setTextureSize(textureWidth, textureHeight);
		rightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
		rightArm.setRotationPoint(-5F, 2F, 0F);
		setRotation(rightArm, 0F, 0F, 0F);
		leftArm = new ModelRenderer(this, 40, 16);
		leftArm.mirror = true;
		leftArm.setTextureSize(textureWidth, textureHeight);
		leftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
		leftArm.setRotationPoint(5F, 2F, 0F);
		setRotation(leftArm, 0F, 0F, 0F);
		rightLeg = new ModelRenderer(this, 0, 16);
		rightLeg.mirror = true;
		rightLeg.setTextureSize(textureWidth, textureHeight);
		rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
		rightLeg.setRotationPoint(-2F, 12F, 0F);
		setRotation(rightLeg, 0F, 0F, 0F);
		leftLeg = new ModelRenderer(this, 0, 16);
		leftLeg.setTextureSize(textureWidth, textureHeight);
		leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
		leftLeg.setRotationPoint(2F, 12F, 0F);
		setRotation(leftLeg, 0F, 0F, 0F);

		// ARMOR
		shoulderPlateRight = new ModelRenderer(this, 0, 58);
		shoulderPlateRight.setTextureSize(textureWidth, textureHeight);
		shoulderPlateRight.addBox(-5F, -2.5F, -2.5F, 6, 1, 5);
		shoulderPlateRight.setRotationPoint(-5F, 2F, 0F);
		setRotation(shoulderPlateRight, 0F, 0F, 0F);

		shoulderPlateLeft = new ModelRenderer(this, 0, 58);
		shoulderPlateLeft.mirror = true;
		shoulderPlateLeft.setTextureSize(textureWidth, textureHeight);
		shoulderPlateLeft.addBox(-1F, -2.5F, -2.5F, 6, 1, 5);
		shoulderPlateLeft.setRotationPoint(5F, 2F, 0F);
		setRotation(shoulderPlateLeft, 0F, 0F, 0F);

		upperArmor = new ModelRenderer(this, 0, 32);
		upperArmor.setTextureSize(textureWidth, textureHeight);
		upperArmor.addBox(-5F, -3.2F, -3F, 10, 7, 6);
		upperArmor.setRotationPoint(0F, 3F, 0F);
		setRotation(upperArmor, 0F, 0F, 0F);

		lowerArmor = new ModelRenderer(this, 0, 46);
		lowerArmor.setTextureSize(textureWidth, textureHeight);
		lowerArmor.addBox(-4.5F, -3.2F, -2.5F, 9, 6, 5);
		lowerArmor.setRotationPoint(0F, 10F, 0F);
		setRotation(lowerArmor, 0F, 0F, 0F);

		// HELMET
		helmBackPlate = new ModelRenderer(this, 0, 55);
		helmBackPlate.setTextureSize(textureWidth, textureHeight);
		helmBackPlate.addBox(-4.5F, -7.6F, 3.5F, 9, 8, 1);
		helmBackPlate.setRotationPoint(0F, 0F, 0F);
		setRotation(helmBackPlate, 0F, 0F, 0F);

		helmTopPlate = new ModelRenderer(this, 0, 54);
		helmTopPlate.setTextureSize(textureWidth, textureHeight);
		helmTopPlate.addBox(-4.5F, -7.6F, -4.5F, 9, 1, 9);
		helmTopPlate.setRotationPoint(0F, -1.0F, 0F);
		setRotation(helmTopPlate, 0F, 0F, 0F);
		helmBackPlate.addChild(helmTopPlate);

		helmRightPlate = new ModelRenderer(this, 0, 48);
		helmRightPlate.setTextureSize(textureWidth, textureHeight);
		helmRightPlate.addBox(-3.5F, -7.6F, -4.5F, 1, 8, 8);
		helmRightPlate.setRotationPoint(-1F, 0F, 0F);
		setRotation(helmRightPlate, 0F, 0F, 0F);
		helmBackPlate.addChild(helmRightPlate);

		helmLeftPlate = new ModelRenderer(this, 0, 48);
		helmLeftPlate.mirror = true;
		helmLeftPlate.setTextureSize(textureWidth, textureHeight);
		helmLeftPlate.addBox(2.5F, -7.6F, -4.5F, 1, 8, 8);
		helmLeftPlate.setRotationPoint(1F, 0F, 0F);
		setRotation(helmLeftPlate, 0F, 0F, 0F);
		helmBackPlate.addChild(helmLeftPlate);

		helmVisor = new ModelRenderer(this, 1, 62);
		helmVisor.setTextureSize(textureWidth, textureHeight);
		helmVisor.addBox(-3.5F, -6.6F, -4.5F, 7, 1, 1);
		helmVisor.setRotationPoint(0F, 0F, 0F);
		setRotation(helmVisor, 0F, 0F, 0F);
		helmTopPlate.addChild(helmVisor);

		noseGuard = new ModelRenderer(this, 46, 11);
		noseGuard.setTextureSize(textureWidth, textureHeight);
		noseGuard.addBox(-1F, -5.6F, -4.5F, 2, 4, 1);
		noseGuard.setRotationPoint(0F, 0F, 0F);
		setRotation(noseGuard, 0F, 0F, 0F);
		helmVisor.addChild(noseGuard);

		cheekGuardRight = new ModelRenderer(this, 32, 10);
		cheekGuardRight.setTextureSize(textureWidth, textureHeight);
		cheekGuardRight.addBox(-2.5F, -4.5F, -4.5F, 2, 5, 1);
		cheekGuardRight.setRotationPoint(0F, 0F, 0F);
		setRotation(cheekGuardRight, 0F, 0F, 0F);
		helmRightPlate.addChild(cheekGuardRight);

		cheekGuardLeft = new ModelRenderer(this, 32, 10);
		cheekGuardLeft.mirror = true;
		cheekGuardLeft.setTextureSize(textureWidth, textureHeight);
		cheekGuardLeft.addBox(0.5F, -4.5F, -4.5F, 2, 5, 1);
		cheekGuardLeft.setRotationPoint(0F, 0F, 0F);
		setRotation(cheekGuardLeft, 0F, 0F, 0F);
		helmLeftPlate.addChild(cheekGuardLeft);

		hornHolderRight = new ModelRenderer(this, 53, 0);
		hornHolderRight.setTextureSize(textureWidth, textureHeight);
		hornHolderRight.addBox(-5F, -6F, -1.5F, 2, 3, 3);
		hornHolderRight.setRotationPoint(0F, 0F, 0F);
		setRotation(hornHolderRight, 0F, 0F, 0F);
		helmRightPlate.addChild(hornHolderRight);

		hornHolderLeft = new ModelRenderer(this, 53, 0);
		hornHolderLeft.mirror = true;
		hornHolderLeft.setTextureSize(textureWidth, textureHeight);
		hornHolderLeft.addBox(3F, -6F, -1.5F, 2, 3, 3);
		hornHolderLeft.setRotationPoint(0F, 0F, 0F);
		setRotation(hornHolderLeft, 0F, 0F, 0F);
		helmLeftPlate.addChild(hornHolderLeft);

		hornRight = new ModelRenderer(this, 38, 0);
		hornRight.setTextureSize(textureWidth, textureHeight);
		hornRight.addBox(-10F, -5.5F, -1F, 5, 2, 2);
		hornRight.setRotationPoint(0F, 0F, 0F);
		hornRight.setRotationPoint(-1.5F, 1.75F, 0F);
		setRotation(hornRight, 0F, 0F, ((float) Math.PI / 8.0F));
		hornHolderRight.addChild(hornRight);

		hornLeft = new ModelRenderer(this, 38, 0);
		hornLeft.mirror = true;
		hornLeft.setTextureSize(textureWidth, textureHeight);
		hornLeft.addBox(5F, -5.5F, -1F, 5, 2, 2);
		hornLeft.setRotationPoint(1.5F, 1.75F, 0F);
		setRotation(hornLeft, 0F, 0F, -((float) Math.PI / 8.0F));
		hornHolderLeft.addChild(hornLeft);

		cape = new ModelRenderer(this, 32, 45);
		cape.addBox(-5F, 0F, 0F, 10, 16, 1);
		cape.setRotationPoint(0F, 0F, 3F);
		cape.setTextureSize(textureWidth, textureHeight);
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		head.render(f5);
		body.render(f5);
		leftArm.render(f5);
		rightArm.render(f5);
		rightLeg.render(f5);
		leftLeg.render(f5);
		renderArmor((EntityDarknut) entity, f, f1, f2, f3, f4, f5);
	}

	/**
	 * Call to render the Darknut's armor, if it has not been removed
	 */
	private void renderArmor(EntityDarknut entity, float f, float f1, float f2, float f3, float f4, float f5) {
		if (entity.isArmored()) {
			shoulderPlateRight.render(f5);
			shoulderPlateLeft.render(f5);
			upperArmor.render(f5);
			lowerArmor.render(f5);
			helmBackPlate.render(f5);
		}
		if (entity.isWearingCape()) {
			cape.render(f5);
		}
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float par2, float par3, float partialTick) {
		setLivingAnimations((EntityDarknut) entity, par2, par3, partialTick);
	}

	private void setLivingAnimations(EntityDarknut entity, float par2, float par3, float partialTick) {
		int i = entity.attackTimer;
		int j = entity.chargeTimer;
		if (entity.isSpinning()) {
			rightArm.rotateAngleX = powRotXR[4];
			rightArm.rotateAngleY = powRotYR[4];
			rightArm.rotateAngleZ = powRotZR[4];
			leftArm.rotateAngleX = powRotXL[4];
			leftArm.rotateAngleY = powRotYL[4];
			leftArm.rotateAngleZ = powRotZL[4];
		} else if (i > 0 && entity.isPowerAttack) {
			// Start at array index 0 (top) and swing down, holding for a few ticks at end of swing
			i = (7 - i);
			if (i < 4) { // animation is 5 ticks total
				rightArm.rotateAngleX = powRotXR[i] + (partialTick * (powRotXR[i + 1] - powRotXR[i]));
				rightArm.rotateAngleY = powRotYR[i] + (partialTick * (powRotYR[i + 1] - powRotYR[i]));
				rightArm.rotateAngleZ = powRotZR[i] + (partialTick * (powRotZR[i + 1] - powRotZR[i]));;
				leftArm.rotateAngleX = powRotXL[i] + (partialTick * (powRotXL[i + 1] - powRotXL[i]));
				leftArm.rotateAngleY = powRotYL[i] + (partialTick * (powRotYL[i + 1] - powRotYL[i]));
				leftArm.rotateAngleZ = powRotZL[i] + (partialTick * (powRotZL[i + 1] - powRotZL[i]));
			} else { // hold on last position for a few ticks
				rightArm.rotateAngleX = powRotXR[4];
				rightArm.rotateAngleY = powRotYR[4];
				rightArm.rotateAngleZ = powRotZR[4];
				leftArm.rotateAngleX = powRotXL[4];
				leftArm.rotateAngleY = powRotYL[4];
				leftArm.rotateAngleZ = powRotZL[4];
			}
		} else if (Math.abs(i) > 0) {
			int isLeft = (i > 0 ? 1 : -1);
			i = Math.abs(i);
			rightArm.rotateAngleX = MathHelper.clamp_float(-1.0F + 3.5F * getSwingAmount((float) i - partialTick, 10.0F), -4.5F, 1.5F);
			rightArm.rotateAngleY = 0.0F;
			rightArm.rotateAngleZ = isLeft * (attackRotZ[(i + 1) % 9] + (partialTick * (attackRotZ[i % 9] - attackRotZ[(i + 1) % 9])));
			leftArm.rotateAngleX = 0.0F;
			leftArm.rotateAngleZ = 0.0F;
		} else if (j > 0) {
			int max = entity.getChargeTime() - 1;
			if (j == max) {
				i = 3; // start on next to last index and work backwards
			} else if (j == (max -1)) {
				i = 2;
			} else if (j == (max - 2)) {
				i = 1;
			} else {
				i = 0;
			}
			rightArm.rotateAngleX = (i == 0 ? -3.5F : powRotXR[i]);
			rightArm.rotateAngleY = powRotYR[i];
			rightArm.rotateAngleZ = 0.0F;
			leftArm.rotateAngleX = powRotXL[i];
			leftArm.rotateAngleY = powRotYL[i];
			leftArm.rotateAngleZ = powRotZL[i];
		} else {
			rightArm.rotateAngleZ = 0.0F;
			leftArm.rotateAngleZ = 0.0F;
		}
	}

	private float getSwingAmount(float f, float max) {
		return (Math.abs(f % max - max * 0.5F) - max * 0.25F) / (max * 0.25F);
	}

	@Override
	public void setRotationAngles(float f1, float f2, float f3, float f4, float f5, float f6, Entity entity) {
		super.setRotationAngles(f1, f2, f3, f4, f5, f6, entity);
		head.rotateAngleY = f4 / (180F / (float)Math.PI);
		head.rotateAngleX = f5 / (180F / (float)Math.PI);
		helmBackPlate.rotateAngleY = head.rotateAngleY;
		helmBackPlate.rotateAngleX = head.rotateAngleX;

		// only set arm angles if not currently animating
		EntityDarknut darknut = (EntityDarknut) entity;
		boolean adjustArms = (darknut.attackTimer == 0 && darknut.chargeTimer == 0 && !darknut.isSpinning());
		if (adjustArms) {
			rightArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 2.0F * f2 * 0.5F;
			rightArm.rotateAngleZ = 0.0F;
			leftArm.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 2.0F * f2 * 0.5F;
			leftArm.rotateAngleZ = 0.0F;
			rightArm.rotateAngleY = 0.0F;
			leftArm.rotateAngleY = 0.0F;
		}

		rightLeg.rotateAngleX = MathHelper.cos(f1 * 0.6662F) * 1.4F * f2;
		leftLeg.rotateAngleX = MathHelper.cos(f1 * 0.6662F + (float) Math.PI) * 1.4F * f2;
		rightLeg.rotateAngleY = 0.0F;
		leftLeg.rotateAngleY = 0.0F;

		if (heldItemLeft != 0) {
			leftArm.rotateAngleX = leftArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) heldItemLeft;
		}
		if (heldItemRight != 0) {
			rightArm.rotateAngleX = rightArm.rotateAngleX * 0.5F - ((float) Math.PI / 10F) * (float) heldItemRight;
		}

		if (onGround > -9990.0F) {
			f6 = onGround;
			body.rotateAngleY = MathHelper.sin(MathHelper.sqrt_float(f6) * (float) Math.PI * 2.0F) * 0.2F;
			if (adjustArms) {
				rightArm.rotationPointZ = MathHelper.sin(body.rotateAngleY) * 5.0F;
				rightArm.rotationPointX = -MathHelper.cos(body.rotateAngleY) * 5.0F;
				leftArm.rotationPointZ = -MathHelper.sin(body.rotateAngleY) * 5.0F;
				leftArm.rotationPointX = MathHelper.cos(body.rotateAngleY) * 5.0F;
				rightArm.rotateAngleY += body.rotateAngleY;
				leftArm.rotateAngleY += body.rotateAngleY;
				leftArm.rotateAngleX += body.rotateAngleY;
				f6 = 1.0F - onGround;
				f6 *= f6;
				f6 *= f6;
				f6 = 1.0F - f6;
				float f7 = MathHelper.sin(f6 * (float) Math.PI);
				float f8 = MathHelper.sin(onGround * (float) Math.PI) * -(head.rotateAngleX - 0.7F) * 0.75F;
				rightArm.rotateAngleX = (float)((double) rightArm.rotateAngleX - ((double) f7 * 1.2D + (double) f8));
				rightArm.rotateAngleY += body.rotateAngleY * 2.0F;
				// the following makes the arm rotate parallel to the body
				rightArm.rotateAngleZ = MathHelper.sin(onGround * (float) Math.PI) * -0.4F;
			}
		}
		if (adjustArms) {
			rightArm.rotateAngleZ += MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
			leftArm.rotateAngleZ -= MathHelper.cos(f3 * 0.09F) * 0.05F + 0.05F;
			rightArm.rotateAngleX += MathHelper.sin(f3 * 0.067F) * 0.05F;
			leftArm.rotateAngleX -= MathHelper.sin(f3 * 0.067F) * 0.05F;
		}
		// Always adjust shoulder plates to match arm angles
		shoulderPlateRight.rotateAngleX = rightArm.rotateAngleX;
		shoulderPlateRight.rotateAngleY = rightArm.rotateAngleY;
		shoulderPlateRight.rotateAngleZ = rightArm.rotateAngleZ;
		shoulderPlateLeft.rotateAngleX = leftArm.rotateAngleX;
		shoulderPlateLeft.rotateAngleY = leftArm.rotateAngleY;
		shoulderPlateLeft.rotateAngleZ = leftArm.rotateAngleZ;
	}

	@Override
	public void postRenderHead(float scale) {
		head.postRender(scale);
	}

	@Override
	public void postRenderArm(boolean isRight, float scale) {
		if (isRight) {
			rightArm.postRender(scale);
		} else {
			leftArm.postRender(scale);
		}
	}

	@Override
	public void setHeldItemValue(boolean isRight, int heldValue) {
		if (isRight) {
			heldItemRight = heldValue;
		} else {
			heldItemLeft = heldValue;
		}
	}
}

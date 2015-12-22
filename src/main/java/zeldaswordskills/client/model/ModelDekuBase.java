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

package zeldaswordskills.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import zeldaswordskills.api.client.animation.*;
import zeldaswordskills.api.client.model.*;
import zeldaswordskills.entity.mobs.EntityDekuBaba;
import zeldaswordskills.entity.mobs.EntityDekuBase;

import com.google.common.collect.ImmutableList;

/**
 * 
 * Basic Deku Baba model with prone animation
 *
 */
public abstract class ModelDekuBase extends ModelBase
{
	protected SmartModelRenderer stem1, stem2, stem3;
	protected SmartModelRenderer head_base;
	protected SmartModelRenderer tongue_base, tongue_mid, tongue_tip;
	protected SmartModelRenderer mouth_base_upper;
	protected SmartModelRenderer mouth_upper;
	protected SmartModelRenderer mouth_upper_top1;
	protected SmartModelRenderer mouth_upper_top2;
	protected SmartModelRenderer mouth_base_lower;
	protected SmartModelRenderer mouth_lower;
	protected SmartModelRenderer mouth_lower_bottom1;
	protected SmartModelRenderer mouth_lower_bottom2;
	protected SmartModelRenderer stem_base;
	protected SmartModelRenderer leaf1_base, leaf1_mid, leaf1_tip;
	protected SmartModelRenderer leaf2_base, leaf2_mid, leaf2_tip;
	protected SmartModelRenderer leaf3_base, leaf3_mid, leaf3_tip;
	protected SmartModelRenderer leaf4_base, leaf4_mid, leaf4_tip;
	protected final ImmutableList<IAnimation> DEATH_ANIMATION, PRONE_ANIMATION;

	public ModelDekuBase() {
		this.textureWidth = 64;
		this.textureHeight = 32;
		// Stem and Head
		// stem1 has a dynamic offset value for the sprouting animation
		this.stem1 = new SmartOffsetModelRenderer(this, 56, 0);
		this.stem1.setRotationPoint(0.0F, 22.0F, 0.0F);
		this.stem1.addBox(-1.0F, -11.0F, -1.0F, 2, 11, 2, 0.0F);
		this.stem1.setInitialPose(0, 0, 0); // save original offset values (even though all are currently zero...)
		// stem2 has a dynamic rotation point during the death animation, so it needs to use SmartModelRendererPlus
		this.stem2 = new SmartModelRendererPlus(this, 56, 0);
		this.stem2.setRotationPoint(0.0F, -11.0F, 0.0F);
		this.stem2.addBox(-1.0F, -6.0F, -1.0F, 2, 6, 2, 0.0F);
		this.stem2.setInitialPose(0, 0, 0); // required to save original rotation point values
		this.stem3 = new SmartModelRenderer(this, 56, 0);
		this.stem3.setRotationPoint(0.0F, -6.0F, 0.0F);
		this.stem3.addBox(-1.0F, -5.0F, -1.0F, 2, 5, 2, 0.0F);
		this.head_base = new SmartModelRenderer(this, 56, 9);
		this.head_base.setRotationPoint(0.0F, -5.0F, 0.0F);
		this.head_base.addBox(-0.5F, -1.0F, -0.5F, 1, 1, 1, 0.0F);
		this.stem1.addChild(this.stem2);
		this.stem2.addChild(this.stem3);
		this.stem3.addChild(this.head_base);
		// Tongue
		this.tongue_base = new SmartModelRenderer(this, 30, 0);
		this.tongue_base.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.tongue_base.addBox(-1.5F, -0.5F, -4.0F, 3, 1, 4, 0.0F);
		this.tongue_base.setInitialPose(-1.5707963267948966F, 0.0F, 0.0F);
		this.tongue_mid = new SmartModelRenderer(this, 34, 0);
		this.tongue_mid.setRotationPoint(0.0F, 0.0F, -4.0F);
		this.tongue_mid.addBox(-1.0F, -0.5F, -3.0F, 2, 1, 3, 0.0F);
		this.tongue_mid.setInitialPose(0.2617993877991494F, 0.0F, 0.0F);
		this.tongue_tip = new SmartModelRenderer(this, 36, 0);
		this.tongue_tip.setRotationPoint(0.0F, 0.0F, -3.0F);
		this.tongue_tip.addBox(-0.5F, -0.5F, -3.0F, 1, 1, 3, 0.0F);
		this.tongue_tip.setInitialPose(-0.2617993877991494F, 0.0F, 0.0F);
		this.head_base.addChild(this.tongue_base);
		this.tongue_base.addChild(this.tongue_mid);
		this.tongue_mid.addChild(this.tongue_tip);
		// Lower Jaw
		this.mouth_base_lower = new SmartModelRenderer(this, 0, 9);
		this.mouth_base_lower.setRotationPoint(0.0F, 0.0F, -0.5F);
		this.mouth_base_lower.addBox(-3.0F, -0.5F, -4.0F, 6, 1, 4, 0.0F);
		this.mouth_base_lower.setInitialPose(-1.0471975511965976F, 0.0F, 0.0F);
		this.mouth_lower = new SmartModelRenderer(this, 26, 25);
		this.mouth_lower.setRotationPoint(0.0F, 0.0F, -4.0F);
		this.mouth_lower.addBox(-3.0F, -0.5F, -6.0F, 6, 1, 6, 0.0F);
		this.mouth_lower.setInitialPose(-0.4363323129985824F, 0.0F, 0.0F);
		this.mouth_lower_bottom1 = new SmartModelRenderer(this, 0, 18);
		this.mouth_lower_bottom1.setRotationPoint(0.0F, 1.0F, 0.0F);
		this.mouth_lower_bottom1.addBox(-2.0F, -0.5F, -5.0F, 4, 1, 5, 0.0F);
		this.mouth_lower_bottom2 = new SmartModelRenderer(this, 0, 14);
		this.mouth_lower_bottom2.setRotationPoint(0.0F, 2.0F, 0.0F);
		this.mouth_lower_bottom2.addBox(-1.0F, -0.5F, -3.0F, 2, 1, 3, 0.0F);
		this.head_base.addChild(this.mouth_base_lower);
		this.mouth_base_lower.addChild(this.mouth_lower);
		this.mouth_lower.addChild(this.mouth_lower_bottom1);
		this.mouth_lower.addChild(this.mouth_lower_bottom2);
		// Upper Jaw
		this.mouth_base_upper = new SmartModelRenderer(this, 0, 5);
		this.mouth_base_upper.setRotationPoint(0.0F, 0.0F, 0.5F);
		this.mouth_base_upper.addBox(-3.0F, -3.0F, -0.5F, 6, 3, 1, 0.0F);
		this.mouth_base_upper.setInitialPose(-0.5235987755982988F, 0.0F, 0.0F);
		this.mouth_upper = new SmartModelRenderer(this, 0, 24);
		this.mouth_upper.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.mouth_upper.addBox(-3.0F, -0.5F, -7.0F, 6, 1, 7, 0.0F);
		this.mouth_upper.setInitialPose(-1.2217304763960306F, 0.0F, 0.0F);
		this.mouth_upper_top1 = new SmartModelRenderer(this, 26, 18);
		this.mouth_upper_top1.setRotationPoint(0.0F, -1.0F, 0.0F);
		this.mouth_upper_top1.addBox(-3.0F, -0.5F, -6.0F, 6, 1, 6, 0.0F);
		this.mouth_upper_top2 = new SmartModelRenderer(this, 26, 13);
		this.mouth_upper_top2.setRotationPoint(0.0F, -2.0F, -1.0F);
		this.mouth_upper_top2.addBox(-2.0F, -0.5F, -4.0F, 4, 1, 4, 0.0F);
		this.head_base.addChild(this.mouth_base_upper);
		this.mouth_base_upper.addChild(this.mouth_upper);
		this.mouth_upper.addChild(this.mouth_upper_top1);
		this.mouth_upper.addChild(this.mouth_upper_top2);
		// Plant base + leaves
		this.stem_base = new SmartModelRenderer(this, 44, 0);
		this.stem_base.setRotationPoint(0.0F, 22.0F, 0.0F);
		this.stem_base.addBox(-1.5F, 0.0F, -1.5F, 3, 2, 3, 0.0F);
		// First Leaf
		this.leaf1_base = new SmartModelRenderer(this, 50, 16);
		this.leaf1_base.setRotationPoint(2.0F, 2.0F, 0.0F);
		this.leaf1_base.addBox(0.0F, -3.0F, -2.5F, 1, 3, 5, 0.0F);
		this.leaf1_base.setInitialPose(0.0F, 0.0F, 0.4363323129985824F);
		this.leaf1_mid = new SmartModelRenderer(this, 50, 14);
		this.leaf1_mid.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf1_mid.addBox(0.0F, -3.0F, -2.0F, 1, 3, 4, 0.0F);
		this.leaf1_mid.setInitialPose(0.0F, 0.0F, 0.5235987755982988F);
		this.leaf1_tip = new SmartModelRenderer(this, 50, 14);
		this.leaf1_tip.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf1_tip.addBox(0.0F, -5.0F, -1.5F, 1, 5, 3, 0.0F);
		this.leaf1_tip.setInitialPose(0.0F, 0.0F, 0.5235987755982988F);
		this.stem_base.addChild(this.leaf1_base);
		this.leaf1_base.addChild(this.leaf1_mid);
		this.leaf1_mid.addChild(this.leaf1_tip);
		// Second Leaf
		this.leaf2_base = new SmartModelRenderer(this, 50, 16);
		this.leaf2_base.setRotationPoint(-2.0F, 2.0F, 0.0F);
		this.leaf2_base.addBox(0.0F, -3.0F, -2.5F, 1, 3, 5, 0.0F);
		this.leaf2_base.setInitialPose(0.0F, 3.141592653589793F, -0.4363323129985824F);
		this.leaf2_mid = new SmartModelRenderer(this, 50, 14);
		this.leaf2_mid.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf2_mid.addBox(0.0F, -3.0F, -2.0F, 1, 3, 4, 0.0F);
		this.leaf2_mid.setInitialPose(0.0F, 0.0F, 0.5235987755982988F);
		this.leaf2_tip = new SmartModelRenderer(this, 50, 14);
		this.leaf2_tip.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf2_tip.addBox(0.0F, -5.0F, -1.5F, 1, 5, 3, 0.0F);
		this.leaf2_tip.setInitialPose(0.0F, 0.0F, 0.5235987755982988F);
		this.stem_base.addChild(this.leaf2_base);
		this.leaf2_base.addChild(this.leaf2_mid);
		this.leaf2_mid.addChild(this.leaf2_tip);
		// Third Leaf
		this.leaf3_base = new SmartModelRenderer(this, 50, 16);
		this.leaf3_base.setRotationPoint(0.0F, 2.0F, 2.0F);
		this.leaf3_base.addBox(-2.5F, -3.0F, 0.0F, 5, 3, 1, 0.0F);
		this.leaf3_base.setInitialPose(-0.4363323129985824F, 0.0F, 0.0F);
		this.leaf3_mid = new SmartModelRenderer(this, 50, 14);
		this.leaf3_mid.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf3_mid.addBox(-2.0F, -3.0F, 0.0F, 4, 3, 1, 0.0F);
		this.leaf3_mid.setInitialPose(-0.5235987755982988F, 0.0F, 0.0F);
		this.leaf3_tip = new SmartModelRenderer(this, 50, 14);
		this.leaf3_tip.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf3_tip.addBox(-1.5F, -5.0F, 0.0F, 3, 5, 1, 0.0F);
		this.leaf3_tip.setInitialPose(-0.5235987755982988F, 0.0F, 0.0F);
		this.stem_base.addChild(this.leaf3_base);
		this.leaf3_base.addChild(this.leaf3_mid);
		this.leaf3_mid.addChild(this.leaf3_tip);
		// Fourth Leaf
		this.leaf4_base = new SmartModelRenderer(this, 50, 16);
		this.leaf4_base.setRotationPoint(0.0F, 2.0F, -2.0F);
		this.leaf4_base.addBox(-2.5F, -3.0F, 0.0F, 5, 3, 1, 0.0F);
		this.leaf4_base.setInitialPose(-0.4363323129985824F, 3.141592653589793F, 0.0F);
		this.leaf4_mid = new SmartModelRenderer(this, 50, 14);
		this.leaf4_mid.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf4_mid.addBox(-2.0F, -3.0F, 0.0F, 4, 3, 1, 0.0F);
		this.leaf4_mid.setInitialPose(-0.5235987755982988F, 0.0F, 0.0F);
		this.leaf4_tip = new SmartModelRenderer(this, 50, 14);
		this.leaf4_tip.setRotationPoint(0.0F, -3.0F, 0.0F);
		this.leaf4_tip.addBox(-1.5F, -5.0F, 0.0F, 3, 5, 1, 0.0F);
		this.leaf4_tip.setInitialPose(-0.5235987755982988F, 0.0F, 0.0F);
		this.stem_base.addChild(this.leaf4_base);
		this.leaf4_base.addChild(this.leaf4_mid);
		this.leaf4_mid.addChild(this.leaf4_tip);
		DEATH_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetPoint(stem2, RotationAxis.X, 16F, 0, 15))
				.add(new AnimationTargetPoint(stem2, RotationAxis.Y, -4F, 0, 7).setAllowInversion(false))
				.add(new AnimationTargetPoint(stem2, RotationAxis.Y, 11F, 8, 15).setAllowInversion(false))
				.add(new AnimationTargetPoint(stem2, RotationAxis.Z, -6F, 0, 15).setAllowInversion(false))
				.add(new AnimationTargetAngle(stem2, RotationAxis.Y, 15.0F, 0, 15, true))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, -15.0F, 0, 15, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(stem2, RotationAxis.Z, -60.0F, 0, 15, true))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 40.0F, 0, 15, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(head_base, RotationAxis.Z, 20.0F, 0, 8, true))
				.add(new AnimationTargetAngle(head_base, RotationAxis.Z, -20.0F, 15, 18, true))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -30.0F, 0, 12, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -60.0F, 12, 15, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -60.0F, 0, 12, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -40.0F, 12, 15, true).setAllowInversion(false))
				.add(new AnimationWave.AnimationWaveSin(RotationAxis.Z, 0.25F, 0.85F, 1.0F, tongue_base, tongue_mid, tongue_tip))
				.build();
		int end = EntityDekuBaba.ACTION_PRONE.duration;
		int start = end - 3;
		PRONE_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, 0.0F, 0, 5, true))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -15.0F, start, end, true))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 0.0F, 0, 5, true))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, start, end, true))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 0.0F, 0, 5, true))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 30.0F, start, end, true))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 0.0F, 0, 5, true))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 60.0F, start, end, true))
				.build();
	}

	/**
	 * Resets all model parts to their initial pose; this only works if the model is
	 * composed of {@link SmartModelRenderer SmartModelRenderers} and {@link #setInitialPose}
	 * was called for each of them during the construction of the model
	 */
	protected void resetPose() {
		for (int i = 0; i < this.boxList.size(); ++i) {
			Object o = boxList.get(i);
			if (o instanceof SmartModelRenderer) {
				((SmartModelRenderer) o).resetPose();
			}
		}
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		this.stem_base.render(f5);
		this.stem1.render(f5);
	}

	@Override
	public final void setLivingAnimations(EntityLivingBase entity, float par2, float par3, float partialTick) {
		resetPose();
		applyAnimations((EntityDekuBase) entity, par2, par3, partialTick);
	}

	/**
	 * Called from {@link #setLivingAnimations(EntityLivingBase, float, float, float) setLivingAnimations} after resetting the pose
	 */
	protected abstract void applyAnimations(EntityDekuBase entity, float par2, float par3, float partialTick);

	/**
	 * Returns true if a death animation was applied
	 */
	protected boolean applyDeathAnimation(EntityDekuBase entity, float par2, float par3, float partialTick) {
		ImmutableList<IAnimation> animation = getDeathAnimation(entity);
		if (animation != null) {
			IAnimation.Helper.applyAnimation(animation, entity.deathTime, partialTick, 1.0F, 1.0F, 0.0F, entity.custom_death < 0);
		}
		return animation != null;
	}

	/**
	 * Return custom death animation, if any, based on the baba's {@link EntityDekuBase#custom_death} type
	 */
	protected ImmutableList<IAnimation> getDeathAnimation(EntityDekuBase entity) {
		return entity.custom_death == 0 ? null : DEATH_ANIMATION;
	}
}

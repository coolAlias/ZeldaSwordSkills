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

import net.minecraft.entity.Entity;
import zeldaswordskills.api.client.animation.*;
import zeldaswordskills.api.client.model.SmartModelRenderer;
import zeldaswordskills.api.client.model.SmartModelRendererComplete;
import zeldaswordskills.api.entity.ai.EntityAction;
import zeldaswordskills.entity.mobs.EntityDekuBase;
import zeldaswordskills.entity.mobs.EntityDekuFire;

import com.google.common.collect.ImmutableList;

public class ModelDekuFire extends ModelDekuBaba
{
	protected SmartModelRenderer[] gland, gland_copy, gland_top;

	protected final ImmutableList<IAnimation> SPIT_ANIMATION, SEVER_ANIMATION;

	public ModelDekuFire() {
		super();
		gland = new SmartModelRenderer[6];
		gland_copy = new SmartModelRenderer[6];
		for (int i = 0; i < gland.length; ++i) {
			gland[i] = (i == 0 ? new SmartModelRendererComplete(this, 0, 0) : new SmartModelRenderer(this, 0, 0));
			gland[i].setRotationPoint(0.0F, (i > 0 ? 0.0F : -2.8F), (i > 0 ? 0.0F : -1.3F));
			gland[i].addBox(-1.5F, -1.5F, -1.5F, 2, 3, 2, 0.0F);
			float angle = (float) Math.toRadians(i == (gland.length - 1) ? 30.0F : i * -30.0F);
			gland[i].setInitialPose(0.0F, angle, 0.0F);
			if (i > 0) {
				gland[0].addChild(gland[i]);
			}
			// Make a copy that won't be added as a child of stem, for rendering when severed
			gland_copy[i] = (i == 0 ? new SmartModelRendererComplete(this, 0, 0) : new SmartModelRenderer(this, 0, 0));
			gland_copy[i].setRotationPoint(0.0F, (i > 0 ? 0.0F : 2.0F), (i > 0 ? 0.0F : -1.35F));
			gland_copy[i].addBox(-1.5F, -1.5F, -1.5F, 2, 3, 2, 0.0F);
			if (i == 0) {
				gland_copy[i].isHidden = true;
			}
			gland_copy[i].setInitialPose(0.0F, angle, 0.0F);
			if (i > 0) {
				gland_copy[0].addChild(gland_copy[i]);
			}
		}
		stem3.addChild(gland[0]);
		gland_top = new SmartModelRenderer[4];
		for (int i = 0; i < gland_top.length; ++i) {
			gland_top[i] = new SmartModelRenderer(this, 0, 0);
			gland_top[i].setRotationPoint(0.0F, (i > 1 ? 1.4F : -1.4F), -0.5F);
			gland_top[i].addBox(-1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F);
			gland_top[i].setInitialPose((i % 2 == 1 ? 0.7853981633974483F : 0.0F), 0.0F, 0.0F);
			gland[0].addChild(gland_top[i]);
			// make a copy for rendering the severed gland
			SmartModelRenderer copy = new SmartModelRenderer(this, 0, 0);
			copy.setRotationPoint(0.0F, (i > 1 ? 1.4F : -1.4F), -0.5F);
			copy.addBox(-1.5F, -0.5F, -0.5F, 3, 1, 1, 0.0F);
			copy.setInitialPose((i % 2 == 1 ? 0.7853981633974483F : 0.0F), 0.0F, 0.0F);
			gland_copy[0].addChild(copy);
		}
		SPIT_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -35.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, 20.0F, 3, 7, true).setProgressType(IProgressType.CUBED))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -15.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 45.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, 3, 7, true).setProgressType(IProgressType.CUBED))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 50.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 15.0F, 3, 7, true).setProgressType(IProgressType.CUBED))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 30.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 50.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 45.0F, 3, 7, true).setProgressType(IProgressType.CUBED))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 60.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationWaveTimed.AnimationWaveTimedSin(RotationAxis.Y, 7, 12, 1.5F, 0.3F, 0.15F, head_base).setAllowSpeed(false))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -45.0F, 0, 5, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -60.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_lower, RotationAxis.X, -25.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -60.0F, 0, 5, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -30.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_upper, RotationAxis.X, -70.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.build();
		SEVER_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationVisible(gland[0], 0, EntityDekuFire.GLAND_DURATION, false, true))
				.add(new AnimationVisible(gland_copy[0], 0, EntityDekuFire.GLAND_DURATION, true, false))
				.add(new AnimationTargetAngle(gland_copy[0], RotationAxis.X, -270.0F, 0, 13, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(gland_copy[0], RotationAxis.X, -405.0F, 12, 16, true).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.X, -0.3F, 0, 5))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Y, -0.15F, 0, 3).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Z, -0.35F, 0, 5).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.X, -0.6F, 5, 12))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Y, 1.25F, 5, 12).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Z, -0.6F, 5, 12).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.X, -0.8F, 13, 16))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Y, 1.0F, 12, 15).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Z, -0.55F, 13, 16).setAllowInversion(false))
				.add(new AnimationTargetOffset(gland_copy[0], RotationAxis.Y, 1.25F, 15, 18).setAllowInversion(false))
				.build();
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		gland_copy[0].render(f5);
	}

	// bridge method
	@Override
	protected void applyAnimations(EntityDekuBase entity, float par2, float par3, float partialTick) {
		super.applyAnimations(entity, par2, par3, partialTick);
		applyAnimations((EntityDekuFire) entity, par2, par3, partialTick);
	}

	protected void applyAnimations(EntityDekuFire entity, float par2, float par3, float partialTick) {
		gland[0].isHidden = !entity.hasGland();
		if (gland[0].isHidden) {
			return; // nothing else to do here
		}
		if (entity.isConfused()) {
			gland[0].setRotationPoint(0.0F, gland[0].rotationPointY, 1.0F);
			gland[0].rotateAngleY = (float) Math.toRadians(180.0F);
			gland_copy[0].setRotationPoint(0.0F, gland_copy[0].rotationPointY, 1.0F);
			gland_copy[0].rotateAngleY = (float) Math.toRadians(180.0F);
		}
		int frame = Math.abs(entity.gland_timer);
		if (frame > 0) {
			frame = EntityDekuFire.GLAND_DURATION - frame;
			IAnimation.Helper.applyAnimation(SEVER_ANIMATION, frame, partialTick, 1.0F, 1.0F, 0.0F, entity.gland_timer < 0);
		}
	}

	@Override
	protected ImmutableList<IAnimation> getAnimation(EntityAction action) {
		return (action == EntityDekuFire.ACTION_SPIT ? SPIT_ANIMATION : super.getAnimation(action));
	}
}

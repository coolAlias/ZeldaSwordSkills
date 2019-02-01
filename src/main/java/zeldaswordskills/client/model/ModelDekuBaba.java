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

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import zeldaswordskills.api.client.animation.*;
import zeldaswordskills.api.entity.ai.EntityAction;
import zeldaswordskills.entity.mobs.EntityDekuBaba;
import zeldaswordskills.entity.mobs.EntityDekuBase;

import com.google.common.collect.ImmutableList;

public class ModelDekuBaba extends ModelDekuBase
{
	protected final ImmutableList<IAnimation>
	/** Basic attack animation: lunge and bite */
	ATTACK_ANIMATION,
	/** List of individual animations to be applied every tick */
	LOOPING_ANIMATIONS,
	/** Ready stance; uses ACTION_SPROUT timer */
	READY_ANIMATION,
	/** Sprouting / retreating into ground animation */
	SPROUT_ANIMATION;

	public ModelDekuBaba() {
		super();
		ATTACK_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -35.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, 45.0F, 3, 7, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -15.0F, 9, 12, true))
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -15.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 45.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, 3, 7, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 50.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 15.0F, 3, 7, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 30.0F, 9, 12, true))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 30.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 50.0F, 0, 3, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 15.0F, 3, 7, true).setProgressType(IProgressType.SQUARED))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 60.0F, 9, 12, true))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 60.0F, 12, 15, true).setAllowOffset(false))
				.add(new AnimationVisible(tongue_base, 9, 13, false, true))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -45.0F, 0, 5, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -90.0F, 7, 9, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_lower, RotationAxis.X, -60.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_lower, RotationAxis.X, 0.0F, 7, 9, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_lower, RotationAxis.X, -25.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -55.0F, 0, 5, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, 0.0F, 7, 9, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_base_upper, RotationAxis.X, -30.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.add(new AnimationTargetAngle(mouth_upper, RotationAxis.X, -90.0F, 7, 9, true).setAllowInversion(false))
				.add(new AnimationTargetAngle(mouth_upper, RotationAxis.X, -70.0F, 12, 15, true).setAllowInversion(false).setAllowOffset(false))
				.build();
		LOOPING_ANIMATIONS = new ImmutableList.Builder<IAnimation>()
				// Circular swaying animation
				.add(new AnimationCircle(RotationAxis.X, RotationAxis.Z, 0.15F, 0.25F, 0.25F, false, stem1, stem2, stem3, head_base))
				// Tongue animations, offset slightly in speed for more natural effect
				.add(new AnimationCircle(RotationAxis.X, RotationAxis.Z, 0.15F, 0.1F, 0.15F, true, tongue_base, tongue_mid, tongue_tip))
				.add(new AnimationWave.AnimationWaveSin(RotationAxis.X, 0.1F, 0.15F, 0.15F, tongue_base, tongue_mid, tongue_tip))
				.build();
		READY_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetAngle(stem1, RotationAxis.X, -15.0F, 5, 10, true))
				.add(new AnimationTargetAngle(stem2, RotationAxis.X, 20.0F, 5, 10, true))
				.add(new AnimationTargetAngle(stem3, RotationAxis.X, 30.0F, 5, 10, true))
				.add(new AnimationTargetAngle(head_base, RotationAxis.X, 60.0F, 5, 10, true))
				.build();
		SPROUT_ANIMATION = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationTargetOffset(stem1, RotationAxis.Y, 2.0F, 0, 0).setAllowInversion(false))
				.add(new AnimationTargetOffset(stem1, RotationAxis.Y, 0.0F, 1, EntityDekuBaba.ACTION_SPROUT.duration).setAllowInversion(false))
				.add(new AnimationVisible(stem1, 0, 1, false, true))
				.build();
	}

	// bridge method
	@Override
	protected void applyAnimations(EntityDekuBase entity, float par2, float par3, float partialTick) {
		applyAnimations((EntityDekuBaba) entity, par2, par3, partialTick);
	}

	protected void applyAnimations(EntityDekuBaba entity, float par2, float par3, float partialTick) {
		int frame = entity.getActionTime(EntityDekuBaba.ACTION_SPROUT.id);
		// partial tick needs to be applied in reverse when retracting
		float modifier = (entity.getCurrentTarget() == null ? -partialTick : partialTick);
		float speed = entity.getActionSpeed(EntityDekuBaba.ACTION_SPROUT.id);
		IAnimation.Helper.applyAnimation(SPROUT_ANIMATION, frame, modifier, speed, 1.0F, 0.0F, false);
		IAnimation.Helper.applyAnimation(READY_ANIMATION, frame, modifier, speed, 1.0F, 0.0F, entity.isConfused());
		boolean flag = applyDeathAnimation(entity, par2, par3, partialTick);
		if (entity.isFullyAlert()) {
			speed = (entity.isConfused() ? 0.9F : 1.0F);
			float factor = (entity.isConfused() ? 1.25F : 1.0F);
			float offset = (entity.isConfused() ? 0.15F : 1.0F);
			IAnimation.Helper.applyAnimation(LOOPING_ANIMATIONS, entity.ticksExisted + entity.getTicksExistedOffset(-1), partialTick, speed, factor, offset, false);
			if (!flag) { // only animate actions if not in the throes of death
				animateActions(entity, par2, par3, partialTick);
			}
		}
	}

	private void animateActions(EntityDekuBaba entity, float par2, float par3, float partialTick) {
		Entity target = entity.getCurrentTarget();
		List<EntityAction> actions = entity.getActiveActions();
		for (EntityAction action : actions) {
			int frame = entity.getActionTime(action.id);
			if (frame > 0) {
				float speed = entity.getActionSpeed(action.id);
				float offset = 0.0F;
				if (target != null && (action == EntityDekuBaba.ACTION_ATTACK || action == EntityDekuBaba.ACTION_BOMB)) {
					offset = 0.075F * MathHelper.clamp_float((float)(entity.getEntityBoundingBox().maxY - target.getEntityBoundingBox().maxY), -2F, 1.5F);
				}
				IAnimation.Helper.applyAnimation(getAnimation(action), frame, partialTick, speed, 1.0F, offset, entity.isConfused());
			}
		}
	}

	/**
	 * Return current animation, if any, based on the provided action state
	 */
	protected ImmutableList<IAnimation> getAnimation(EntityAction action) {
		if (action == EntityDekuBaba.ACTION_ATTACK) return ATTACK_ANIMATION;
		if (action == EntityDekuBaba.ACTION_BOMB) return ATTACK_ANIMATION;
		if (action == EntityDekuBaba.ACTION_PRONE) return PRONE_ANIMATION;
		return null;
	}
}

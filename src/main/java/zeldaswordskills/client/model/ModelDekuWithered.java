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

import zeldaswordskills.api.client.animation.AnimationCircle;
import zeldaswordskills.api.client.animation.IAnimation;
import zeldaswordskills.api.client.animation.RotationAxis;
import zeldaswordskills.api.client.model.SmartModelRenderer;
import zeldaswordskills.entity.mobs.EntityDekuBase;

import com.google.common.collect.ImmutableList;

/**
 * 
 * Withered Baba model is always 'prone' and has additional model parts for the thorns
 *
 */
public class ModelDekuWithered extends ModelDekuBase
{
	protected SmartModelRenderer thorn11, thorn12, thorn13, thorn14, thorn15, thorn16, thorn17, thorn18;
	protected SmartModelRenderer thorn21, thorn22, thorn23, thorn24, thorn25;
	protected SmartModelRenderer thorn31, thorn32, thorn33, thorn34;
	protected final ImmutableList<IAnimation> PRONE_OFFSET;

	public ModelDekuWithered() {
		super();
		this.thorn11 = new SmartModelRenderer(this, 48, 1);
		this.thorn11.setRotationPoint(0.7F, -3.5F, 0.0F);
		this.thorn11.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn11.setInitialPose(0.6981317007977318F, 0.0F, 0.8726646259971648F);
		this.thorn12 = new SmartModelRenderer(this, 48, 1);
		this.thorn12.setRotationPoint(-0.7F, -4.0F, 0.0F);
		this.thorn12.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn12.setInitialPose(0.0F, 0.7853981633974483F, 0.6108652381980153F);
		this.thorn13 = new SmartModelRenderer(this, 48, 1);
		this.thorn13.setRotationPoint(0.0F, -2.0F, 0.7F);
		this.thorn13.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn13.setInitialPose(0.8726646259971648F, 0.6108652381980153F, 0.0F);
		this.thorn14 = new SmartModelRenderer(this, 48, 1);
		this.thorn14.setRotationPoint(0.7F, -9.2F, 0.0F);
		this.thorn14.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn14.setInitialPose(0.7853981633974483F, 0.8726646259971648F, 0.0F);
		this.thorn15 = new SmartModelRenderer(this, 48, 1);
		this.thorn15.setRotationPoint(-0.7F, -8.7F, 0.0F);
		this.thorn15.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn15.setInitialPose(0.2617993877991494F, 0.6108652381980153F, 0.7853981633974483F);
		this.thorn16 = new SmartModelRenderer(this, 48, 1);
		this.thorn16.setRotationPoint(-0.1F, -3.0F, -0.7F);
		this.thorn16.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn16.setInitialPose(0.7853981633974483F, 0.6108652381980153F, 0.6981317007977318F);
		this.thorn17 = new SmartModelRenderer(this, 48, 1);
		this.thorn17.setRotationPoint(-0.1F, -8.0F, -0.7F);
		this.thorn17.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn17.setInitialPose(0.7853981633974483F, 0.6108652381980153F, -0.6981317007977318F);
		this.thorn18 = new SmartModelRenderer(this, 48, 1);
		this.thorn18.setRotationPoint(-0.1F, -6.7F, 0.7F);
		this.thorn18.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn18.setInitialPose(0.7853981633974483F, 0.6108652381980153F, -0.6981317007977318F);
		this.stem1.addChild(this.thorn11);
		this.stem1.addChild(this.thorn12);
		this.stem1.addChild(this.thorn13);
		this.stem1.addChild(this.thorn14);
		this.stem1.addChild(this.thorn15);
		this.stem1.addChild(this.thorn16);
		this.stem1.addChild(this.thorn17);
		this.stem1.addChild(this.thorn18);
		this.thorn21 = new SmartModelRenderer(this, 48, 1);
		this.thorn21.setRotationPoint(-0.2F, -1.0F, 0.7F);
		this.thorn21.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn21.setInitialPose(0.7853981633974483F, 0.6108652381980153F, -0.5235987755982988F);
		this.thorn22 = new SmartModelRenderer(this, 48, 1);
		this.thorn22.setRotationPoint(0.1F, -4.8F, 0.7F);
		this.thorn22.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn22.setInitialPose(0.7853981633974483F, 0.6108652381980153F, -1.0471975511965976F);
		this.thorn23 = new SmartModelRenderer(this, 48, 1);
		this.thorn23.setRotationPoint(0.7F, -2.3F, 0.0F);
		this.thorn23.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn23.setInitialPose(0.0F, 0.7853981633974483F, 0.6108652381980153F);
		this.thorn24 = new SmartModelRenderer(this, 48, 1);
		this.thorn24.setRotationPoint(-0.7F, -2.6F, 0.0F);
		this.thorn24.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn24.setInitialPose(0.0F, 0.7853981633974483F, 0.6108652381980153F);
		this.thorn25 = new SmartModelRenderer(this, 48, 1);
		this.thorn25.setRotationPoint(0.2F, -3.2F, -0.7F);
		this.thorn25.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn25.setInitialPose(0.7853981633974483F, 0.6108652381980153F, 0.2617993877991494F);
		this.stem2.addChild(this.thorn21);
		this.stem2.addChild(this.thorn22);
		this.stem2.addChild(this.thorn23);
		this.stem2.addChild(this.thorn24);
		this.stem2.addChild(this.thorn25);
		this.thorn31 = new SmartModelRenderer(this, 48, 1);
		this.thorn31.setRotationPoint(0.0F, -2.7F, 0.7F);
		this.thorn31.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn31.setInitialPose(0.8726646259971648F, 0.6108652381980153F, 0.0F);
		this.thorn32 = new SmartModelRenderer(this, 48, 1);
		this.thorn32.setRotationPoint(0.1F, -1.7F, -0.7F);
		this.thorn32.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn32.setInitialPose(0.7853981633974483F, 0.6108652381980153F, 0.6981317007977318F);
		this.thorn33 = new SmartModelRenderer(this, 48, 1);
		this.thorn33.setRotationPoint(-0.7F, -1.3F, -0.1F);
		this.thorn33.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn33.setInitialPose(0.7853981633974483F, 0.8726646259971648F, 0.0F);
		this.thorn34 = new SmartModelRenderer(this, 48, 1);
		this.thorn34.setRotationPoint(0.7F, -1.1F, -0.1F);
		this.thorn34.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, 0.0F);
		this.thorn34.setInitialPose(0.8726646259971648F, 0.9599310885968813F, 0.0F);
		this.stem3.addChild(this.thorn31);
		this.stem3.addChild(this.thorn32);
		this.stem3.addChild(this.thorn33);
		this.stem3.addChild(this.thorn34);
		PRONE_OFFSET = new ImmutableList.Builder<IAnimation>()
				.add(new AnimationCircle(RotationAxis.X, RotationAxis.Z, 1.0F, 0.25F, 0.0F, false, stem1))
				.build();
	}

	@Override
	public void applyAnimations(EntityDekuBase entity, float par2, float par3, float partialTick) {
		IAnimation.Helper.applyAnimation(PRONE_ANIMATION, 10, partialTick, 1.0F, 1.0F, 0.0F, false);
		float angle = (float) Math.toRadians(entity.getTicksExistedOffset(-1));
		IAnimation.Helper.applyAnimation(PRONE_OFFSET, 1, 1.0F, 1.0F, 1.0F, angle, false);
		applyDeathAnimation(entity, par2, par3, partialTick);
	}
}

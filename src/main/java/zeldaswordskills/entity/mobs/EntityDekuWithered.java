/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.entity.mobs;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityDekuWithered extends EntityDekuBase
{
	public EntityDekuWithered(World world) {
		super(world);
		this.experienceValue = 2;
		this.setNoAI(true);
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (isSourceFatal(source)) {
			return super.attackEntityFrom(source, getMaxHealth());
		}
		return super.attackEntityFrom(source, amount);
	}

	@Override
	protected boolean isSourceFatal(DamageSource source) {
		return isSlashing(source); // already covers boomerangs and spin attacks
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.setRotation(this.getTicksExistedOffset(-1), this.rotationPitch);
		// don't call super - don't want any movement or any such thing
		double widthSq = (this.width * 2.0F * this.width * 2.0F);
		float mod = 1.0F * (float) worldObj.getDifficulty().getDifficultyId();
		List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox().expand(3F + mod, 0.0F, 3F + mod));
		for (EntityLivingBase entity : entities) {
			if (entity instanceof EntityDekuBase || !entity.isEntityAlive() || !canEntityBeSeen(entity)) {
				continue;
			}
			double d0 = this.getDistanceSq(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);
			double d1 = widthSq + entity.width + mod;
			if (d0 <= d1) {
				this.attackEntityAsMob(entity);
			}
		}
	}
}

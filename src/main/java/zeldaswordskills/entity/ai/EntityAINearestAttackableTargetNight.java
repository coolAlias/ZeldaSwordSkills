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

package zeldaswordskills.entity.ai;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;

/**
 * 
 * Identical to {@link EntityAINearestAttackableTarget} except that aggressiveness
 * may be limited by light level to replicate behavior such as spiders during the day.
 *
 */
public class EntityAINearestAttackableTargetNight<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T>
{
	/** Light level below which task owner will search for targets */
	private final float minLightLevel;

	public EntityAINearestAttackableTargetNight(EntityCreature taskOwner, Class<T> targetClass, int targetChance, boolean shouldCheckSight, float minLightLevel) {
		this(taskOwner, targetClass, targetChance, shouldCheckSight, false, minLightLevel);
	}

	public EntityAINearestAttackableTargetNight(EntityCreature taskOwner, Class<T> targetClass, int targetChance, boolean shouldCheckSight, boolean nearbyOnly, float minLightLevel) {
		this(taskOwner, targetClass, targetChance, shouldCheckSight, nearbyOnly, null, minLightLevel);
	}

	public EntityAINearestAttackableTargetNight(EntityCreature taskOwner, Class<T> targetClass, int targetChance, boolean shouldCheckSight, boolean nearbyOnly, Predicate<T> targetEntitySelector, float minLightLevel) {
		super(taskOwner, targetClass, targetChance, shouldCheckSight, nearbyOnly, targetEntitySelector);
		this.minLightLevel = minLightLevel;
	}

	@Override
	public boolean shouldExecute() {
		return (taskOwner.getBrightness(1.0F) < minLightLevel && super.shouldExecute());
	}
}

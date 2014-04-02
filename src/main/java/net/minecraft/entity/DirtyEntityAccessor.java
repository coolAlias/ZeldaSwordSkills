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

package net.minecraft.entity;

import net.minecraft.util.DamageSource;

public class DirtyEntityAccessor {

	/** Damages the target for the amount of damage using the vanilla method; posts LivingHurtEvent */
	public static void damageEntity(EntityLivingBase target, DamageSource source, float amount) {
		target.damageEntity(source, amount);
	}

	/**
	 * Returns the amount of damage the entity will receive after armor and potions are taken into account
	 */
	public static float getModifiedDamage(EntityLivingBase entity, DamageSource source, float amount) {
		amount = entity.applyArmorCalculations(source, amount);
		amount = entity.applyPotionDamageCalculations(source, amount);
		return Math.max(amount - entity.getAbsorptionAmount(), 0.0F);
	}
}

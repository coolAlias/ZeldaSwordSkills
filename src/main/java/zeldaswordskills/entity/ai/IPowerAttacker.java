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

import net.minecraft.entity.EntityLivingBase;

/**
 * 
 * Interface for entities using {@link EntityAIPowerAttack}
 *
 */
public interface IPowerAttacker {

	/**
	 * Called when the entity first begins charging up the power attack
	 */
	void beginPowerAttack();

	/**
	 * Called when the power attack AI resets, whether after successful execution or prematurely
	 */
	void cancelPowerAttack();

	/**
	 * Number of ticks required to charge up the power attack; i.e. the time that
	 * must pass since beginPowerAttack until performPowerAttack
	 */
	int getChargeTime();

	/**
	 * Called when the target has been struck and damage should be dealt,
	 * similar to {@link EntityLivingBase#attackEntityAsMob}
	 * @target	Guaranteed to be within reach and visible, but no other position checking is done
	 */
	void performPowerAttack(EntityLivingBase target);

	/**
	 * Called when target was out of range at the time the power attack was performed
	 */
	void onAttackMissed();

}

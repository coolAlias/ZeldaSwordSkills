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

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.village.Village;

/**
 * 
 * A generalized AI for any EntityCreature to defend a village
 *
 */
public class GenericAIDefendVillage extends EntityAITarget
{
	/** The owner of this AI will defend the village from aggressors */
	private EntityCreature defender;
	/** The entity attacking the village will become the defender's attack target. */
	private EntityLivingBase aggressor;

	public <T extends EntityCreature & IVillageDefender> GenericAIDefendVillage(T defender) {
		super(defender, false, true);
		this.defender = defender;
		setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		Village village = ((IVillageDefender) defender).getVillageToDefend();
		if (village == null) {
			return false;
		} else {
			aggressor = village.findNearestVillageAggressor(defender);

			if (!isSuitableTarget(aggressor, false)) {
				if (taskOwner.getRNG().nextInt(20) == 0) {
					aggressor = village.getNearestTargetPlayer(defender);
					return isSuitableTarget(aggressor, false);
				} else {
					return false;
				}
			} else {
				return true;
			}
		}
	}

	@Override
	public void startExecuting() {
		defender.setAttackTarget(aggressor);
		super.startExecuting();
	}
}

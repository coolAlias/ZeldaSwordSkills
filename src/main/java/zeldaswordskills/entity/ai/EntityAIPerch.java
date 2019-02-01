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
import net.minecraft.entity.ai.EntityAIBase;

/**
 * 
 * Entity stays in place so long as it remains {@link IWallPerch#isPerched() perched}.
 * Perched status will be set to false if {@link IWallPerch#canPerch()} no longer returns true.
 * Uses mutexBit 1 to prevent most other movement- and attack-related AI. 
 *
 */
public class EntityAIPerch extends EntityAIBase
{
	protected final EntityCreature entity;
	protected IWallPerch entityPerch;

	public <T extends EntityCreature & IWallPerch> EntityAIPerch(T entity) {
		this.entity = entity;
		this.entityPerch = (IWallPerch) entity;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return entityPerch.isPerched();
	}

	@Override
	public boolean continueExecuting() {
		if (!entityPerch.canPerch()) {
			entityPerch.setPerched(false);
		}
		return entityPerch.isPerched();
	}
}

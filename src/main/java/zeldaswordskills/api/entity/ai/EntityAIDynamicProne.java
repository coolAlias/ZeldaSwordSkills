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

package zeldaswordskills.api.entity.ai;

import net.minecraft.entity.EntityCreature;

/**
 * 
 * AI that causes the entity to become 'prone', preventing all other AI with
 * matching mutex bits that also have an inferior priority level.
 * 
 * Relies on the {@link IEntityDynamic} entity for determining when, exactly,
 * it is allowed to execute.
 *
 */
public class EntityAIDynamicProne<T extends EntityCreature & IEntityDynamic> extends EntityAIDynamic<T> {

	public EntityAIDynamicProne(T entity, EntityAction action, int mutex) {
		super(entity, action, 0, mutex);
	}

	@Override
	public boolean isInterruptible() {
		return false;
	}
}

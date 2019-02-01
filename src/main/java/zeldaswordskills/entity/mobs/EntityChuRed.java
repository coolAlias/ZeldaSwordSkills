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

package zeldaswordskills.entity.mobs;

import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

/**
 * 
 * Red Chus are the weakest of the Chu types, but are highly resistant to fire.
 *
 */
public class EntityChuRed extends EntityChu
{
	public EntityChuRed(World world) {
		super(world);
	}

	@Override
	protected EntityChuRed createInstance() {
		return new EntityChuRed(this.worldObj);
	}

	@Override
	public ChuType getType() {
		return ChuType.RED;
	}

	@Override
	protected void applyTypeTraits() {
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 75);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_WATER, Integer.MAX_VALUE, 100);
	}
}

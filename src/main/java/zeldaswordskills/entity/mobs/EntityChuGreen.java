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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

/**
 * 
 * Green Chus are slightly stronger than the Red Chu; their attacks cause weakness.
 *
 */
public class EntityChuGreen extends EntityChu
{
	public EntityChuGreen(World world) {
		super(world);
	}

	@Override
	protected EntityChuGreen createInstance() {
		return new EntityChuGreen(this.worldObj);
	}

	@Override
	public ChuType getType() {
		return ChuType.GREEN;
	}

	@Override
	protected void applySecondaryEffects(EntityLivingBase target) {
		if (this.rand.nextFloat() < (0.25F * this.getSlimeSize())) {
			ZSSEntityInfo.get(target).applyBuff(Buff.ATTACK_DOWN, 200, 50);
		}
	}
}

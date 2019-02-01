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

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import zeldaswordskills.api.damage.DamageUtils.DamageSourceIce;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

public class EntityKeeseIce extends EntityKeese
{
	public EntityKeeseIce(World world) {
		super(world);
		this.experienceValue = 3;
	}

	@Override
	protected EntityKeeseIce createInstance() {
		return new EntityKeeseIce(this.worldObj);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(12.0F);
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_COLD, Integer.MAX_VALUE, 100);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_FIRE, Integer.MAX_VALUE, 100);
	}

	@Override
	protected DamageSource getDamageSource() {
		return new DamageSourceIce("mob", this, 100, 0);
	}
}

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

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.item.ZSSItems;

public class EntityWizzrobeFire extends EntityWizzrobe
{
	public EntityWizzrobeFire(World world) {
		super(world);
	}

	@Override
	public MagicType getMagicType() {
		return MagicType.FIRE;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_FIRE, Integer.MAX_VALUE, 50);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_COLD, Integer.MAX_VALUE, 100);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_WATER, Integer.MAX_VALUE, 100);
	}

	@Override
	protected ItemStack getRareDrop(int modifier) {
		ItemStack stack = super.getRareDrop(modifier);
		if (stack == null) {
			stack = new ItemStack(ZSSItems.arrowFire);
		}
		return stack;
	}
}

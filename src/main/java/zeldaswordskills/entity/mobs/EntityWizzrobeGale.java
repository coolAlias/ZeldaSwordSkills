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

import net.minecraft.world.World;
import zeldaswordskills.api.entity.MagicType;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

public class EntityWizzrobeGale extends EntityWizzrobe
{
	public EntityWizzrobeGale(World world) {
		super(world);
	}

	@Override
	public MagicType getMagicType() {
		return MagicType.WIND;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		ZSSEntityInfo.get(this).applyBuff(Buff.RESIST_WIND, Integer.MAX_VALUE, 50);
		ZSSEntityInfo.get(this).applyBuff(Buff.WEAKNESS_QUAKE, Integer.MAX_VALUE, 100);
	}
}

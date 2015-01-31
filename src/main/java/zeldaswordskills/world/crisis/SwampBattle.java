/**
    Copyright (C) <2015> <coolAlias>

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

package zeldaswordskills.world.crisis;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.mobs.EntityWizzrobe;

public class SwampBattle extends BossBattle {

	public SwampBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	protected int getNumBosses() {
		return 1;
	}

	@Override
	protected void generateBossMobs(World world, int number) {
		Entity mob = core.getBossType().getNewMob(world);
		if (mob instanceof EntityWizzrobe) { // already a boss-level mob, doesn't need boosting
			((EntityWizzrobe) mob).setTeleBounds(AxisAlignedBB.getBoundingBox(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ));
			spawnMobInCorner(world, mob, world.rand.nextInt(4), false, false);
		}
	}
}

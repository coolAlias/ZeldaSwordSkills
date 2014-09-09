/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.StructureGenUtils;

public class ForestBattle extends BossBattle {

	public ForestBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	public void beginCrisis(World world) {
		super.beginCrisis(world);
		scheduleUpdateTick(300 + world.rand.nextInt(300));
		StructureGenUtils.fillWithoutReplace(world, box.minX + 1, box.minX + 2, box.minY + 1, box.minY + 4, box.minZ + 1, box.maxZ, Blocks.web, 0, 3);
		StructureGenUtils.fillWithoutReplace(world, box.maxX - 1, box.maxX, box.minY + 1, box.minY + 4, box.minZ + 1, box.maxZ, Blocks.web, 0, 3);
		StructureGenUtils.fillWithoutReplace(world, box.minX + 2, box.maxX - 1, box.minY + 1, box.minY + 4, box.minZ + 1, box.minZ + 2, Blocks.web, 0, 3);
		StructureGenUtils.fillWithoutReplace(world, box.minX + 2, box.maxX - 1, box.minY + 1, box.minY + 4, box.maxZ - 1, box.maxZ, Blocks.web, 0, 3);
	}

	@Override
	protected void onUpdateTick(World world) {
		boolean flag = false;
		if (world.rand.nextInt(4) < 3) {
			flag = true;
			for (int i = 0; i < (difficulty + 2); ++i) {
				setRandomBlockTo(world, Blocks.web, 0, Sounds.WEB_SPLAT);
			}
		}
		if (!flag || world.rand.nextInt(2) == 0) {
			destroyRandomPillar(world, difficulty == 3);
		}
		scheduleUpdateTick(100 + world.rand.nextInt(500));
	}
}

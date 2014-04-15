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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.util.StructureGenUtils;

public class FireBattle extends BossBattle {

	public FireBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	public void beginCrisis(World world) {
		super.beginCrisis(world);
		scheduleUpdateTick(50);
	}

	@Override
	protected void endCrisis(World world) {
		StructureGenUtils.replaceMaterialWith(world, box.minX, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, Material.lava, 0, 0);
		super.endCrisis(world);
	}

	@Override
	protected void onUpdateTick(World world) {
		if (difficulty > 1) {
			if (eventTimer % (550 - (difficulty * 50)) == 0) {
				setRandomBlockTo(world, Block.lavaStill, 0, "");
			}
			if (eventTimer % (800 - (difficulty * 50)) == 0) {
				boolean flag = (difficulty == 3);
				spawnMobInCorner(world, new EntitySkeleton(world), world.rand.nextInt(4), flag, flag);
			}
		}
		if (eventTimer % 500 == 0) {
			destroyRandomPillar(world, true);
		}
		scheduleUpdateTick(50);
	}
}

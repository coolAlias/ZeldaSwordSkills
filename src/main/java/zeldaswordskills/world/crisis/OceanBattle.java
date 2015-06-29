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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.StructureGenUtils;

public class OceanBattle extends BossBattle {

	public OceanBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	public void beginCrisis(World world) {
		super.beginCrisis(world);
		eventTimer = 6000 - (600 * difficulty);
		scheduleUpdateTick(-(1200 - eventTimer)); // one minute of falling sand
	}

	@Override
	protected void endCrisis(World world) {
		super.endCrisis(world);
		StructureGenUtils.replaceMaterialWith(world, box.minX + 1, box.maxX, box.minY + 1, box.maxY, box.minZ + 1, box.maxZ, Material.sand, Block.waterStill.blockID, 0);
	}

	@Override
	protected void onUpdateTick(World world) {
		world.playSoundEffect(core.xCoord + 0.5D, box.getCenterY(), core.zCoord + 0.5D, Sounds.ROCK_FALL, 1.0F, 1.0F);
		StructureGenUtils.fillWithoutReplace(world, box.minX + 1, box.maxX, box.maxY-1, box.maxY, box.minZ + 1, box.maxZ, Block.sand.blockID, 0, 3);
		scheduleUpdateTick((100 - (difficulty * 20)) + world.rand.nextInt(60));
	}
}

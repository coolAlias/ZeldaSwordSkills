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

package zeldaswordskills.world.gen.feature;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import zeldaswordskills.block.ZSSBlocks;

public class WorldGenBombFlowers extends WorldGenerator
{
	public WorldGenBombFlowers() {
		super(false);
	}

	/**
	 * Attempts to generate several bomb flowers at the given chunk coordinates
	 * @param chunkX Chunk coordinate of the chunk's lowest x-coordinate
	 * @param chunkZ Chunk coordinate of the chunk's lowest z-coordinate
	 */
	public void generate(World world, Random rand, int chunkX, int chunkZ) {
		int i = (chunkX << 4) + 8;
		int k = (chunkZ << 4) + 8;
		for (int n = 0; n < 6; ++n) {
			int j = world.getHeight(new BlockPos(i, 64, k)).getY();
			generateAt(world, rand, new BlockPos(i, j, k)); // surface
			j = rand.nextInt(48) + rand.nextInt(48);
			generateAt(world, rand, new BlockPos(i, j, k)); // subterranean
		}
	}

	/**
	 * Attempts to generate several bomb flowers around the given coordinates
	 */
	private void generateAt(World world, Random rand, BlockPos pos) {
		int n = 6;
		for (int l = 0; l < 64 && n > 0; ++l) {
			BlockPos pos1 = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
			if (generate(world, rand, pos1)) {
				--n;
			}
		}
	}

	/**
	 * Attempts to generate a single bomb flower at the given position
	 * @return true if a block was placed
	 */
	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		// Don't allow placement on cobblestone: probably a village blacksmith
		if (world.isAirBlock(pos) && world.getBlockState(pos.down()).getBlock() != Blocks.cobblestone && ZSSBlocks.bombFlower.canPlaceBlockAt(world, pos)) {
			world.setBlockState(pos, ZSSBlocks.bombFlower.getStateFromMeta(rand.nextInt(8)), 2);
			return true;
		}
		return false;
	}
}

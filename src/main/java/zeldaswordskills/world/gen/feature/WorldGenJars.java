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

package zeldaswordskills.world.gen.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.lib.Config;

public class WorldGenJars extends WorldGenerator {

	public WorldGenJars() {}

	public WorldGenJars(boolean doNotify) {
		super(doNotify);
	}
	
	/**
	 * Generates n jars in a cluster around x/y/z
	 */
	public void generate2(World world, Random rand, int x, int y, int z, int n, boolean isUnderground) {
		for (int l = 0; l < 64 && n > 0; ++l) {
			int i = x + rand.nextInt(4) - rand.nextInt(4);
			int j = y + rand.nextInt(4) - rand.nextInt(4);
			int k = z + rand.nextInt(4) - rand.nextInt(4);

			if (canPlaceBlockAt(world, i, j, k, isUnderground) && (!world.provider.hasNoSky || j < 127) && Block.blocksList[ZSSBlocks.ceramicJar.blockID].canBlockStay(world, i, j, k)) {
				world.setBlock(i, j, k, ZSSBlocks.ceramicJar.blockID, 0, 2);
				--n;
			}
		}
	}
	
	private boolean canPlaceBlockAt(World world, int i, int j, int k, boolean isUnderground) {
		return world.isAirBlock(i, j, k) || (!isUnderground && Config.genJarsInWater() && world.getBlockMaterial(i, j, k) == Material.water && !world.canBlockFreeze(i, j, k, false));
	}
	
	/**
	 * Attempts to generate a single jar cluster
	 * @param jarsPerCluster max number of jars to generate in this cluster
	 */
	public void doJarGen(World world, Random rand, int chunkX, int chunkZ, int jarsPerCluster, boolean isUnderground) {
		int i = chunkX + rand.nextInt(16) + 8;
		int k = chunkZ + rand.nextInt(16) + 8;
		int j = (world.provider.isHellWorld ? rand.nextInt(128) : world.getHeightValue(i, k) + 1);
		int n = jarsPerCluster - rand.nextInt(jarsPerCluster);
		if (Config.genJarsInWater() && !isUnderground) {
			while (j > 0 && world.getBlockMaterial(i, j, k) == Material.water) {
				--j;
			}
		}
		generate2(world, rand, i, j, k, n, isUnderground);
	}
	
	@Override
	public boolean generate(World world, Random rand, int i, int j, int k) {
		generate2(world, rand, i, j, k, (Config.getJarsPerCluster() - rand.nextInt(Config.getJarsPerCluster())), false);
		return true;
	}
}

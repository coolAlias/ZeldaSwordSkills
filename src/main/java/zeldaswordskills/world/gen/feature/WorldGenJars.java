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

package zeldaswordskills.world.gen.feature;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.ref.Config;

/**
 * 
 * Self-contained world generator; needs only to be registered to
 * the MinecraftForge.EVENT_BUS using its INSTANCE
 *
 */
public class WorldGenJars extends WorldGenerator
{
	public static final WorldGenJars INSTANCE = new WorldGenJars();

	private WorldGenJars() {
		super(false);
	}

	/**
	 * Generates n jars in a cluster around x/y/z
	 */
	private void generate2(World world, Random rand, BlockPos pos, int n, boolean isUnderground) {
		for (int l = 0; l < 64 && n > 0; ++l) {
			int i = pos.getX() + rand.nextInt(4) - rand.nextInt(4);
			int j = pos.getY() + rand.nextInt(4) - rand.nextInt(4);
			int k = pos.getZ() + rand.nextInt(4) - rand.nextInt(4);
			BlockPos newPos = new BlockPos(i, j, k);
			if (canPlaceBlockAt(world, newPos, isUnderground) && (!world.provider.getHasNoSky() || j < 127) && ZSSBlocks.ceramicJar.canPlaceBlockAt(world, newPos)) {
				world.setBlockState(newPos, ZSSBlocks.ceramicJar.getDefaultState(), 2);
				--n;
			}
		}
	}

	private boolean canPlaceBlockAt(World world, BlockPos pos, boolean isUnderground) {
		return world.isAirBlock(pos) || (!isUnderground && Config.genJarsInWater() && world.getBlockState(pos).getBlock().getMaterial() == Material.water && !world.canBlockFreeze(pos, false));
	}

	/**
	 * Attempts to generate a single jar cluster
	 * @param jarsPerCluster max number of jars to generate in this cluster
	 */
	private void doJarGen(World world, Random rand, int chunkX, int chunkZ, int jarsPerCluster, boolean isUnderground) {
		int i = chunkX + rand.nextInt(16) + 8;
		int k = chunkZ + rand.nextInt(16) + 8;
		int j = (world.provider.getDimensionId() == -1 ? rand.nextInt(128) : world.getHeight(new BlockPos(i, 64, k)).getY() + 1);
		int n = jarsPerCluster - rand.nextInt(jarsPerCluster);
		if (Config.genJarsInWater() && !isUnderground) {
			while (j > 0 && world.getBlockState(new BlockPos(i, j, k)).getBlock().getMaterial() == Material.water) {
				--j;
			}
		}
		generate2(world, rand, new BlockPos(i, j, k), n, isUnderground);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		generate2(world, rand, pos, (Config.getJarsPerCluster() - rand.nextInt(Config.getJarsPerCluster())), false);
		return true;
	}

	@SubscribeEvent
	public void onPreDecorate(DecorateBiomeEvent.Pre event) {
		try {
			if (event.world.provider.getDimensionId() == -1) {
				for (int n = 0; n < Config.getJarClustersPerChunkNether(); ++n) {
					if (event.rand.nextFloat() < Config.getJarGenChanceNether()) {
						doJarGen(event.world, event.rand, event.pos.getX(), event.pos.getZ(), Config.getJarsPerClusterNether(), true);
					}
				}
			} else if (event.rand.nextFloat() < Config.getJarGenChance() && event.rand.nextInt(4) == 0) {
				doJarGen(event.world, event.rand, event.pos.getX(), event.pos.getZ(), Config.getJarsPerCluster(), false);
			}
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (e.getMessage() != null && e.getMessage().equals("Already decorating!!") ||
					(cause != null && cause.getMessage() != null && cause.getMessage().equals("Already decorating!!")))
			{
				;
			} else {
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void onPostDecorate(DecorateBiomeEvent.Post event) {
		try {
			if (event.world.provider.isSurfaceWorld()) {
				for (int n = 0; n < Config.getJarClustersPerChunkSub(); ++n) {
					if (event.rand.nextFloat() < Config.getJarGenChanceSub()) {
						int i = event.pos.getX() + event.rand.nextInt(16) + 8;
						int j = event.rand.nextInt(48) + event.rand.nextInt(48);
						int k = event.pos.getZ() + event.rand.nextInt(16) + 8;
						if (j < 60) {
							generate2(event.world, event.rand, new BlockPos(i, j, k), Config.getJarsPerClusterSub(), true);
						}
					}
				}
			}
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (e.getMessage() != null && e.getMessage().equals("Already decorating!!") ||
					(cause != null && cause.getMessage() != null && cause.getMessage().equals("Already decorating!!")))
			{
				;
			} else {
				e.printStackTrace();
			}
		}
	}
}

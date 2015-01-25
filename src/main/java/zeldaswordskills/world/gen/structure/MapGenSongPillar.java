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

package zeldaswordskills.world.gen.structure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.ForgeDirection;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.block.IDungeonBlock;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityInscription;
import zeldaswordskills.ref.Config;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.StructureGenUtils;

public class MapGenSongPillar extends ZSSMapGenBase
{
	/** List of biomes in which ruined pillar generation is allowed */
	private static final Set<String> allowedBiomes = new HashSet<String>();

	/** Song inscription to place on top of the pillar for the current generation */
	private AbstractZeldaSong song;

	/** Biome of currently generating chunk */
	private BiomeGenBase biome;

	/** Temporary holding for chunk int coordinates as a Long value */
	private long chunk;

	/** Radius (in chunks) within to search for other pillars */
	private int range;

	public MapGenSongPillar() {
		range = Config.getPillarRange();
		// TODO populate from Config
		if (allowedBiomes.isEmpty()) {
			allowedBiomes.addAll(Arrays.asList(BiomeType.COLD.defaultBiomes));
			allowedBiomes.addAll(Arrays.asList(BiomeType.FOREST.defaultBiomes));
			allowedBiomes.addAll(Arrays.asList(BiomeType.JUNGLE.defaultBiomes));
			allowedBiomes.addAll(Arrays.asList(BiomeType.MOUNTAIN.defaultBiomes));
			allowedBiomes.addAll(Arrays.asList(BiomeType.PLAINS.defaultBiomes));
			allowedBiomes.addAll(Arrays.asList(BiomeType.TAIGA.defaultBiomes));
		}
	}

	@Override
	public void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ) {
		this.worldObj = world;
		loadOrCreateData(worldObj);
		int x = (chunkX << 4) + rand.nextInt(16);
		int z = (chunkZ << 4) + rand.nextInt(16);
		biome = world.getBiomeGenForCoords(x, z);
		boolean flag = (biome == BiomeGenBase.swampland && rand.nextFloat() < 0.35F);
		boolean flag2 = (biome == BiomeGenBase.savanna && rand.nextFloat() < 0.35F);
		song = (flag ? ZeldaSongs.songSoaring : (flag2 ? ZeldaSongs.songSun : null));
		if (song == null && biome != null && biome.biomeName != null) {
			flag = allowedBiomes.contains(biome.biomeName);
		}
		if (flag && generate2(rand, x, z)) {
			onPillarPlaced(chunkX, chunkZ);
		}
		song = null;
	}

	private boolean generate2(Random rand, int x, int z) {
		int y = worldObj.getHeightValue(x, z) - 1;
		if (rand.nextFloat() < (rand.nextFloat() * 0.2F) && canGenerateAt(worldObj, x, y, z)) {
			doGenerate(worldObj, rand, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public String getTagName() {
		return "zssPillars";
	}

	/**
	 * Call after a pillar is placed to store it into the map / world data
	 */
	private void onPillarPlaced(int chunkX, int chunkZ) {
		structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), song);
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("song", (song == null ? "NULL" : song.getUnlocalizedName()));
		addRoomTag(compound, chunkX, chunkZ);
	}

	@Override
	protected void translateNbtIntoMap(NBTTagCompound compound) {
		if (compound.hasKey("chunkX") && compound.hasKey("chunkZ") && compound.hasKey("song")) {
			int i = compound.getInteger("chunkX");
			int j = compound.getInteger("chunkZ");
			AbstractZeldaSong song = ZeldaSongs.getSongByName(compound.getString("song"));
			structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), song);
		} else {
			ZSSMain.logger.warn("Failed to translate Song Pillar NBT compound into structure map");
		}
	}

	/**
	 * Not using this method either
	 */
	@Override
	protected StructureBoundingBox getStructureBBAt(int x, int y, int z) {
		return null;
	}

	/**
	 * Not using this method, since no RoomBase
	 */
	@Override
	public boolean areStructuresWithinRange(RoomBase room, int range) {
		return false;
	}

	/**
	 * Actually generates the pillar
	 */
	private void doGenerate(World world, Random rand, int x, int y, int z) {
		int meta = 0; // 0 - stone, 1 - mossy stone, 2 - cracked stone, 3 - chiseled stone brick
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int k = z - 1; k <= z + 1; ++k) {
				meta = (rand.nextFloat() < 0.2F ? 1 : rand.nextFloat() < 0.2F ? 2 : 0);
				world.setBlock(i, y, k, Blocks.stonebrick, meta, 2);
			}
		}
		world.setBlock(x, y, z + 2, Blocks.stonebrick, (rand.nextFloat() < 0.4F ? 1 : 0), 2);
		world.setBlock(x, y, z - 2, Blocks.stonebrick, (rand.nextFloat() < 0.4F ? 1 : 0), 2);
		world.setBlock(x + 2, y, z, Blocks.stonebrick, (rand.nextFloat() < 0.4F ? 1 : 0), 2);
		world.setBlock(x - 2, y, z, Blocks.stonebrick, (rand.nextFloat() < 0.4F ? 1 : 0), 2);
		if (song == null) {
			if (world.isSideSolid(x + 2, y, z + 2, ForgeDirection.UP) && rand.nextFloat() < 0.35F) {
				StructureGenUtils.setBlockIfReplaceable(world, x + 2, y + 1, z + 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x - 2, y, z + 2, ForgeDirection.UP) && rand.nextFloat() < 0.35F) {
				StructureGenUtils.setBlockIfReplaceable(world, x - 2, y + 1, z + 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x + 2, y , z - 2, ForgeDirection.UP) && rand.nextFloat() < 0.35F) {
				StructureGenUtils.setBlockIfReplaceable(world, x + 2, y + 1, z - 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x - 2, y, z - 2, ForgeDirection.UP) && rand.nextFloat() < 0.35F) {
				StructureGenUtils.setBlockIfReplaceable(world, x - 2, y + 1, z - 2, Blocks.stonebrick, 3);
			}
			world.setBlock(x, y + 1, z, Blocks.stonebrick, 2, 2);
			world.setBlock(x, y + 2, z, Blocks.stonebrick, 1, 2);
			if (rand.nextFloat() < 0.5F) {
				world.setBlock(x, y + 3, z, Blocks.stonebrick, 1, 2);
				world.setBlock(x, y + 4, z, Blocks.stonebrick, 1, 2);
				if (rand.nextFloat() < 0.5F) {
					world.setBlock(x + 1, y + 4, z, Blocks.stone_brick_stairs, 5, 2);
				}
				if (rand.nextFloat() < 0.5F) {
					world.setBlock(x - 1, y + 4, z, Blocks.stone_brick_stairs, 4, 2);
				}
				if (rand.nextFloat() < 0.5F) {
					world.setBlock(x, y + 4, z + 1, Blocks.stone_brick_stairs, 7, 2);
				}
				if (rand.nextFloat() < 0.5F) {
					world.setBlock(x, y + 4, z - 1, Blocks.stone_brick_stairs, 6, 2);
				}
			}
		} else {
			if (world.isSideSolid(x + 2, y, z + 2, ForgeDirection.UP)) {
				StructureGenUtils.setBlockIfReplaceable(world, x + 2, y + 1, z + 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x - 2, y, z + 2, ForgeDirection.UP)) {
				StructureGenUtils.setBlockIfReplaceable(world, x - 2, y + 1, z + 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x + 2, y , z - 2, ForgeDirection.UP)) {
				StructureGenUtils.setBlockIfReplaceable(world, x + 2, y + 1, z - 2, Blocks.stonebrick, 3);
			}
			if (world.isSideSolid(x - 2, y, z - 2, ForgeDirection.UP)) {
				StructureGenUtils.setBlockIfReplaceable(world, x - 2, y + 1, z - 2, Blocks.stonebrick, 3);
			}
			world.setBlock(x, y + 1, z, Blocks.stonebrick, 2, 2);
			world.setBlock(x, y + 2, z, Blocks.stonebrick, 1, 2);
			world.setBlock(x, y + 3, z, Blocks.stonebrick, 1, 2);
			world.setBlock(x, y + 4, z, ZSSBlocks.secretStone, 3, 2); // stonebrick secret stone
			world.setBlock(x + 1, y + 4, z, Blocks.stone_brick_stairs, 5, 2);
			world.setBlock(x - 1, y + 4, z, Blocks.stone_brick_stairs, 4, 2);
			world.setBlock(x, y + 4, z + 1, Blocks.stone_brick_stairs, 7, 2);
			world.setBlock(x, y + 4, z - 1, Blocks.stone_brick_stairs, 6, 2);
			world.setBlock(x, y + 5, z, ZSSBlocks.inscription, 0, 2);
			TileEntity te = world.getTileEntity(x, y + 5, z);
			if (te instanceof TileEntityInscription) {
				((TileEntityInscription) te).setSong(song);
			}
		}
	}

	/**
	 * Returns true if all blocks underneath are solid and the blocks to replace
	 * are comprised of non-solid blocks and / or all the same block
	 */
	private boolean canGenerateAt(World world, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);
		Block block2;
		for (int i = x - 1; i <= x + 1; ++i) {
			for (int j = y - 1; j <= y; ++j) {
				for (int k = z - 1; k <= z + 1; ++k) {
					block2 = world.getBlock(i, j, k);
					if (block2 instanceof IDungeonBlock || world.getBlock(i, j + 4, k) instanceof IDungeonBlock) {
						return false;
					} else if (j < y) {
						if (!block2.getMaterial().isSolid() || block2.getMaterial() == Material.leaves) {
							return false;
						}
					} else {
						if (block2.getMaterial().isSolid()) {
							if (!block.getMaterial().isSolid()) {
								block = block2;
							} else if (block != block2) {
								return false;
							}
						}
					}
				}
			}
		}
		int min = (song == null ? Config.getBrokenPillarMin() : Config.getSongPillarMin());
		int d = findNearestPillar(world, x, z, min / 4);
		if (d < 0) {
			return true;
		}
		float f = 0.75F - ((float)(min - d) / (float) min);
		return (world.rand.nextFloat() < f);
	}

	/**
	 * Returns the distance (in chunks) to the nearest song pillar, or -1 if none were found.
	 * Pillars with a different or null song double the distance (meaning they can be closer).
	 */
	private int findNearestPillar(World world, int x, int z, int min) {
		int d1 = -1;
		double d2 = 0;
		x = (x >> 4);
		z = (z >> 4);
		boolean flag = song != null; // true as long as no other song pillars within radius
		boolean cont = true;
		for (int i = x - range; cont && i <= x + range; ++i) {
			for (int k = z - range; cont && k <= z + range; ++k) {
				chunk = Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, k));
				if (structureMap.containsKey(chunk)) {
					AbstractZeldaSong zs = (AbstractZeldaSong) structureMap.get(chunk);
					d2 = Math.ceil(Math.sqrt(((i - x) * (i - x)) + ((k - z) * (k - z))));
					if (flag && zs != null) {
						flag = false;
					}
					if (zs == null) { // null pillars can generate closer together
						d2 *= 3;
					}
					if (song == null && world.rand.nextFloat() < 0.35F) {
						d2 *= 2;
					}
					if (d2 < d1 || d1 < 0) {
						d1 = (int) d2;
					}
					cont = d1 > min; // stop if no chance of generating
				}
			}
		}
		return (flag && d1 > (min / 2) && world.rand.nextFloat() < 0.2F ? -1 : d1);
	}
}

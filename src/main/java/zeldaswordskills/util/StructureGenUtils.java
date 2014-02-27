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

package zeldaswordskills.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.block.BlockChestLocked;
import zeldaswordskills.lib.Config;
import zeldaswordskills.world.gen.structure.RoomBase;

/**
 * 
 * A collection of methods useful for manipulating StructureBoundingBox during structure
 * generation.
 *
 */
public class StructureGenUtils
{	
	/**
	 * Scans the surface of the chunk and returns the average height for the entire chunk
	 * by sampling the world height value of 25 of the 256 surface blocks
	 * posX and posZ are real world coordinates, not chunk coordinates
	 */
	public static int getAverageSurfaceHeight(World world, int posX, int posZ) {
		int height = world.getHeightValue(posX, posZ);
		int count = 1;
		for (int i = posX + 3; i < posX + 16; i += 3) {
			for (int j = posZ + 3; j < posZ + 16; j += 3) {
				height += world.getHeightValue(i, j);
				++count;
			}
		}
		return height / count;
	}
	
	/**
	 * Returns the distance squared between the centers of two bounding boxes
	 */
	public static double getDistanceSqBetween(StructureBoundingBox box1, StructureBoundingBox box2) {
		int dx = box1.getCenterX() - box2.getCenterX();
		int dy = box1.getCenterY() - box2.getCenterY();
		int dz = box1.getCenterZ() - box2.getCenterZ();
		return (dx * dx + dy * dy + dz * dz);
	}
	
	/**
	 * Returns average distance to ground based on 5 points in bounding box's lowest layer
	 * @param max if any distance exceeds this threshold, this value will be returned
	 */
	public static int getAverageDistanceToGround(World world, StructureBoundingBox box, int max) {
		int i = getDistanceToGround(world, box.getCenterX(), box.minY, box.getCenterZ());
		int total = i;
		if (i > max) { return max; }
		i = getDistanceToGround(world, box.minX, box.minY, box.minZ);
		total += i;
		if (i > max) { return max; }
		i = getDistanceToGround(world, box.minX, box.minY, box.maxZ);
		total += i;
		if (i > max) { return max; }
		i = getDistanceToGround(world, box.maxX, box.minY, box.minZ);
		total += i;
		if (i > max) { return max; }
		i = getDistanceToGround(world, box.maxX, box.minY, box.maxZ);
		total += i;
		if (i > max) { return max; }
		return total / 5;
	}
	
	/**
	 * Returns number of blocks between coordinates given and solid ground, or 0 if solid ground is above
	 */
	public static int getDistanceToGround(World world, int x, int y, int z) {
		int i = 0;
		while (!world.getBlockMaterial(x, y - 1, z).isSolid() && y > 5) {
			--y;
			++i;
		}
		return i;
		/*
		int i = world.getTopSolidOrLiquidBlock(x, z);
		if (i > 0 && y > i) {
			return y - i;
		} else {
			return 0;
		}
		*/
	}
	
	/**
	 * Fills area defined by arguments and within the structure's bounding box with given metadata block,
	 * up to but not including the max boundary
	 */
	public static void fillWithBlocks(World world, StructureBoundingBox box, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int blockId, int meta) {
		fillWithBlocks(world, box, minX, maxX, minY, maxY, minZ, maxZ, blockId, meta, false);
	}

	/**
	 * Fills area defined by arguments with given metadata block, up to but not including the max boundary
	 * @param ignoreBounds if true, will fill in blocks even outside of the structure's bounding box bounds
	 */
	public static void fillWithBlocks(World world, StructureBoundingBox box, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int blockId, int meta, boolean ignoreBounds) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					setBlockAtPosition(world, box, i, j, k, blockId, meta, ignoreBounds);
				}
			}
		}
	}
	
	/**
	 * Fills area defined by arguments with given metadata block, up to but not including the max boundary
	 * and without replacing any currently existing solid blocks
	 * @param flag block notification flag; see setBlock for details
	 */
	public static void fillWithoutReplace(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int blockId, int meta, int flag) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					if (!world.getBlockMaterial(i, j, k).isSolid()) {
						world.setBlock(i, j, k, blockId, meta, flag);
					}
				}
			}
		}
	}
	
	/**
	 * Fills downward from the structure's bottom layer to the ground level, replacing
	 * any non-solid or leaf blocks with the block and meta provided
	 */
	public static void fillDown(World world, StructureBoundingBox box, int blockId, int metadata) {
		for (int i = box.minX; i <= box.maxX; ++i) {
			for (int k = box.minZ; k <= box.maxZ; ++k) {
				for (int j = box.minY - 1; j > 4 && (!world.getBlockMaterial(i, j, k).isSolid() || world.getBlockMaterial(i, j, k) == Material.leaves); --j) {
					world.setBlock(i, j, k, blockId, metadata, 2);
				}
			}
		}
	}
	
	/**
	 * Destroys all blocks within bounds provided, up to but excluding the max bounds
	 * @param blockId the blockId to destroy, or -1 for all blocks in the area
	 * @param drop whether destroyed blocks should drop items
	 */
	public static void destroyBlocksAround(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int blockId, boolean drop) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					if (blockId < 0 || world.getBlockId(i, j, k) == blockId) {
						world.destroyBlock(i, j, k, drop);
					}
				}
			}
		}
	}
	
	public static int getXWithOffset(StructureBoundingBox box, int x, int z) {
		return box.minX + x;
		/*
        switch (coordBaseMode) {
            case 0:
            case 2: return boundingBox.minX + x;
            case 1: return boundingBox.maxX - z;
            case 3: return boundingBox.minX + z;
            default: return x;
        }
		 */
	}

	public static int getYWithOffset(StructureBoundingBox box, int y) {
		//return this.coordBaseMode == -1 ? y : y + this.boundingBox.minY;
		return box.minY + y;
	}

	public static int getZWithOffset(StructureBoundingBox box, int x, int z) {
		return box.minZ + z;
		/*
    	switch (coordBaseMode) {
            case 0: return boundingBox.minZ + z;
            case 1:
            case 3: return boundingBox.minZ + x;
            case 2: return boundingBox.maxZ - z;
            default: return z;
        }
		 */
	}
	
	/**
	 * Returns true if the block at x/y/z is a chest or locked chest
	 */
	public static boolean isBlockChest(World world, int x, int y, int z) {
		int id = world.getBlockId(x, y, z);
		Block block = (id > 0 ? Block.blocksList[id] : null);
		return (block instanceof BlockChest || block instanceof BlockChestLocked);
	}
	
	/**
	 * Replaces all blocks of given material in area with blockID and meta provided
	 */
	public static void replaceMaterialWith(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Material material, int blockId, int meta) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					if (world.getBlockMaterial(i, j, k) == material) {
						world.setBlock(i, j, k, blockId, meta, 3);
					}
				}
			}
		}
	}
	
	/**
	 * Sets the block at a position offset by the amounts x/y/z within the bounding box
	 */
	public static void setBlockAtPosition(World world, StructureBoundingBox box, int x, int y, int z, int blockId, int meta) {
		setBlockAtPosition(world, box, x, y, z, blockId, meta, false);
	}

	/**
	 * Sets the block at a position offset by the amounts x/y/z within the bounding box
	 * @param ignoreBounds if true, will set a block even if it is outside of the structure's bounding box bounds
	 */
	public static void setBlockAtPosition(World world, StructureBoundingBox box, int x, int y, int z, int blockId, int meta, boolean ignoreBounds) {
		int j1 = getXWithOffset(box, x, z);
		int k1 = getYWithOffset(box, y);
		int l1 = getZWithOffset(box, x, z);
		if (ignoreBounds || box.isVecInside(j1, k1, l1)) {
			world.setBlock(j1, k1, l1, blockId, meta, 2);
		}
	}
	
	/**
	 * Adjusts bounding box up to n blocks up or down if there is any of the given material
	 * above or below any of the four corners
	 * @param checkAbove whether to check the blocks above the structure
	 * @param moveUp whether the structure is moving up
	 */
	public static void adjustCornersForMaterial(World world, StructureBoundingBox box, Material material, int n, boolean checkAbove, boolean moveUp) {
		int count = n;
		int i = (moveUp ? 1 : -1);

		while (count > 0 && world.getBlockMaterial(box.maxX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.maxZ) == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockMaterial(box.maxX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.minZ) == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockMaterial(box.minX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.maxZ) == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockMaterial(box.minX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.minZ) == material) {
			--count;
			box.offset(0, i, 0);
		}
	}

	/**
	 * Adjusts a bounding box for air blocks above or below so that as little of the structure
	 * is showing as possible; call before final generation begins
	 */
	public static void adjustForAir(World world, RoomBase room, StructureBoundingBox box) {
		int worldHeight = (room.inNether ? 128 : 160);
		int difficulty = (room.inNether ? Config.getNetherDungeonDifficulty() : Config.getMainDungeonDifficulty());
		// Ocean and difficulty setting one make no adjustments
		if (room.inOcean || difficulty == 1) { return; }
		int topCount = getNumBlocksOfMaterial(world, box, Material.air, 1);
		int bottomCount = getNumBlocksOfMaterial(world, box, Material.air, -1);
		// same number of air blocks on both sides
		if (topCount == bottomCount) { return; }

		// shifting up needs to check below the structure, and vice versa
		boolean shiftUp = bottomCount > topCount;
		int maxShift = box.getYSize();
		int i = (shiftUp ? 1 : -1);

		// Adjust center position first
		while (maxShift > 0 && world.isAirBlock(box.getCenterX(), (shiftUp ? box.minY : box.maxY) - i,
				box.getCenterZ()) && box.maxY < worldHeight && box.minY > 8)
		{
			--maxShift;
			box.offset(0, i, 0);
		}

		// Adjust corner positions
		if (maxShift > 0) {
			adjustCornersForMaterial(world, box, Material.air, maxShift, !shiftUp, shiftUp);
		}

		int newCount = getNumBlocksOfMaterial(world, box, Material.air, shiftUp ? -1 : 1);
		// Dungeon surface no longer showing at all; chance of resurfacing depending on difficulty
		if (newCount == 0) {
			if ((room.inNether && difficulty != 3) || world.rand.nextFloat() < (1.0F - (0.3F * difficulty))) {
				box.offset(0, (shiftUp ? -1 : 1), 0);
			}
		} else if (newCount > (shiftUp ? bottomCount : topCount)) {
			box.offset(0, (shiftUp ? -1 : 1) * (box.getYSize() - maxShift), 0);
		}
	}

	/**
	 * Returns number of blocks of given material either directly above or below the structure
	 * Only checks 5 points: center and 4 corners
	 * @param offY checks layer above or below this many blocks; positive value checks above
	 */
	public static int getNumBlocksOfMaterial(World world, StructureBoundingBox box, Material material, int offY) {
		int count = 0;
		int y = (offY > 0 ? box.maxY + offY : box.minY + offY);
		count += (world.getBlockMaterial(box.getCenterX(), y, box.getCenterZ()) == material ? 1 : 0);
		count += (world.getBlockMaterial(box.maxX, y, box.maxZ) == material ? 1 : 0);
		count += (world.getBlockMaterial(box.maxX, y, box.minZ) == material ? 1 : 0);
		count += (world.getBlockMaterial(box.minX, y, box.maxZ) == material ? 1 : 0);
		count += (world.getBlockMaterial(box.minX, y, box.minZ) == material ? 1 : 0);
		return count;
	}
	
	/**
	 * Returns the total number of blocks of the given material within the defined range
	 * above or below the bounding box
	 * @param y negative values check that many blocks below; positive above
	 */
	public static int getNumBlocksOfMaterialInArea(World world, StructureBoundingBox box, Material material, int y) {
		if (y < 0) {
			return getNumBlocksOfMaterialInArea(world, Material.air, box.minX, box.maxX + 1, box.minY + y - 1, box.minY + y, box.minZ, box.maxZ + 1);
		} else {
			return getNumBlocksOfMaterialInArea(world, Material.air, box.minX, box.maxX + 1, box.maxY + y, box.maxY + y + 1, box.minZ, box.maxZ + 1);
		}
	}
	
	/**
	 * Returns the total number of blocks of the given material within the defined range,
	 * up to but not including the max boundary
	 */
	public static int getNumBlocksOfMaterialInArea(World world, Material material, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
		int count = 0;
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					if (world.getBlockMaterial(i, j, k) == material) {
						++count;
					}
				}
			}
		}
		return count;
	}
}

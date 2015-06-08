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

package zeldaswordskills.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.block.BlockChestLocked;
import zeldaswordskills.ref.Config;
import zeldaswordskills.world.gen.structure.RoomBase;

/**
 * 
 * A collection of methods useful for manipulating StructureBoundingBox during structure
 * generation.
 *
 */
public class StructureGenUtils
{
	/** Used for metadata rotation */
	public static final int[] rotateRight = new int[] {1, 2, 3, 0};

	/**
	 * Scans the surface of the chunk and returns the average height for the entire chunk
	 * by sampling the world height value of 25 of the 256 surface blocks
	 */
	public static int getAverageSurfaceHeight(World world, BlockPos pos) {
		int height = world.getHeight(pos).getY();
		int count = 1;
		for (int i = pos.getX() + 3; i < pos.getX() + 16; i += 3) {
			for (int k = pos.getZ() + 3; k < pos.getZ() + 16; k += 3) {
				height += world.getHeight(new BlockPos(i, 64, k)).getY();
				++count;
			}
		}
		return height / count;
	}

	/**
	 * Returns the distance squared between the centers of two bounding boxes
	 */
	public static double getDistanceSqBetween(StructureBoundingBox box1, StructureBoundingBox box2) {
		Vec3i c1 = box1.getCenter();
		Vec3i c2 = box2.getCenter();
		int dx = c1.getX() - c2.getX();
		int dy = c1.getY() - c2.getY();
		int dz = c1.getZ() - c2.getZ();
		return (dx * dx + dy * dy + dz * dz);
	}

	/**
	 * Returns average distance to ground based on 5 points in bounding box's lowest layer
	 * @param max if any distance exceeds this threshold, this value will be returned
	 */
	public static int getAverageDistanceToGround(World world, StructureBoundingBox box, int max) {
		Vec3i center = box.getCenter();
		int i = getDistanceToGround(world, center.getX(), box.minY, center.getZ());
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
		while (!world.isSideSolid(new BlockPos(x, y - 1, z), EnumFacing.UP) && y > 5) {
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
	public static void fillWithBlocks(World world, StructureBoundingBox box, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, IBlockState state) {
		fillWithBlocks(world, box, minX, maxX, minY, maxY, minZ, maxZ, state, false);
	}

	/**
	 * Fills area defined by arguments with given metadata block, up to but not including the max boundary
	 * @param ignoreBounds if true, will fill in blocks even outside of the structure's bounding box bounds
	 */
	public static void fillWithBlocks(World world, StructureBoundingBox box, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, IBlockState state, boolean ignoreBounds) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					setBlockAtPosition(world, box, i, j, k, state, ignoreBounds);
				}
			}
		}
	}

	/**
	 * Fills area defined by arguments with given metadata block, up to but not including the max boundary
	 * and without replacing any currently existing solid blocks
	 * @param flag block notification flag; see setBlock for details
	 */
	public static void fillWithoutReplace(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, IBlockState state, int flag) {
		BlockPos pos;
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					pos = new BlockPos(i, j, k);
					if (!world.getBlockState(pos).getBlock().getMaterial().isSolid()) {
						world.setBlockState(pos, state, flag);
					}
				}
			}
		}
	}

	/**
	 * Fills downward from the structure's bottom layer to the ground level, replacing
	 * any non-solid or leaf blocks with the block and meta provided
	 */
	public static void fillDown(World world, StructureBoundingBox box, IBlockState state) {
		for (int i = box.minX; i <= box.maxX; ++i) {
			for (int k = box.minZ; k <= box.maxZ; ++k) {
				for (int j = box.minY - 1; j > 4 && canReplace(world.getBlockState(new BlockPos(i, j, k)).getBlock().getMaterial()); --j) {
					world.setBlockState(new BlockPos(i, j, k), state, 2);
				}
			}
		}
	}

	/**
	 * Returns true if material is not solid or is leaves
	 * @param material
	 */
	private static boolean canReplace(Material material) {
		return !material.isSolid() || material == Material.leaves;
	}

	/**
	 * Destroys all blocks within bounds provided, up to but excluding the max bounds
	 * @param block the block to destroy, or null for all blocks in the area
	 * @param drop whether destroyed blocks should drop items
	 */
	public static void destroyBlocksAround(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Block block, boolean drop) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					BlockPos pos = new BlockPos(i, j, k);
					if (block == null || world.getBlockState(pos).getBlock() == block) {
						world.destroyBlock(pos, drop);
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
	 * Returns true if the block at position is a chest or locked chest
	 */
	public static boolean isBlockChest(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		return (block instanceof BlockChest || block instanceof BlockChestLocked);
	}

	/**
	 * Replaces all blocks of given material in area with block and meta provided
	 */
	public static void replaceMaterialWith(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Material material, IBlockState state) {
		for (int i = minX; i < maxX; ++i) {
			for (int j = minY; j < maxY; ++j) {
				for (int k = minZ; k < maxZ; ++k) {
					BlockPos pos = new BlockPos(i, j, k);
					if (world.getBlockState(pos).getBlock().getMaterial() == material) {
						world.setBlockState(pos, state, 3);
					}
				}
			}
		}
	}

	/**
	 * Sets the block at a position offset by the amounts x/y/z within the bounding box
	 */
	public static void setBlockAtPosition(World world, StructureBoundingBox box, int x, int y, int z, IBlockState state) {
		setBlockAtPosition(world, box, x, y, z, state, false);
	}

	/**
	 * Sets the block at a position offset by the amounts x/y/z within the bounding box
	 * @param ignoreBounds if true, will set a block even if it is outside of the structure's bounding box bounds
	 */
	public static void setBlockAtPosition(World world, StructureBoundingBox box, int x, int y, int z, IBlockState state, boolean ignoreBounds) {
		int j1 = getXWithOffset(box, x, z);
		int k1 = getYWithOffset(box, y);
		int l1 = getZWithOffset(box, x, z);
		if (ignoreBounds || box.isVecInside(new Vec3i(j1, k1, l1))) {
			world.setBlockState(new BlockPos(j1, k1, l1), state, 2);
		}
	}

	/**
	 * Sets the block at this position only if the current block is replaceable
	 */
	public static void setBlockIfReplaceable(World world, BlockPos pos, IBlockState state) {
		if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
			world.setBlockState(pos, state, 2);
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
		while (count > 0 && world.getBlockState(new BlockPos(box.maxX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.maxZ)).getBlock().getMaterial() == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockState(new BlockPos(box.maxX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.minZ)).getBlock().getMaterial() == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockState(new BlockPos(box.minX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.maxZ)).getBlock().getMaterial() == material) {
			--count;
			box.offset(0, i, 0);
		}
		while (count > 0 && world.getBlockState(new BlockPos(box.minX, (checkAbove ? box.maxY + 1 : box.minY - 1), box.minZ)).getBlock().getMaterial() == material) {
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
		Vec3i center = box.getCenter();

		// Adjust center position first
		while (maxShift > 0 && world.isAirBlock(new BlockPos(center.getX(), (shiftUp ? box.minY : box.maxY) - i, center.getZ())) && box.maxY < worldHeight && box.minY > 8) {
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
	 * Returns true if the area specified is devoid of any blocks, i.e. there is only air.
	 * Positions are all absolute coordinates, not relative.
	 * @param maxX, maxY, and maxZ values are inclusive
	 * @param minX, minY, and minZ are the lowest values to check - they must not be greater in value than the max values
	 */
	public static boolean isAreaClear(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
		for (int i = minX; i <= maxX; ++i) {
			for (int j = minY; j <= maxY; ++j) {
				for (int k = minZ; k <= maxZ; ++k) {
					if (!world.isAirBlock(new BlockPos(i, j, k))) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns number of blocks of given material either directly above or below the structure
	 * Only checks 5 points: center and 4 corners
	 * @param offY checks layer above or below this many blocks; positive value checks above
	 */
	public static int getNumBlocksOfMaterial(World world, StructureBoundingBox box, Material material, int offY) {
		int count = 0;
		int y = (offY > 0 ? box.maxY + offY : box.minY + offY);
		Vec3i center = box.getCenter();
		count += (world.getBlockState(new BlockPos(center.getX(), y, center.getZ())).getBlock().getMaterial() == material ? 1 : 0);
		count += (world.getBlockState(new BlockPos(box.maxX, y, box.maxZ)).getBlock().getMaterial() == material ? 1 : 0);
		count += (world.getBlockState(new BlockPos(box.maxX, y, box.minZ)).getBlock().getMaterial() == material ? 1 : 0);
		count += (world.getBlockState(new BlockPos(box.minX, y, box.maxZ)).getBlock().getMaterial() == material ? 1 : 0);
		count += (world.getBlockState(new BlockPos(box.minX, y, box.minZ)).getBlock().getMaterial() == material ? 1 : 0);
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
					if (world.getBlockState(new BlockPos(i, j, k)).getBlock().getMaterial() == material) {
						++count;
					}
				}
			}
		}
		return count;
	}

	//=========================================================================//
	//======================= ROTATION-FRIENDLY METHODS =======================//
	//=========================================================================//

	/** The directional values associated with player facing: */
	public static final int SOUTH = 0, WEST = 1, NORTH = 2, EAST = 3;

	/**
	 * Returns amount to offset the x coordinate based on facing, assuming default facing of EAST
	 * @param dx	The default offset for x coordinate
	 * @param dz	The default offset for z coordinate
	 * @return		Value r such that (x + r) is the correct x position for this facing
	 */
	public static int getOffsetX(int dx, int dz, int facing) {
		switch (facing) {
		case EAST: return dx;
		case NORTH: return dz;
		case WEST: return -dx;
		case SOUTH: return -dz;
		default: return dx;
		}
	}

	/**
	 * Returns amount to offset the z coordinate based on facing, assuming default facing of EAST
	 * @param dx	The default offset for x coordinate
	 * @param dz	The default offset for z coordinate
	 * @return		Value r such that (z + r) is the correct z position for this facing
	 */
	public static int getOffsetZ(int dx, int dz, int facing) {
		switch (facing) {
		case EAST: return dz;
		case NORTH: return -dx;
		case WEST: return -dz;
		case SOUTH: return dx;
		default: return dz;
		}
	}

	/**
	 * Returns the correct metadata value for the block type based on the facing given
	 * and the original metadata, where the original metadata should be the metadata
	 * providing the desired orientation for a facing of EAST.
	 * 
	 * @param facing	The facing to which this block is being rotated
	 * @param rotations	The number of rotations to apply
	 * @param block		The block being rotated
	 * @param origMeta	The block's original metadata value
	 */
	public static final int getMetadata(int facing, Block block, int origMeta) {
		if (BlockRotationData.getBlockRotationType(block) == null) {
			return origMeta; // no rotation data, return original metadata value
		}

		int meta = origMeta;
		int bitface;
		int tickDelay = (meta >> 2);// used by repeaters, comparators, etc.
		int bit4 = (meta & 4);		// most commonly used for actual rotation
		int bit8 = (meta & 8);		// usually 'on' or 'off' flag, but also top/bottom for doors
		int bit9 = (meta >> 3);		// used by pistons for something, can't remember what...
		int extra = (meta & ~3);	// used by doors for hinge orientation, I think

		// east is 3 (0 rotations), north 2 (3 rot), west 1 (2 rot), south 0 (1 rot)
		int rotations = rotateRight[facing];
		for (int i = 0; i < rotations; ++i) {
			bitface = meta % 4;
			switch(BlockRotationData.getBlockRotationType(block)) {
			case ANVIL:
				meta ^= 1;
				break;
			case DOOR:
				if (bit8 != 0) return meta;
				meta = (bitface == 3 ? 0 : bitface + 1);
				meta |= extra;
				break;
			case GENERIC:
				meta = (bitface == 3 ? 0 : bitface + 1) | bit4 | bit8;
				break;
			case PISTON_CONTAINER:
				meta -= meta > 7 ? 8 : 0;
				if (meta > 1) meta = meta == 2 ? 5 : meta == 5 ? 3 : meta == 3 ? 4 : 2;
				meta |= bit8 | bit9 << 3;
				break;
			case QUARTZ:
				meta = meta == 3 ? 4 : meta == 4 ? 3 : meta;
				break;
			case RAIL:
				if (meta < 2) meta ^= 1;
				else if (meta < 6) meta = meta == 2 ? 5 : meta == 5 ? 3 : meta == 3 ? 4 : 2;
				else meta = meta == 9 ? 6 : meta + 1;
				break;
			case REPEATER:
				meta = (bitface == 3 ? 0 : bitface + 1) | (tickDelay << 2);
				break;
			case SIGNPOST:
				meta = meta < 12 ? meta + 4 : meta - 12;
				break;
			case SKULL:
				meta = meta == 1 ? 1 : meta == 4 ? 2 : meta == 2 ? 5 : meta == 5 ? 3 : 4;
				break;
			case STAIRS:
				meta = (bitface == 0 ? 2 : bitface == 2 ? 1 : bitface == 1 ? 3 : 0) | bit4;
				break;
			case TRAPDOOR:
				meta = (bitface == 0 ? 3 : bitface == 3 ? 1 : bitface == 1 ? 2 : 0) | bit4 | bit8;
				break;
			case VINE:
				meta = meta == 1 ? 2 : meta == 2 ? 4 : meta == 4 ? 8 : 1;
				break;
			case WALL_MOUNTED:
				if (meta > 0 && meta < 5) meta = meta == 4 ? 1 : meta == 1 ? 3 : meta == 3 ? 2 : 4;
				break;
			case LEVER:
				meta -= meta > 7 ? 8 : 0;
				if (meta > 0 && meta < 5) meta = meta == 4 ? 1 : meta == 1 ? 3 : meta == 3 ? 2 : 4;
				else if (meta == 5 || meta == 6) meta = meta == 5 ? 6 : 5;
				else meta = meta == 7 ? 0 : 7;
				meta |= bit8;
				break;
			case WOOD:
				if (meta > 4 && meta < 12) meta = meta < 8 ? meta + 4 : meta - 4;
				break;
			default:
				break;
			}
		}
		return meta;
	}

	/**
	 * Fixes blocks' state after they've been placed in the world, specifically for blocks
	 * such as rails, furnaces, etc. whose orientation is automatically determined by the block
	 * when placed via the onBlockAdded method.
	 */
	public static final void setMetadata(World world, BlockPos pos, IBlockState origState) {
		Block block = world.getBlockState(pos).getBlock(); 
		if (BlockRotationData.getBlockRotationType(block) == null) {
			return;
		}
		switch(BlockRotationData.getBlockRotationType(block)) {
		case PISTON_CONTAINER: world.setBlockState(pos, origState, 2); break;
		case RAIL: world.setBlockState(pos, origState, 2); break;
		default: break;
		}
	}

	/**
	 * Fills area with the given block, with the area rotated based on facing such that,
	 * if a player's facing is given, minX is always closest to the player, maxX furthest
	 * away, minZ to the left, and maxZ to the right, all relative to x/y/z.
	 * 
	 * All min/max parameters are inclusive, so min/maxX={0,4} will place 5 blocks, starting at
	 * x and going up to and including x+4.
	 * 
	 * Negative values are acceptable: min/maxX={-2,2} will place blocks from x-2 to x+2.
	 * 
	 * @param x			x-coordinate around which to set blocks (and rotate around, if necessary)
	 * @param z			z-coordinate around which to set blocks (and rotate around, if necessary)
	 * @param minX		x+minX is the initial x coordinate at which to begin setting blocks
	 * @param maxX		x+maxX is the highest x coordinate to set
	 * @param minY		initial y position
	 * @param maxY		maximum y position
	 * @param minZ		z+minZ is the initial z coordinate at which to begin setting blocks
	 * @param maxZ		z+maxZ is the highest z coordinate to set
	 * @param facing	Default is considered EAST
	 * @param meta		This should already be rotated to the correct value, to avoid processing every single call to setBlock
	 */
	public static void rotatedFillWithBlocks(World world, int x, int z, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, int facing, IBlockState state) {
		for (int i = minX; i <= maxX; ++i) {
			for (int j = minY; j <= maxY; ++j) {
				for (int k = minZ; k <= maxZ; ++k) {
					world.setBlockState(new BlockPos(x + getOffsetX(i, k, facing), j, z + getOffsetZ(i, k, facing)), state, 2);
				}
			}
		}
	}

	/**
	 * Returns whether the designated area is devoid of entities and non-air blocks, with the
	 * area defined in relative terms and rotated as necessary based on the given facing.
	 * Relative positions define the size of the structure, such that:
	 * [backX - frontX] + 1	= the total length of the structure, thus backX must be greater than frontX
	 * [maxY - minY] + 1	= the total height of the structure
	 * [rightZ - leftZ] + 1	= the total width of the structure, thus rightZ must be greater than leftZ
	 * 
	 * All bounds are inclusive (thus the +1 used above when determining structure dimensions).
	 * 
	 * Example:
	 * canGenerate(world, x, z, 0, 9, 64, 68, -3, 6, facing), starting from the block clicked
	 * at x/64/z, will check the area enclosed by going 9 blocks forward, 3 blocks to the left,
	 * and 6 blocks to the right, starting from y=64 to y=68, for a total area of 10x5x10.
	 * 
	 * @param frontX	The starting position, relative to x, at which to check for blocks (usually 0)
	 * @param backX		The furthest position, relative to x, at which to check for blocks (usually > 0)
	 * @param minY		Absolute minimum y position at which to check for blocks
	 * @param maxY		Absolute topmost y position at which to check for blocks
	 * @param leftZ		The leftmost position, relative to z, at which to check for blocks (usually < 0)
	 * @param rightZ	The rightmost position, relative to z, at which to check for blocks (usually > 0)
	 * @param facing	Usually the direction the player is facing when generating the structure
	 * @return			True if there were no blocks (other than air) or entities found in the area
	 */
	public static boolean isRotatedAreaClear(World world, int x, int z, int frontX, int backX, int minY, int maxY, int leftZ, int rightZ, int facing) {
		int front = Math.min(x + getOffsetX(frontX, leftZ, facing), x + getOffsetX(frontX, rightZ, facing));
		int back = Math.max(x + getOffsetX(backX, leftZ, facing), x + getOffsetX(backX, rightZ, facing));
		int left = Math.min(z + getOffsetZ(frontX, leftZ, facing), z + getOffsetZ(frontX, rightZ, facing));
		int right = Math.max(z + getOffsetZ(backX, leftZ, facing), z + getOffsetZ(backX, rightZ, facing));
		// determine min and max after accounting for rotation
		int minX = Math.min(front, back);
		int maxX = Math.max(front, back);
		int minZ = Math.min(left, right);
		int maxZ = Math.max(left, right);
		List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
		return (entities == null || entities.size() == 0) && isAreaClear(world, minX, maxX, minY, maxY, minZ, maxZ);
	}
}

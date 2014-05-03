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

package zeldaswordskills.world.gen.structure;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.StructureGenUtils;


/**
 * 
 * Basic shared functionality for all structures, such as bounding box and location data
 *
 */
public abstract class RoomBase
{
	/** Directional values, starting south (+z) and moving clockwise: */
	public static final int SOUTH = 0, WEST = 1, NORTH = 2, EAST = 3;
	
	/** Whether this particular dungeon was below water */
	public boolean submerged = false;

	/** Whether this particular dungeon came into contact with lava */
	public boolean inLava = false;

	/** Whether this dungeon will generate on the ocean floor (not just in the Ocean biome) */
	public boolean inOcean = false;

	/** Mountain dungeons have the highest chance of adding fairy spawners */
	public boolean inMountain = false;

	/** Nether dungeons have the highest chance of fire-based equipment */
	public boolean inNether = false;

	/** Generates a dungeon with a locked door and completely unbreakable walls if true */
	public boolean isLocked = false;

	/** This structure's bounding box */
	protected StructureBoundingBox bBox;
	
	/** The structure's chunk coordinates */
	public final int chunkX, chunkZ;

	/** The block which must have a majority representation in the area to replace */
	protected final int blockRequired;
	
	/** The metadata that will be used for setting this block's texture */
	protected int metadata = 0;

	/** Set of all blocks that it's okay for this structure to replace */
	protected static final Set<Integer> replaceBlocks = new HashSet<Integer>();

	/** Returns this structure's bounding box */
	public final StructureBoundingBox getBoundingBox() { return bBox; }

	/** Returns total area of one horizontal slice of the structure's bounding box */
	public final int getArea() { return (bBox.getXSize() * bBox.getZSize()); }

	/** Returns total cubic area represented by the structure's bounding box */
	public final int getVolume() { return (bBox.getXSize() * bBox.getYSize() * bBox.getZSize()); }

	/** Attempts to generate the structure at the given coordinates, returning true if successful */
	public abstract boolean generate(ZSSMapGenBase mapGen, World world, Random rand, int x, int y, int z);

	/** Adds the final touches: chests, dungeon core, pedestal, etc. */
	protected abstract void decorateDungeon(World world, Random rand);
	
	/** Places and sets up the Dungeon Core for this room */
	protected abstract void placeDungeonCore(World world);
	
	/**
	 * Places a pedestal in the dungeon's center with a Master Sword stuck in it
	 */
	protected void placePedestal(World world, int offsetY) {
		StructureGenUtils.setBlockAtPosition(world, bBox, bBox.getXSize() / 2, offsetY, bBox.getZSize() / 2, ZSSBlocks.pedestal.blockID, 0);
		int x = StructureGenUtils.getXWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		int y = StructureGenUtils.getYWithOffset(bBox, offsetY);
		int z = StructureGenUtils.getZWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPedestal) {
			((TileEntityPedestal) te).setSword(new ItemStack(ZSSItems.swordMaster), null);
		}
	}
	
	/**
	 * Basic constructor for rooms sets up the bounding box and sets the block required field
	 */
	public RoomBase(int chunkX, int chunkZ, int size, int maxHeight, int blockRequired) {
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.blockRequired = blockRequired;
		this.bBox = new StructureBoundingBox(1, 1, 1, Math.max(size, 3), MathHelper.clamp_int(size, 3, maxHeight), Math.max(size, 3));
	}

	/**
	 * Returns true if there aren't too many unacceptable materials within the structure's location
	 */
	protected boolean canGenerate(World world) {
		int failedAmount = 0;
		int maxFail = (bBox.getXSize() * bBox.getZSize() / 2);
		for (int i = bBox.minX; i <= bBox.maxX; ++i) {
			for (int j = bBox.minY; j <= bBox.maxY; ++j) {
				for (int k = bBox.minZ; k <= bBox.maxZ; ++k) {
					int blockId = world.getBlockId(i, j, k);
					if (!canReplaceBlockAt(j, blockId)) {
						if (blockId == ZSSBlocks.secretStone.blockID || (Config.avoidModBlocks() && blockId > 255)) {
							return false;
						} else if (++failedAmount > maxFail) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Standard room gen procedure builds basic cube, fills with liquids/air, and calls decorateDungeon
	 */
	protected void doStandardRoomGen(World world, Random rand) {
		StructureGenUtils.fillWithBlocks(world, bBox, 0, bBox.getXSize(), 0, bBox.getYSize(), 0, bBox.getZSize(), ZSSBlocks.secretStone.blockID, getMetadata());
		genSubmerged(world);
		generateAir(world);
		decorateDungeon(world, rand);
	}
	
	/**
	 * Returns true if the structure is considered well-hidden; i.e. not too many blocks exposed to air / water
	 */
	protected boolean isWellHidden(World world) {
		int difficulty = Config.getMainDungeonDifficulty();
		if (inOcean) { return true;
			//return world.rand.nextFloat() < (1.0F - (difficulty * 0.25F)) ||
					//StructureGenUtils.getNumBlocksOfMaterialInArea(world, bBox, Material.water, 1) < (getArea() / (difficulty + 1));
		}
		Material material = (!inNether && StructureGenUtils.getNumBlocksOfMaterial(world, bBox, Material.water, 1) > 0 ? Material.water : Material.air);
		int above = StructureGenUtils.getNumBlocksOfMaterial(world, bBox, material, 1);
		int below = StructureGenUtils.getNumBlocksOfMaterial(world, bBox, material, -1);
		if (inNether) {
			return world.rand.nextFloat() < (0.35F - (difficulty * 0.1F)) || ((difficulty != 3 || (above + below) < 4) &&
					StructureGenUtils.getNumBlocksOfMaterialInArea(world, bBox, material, 1) < (getArea() / (difficulty + 1))); // above < (5 - difficulty) && 
		}
		
		return world.rand.nextFloat() < (0.35F - (difficulty * 0.1F)) || (above + below) < (5 - difficulty);
	}

	/** Shortcut for canReplaceBlockAt(int y, int id) */
	protected boolean canReplaceBlockAt(World world, int x, int y, int z) {
		return canReplaceBlockAt(y, world.getBlockId(x, y, z));
	}

	/**
	 * Returns true if the block id at height y can be replaced by this structure,
	 * specifically if replaceBlocks contains the block or the block's material is
	 * liquid and not in the top two layers
	 */
	protected boolean canReplaceBlockAt(int y, int id) {
		if (id < 1 || Block.blocksList[id] == null) { return false; }
		boolean flag1 = (submerged && !inLava && Block.blocksList[id].blockMaterial == Material.water);
		boolean flag2 = (inNether && Block.blocksList[id].blockMaterial == Material.lava);
		return (replaceBlocks.contains(id) || flag1 || flag2 || (Block.blocksList[id].blockMaterial.isLiquid() && y < (bBox.maxY - 2)));
	}
	
	/**
	 * Returns the secret stone metadata value of the block to place
	 */
	protected int getMetadata() {
		return metadata + (isLocked ? 8 : 0);
	}
	
	/** Sets the room's metadata based on world biome */
	protected abstract void setMetadata(World world, int x, int z);
	
	/**
	 * Fills room with air according to submerged / ocean status
	 */
	protected void generateAir(World world) {
		if (!inOcean) {
			StructureGenUtils.fillWithBlocks(world, bBox, 1, bBox.getXSize() - 1, (submerged ? (inLava || isLocked ? 2 : 3) : 1), bBox.getYSize() - 1, 1, bBox.getZSize() - 1, 0, 0);
		}
	}
	
	/**
	 * Generation for submerged dungeons adds liquid layers: lava 1, water 2, ocean filled
	 * Checks internally if this room is valid for liquid generation
	 */
	protected void genSubmerged(World world) {
		if (submerged && bBox.getXSize() > 3) {
			int fillTo = (inLava ? 2 : inOcean ? bBox.getYSize() - 1 : 3);
			int blockID = (inLava ? Block.lavaStill.blockID : Block.waterStill.blockID);
			StructureGenUtils.fillWithBlocks(world, bBox, 1, bBox.getXSize() - 1, 1, fillTo, 1, bBox.getZSize() - 1, blockID, 0);
		}
	}

	/**
	 * After a failed validation, attempts to place structure in ocean if applicable
	 * @param sink if true, sinks the structure by some amount into the ocean floor
	 */
	protected boolean placeInOcean(World world, boolean sink) {
		bBox.offset(0, 4, 0); // move back up a little
		int x = bBox.getCenterX();
		int z = bBox.getCenterZ();
		boolean flag = world.getBiomeGenForCoords(x, z).biomeName.toLowerCase().contains("ocean");
		if (flag && !inLava && world.getBlockMaterial(x, bBox.maxY, z) == Material.water) {
			int count = 0;
			while (bBox.minY > 16 && count < 8 && world.getBlockMaterial(x, bBox.minY, z) == Material.water) {
				bBox.offset(0, -1, 0);
				++count;
			}
			
			if (world.getBlockMaterial(x, bBox.minY, z) != Material.water) {
				inOcean = true;
				StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.water, 6, false, false);
				if (sink) {
					int diff = Config.getMainDungeonDifficulty();
					int adj = 2 - diff;
					if (world.rand.nextFloat() > (diff * 0.25F)) {
						if (diff == 3) {
							++adj;
						} else {
							adj += (world.rand.nextFloat() < 0.5F ? 1 : -1);
						}
					}
					bBox.offset(0, -(bBox.getYSize() - adj), 0);
				}
				
				return true;
			}
		}

		return false;
	}

	/**
	 * Adjusts nether dungeons to rest on solid ground when submerged in lava
	 * @return true if final bottom block is not another secret dungeon block
	 */
	protected boolean placeInNether(World world) {
		while (bBox.minY > 8 && world.getBlockMaterial(bBox.getCenterX(), bBox.minY, bBox.getCenterZ()) == Material.lava) {
			bBox.offset(0, -1, 0);
		}
		StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.lava, 4, false, false);
		return (world.getBlockId(bBox.getCenterX(), bBox.minY, bBox.getCenterZ()) != ZSSBlocks.secretStone.blockID);
	}

	protected int validations = 0;

	/** Number of y layers the room may be adjusted downwards */
	protected static final int NUM_VALIDATIONS = 8;

	/**
	 * If top layer is not valid (contains liquid or not enough of correct material), adjusts
	 * the structure downwards until no longer the case, the structure's lower level is too
	 * low, or the number of attempts exceeds NUM_VALIDATIONS
	 */
	protected boolean validateTopLayer(World world) {
		int invalidBlocks = 0; // number of blocks not matching the required block type
		int area = getArea();
		++validations;

		for (int i = bBox.minX; i <= bBox.maxX; ++i) {
			for (int k = bBox.minZ; k <= bBox.maxZ; ++k) {
				if (validations > NUM_VALIDATIONS || bBox.minY < 5) {
					return false;
				} else {
					int blockId = world.getBlockId(i, bBox.maxY, k);
					if (Block.blocksList[blockId] != null && Block.blocksList[blockId].blockMaterial.isLiquid()) {
						submerged = true;
						inLava = (blockId == Block.lavaStill.blockID);
						bBox.offset(0, -1, 0);
						return ((inNether && bBox.maxY < 48) || validateTopLayer(world));
					} else if (blockId != blockRequired) {
						if (++invalidBlocks > area / 2) {
							bBox.offset(0, -1, 0);
							return validateTopLayer(world);
						}
					}
				}
			}
		}

		return true;
	}
	
	/**
	 * Returns a new tag compound with the room's chunk coordinates and bounding box
	 */
	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setTag("BB", bBox.func_143047_a("BB"));
		return compound;
	}
	
	/**
	 * Reads the room's bounding box from NBT (currently unused)
	 */
	public void readFromNBT(NBTTagCompound compound) {
		bBox = new StructureBoundingBox(compound.getIntArray("BB"));
	}

	static {
		replaceBlocks.add(Block.cobblestone.blockID);
		replaceBlocks.add(Block.stone.blockID);
		replaceBlocks.add(Block.dirt.blockID);
		replaceBlocks.add(Block.grass.blockID);
		replaceBlocks.add(Block.gravel.blockID);
		replaceBlocks.add(Block.netherrack.blockID);
		replaceBlocks.add(Block.sand.blockID);
		replaceBlocks.add(Block.sandStone.blockID);
		replaceBlocks.add(Block.snow.blockID);
	}
}

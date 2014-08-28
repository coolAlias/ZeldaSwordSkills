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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.world.gen.DungeonLootLists;

/**
 * 
 * Generates a 'boss' room: larger, more complex, and always locked with a special door.
 * 
 * The room size must be at least 9x9 blocks in area, with a random height from 7-10
 * 
 * Each biome has its own boss room, and each room type will always contain certain items.
 *
 */
public class RoomBoss extends RoomBase
{
	/** Mapping of cardinal direction to block-orientation values for blocks using 2,3,4,5 (containers, pistons, etc) */
	public static final int[] facingToOrientation = {3,4,2,5};
	
	/** Side of the structure in which the door is located; use above directional values */
	private int doorSide = SOUTH;
	
	/** The type of dungeon this is, as determined by biome using BossType.getBossType */
	private final BossType type;

	public RoomBoss(BossType bossType, int chunkX, int chunkZ, Random rand, int size, int blockRequired) {
		super(chunkX, chunkZ, Math.max(size, 9), (rand.nextInt(4) + 7), blockRequired);
		isLocked = true;
		type = bossType;
	}
	
	/** Returns the BossType for this Boss Room */
	public BossType getBossType() { return type; }
	
	@Override
	public boolean generate(ZSSMapGenBase mapGen, World world, Random rand, int x, int y, int z) {
		if (initDungeon(world, x, y, z) && canGenerate(world)) {
			doStandardRoomGen(world, rand);
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Performs initial set up and placement of the dungeon
	 * @return false if anything went awry and generation should be canceled
	 */
	protected boolean initDungeon(World world, int x, int y, int z) {
		bBox.offset(x, y, z);
		if (type == null || y < bBox.getYSize() || y > (type == BossType.HELL ? 96 : 160)) {
			return false;
		}
		
		switch(type) {
		case HELL:
			inNether = true;
			if (placeInNether(world)) {
				doDefaultAdjustments(world);
				if (world.rand.nextFloat() < 0.75F) {
					submerged = true;
					inLava = true;
				}
			} else {
				return false;
			}
			break;
		case OCEAN:
			if (!placeInOcean(world, false)) {
				return false;
			}
			break;
		case SWAMP:
			submerged = true;
			doDefaultAdjustments(world);
			bBox.offset(0, -1, 0);
			break;
		default:
			doDefaultAdjustments(world);
		}
		if (submerged) {
			--bBox.minY;
		}
		
		determineDoorSide(world);
		setMetadata(world, x, z);
		boolean flag = StructureGenUtils.getAverageDistanceToGround(world, bBox, 6) < 4;
		return (doorSide != -1 && flag && (submerged || !isWaterAroundOrUnder(world)));
	}
	
	/**
	 * Makes default adjustments for surrounding materials such as dirt, air, etc.
	 */
	private void doDefaultAdjustments(World world) {
		// adjust up for dirt, grass, and sand
		StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.ground, 4, false, true);
		StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.grass, 4, false, true);
		StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.sand, 4, false, true);
		// adjust down for air and water pockets
		StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.air, 4, false, false);
		if (type != BossType.SWAMP) {
			StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.water, 4, false, false);
		}
		// adjust down one more so door is flush with ground
		//bBox.offset(0, -1, 0); TODO add stairs or slabs up instead
	}
	
	/**
	 * Returns true if there are 2 or more blocks of water directly under the structure, 
	 * or if there are 3 or more blocks of water next to any given side
	 */
	protected boolean isWaterAroundOrUnder(World world) {
		if (StructureGenUtils.getNumBlocksOfMaterial(world, bBox, Material.water, -1) > 1) {
			return true;
		} else if (StructureGenUtils.getNumBlocksOfMaterialInArea(world, Material.water, bBox.minX - 1, bBox.minX, bBox.minY, bBox.minY + 2, bBox.minZ, bBox.maxZ) > 2) {
			return true;
		} else if (StructureGenUtils.getNumBlocksOfMaterialInArea(world, Material.water, bBox.maxX + 1, bBox.maxX + 2, bBox.minY, bBox.minY + 2, bBox.minZ, bBox.maxZ) > 2) {
			return true;
		} else if (StructureGenUtils.getNumBlocksOfMaterialInArea(world, Material.water, bBox.minX, bBox.maxX, bBox.minY, bBox.minY + 2, bBox.minZ - 1, bBox.minZ) > 2) {
			return true;
		} else if (StructureGenUtils.getNumBlocksOfMaterialInArea(world, Material.water, bBox.minX, bBox.maxX, bBox.minY, bBox.minY + 2, bBox.maxZ + 1, bBox.maxZ + 2) > 2) {
			return true;
		}
		return false;
	}
	
	@Override
	protected void setMetadata(World world, int x, int z) {
		metadata = type.metadata;
	}
	
	/**
	 * Adds the final touches: chests, dungeon core, pedestal, etc.
	 */
	protected void decorateDungeon(World world, Random rand) {
		int meta = getMetadata();
		StructureGenUtils.fillDown(world, bBox, BlockSecretStone.getIdFromMeta(meta), 0);
		placeDoor(world);
		placeDungeonCore(world);
		placePillars(world, meta);
		placeCenterPiece(world, rand, meta);
		placeChandelier(world);
		placeParapet(world, meta);
		placeLedge(world, rand, meta);
		placeChestOnRoof(world, rand);
		placeJars(world, rand, rand.nextInt(5), false);
		placeJars(world, rand, rand.nextInt(5) + 3, true);
		placeWindows(world);
	}

	@Override
	protected void placeDungeonCore(World world) {
		StructureGenUtils.setBlockAtPosition(world, bBox, bBox.getXSize() / 2, 0, bBox.getZSize() / 2, ZSSBlocks.dungeonCore.blockID, getMetadata() | 0x8);
		int x = StructureGenUtils.getXWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		int y = StructureGenUtils.getYWithOffset(bBox, 0);
		int z = StructureGenUtils.getZWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonCore) {
			TileEntityDungeonCore core = (TileEntityDungeonCore) te;
			core.setDungeonBoundingBox(bBox);
			core.setBossType(type);
			core.setDoor(ZSSBlocks.doorLocked, doorSide);
		}
	}
	
	/**
	 * Determines which side is most suitable for the door
	 */
	protected void determineDoorSide(World world) {
		int x = bBox.getCenterX();
		int y = bBox.minY + 1;
		int z = bBox.getCenterZ();
		
		int id1 = world.getBlockId(x, y, bBox.maxZ + 1);
		int id2 = world.getBlockId(x, y + 1, bBox.maxZ + 1);
		if (!Block.opaqueCubeLookup[id1] && !Block.opaqueCubeLookup[id2]) {
			doorSide = SOUTH;
			return;
		}
		
		id1 = world.getBlockId(x, y, bBox.minZ - 1);
		id2 = world.getBlockId(x, y + 1, bBox.minZ - 1);
		if (!Block.opaqueCubeLookup[id1] && !Block.opaqueCubeLookup[id2]) {
			doorSide = NORTH;
			return;
		}
		
		id1 = world.getBlockId(bBox.maxX + 1, y, z);
		id2 = world.getBlockId(bBox.maxX + 1, y + 1, z);
		if (!Block.opaqueCubeLookup[id1] && !Block.opaqueCubeLookup[id2]) {
			doorSide = EAST;
			return;
		}
		
		id1 = world.getBlockId(bBox.minX - 1, y, z);
		id2 = world.getBlockId(bBox.minX - 1, y + 1, z);
		if (!Block.opaqueCubeLookup[id1] && !Block.opaqueCubeLookup[id2]) {
			doorSide = WEST;
			return;
		}
		
		doorSide = -1;
	}
	
	/**
	 * Actually places the door; use after determining door side for best results
	 */
	protected void placeDoor(World world) {
		int x = bBox.getCenterX();
		int y = bBox.minY + (submerged ? 2 : 1);
		int z = bBox.getCenterZ();
		
		//determineDoorSide(world, true);
		switch(doorSide) {
		case SOUTH: z = bBox.maxZ; break;
		case NORTH: z = bBox.minZ; break;
		case EAST: x = bBox.maxX; break;
		case WEST: x = bBox.minX; break;
		default: LogHelper.warning("Placing Boss door with invalid door side");
		}
		
		world.setBlock(x, y, z, ZSSBlocks.doorLocked.blockID, type.ordinal() & ~0x8, 2);
		world.setBlock(x, y + 1, z, ZSSBlocks.doorLocked.blockID, type.ordinal() | 0x8, 2);
	}
	
	/**
	 * Places the center half-slabs and block for either a chest or a pedestal
	 */
	protected void placeCenterPiece(World world, Random rand, int meta) {
		int minX = bBox.getXSize() / 2 - 1;
		int minY = 1;
		int minZ = bBox.getZSize() / 2 - 1;
		if (submerged) {
			StructureGenUtils.fillWithBlocks(world, bBox, minX, minX + 3, minY, minY + 1, minZ, minZ + 3, BlockSecretStone.getIdFromMeta(getMetadata()), 0);
			++minY;
		}
		if (!inOcean) {
			StructureGenUtils.fillWithBlocks(world, bBox, minX, minX + 3, minY, minY + 1, minZ, minZ + 3, Block.stoneSingleSlab.blockID, BlockSecretStone.getSlabTypeFromMeta(meta));
		}
		world.setBlock(bBox.getCenterX(), bBox.minY + (submerged && !inOcean ? 2 : 1), bBox.getCenterZ(), (type == BossType.TAIGA ? Block.blockNetherQuartz.blockID : BlockSecretStone.getIdFromMeta(getMetadata())), 0, 2);
		placeHinderBlock(world);
		
		boolean hasChest = false;
		switch(type) {
		case DESERT:
		case OCEAN:
		case MOUNTAIN: placeChest(world, rand, true); hasChest = true; break;
		case FOREST: placePedestal(world, minY + 1); break;
		case HELL: placeFlame(world, BlockSacredFlame.DIN); break;
		case SWAMP: placeFlame(world, BlockSacredFlame.FARORE); break;
		case TAIGA: placeFlame(world, BlockSacredFlame.NAYRU); break;
		default:
		}
		
		if (!hasChest) {
			placeChest(world, rand, false);
		}
	}
	
	/**
	 * Places the hanging chandelier only if height is sufficient
	 */
	protected void placeChandelier(World world) {
		if (bBox.getYSize() > 7) {
			int x = bBox.getCenterX();
			int y = bBox.maxY - 1;
			int z = bBox.getCenterZ();
			if (type == BossType.OCEAN) {
				world.setBlock(x + 1, y, z + 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x + 1, y, z - 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x - 1, y, z + 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x - 1, y, z - 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x, y, z, Block.glowStone.blockID, 0, 2);
			} else {
				world.setBlock(x, y, z, Block.fence.blockID, 0, 2);
				world.setBlock(x + 1, y, z, Block.fence.blockID, 0, 2);
				world.setBlock(x + 1, y, z, Block.fence.blockID, 0, 2);
				world.setBlock(x, y, z + 1, Block.fence.blockID, 0, 2);
				world.setBlock(x, y, z - 1, Block.fence.blockID, 0, 2);
				world.setBlock(x + 1, y, z + 1, Block.fence.blockID, 0, 2);
				world.setBlock(x + 1, y, z - 1, Block.fence.blockID, 0, 2);
				world.setBlock(x - 1, y, z + 1, Block.fence.blockID, 0, 2);
				world.setBlock(x - 1, y, z - 1, Block.fence.blockID, 0, 2);
				world.setBlock(x, y - 1, z, Block.fence.blockID, 0, 2);
				world.setBlock(x + 1, y - 1, z + 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x + 1, y - 1, z - 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x - 1, y - 1, z + 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x - 1, y - 1, z - 1, Block.glowStone.blockID, 0, 2);
				world.setBlock(x, y - 2, z, Block.glowStone.blockID, 0, 2);
			}
		}
	}
	
	/**
	 * Places and generates the contents of this dungeon's main chest
	 */
	protected void placeChest(World world, Random rand, boolean inCenter) {
		int x = (inCenter ? bBox.getCenterX() : (rand.nextFloat() < 0.5F ? bBox.minX + 1 : bBox.maxX - 1));
		int y = bBox.minY + (inCenter ? 2 : 1) + (submerged && !inOcean ? 1 : 0);
		int z = (inCenter ? bBox.getCenterZ() : (rand.nextFloat() < 0.5F ? bBox.minZ + 1 : bBox.maxZ - 1));
		int blockId = (inCenter ? Block.chest.blockID : ZSSBlocks.chestLocked.blockID);
		world.setBlock(x, y, z, blockId);
		if (inCenter) {
			world.setBlockMetadataWithNotify(x, y, z, facingToOrientation[doorSide], 2);
		} else if (submerged && !inOcean) {
			world.setBlock(x, y - 1, z, BlockSecretStone.getIdFromMeta(getMetadata()), 0, 2);
		}
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof IInventory) {
			DungeonLootLists.generateBossChestContents(world, rand, (IInventory) te, this);
		}
	}
	
	/**
	 * Attempts to place a chest (unlocked, but with locked-level loot) in one of the four roof corners
	 */
	protected void placeChestOnRoof(World world, Random rand) {
		if (rand.nextFloat() < 0.1F) {
			int x = (rand.nextFloat() < 0.5F ? bBox.minX : bBox.maxX);
			int y = bBox.maxY + 1;
			int z = (rand.nextFloat() < 0.5F ? bBox.minZ : bBox.maxZ);
			world.setBlock(x, y, z, Block.chest.blockID);
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof IInventory) {
				DungeonLootLists.generateChestContents(world, rand, (IInventory) te, this, true);
			}
		}
	}
	
	/**
	 * Places the designated Sacred Flame on the center block
	 */
	protected void placeFlame(World world, int meta) {
		world.setBlock(bBox.getCenterX(), bBox.minY + (submerged ? 3 : 2), bBox.getCenterZ(), ZSSBlocks.sacredFlame.blockID, meta, 2);
	}
	
	/**
	 * Places a secret stone block between the chest and door to prevent early looting
	 */
	protected void placeHinderBlock(World world) {
		int x = bBox.getCenterX() + (doorSide == EAST ? 1 : (doorSide == WEST ? -1 : 0));
		int y = bBox.minY + (submerged && !inOcean ? 3 : 2);
		int z = bBox.getCenterZ() + (doorSide == SOUTH ? 1 : (doorSide == NORTH ? -1 : 0));
		world.setBlock(x, y, z, ZSSBlocks.secretStone.blockID, getMetadata(), 2);
	}
	
	/**
	 * Adds n random breakable jars, either on the floor or on the roof
	 */
	protected void placeJars(World world, Random rand, int n, boolean onRoof) {
		for (int i = 0; i < n; ++i) {
			int x = bBox.minX + rand.nextInt(bBox.getXSize());
			int y = (onRoof ? bBox.maxY : bBox.minY) + 1;
			int z = bBox.minZ + rand.nextInt(bBox.getZSize());
			Material m = world.getBlockMaterial(x, y, z);
			if (m == Material.air || m.isLiquid()) {
				world.setBlock(x, y, z, ZSSBlocks.ceramicJar.blockID);
			}
		}
	}
	
	/**
	 * Places a one-block wide ledge around the inside wall perimeter at centerY only
	 * if certain conditions are met (sufficient y size, random)
	 */
	protected void placeLedge(World world, Random rand, int meta) {
		if (type != BossType.OCEAN && bBox.getYSize() > 7 && rand.nextFloat() < (type == BossType.HELL ? 0.75F : 0.5F)) {
			int y = bBox.getCenterY();
			int blockId = BlockSecretStone.getIdFromMeta(meta);
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 1, bBox.minX + 2, y, y + 1, bBox.minZ + 1, bBox.maxZ, blockId, 0, 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.maxX - 1, bBox.maxX, y, y + 1, bBox.minZ + 1, bBox.maxZ, blockId, 0, 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 2, bBox.maxX - 1, y, y + 1, bBox.minZ + 1, bBox.minZ + 2, blockId, 0, 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 2, bBox.maxX - 1, y, y + 1, bBox.maxZ - 1, bBox.maxZ, blockId, 0, 3);
		}
	}
	
	/**
	 * Places parapet encircling the room's top
	 */
	protected void placeParapet(World world, int meta) {
		int x1 = bBox.minX - 1;
		int x2 = bBox.maxX + 1;
		int z1 = bBox.minZ - 1;
		int z2 = bBox.maxZ + 1;
		int y = bBox.maxY;
		int blockId = BlockSecretStone.getIdFromMeta(meta);
		int stairId = BlockSecretStone.getStairIdFromMeta(meta);
		
		for (int i = x1; i <= x2; ++i) {
			world.setBlock(i, y, z1, stairId, 6, 3);
			world.setBlock(i, y + 1, z1, blockId, 0, 2);
			world.setBlock(i, y, z2, stairId, 7, 3);
			world.setBlock(i, y + 1, z2, blockId, 0, 2);
			if (i % 2 == 0) {
				world.setBlock(i, y + 2, z1, blockId, 0, 2);
				world.setBlock(i, y + 2, z2, blockId, 0, 2);
			}
		}
		
		for (int i = z1; i <= z2; ++i) {
			world.setBlock(x1, y, i, stairId, 4, 3);
			world.setBlock(x1, y + 1, i, blockId, 0, 2);
			world.setBlock(x2, y, i, stairId, 5, 3);
			world.setBlock(x2, y + 1, i, blockId, 0, 2);
			if (i % 2 == 0) {
				if (world.getBlockId(x1 + 1, y + 2, i) != blockId) {
					world.setBlock(x1, y + 2, i, blockId, 0, 2);
				}
				if (world.getBlockId(x2 - 1, y + 2, i) != blockId) {
					world.setBlock(x2, y + 2, i, blockId, 0, 2);
				}
			}
		}
	}
	
	/**
	 * Places pillars in the four corners of the dungeon with chance based on room size
	 */
	protected void placePillars(World world, int meta) {
		if (type != BossType.DESERT && world.rand.nextFloat() < (bBox.getXSize() * 0.06F)) {
			int offset = (bBox.getXSize() < 11 ? 2 : 3);
			int x1 = (bBox.getXSize() < 11 ? bBox.getCenterX() : bBox.minX) + offset;
			int x2 = (bBox.getXSize() < 11 ? bBox.getCenterX() : bBox.maxX) - offset;
			int z1 = (bBox.getZSize() < 11 ? bBox.getCenterZ() : bBox.minZ) + offset;
			int z2 = (bBox.getZSize() < 11 ? bBox.getCenterZ() : bBox.maxZ) - offset;
			int blockId = BlockSecretStone.getIdFromMeta(meta);
			
			for (int y = bBox.minY + 1; y < bBox.maxY; ++y) {
				world.setBlock(x1, y, z1, blockId, 0, 2);
				world.setBlock(x1, y, z2, blockId, 0, 2);
				world.setBlock(x2, y, z1, blockId, 0, 2);
				world.setBlock(x2, y, z2, blockId, 0, 2);
			}
			
			if (bBox.getXSize() > 10 && world.rand.nextFloat() < 0.5F) {
				placePillarLandings(world, blockId, x1, bBox.getCenterY(), z1, 0);
				placePillarLandings(world, blockId, x2, bBox.getCenterY(), z1, 1);
				placePillarLandings(world, blockId, x1, bBox.getCenterY(), z2, 2);
				placePillarLandings(world, blockId, x2, bBox.getCenterY(), z2, 3);
			}
		}
	}
	
	/**
	 * Places one-block landings on two sides of the pillar, depending on facing
	 * @param corner 0 NW, 1 NE, 2 SW, 3 SE
	 */
	protected void placePillarLandings(World world, int blockId, int x, int y, int z, int corner) {
		switch(corner % 4) {
		case 0:
			world.setBlock(x + 1, y, z, blockId);
			world.setBlock(x, y, z + 1, blockId);
			break;
		case 1:
			world.setBlock(x - 1, y, z, blockId);
			world.setBlock(x, y, z + 1, blockId);
			break;
		case 2:
			world.setBlock(x + 1, y, z, blockId);
			world.setBlock(x, y, z - 1, blockId);
			break;
		case 3:
			world.setBlock(x - 1, y, z, blockId);
			world.setBlock(x, y, z - 1, blockId);
		}
	}
	
	/**
	 * Places 'windows' at regular intervals in the walls
	 */
	protected void placeWindows(World world) {
		if (Config.areWindowsEnabled() && world.rand.nextFloat() < (bBox.getXSize() * 0.06F)) {
			int interval = (bBox.getXSize() % 2 == 1 ? 2 : 3);
			int j = bBox.getCenterY() + 1;
			for (int i = bBox.minX + 1; i < bBox.maxX; ++i) {
				if (i % interval == 0) {
					world.setBlockToAir(i, j, bBox.minZ);
					world.setBlockToAir(i, j, bBox.maxZ);
				}
			}
			for (int k = bBox.minZ + 1; k < bBox.maxZ; ++k) {
				if (k % interval == 0) {
					world.setBlockToAir(bBox.minX, j, k);
					world.setBlockToAir(bBox.maxX, j, k);
				}
			}
		}
	}
	
	@Override
	protected boolean canReplaceBlockAt(int y, int id) {
		boolean flag1 = (Block.blocksList[id] != null && !Block.blocksList[id].blockMaterial.blocksMovement());
		boolean flag2 = (Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.leaves);
		boolean flag3 = (type == BossType.TAIGA && (id == Block.wood.blockID || (Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.ice)));
		return (id == 0 || flag1 || flag2 || flag3 || super.canReplaceBlockAt(y, id));
	}
	
	@Override
	protected boolean placeInOcean(World world, boolean sink) {
		if (type == BossType.OCEAN) {
			while (bBox.minY > 60 && world.getBlockMaterial(bBox.getCenterX(), bBox.minY, bBox.getCenterZ()) == Material.air) {
				bBox.offset(0, -1, 0);
			}
			while (bBox.minY > 16 && world.getBlockMaterial(bBox.getCenterX(), bBox.minY, bBox.getCenterZ()) == Material.water) {
				bBox.offset(0, -1, 0);
			}
			if (world.getBlockMaterial(bBox.getCenterX(), bBox.minY, bBox.getCenterZ()) != Material.water &&
				world.getBlockMaterial(bBox.getCenterX(), bBox.maxY, bBox.getCenterZ()) == Material.water) {
				inOcean = true;
				submerged = true;
				StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.water, 6, false, false);
				return true;
			}
		}

		return false;
	}
}

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
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.DungeonLootLists;

public class RoomSecret extends RoomBase
{
	/** Max height any dungeon can reach */
	protected static final int MAX_HEIGHT = 5;
	
	/** Block which will be placed as a door, if any */
	private Block door = null;
	
	/** Side of the structure that the door is on */
	private int side;

	/**
	 * Creates a new secret room object ready for generation
	 * @param size minimum size of 3
	 * @param blockRequired the block that must make up the majority of the structure's volume
	 */
	public RoomSecret(int chunkX, int chunkZ, int size, int blockRequired) {
		super(chunkX, chunkZ, size, MAX_HEIGHT, blockRequired);
	}
	
	@Override
	public boolean generate(ZSSMapGenBase mapGen, World world, Random rand, int x, int y, int z) {
		if (y < bBox.maxY) { return false; }
		inNether = (world.provider.dimensionId == -1);
		bBox.offset(x, y - bBox.maxY, z);
		int worldHeight = (inNether ? 128 : world.getHeightValue(bBox.getCenterX(), bBox.getCenterZ()));
		if (bBox.maxY > worldHeight) {
			bBox.offset(0, worldHeight - bBox.maxY - 1, 0);
		}
		if (!validateTopLayer(world) && !placeInOcean(world, true)) {
			return false;
		} else if (inNether && submerged && !placeInNether(world)) {
			return false;
		}

		StructureGenUtils.adjustForAir(world, this, bBox);
		checkSpecialCases(world, rand);
		setMetadata(world, x, z);
		
		int range = (inOcean ? Config.getMinOceanDistance() : inNether ? Config.getNetherMinDistance() : Config.getMinLandDistance());
		if (!mapGen.areStructuresWithinRange(this, range) && isWellHidden(world) && canGenerate(world)) {
			doStandardRoomGen(world, rand);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	protected void setMetadata(World world, int x, int z) {
		BossType type = BossType.getBossType(world, x, z);
		boolean inWater = inOcean || StructureGenUtils.getNumBlocksOfMaterial(world, bBox, Material.water, 1) > 0;
		if (type != null) {
			switch(type) {
			case HELL: metadata = 2; break; // nether brick
			case OCEAN: metadata = (inWater ? 1 : 0); break; // sandstone or stone
			default: metadata = 0;
			}
		} else {
			//BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
			metadata = (inWater ? 1 : 0); // biome != null && biome.biomeName.toLowerCase().contains("beach") && 
		}
		if (door != null) {
			metadata |= 8;
		}
	}
	
	@Override
	protected void decorateDungeon(World world, Random rand) {
		if (door != null) { placeDoor(world, rand); }
		doChestGen(world, rand);
		placeDungeonCore(world);
		placeJars(world, rand);
	}

	@Override
	protected void placeDungeonCore(World world) {
		StructureGenUtils.setBlockAtPosition(world, bBox, bBox.getXSize() / 2, 0, bBox.getZSize() / 2, ZSSBlocks.dungeonCore.blockID, getMetadata() | 0x8);
		int x = StructureGenUtils.getXWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		int y = StructureGenUtils.getYWithOffset(bBox, 0);
		int z = StructureGenUtils.getZWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonCore) {
			((TileEntityDungeonCore) te).setDungeonBoundingBox(bBox);
			if (door != null) {
				((TileEntityDungeonCore) te).setDoor(door, side);
			}
			if (!inNether && submerged && !inLava && !inOcean && bBox.getXSize() > 4) {
				if (inMountain || world.rand.nextFloat() < Config.getFairySpawnerChance()) {
					((TileEntityDungeonCore) te).setSpawner();
				}
			}
		}
	}
	
	/**
	 * Makes final checks for submerged, lava, fairy spawners, etc. and adjusts bounding box minY if needed
	 */
	private void checkSpecialCases(World world, Random rand) {
		BiomeGenBase biome = world.getBiomeGenForCoords(bBox.getCenterX(), bBox.getCenterZ());
		boolean flag = (!submerged && bBox.maxY > 64 && biome != null);
		if (inNether && !inLava) {
			if (rand.nextFloat() < 0.25F) {
				submerged = true;
				inLava = true;
			}
		} else if (flag && (biome.biomeName.toLowerCase().contains("hill") || biome.biomeName.toLowerCase().contains("mountain"))) {
			submerged = rand.nextFloat() < Config.getFairySpawnerChance();
			inMountain = true;
		}
		if (submerged && bBox.getXSize() > 3) {
			--bBox.minY;
		}
		if (bBox.getYSize() == MAX_HEIGHT && rand.nextInt(8) == 0) {
			door = (rand.nextInt(8) == 0 ? ZSSBlocks.barrierHeavy : ZSSBlocks.barrierLight);
			side = rand.nextInt(4);
		}
	}

	/**
	 * Determines location(s) for chest(s), as well as number of items to generate
	 */
	private void doChestGen(World world, Random rand) {
		int rX = bBox.getXSize() - 2;
		int rY = (inLava && bBox.getYSize() > 3 ? 2 : 1);
		int rZ = bBox.getZSize() - 2;
		generateChestContents(world, rand, rand.nextInt(rX) + 1, rY, rand.nextInt(rZ) + 1, true);
		if (bBox.getXSize() > 5 && rand.nextFloat() < Config.getDoubleChestChance()) {
			generateChestContents(world, rand, rand.nextInt(rX) + 1, rY, rand.nextInt(rZ) + 1, false);
		}
	}

	/**
	 * Places a chest and generates items at x/y/z in the structure, returning true if successful
	 * @param first true for first chest generated
	 */
	private boolean generateChestContents(World world, Random rand, int x, int y, int z, boolean first) {
		int i1 = StructureGenUtils.getXWithOffset(bBox, x, z);
		int j1 = StructureGenUtils.getYWithOffset(bBox, y);
		int k1 = StructureGenUtils.getZWithOffset(bBox, x, z);
		if (bBox.isVecInside(i1, j1, k1) && !StructureGenUtils.isBlockChest(world, i1, j1, k1)) {
			Block chestBlock = (rand.nextFloat() < Config.getLockedChestChance() ? ZSSBlocks.chestLocked : Block.chest);
			world.setBlock(i1, j1, k1, chestBlock.blockID, 0, 2);
			TileEntity te = world.getBlockTileEntity(i1, j1, k1);
			if (te instanceof IInventory) {
				IInventory chest = (IInventory) te;
				DungeonLootLists.generateChestContents(world, rand, chest, this, chestBlock == ZSSBlocks.chestLocked);
				if (first && rand.nextFloat() < Config.getHeartPieceChance()) {
					WorldUtils.addItemToInventoryAtRandom(rand, new ItemStack(ZSSItems.heartPiece), chest, 3);
				}
				// TODO make a config value for special loot chance
				if (door != null) {
					ItemStack loot = ChestGenHooks.getInfo(DungeonLootLists.BOSS_LOOT).getOneItem(rand);
					// TODO add wooden and rusty pegs
					if (door == ZSSBlocks.barrierLight) {
						if (rand.nextFloat() < 0.25F) {
							// TODO remove golden gauntlets - they should be availabe in the rusty peg rooms
							loot = new ItemStack(ZSSItems.gauntletsGolden);
						} else if (rand.nextFloat() < 0.25F) {
							loot = new ItemStack(ZSSItems.hammerSkull);
						}
					} else if (door == ZSSBlocks.barrierHeavy) {
						if (rand.nextFloat() < 0.25F) {
							loot = new ItemStack(ZSSItems.hammerMegaton);
						}
					}
					if (loot != null) {
						WorldUtils.addItemToInventoryAtRandom(rand, loot, chest, 3);
					}
				}
			}
			// this should set the block underneath the chest as stone if it's surrounded by lava
			if (world.getBlockMaterial(i1, j1 - 1, k1) == Material.lava && bBox.getYSize() > 3) {
				world.setBlock(i1, j1 - 1, k1, ZSSBlocks.secretStone.blockID, getMetadata(), 2);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Actually places the door; use after determining door side for best results
	 */
	private void placeDoor(World world, Random rand) {
		int x = bBox.getCenterX();
		int y = bBox.minY + (submerged ? 2 : 1);
		int z = bBox.getCenterZ();
		switch(side % 4) {
		case SOUTH: z = bBox.maxZ; break;
		case NORTH: z = bBox.minZ; break;
		case EAST: x = bBox.maxX; break;
		case WEST: x = bBox.minX; break;
		}
		world.setBlock(x, y, z, door.blockID, 0, 2);
		world.setBlock(x, y + 1, z, door.blockID, 0, 2);
	}
	
	/**
	 * Adds some random breakable jars, if possible
	 */
	private void placeJars(World world, Random rand) {
		int size = bBox.getXSize();
		if (size > 4) {
			int n = rand.nextInt(size - 3);
			for (int i = 0; i < n; ++i) {
				int x = bBox.minX + rand.nextInt(size);
				int y = bBox.minY + 1;
				int z = bBox.minZ + rand.nextInt(bBox.getZSize());
				Material m = world.getBlockMaterial(x, y, z);
				if (m == Material.air || m.isLiquid()) {
					world.setBlock(x, y, z, ZSSBlocks.ceramicJar.blockID);
				}
			}
		}
	}
}

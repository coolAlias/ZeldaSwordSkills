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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.block.BlockDoorLocked;
import zeldaswordskills.block.BlockHeavy;
import zeldaswordskills.block.BlockPeg;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.BlockTime;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
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

	/** Metadata value for the door variant, if any */
	private int doorMeta;

	/** Side of the structure that the door is on */
	private EnumFacing face = EnumFacing.EAST;

	/**
	 * Creates a new secret room object ready for generation
	 * @param size minimum size of 3
	 * @param blockRequired the block that must make up the majority of the structure's volume
	 */
	public RoomSecret(int chunkX, int chunkZ, int size, Block blockRequired) {
		super(chunkX, chunkZ, size, MAX_HEIGHT, blockRequired);
	}

	@Override
	public boolean generate(ZSSMapGenBase mapGen, World world, Random rand, int x, int y, int z) {
		if (y < bBox.maxY) {
			return false;
		}
		inNether = (world.provider.getDimensionName().equals("Nether"));
		bBox.offset(x, y - bBox.maxY, z);
		Vec3i center = bBox.getCenter();
		int worldHeight = (inNether ? 128 : world.getHeight(new BlockPos(center.getX(), bBox.minY, center.getZ())).getY());
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
		setMetadata(world, new BlockPos(bBox.getCenter()));
		int range = (inOcean ? Config.getMinOceanDistance() : inNether ? Config.getNetherMinDistance() : Config.getMinLandDistance());
		if (!mapGen.areStructuresWithinRange(this, range) && isWellHidden(world) && canGenerate(world)) {
			doStandardRoomGen(world, rand);
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void setMetadata(World world, BlockPos pos) {
		BossType type = (Config.areBossDungeonsRandom() ? null : BossType.getBossType(world, pos));
		boolean inWater = inOcean || StructureGenUtils.getNumBlocksOfMaterial(world, bBox, Material.water, 1) > 0;
		if (type != null) {
			switch(type) {
			case HELL: metadata = 2; break; // nether brick
			case OCEAN: metadata = (inWater ? 6 : 0); break; // gravel or stone
			default: metadata = 0;
			}
		} else if (world.provider.getDimensionName().equals("Nether")) {
			metadata = 2; // nether brick
		} else {
			metadata = (inWater ? 6 : 0); 
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
		StructureGenUtils.setBlockAtPosition(world, bBox, bBox.getXSize() / 2, 0, bBox.getZSize() / 2, ZSSBlocks.dungeonCore.getStateFromMeta(getMetadata() | 0x8));
		int x = StructureGenUtils.getXWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		int y = StructureGenUtils.getYWithOffset(bBox, 0);
		int z = StructureGenUtils.getZWithOffset(bBox, bBox.getXSize() / 2, bBox.getZSize() / 2);
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (te instanceof TileEntityDungeonCore) {
			TileEntityDungeonCore core = (TileEntityDungeonCore) te;
			core.setRenderState(BlockSecretStone.EnumType.byMetadata(getMetadata()).getDroppedBlock().getDefaultState());
			core.setDungeonBoundingBox(bBox);
			if (door != null) {
				core.setDoor(door, doorMeta, face);
			}
			if (!inNether && submerged && !inLava && !inOcean && bBox.getXSize() > 4) {
				if (inMountain || world.rand.nextFloat() < Config.getFairySpawnerChance()) {
					core.setSpawner();
				}
			}
		}
	}

	/**
	 * Makes final checks for submerged, lava, fairy spawners, etc. and adjusts bounding box minY if needed
	 */
	private void checkSpecialCases(World world, Random rand) {
		BiomeGenBase biome = world.getBiomeGenForCoords(new BlockPos(bBox.getCenter()));
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
		if (bBox.getXSize() > 5 && rand.nextFloat() < Config.getBarredRoomChance()) {
			setDoor(rand);
			face = EnumFacing.Plane.HORIZONTAL.random(rand);
		}
	}

	/**
	 * Called for barred rooms to randomly determine what type of door to use
	 */
	protected void setDoor(Random rand) {
		if (rand.nextInt(16) == 0) {
			if (submerged && rand.nextInt(3) == 0) {
				door = ZSSBlocks.doorLocked;
			} else {
				door = ZSSBlocks.timeBlock;
				doorMeta = BlockTime.EnumType.TIME.getMetadata();
			}
		} else if (!submerged) {
			if (rand.nextInt(3) == 0) {
				if (rand.nextInt(3) == 0) {
					door = ZSSBlocks.heavyBlock;
					doorMeta = BlockHeavy.EnumType.HEAVY.getMetadata();
				} else {
					door = ZSSBlocks.pegRusty;
				}
			} else if (rand.nextInt(3) == 0) {
				door = ZSSBlocks.doorLocked;
			} else if (rand.nextInt(3) == 0) {
				door = ZSSBlocks.heavyBlock;
				doorMeta = BlockHeavy.EnumType.LIGHT.getMetadata();
			} else {
				door = ZSSBlocks.pegWooden;
			}
		}
	}

	/**
	 * Determines location(s) for chest(s), as well as number of items to generate
	 */
	private void doChestGen(World world, Random rand) {
		int rX = bBox.getXSize() - 2;
		int rY = (inLava && bBox.getYSize() > 3 ? 2 : 1);
		int rZ = bBox.getZSize() - 2;
		if (door instanceof BlockPeg) {
			switch(face) {
			case SOUTH: rX = 1; break;
			case NORTH: rX = rZ = 1; break;
			case EAST: rZ = 1; break;
			case WEST: rX = rZ = 1; break;
			default: // UP and DOWN are not possible
			}
			generateChestContents(world, rand, rX, rY, rZ, true);
		} else {
			generateChestContents(world, rand, rand.nextInt(rX) + 1, rY, rand.nextInt(rZ) + 1, true);
			if (bBox.getXSize() > 5 && rand.nextFloat() < Config.getDoubleChestChance()) {
				generateChestContents(world, rand, rand.nextInt(rX) + 1, rY, rand.nextInt(rZ) + 1, false);
			}
		}
	}

	/**
	 * Places a chest and generates items at x/y/z in the structure, returning true if successful
	 * @param first true for first chest generated
	 */
	private boolean generateChestContents(World world, Random rand, int x, int y, int z, boolean first) {
		int i = StructureGenUtils.getXWithOffset(bBox, x, z);
		int j = StructureGenUtils.getYWithOffset(bBox, y);
		int k = StructureGenUtils.getZWithOffset(bBox, x, z);
		BlockPos pos = new BlockPos(i, j, k);
		if (bBox.isVecInside(new Vec3i(i, j, k)) && !StructureGenUtils.isBlockChest(world, pos)) {
			Block chestBlock = (rand.nextFloat() < Config.getLockedChestChance() ? ZSSBlocks.chestLocked : Blocks.chest);
			if (door != ZSSBlocks.timeBlock && !first && rand.nextFloat() < Config.getLockedChestChance()) {
				chestBlock = ZSSBlocks.chestInvisible;
			}
			placeChest(world, pos, chestBlock);
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory) {
				IInventory chest = (IInventory) te;
				DungeonLootLists.generateChestContents(world, rand, chest, this, chestBlock != Blocks.chest);
				if ((first || chestBlock == ZSSBlocks.chestInvisible) && rand.nextFloat() < Config.getHeartPieceChance()) {
					WorldUtils.addItemToInventoryAtRandom(rand, new ItemStack(ZSSItems.heartPiece), chest, 3);
				}
				if (door != null) {
					ItemStack loot = ChestGenHooks.getInfo(DungeonLootLists.BOSS_LOOT).getOneItem(rand);
					if (rand.nextFloat() < 0.0625F * (1.0F / Math.max(Config.getBarredRoomChance(), 0.1F))) {
						if (door == ZSSBlocks.pegWooden) {
							loot = new ItemStack(ZSSItems.gauntletsSilver);
						} else if (door == ZSSBlocks.heavyBlock && BlockHeavy.EnumType.byMetadata(doorMeta) == BlockHeavy.EnumType.LIGHT) {
							loot = new ItemStack(ZSSItems.hammerSkull);
						} else if (door == ZSSBlocks.pegRusty) {
							loot = new ItemStack(ZSSItems.gauntletsGolden);
						} else if (door == ZSSBlocks.heavyBlock && BlockHeavy.EnumType.byMetadata(doorMeta) == BlockHeavy.EnumType.HEAVY) {
							loot = new ItemStack(ZSSItems.hammerMegaton);
						} else if (door == ZSSBlocks.timeBlock) {
							// TODO
						}
					}
					if (loot != null) {
						WorldUtils.addItemToInventoryAtRandom(rand, loot, chest, 3);
					}
				}
			}
			// this should set the block underneath the chest as stone if it's surrounded by lava
			if (world.getBlockState(pos.down()).getBlock().getMaterial() == Material.lava && bBox.getYSize() > 3) {
				world.setBlockState(pos.down(), ZSSBlocks.secretStone.getStateFromMeta(getMetadata()), 2);
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
		Vec3i center = bBox.getCenter();
		int x = center.getX();
		int y = bBox.minY + (submerged ? 2 : 1);
		int z = center.getZ();
		switch(face) {
		case SOUTH: z = bBox.maxZ; break;
		case NORTH: z = bBox.minZ; break;
		case EAST: x = bBox.maxX; break;
		case WEST: x = bBox.minX; break;
		default: // UP and DOWN not possible
		}
		world.setBlockState(new BlockPos(x, y, z), door.getDefaultState(), 2);
		doorMeta = (door instanceof BlockDoorLocked ? (doorMeta | 8) : doorMeta); // upper part of door
		world.setBlockState(new BlockPos(x, y + 1, z), (door instanceof BlockPeg ? Blocks.air.getDefaultState() : door.getStateFromMeta(doorMeta)), 2);
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
				BlockPos pos = new BlockPos(x, y, z);
				Material m = world.getBlockState(pos).getBlock().getMaterial();
				if (m == Material.air || m.isLiquid()) {
					world.setBlockState(pos, ZSSBlocks.ceramicJar.getDefaultState());
				}
			}
		}
	}
}

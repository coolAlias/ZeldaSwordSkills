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
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.BossType;
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
	/** Side of the structure in which the door is located; will not be null after dungeon initialized */
	private EnumFacing doorSide = null;

	/** The type of dungeon this is, as determined by biome using BossType.getBossType */
	private final BossType type;

	public RoomBoss(BossType bossType, int chunkX, int chunkZ, Random rand, int size, Block blockRequired) {
		super(chunkX, chunkZ, Math.max(size, 9), (rand.nextInt(4) + 7), blockRequired);
		isLocked = true;
		type = bossType;
	}

	/** Returns the BossType for this Boss Room */
	public BossType getBossType() {
		return type;
	}

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
		if (type == null || y < bBox.getYSize() || y > (world.provider.getDimensionId() == -1 ? 96 : 160)) {
			return false;
		}
		// allow all boss types to potentially generate in the Nether (for randomized locations config option)
		if (world.provider.getDimensionId() == -1) {
			inNether = true;
			if (!placeInNether(world)) {
				return false;
			}
		}

		switch(type) {
		case HELL:
			doDefaultAdjustments(world);
			if (world.rand.nextFloat() < 0.75F) {
				submerged = true;
				inLava = true;
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
		setMetadata(world, new BlockPos(x, bBox.minY, z));
		boolean flag = StructureGenUtils.getAverageDistanceToGround(world, bBox, 6) < 4;
		return (doorSide != null && flag && (submerged || !isWaterAroundOrUnder(world)));
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
	protected void setMetadata(World world, BlockPos pos) {
		metadata = type.metadata;
	}

	/**
	 * Adds the final touches: chests, dungeon core, pedestal, etc.
	 */
	protected void decorateDungeon(World world, Random rand) {
		int meta = getMetadata();
		StructureGenUtils.fillDown(world, bBox, BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock().getDefaultState());
		placeDoor(world);
		placeDungeonCore(world);
		placePillars(world, meta);
		placeCenterPiece(world, rand, meta);
		placeChandelier(world);
		placeParapet(world, meta);
		placeLedge(world, rand, meta);
		placeChestOnRoof(world, rand);
		placeInvisibleChest(world, rand, rand.nextInt(3)); // very rarely may get 2 invisible chests
		placeJars(world, rand, rand.nextInt(5), false);
		placeJars(world, rand, rand.nextInt(5) + 3, true);
		placeWindows(world);
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
			core.setBossType(type);
			core.setDoor(ZSSBlocks.doorBoss, type.ordinal(), doorSide);
		}
	}

	/**
	 * Determines which side is most suitable for the door
	 */
	protected void determineDoorSide(World world) {
		Vec3i center = bBox.getCenter();
		int x = center.getX();
		int y = bBox.minY + 1;
		int z = center.getZ();
		int dx, dz; 
		doorSide = EnumFacing.Plane.HORIZONTAL.random(world.rand);
		for (int i = 0; i < 4; ++i) {
			dx = x;
			dz = z;
			switch(doorSide) {
			case SOUTH: dz = bBox.maxZ + 1; break;
			case NORTH: dz = bBox.minZ - 1; break;
			case EAST: dx = bBox.maxX + 1; break;
			case WEST: dx = bBox.maxX - 1; break;
			default: ZSSMain.logger.warn(String.format("Invalid boss door side %d at %d/%d/%d", doorSide, x, y, z));
			}
			Block block1 = world.getBlockState(new BlockPos(dx, y, dz)).getBlock();
			Block block2 = world.getBlockState(new BlockPos(dx, y + 1, dz)).getBlock();
			if (!block1.isFullBlock() && !block2.isFullBlock()) {
				return; // the blocks in front of the door are not full blocks, this is a good side for the door
			}
			doorSide = doorSide.rotateY();
		}
		doorSide = null;
	}

	/**
	 * Actually places the door; use after determining door side for best results
	 */
	protected void placeDoor(World world) {
		Vec3i center = bBox.getCenter();
		int x = center.getX();
		int y = bBox.minY + (submerged ? 2 : 1);
		int z = center.getZ();
		switch(doorSide) {
		case SOUTH: z = bBox.maxZ; break;
		case NORTH: z = bBox.minZ; break;
		case EAST: x = bBox.maxX; break;
		case WEST: x = bBox.minX; break;
		default: ZSSMain.logger.warn("Placing Boss door with invalid door side");
		}
		world.setBlockState(new BlockPos(x, y, z), ZSSBlocks.doorBoss.getStateFromMeta(type.ordinal() & ~0x8), 2);
		world.setBlockState(new BlockPos(x, y + 1, z), ZSSBlocks.doorBoss.getStateFromMeta(type.ordinal() | 0x8), 2);
	}

	/**
	 * Places the center half-slabs and block for either a chest or a pedestal
	 */
	protected void placeCenterPiece(World world, Random rand, int meta) {
		int minX = bBox.getXSize() / 2 - 1;
		int minY = 1;
		int minZ = bBox.getZSize() / 2 - 1;
		if (submerged) {
			StructureGenUtils.fillWithBlocks(world, bBox, minX, minX + 3, minY, minY + 1, minZ, minZ + 3, BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock().getDefaultState());
			++minY;
		}
		if (!inOcean) {
			StructureGenUtils.fillWithBlocks(world, bBox, minX, minX + 3, minY, minY + 1, minZ, minZ + 3, BlockSecretStone.EnumType.byMetadata(meta).getSlab());
		}
		Vec3i center = bBox.getCenter();
		world.setBlockState(new BlockPos(center.getX(), bBox.minY + (submerged && !inOcean ? 2 : 1), center.getZ()), (type == BossType.TAIGA ? Blocks.quartz_block.getDefaultState() : BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock().getDefaultState()), 2);
		placeHinderBlock(world);
		boolean hasChest = false;
		switch(type) {
		case DESERT: // fall through
		case OCEAN: // fall through
		case MOUNTAIN: placeMainChest(world, rand, true); hasChest = true; break;
		case FOREST:
			if (rand.nextFloat() < Config.getMasterSwordChance()) {
				placePedestal(world, minY + 1);
			} else { // chance of double chest
				placeMainChest(world, rand, true);
				hasChest = !(rand.nextFloat() < (2 * Config.getDoubleChestChance()));
			}
			break;
		case HELL: placeFlame(world, BlockSacredFlame.EnumType.DIN); break;
		case SWAMP: placeFlame(world, BlockSacredFlame.EnumType.FARORE); break;
		case TAIGA: placeFlame(world, BlockSacredFlame.EnumType.NAYRU); break;
		default:
		}
		if (!hasChest) {
			placeMainChest(world, rand, false);
		}
	}

	/**
	 * Places the hanging chandelier only if height is sufficient
	 */
	protected void placeChandelier(World world) {
		if (bBox.getYSize() > 7) {
			Vec3i center = bBox.getCenter();
			int x = center.getX();
			int y = bBox.maxY - 1;
			int z = center.getZ();
			switch(type) {
			case OCEAN:
				world.setBlockState(new BlockPos(x + 1, y, z + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y, z - 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y, z + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y, z - 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x, y, z), Blocks.glowstone.getDefaultState(), 2);
				break;
			case SWAMP:
				world.setBlockState(new BlockPos(bBox.minX + 1, y, bBox.minZ + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(bBox.minX + 1, y, bBox.maxZ - 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(bBox.maxX - 1, y, bBox.minZ + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(bBox.maxX - 1, y, bBox.maxZ - 1), Blocks.glowstone.getDefaultState(), 2);
				break;
			default:
				world.setBlockState(new BlockPos(x, y, z), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y, z), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y, z), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x, y, z + 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x, y, z - 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y, z + 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y, z - 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y, z + 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y, z - 1), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x, y - 1, z), Blocks.oak_fence.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y - 1, z + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x + 1, y - 1, z - 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y - 1, z + 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x - 1, y - 1, z - 1), Blocks.glowstone.getDefaultState(), 2);
				world.setBlockState(new BlockPos(x, y - 2, z), Blocks.glowstone.getDefaultState(), 2);
			}
		}
	}

	/**
	 * Places and generates the contents of this dungeon's main chest
	 */
	protected void placeMainChest(World world, Random rand, boolean inCenter) {
		Vec3i center = bBox.getCenter();
		int x = (inCenter ? center.getX() : (rand.nextFloat() < 0.5F ? bBox.minX + 1 : bBox.maxX - 1));
		int y = bBox.minY + (inCenter ? 2 : 1) + (submerged && !inOcean ? 1 : 0);
		int z = (inCenter ? center.getZ() : (rand.nextFloat() < 0.5F ? bBox.minZ + 1 : bBox.maxZ - 1));
		BlockPos pos = new BlockPos(x, y, z);
		Block chest = (inCenter ? Blocks.chest : ZSSBlocks.chestLocked);
		placeChest(world, pos, chest);
		if (inCenter) {
			world.setBlockState(pos, chest.getStateFromMeta(doorSide.getIndex()), 2);
		} else if (submerged && !inOcean) {
			world.setBlockState(pos.down(), BlockSecretStone.EnumType.byMetadata(getMetadata()).getDroppedBlock().getDefaultState(), 2);
		}
		TileEntity te = world.getTileEntity(pos);
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
			placeChestAt(world, new BlockPos(x, y, z), Blocks.chest, rand, true);
		}
	}

	/**
	 * Attempts to place n invisible chests, either on the roof or in the main room
	 */
	protected void placeInvisibleChest(World world, Random rand, int n) {
		for (int i = 0; i < n; ++i) {
			if (rand.nextFloat() < Config.getDoubleChestChance()) {
				int x = bBox.minX + rand.nextInt(bBox.getXSize());
				int y = (rand.nextFloat() < 0.5F ? bBox.minY + 1 : bBox.maxY + 1);
				int z = bBox.minZ + rand.nextInt(bBox.getZSize());
				if (bBox.isVecInside(new Vec3i(x, y, z))) {
					placeChestAt(world, new BlockPos(x, y, z), ZSSBlocks.chestInvisible, rand, true);
				}
			}
		}
	}

	/**
	 * Places the chest block at the given coordinates provided no other block exists,
	 * and populates the chest with loot (locked level loot if goodLoot is true)
	 */
	protected void placeChestAt(World world, BlockPos pos, Block chest, Random rand, boolean goodLoot) {
		if (world.isAirBlock(pos)) {
			placeChest(world, pos, chest);
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory) {
				DungeonLootLists.generateChestContents(world, rand, (IInventory) te, this, goodLoot);
			}
		}
	}

	/**
	 * Places the designated Sacred Flame on the center block
	 */
	protected void placeFlame(World world, BlockSacredFlame.EnumType flame) {
		Vec3i center = bBox.getCenter();
		world.setBlockState(new BlockPos(center.getX(), bBox.minY + (submerged ? 3 : 2), center.getZ()), ZSSBlocks.sacredFlame.getDefaultState().withProperty(BlockSacredFlame.VARIANT, flame), 2);
	}

	/**
	 * Places a secret stone block between the chest and door to prevent early looting
	 */
	protected void placeHinderBlock(World world) {
		Vec3i center = bBox.getCenter();
		int x = center.getX() + doorSide.getFrontOffsetX();
		int y = bBox.minY + (submerged && !inOcean ? 3 : 2);
		int z = center.getZ() + doorSide.getFrontOffsetZ();
		world.setBlockState(new BlockPos(x, y, z), ZSSBlocks.secretStone.getStateFromMeta(getMetadata()), 2);
	}

	/**
	 * Adds n random breakable jars, either on the floor or on the roof
	 */
	protected void placeJars(World world, Random rand, int n, boolean onRoof) {
		for (int i = 0; i < n; ++i) {
			int x = bBox.minX + rand.nextInt(bBox.getXSize());
			int y = (onRoof ? bBox.maxY : bBox.minY) + 1;
			int z = bBox.minZ + rand.nextInt(bBox.getZSize());
			BlockPos pos = new BlockPos(x, y, z);
			Material m = world.getBlockState(pos).getBlock().getMaterial();
			if (m == Material.air || m.isLiquid()) {
				world.setBlockState(pos, ZSSBlocks.ceramicJar.getDefaultState());
			}
		}
	}

	/**
	 * Places a one-block wide ledge around the inside wall perimeter at centerY only
	 * if certain conditions are met (sufficient y size, random)
	 */
	protected void placeLedge(World world, Random rand, int meta) {
		if (type == BossType.OCEAN || type == BossType.MOUNTAIN) {
			return;
		}
		if (bBox.getYSize() > 7 && rand.nextFloat() < (type == BossType.HELL ? 0.75F : 0.5F)) {
			int y = bBox.getCenter().getY(); // centerY
			Block block = BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock();
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 1, bBox.minX + 2, y, y + 1, bBox.minZ + 1, bBox.maxZ, block.getDefaultState(), 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.maxX - 1, bBox.maxX, y, y + 1, bBox.minZ + 1, bBox.maxZ, block.getDefaultState(), 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 2, bBox.maxX - 1, y, y + 1, bBox.minZ + 1, bBox.minZ + 2, block.getDefaultState(), 3);
			StructureGenUtils.fillWithoutReplace(world, bBox.minX + 2, bBox.maxX - 1, y, y + 1, bBox.maxZ - 1, bBox.maxZ, block.getDefaultState(), 3);
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
		Block block = BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock();
		Block stairs = BlockSecretStone.EnumType.byMetadata(meta).getStairBlock();
		for (int i = x1; i <= x2; ++i) {
			world.setBlockState(new BlockPos(i, y, z1), stairs.getStateFromMeta(6), 3);
			world.setBlockState(new BlockPos(i, y + 1, z1), block.getDefaultState(), 2);
			world.setBlockState(new BlockPos(i, y, z2), stairs.getStateFromMeta(7), 3);
			world.setBlockState(new BlockPos(i, y + 1, z2), block.getDefaultState(), 2);
			if (i % 2 == 0) {
				world.setBlockState(new BlockPos(i, y + 2, z1), block.getDefaultState(), 2);
				world.setBlockState(new BlockPos(i, y + 2, z2), block.getDefaultState(), 2);
			}
		}
		for (int i = z1; i <= z2; ++i) {
			world.setBlockState(new BlockPos(x1, y, i), stairs.getStateFromMeta(4), 3);
			world.setBlockState(new BlockPos(x1, y + 1, i), block.getDefaultState(), 2);
			world.setBlockState(new BlockPos(x2, y, i), stairs.getStateFromMeta(5), 3);
			world.setBlockState(new BlockPos(x2, y + 1, i), block.getDefaultState(), 2);
			if (i % 2 == 0) {
				if (world.getBlockState(new BlockPos(x1 + 1, y + 2, i)).getBlock() != block) {
					world.setBlockState(new BlockPos(x1, y + 2, i), block.getDefaultState(), 2);
				}
				if (world.getBlockState(new BlockPos(x2 - 1, y + 2, i)).getBlock() != block) {
					world.setBlockState(new BlockPos(x2, y + 2, i), block.getDefaultState(), 2);
				}
			}
		}
	}

	/**
	 * Places pillars in the four corners of the dungeon with chance based on room size
	 */
	protected void placePillars(World world, int meta) {
		if (type == BossType.DESERT || type == BossType.SWAMP || type == BossType.MOUNTAIN) {
			return;
		}
		if (world.rand.nextFloat() < (bBox.getXSize() * 0.06F)) {
			Vec3i center = bBox.getCenter();
			int offset = (bBox.getXSize() < 11 ? 2 : 3);
			int x1 = (bBox.getXSize() < 11 ? center.getX() : bBox.minX) + offset;
			int x2 = (bBox.getXSize() < 11 ? center.getX() : bBox.maxX) - offset;
			int z1 = (bBox.getZSize() < 11 ? center.getZ() : bBox.minZ) + offset;
			int z2 = (bBox.getZSize() < 11 ? center.getZ() : bBox.maxZ) - offset;
			IBlockState state = BlockSecretStone.EnumType.byMetadata(meta).getDroppedBlock().getDefaultState();
			for (int y = bBox.minY + 1; y < bBox.maxY; ++y) {
				world.setBlockState(new BlockPos(x1, y, z1), state, 2);
				world.setBlockState(new BlockPos(x1, y, z2), state, 2);
				world.setBlockState(new BlockPos(x2, y, z1), state, 2);
				world.setBlockState(new BlockPos(x2, y, z2), state, 2);
			}
			if (bBox.getXSize() > 10 && world.rand.nextFloat() < 0.5F) {
				placePillarLandings(world, state, new BlockPos(x1, center.getY(), z1), 0);
				placePillarLandings(world, state, new BlockPos(x2, center.getY(), z1), 1);
				placePillarLandings(world, state, new BlockPos(x1, center.getY(), z2), 2);
				placePillarLandings(world, state, new BlockPos(x2, center.getY(), z2), 3);
			}
		}
	}

	/**
	 * Places one-block landings on two sides of the pillar, depending on facing
	 * @param corner 0 NW, 1 NE, 2 SW, 3 SE
	 */
	protected void placePillarLandings(World world, IBlockState state, BlockPos pos, int corner) {
		switch(corner % 4) {
		case 0:
			world.setBlockState(pos.east(), state);
			world.setBlockState(pos.south(), state);
			break;
		case 1:
			world.setBlockState(pos.west(), state);
			world.setBlockState(pos.south(), state);
			break;
		case 2:
			world.setBlockState(pos.east(), state);
			world.setBlockState(pos.north(), state);
			break;
		case 3:
			world.setBlockState(pos.west(), state);
			world.setBlockState(pos.north(), state);
		}
	}

	/**
	 * Places 'windows' at regular intervals in the walls
	 */
	protected void placeWindows(World world) {
		if (Config.areWindowsEnabled() && world.rand.nextFloat() < (bBox.getXSize() * 0.06F)) {
			int interval = (bBox.getXSize() % 2 == 1 ? 2 : 3);
			int j = bBox.getCenter().getY() + 1;
			Block window = (world.rand.nextFloat() < 0.25F ? Blocks.iron_bars : Blocks.air);
			boolean hasVines = (type == BossType.FOREST || type == BossType.SWAMP);
			for (int i = bBox.minX + 1; i < bBox.maxX; ++i) {
				if (i % interval == 0) {
					world.setBlockState(new BlockPos(i, j, bBox.minZ), window.getDefaultState(), 2);
					world.setBlockState(new BlockPos(i, j, bBox.maxZ), window.getDefaultState(), 2);
					if (hasVines) {
						if (world.rand.nextFloat() < 0.25F) {
							placeVines(world, new BlockPos(i, j - 1, bBox.minZ - 1), 1);
						}
						if (world.rand.nextFloat() < 0.25F) {
							placeVines(world, new BlockPos(i, j - 1, bBox.maxZ + 1), 4);
						}
					}
				}
			}
			for (int k = bBox.minZ + 1; k < bBox.maxZ; ++k) {
				if (k % interval == 0) {
					world.setBlockState(new BlockPos(bBox.minX, j, k), window.getDefaultState(), 2);
					world.setBlockState(new BlockPos(bBox.maxX, j, k), window.getDefaultState(), 2);
					if (hasVines) {
						if (world.rand.nextFloat() < 0.25F) {
							placeVines(world, new BlockPos(bBox.minX - 1, j - 1, k), 8);
						}
						if (world.rand.nextFloat() < 0.25F) {
							placeVines(world, new BlockPos(bBox.maxX + 1, j - 1, k), 2);
						}
					}
				}
			}
		}
	}

	/**
	 * Places a random number of vines with the given metadata facing starting from y and moving downward
	 */
	protected void placeVines(World world, BlockPos pos, int meta) {
		int n = world.rand.nextInt(5) + 1;
		for (int i = 0; i < n; ++i) {
			if (!world.isAirBlock(pos.down(i))) {
				break;
			}
			world.setBlockState(pos.down(i), Blocks.vine.getStateFromMeta(meta), 2);
		}
	}

	@Override
	protected boolean canReplaceBlockAt(int y, Block block) {
		boolean flag1 = (block != null && !block.getMaterial().blocksMovement());
		boolean flag2 = (block != null && block.getMaterial() == Material.leaves);
		boolean flag3 = (type == BossType.TAIGA && (block == Blocks.log || block == Blocks.log2 || (block != null && block.getMaterial() == Material.ice)));
		return (block == null || flag1 || flag2 || flag3 || super.canReplaceBlockAt(y, block));
	}

	@Override
	protected boolean placeInOcean(World world, boolean sink) {
		if (type == BossType.OCEAN) {
			Vec3i center = bBox.getCenter();
			while (bBox.minY > 60 && world.getBlockState(new BlockPos(center.getX(), bBox.minY, center.getZ())).getBlock().getMaterial() == Material.air) {
				bBox.offset(0, -1, 0);
			}
			while (bBox.minY > 16 && world.getBlockState(new BlockPos(center.getX(), bBox.minY, center.getZ())).getBlock().getMaterial() == Material.water) {
				bBox.offset(0, -1, 0);
			}
			if (world.getBlockState(new BlockPos(center.getX(), bBox.minY, center.getZ())).getBlock().getMaterial() != Material.water &&
					world.getBlockState(new BlockPos(center.getX(), bBox.maxY, center.getZ())).getBlock().getMaterial() == Material.water) {
				inOcean = true;
				submerged = true;
				StructureGenUtils.adjustCornersForMaterial(world, bBox, Material.water, 6, false, false);
				return true;
			}
		}
		return false;
	}
}

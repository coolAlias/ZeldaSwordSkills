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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;

public class BlockRotationData
{
	/** Valid rotation types. Each type is handled like vanilla blocks of this kind. */
	public static enum Rotation {
		/** 0 - north/south, 1 - east/west */
		ANVIL,
		/**
		 * 0x8 flags top, for which 0x1 flags the hinge direction;
		 * Facings (for bottom only): 0 - west, 1 - north, 2 - east, 3 - south, 0x4 flags door as open
		 */
		DOOR,
		/** Facings: 0 - south, 1 - west, 2 - north, 3 - east; e.g. beds, pumpkins, tripwire hooks */
		GENERIC,
		/**
		 * Most containers (chests, furnaces) use this, as well as pistons, ladders, and other things.
		 * Facings: 2 - north, 3 - south, 4 - west, 5 - east [for ladders and signs, they are attached to that side of the block]
		 */
		PISTON_CONTAINER,
		QUARTZ,
		RAIL,
		REPEATER,
		/**
		 * Marks the direction in which text / banners show. Increments are in 1/16 of a
		 * full circle, starting from south and moving clockwise as if looking at a compass.
		 * E.g., 0 is south, 1 is south-southwest, 2 is southwest, all the way up to 16 which is south-southeast
		 */
		SIGNPOST,
		SKULL,
		/** Ascends to the: 0 - east, 1 - west, 2 - south, 3 - north; 0x4 flags inverted stairs */
		STAIRS,
		/**
		 * Attached to wall: 0 - south, 1 - north, 2 - east, 3 - west
		 * 0x4 flags trapdoor as open, 0x8 flags trapdoor as being in top half of block
		 */
		TRAPDOOR,
		/** Side of block to which vine is anchored: 1 - south, 2 - west, 4 - north, 8 - east */
		VINE,
		/**
		 * Facings: 1 - east, 2 - west, 3 - south, 4 - north
		 * (button only: 0 - down, 5 - up)
		 */
		WALL_MOUNTED,
		/**
		 * Facings: 1 - east, 2 - west, 3 - south, 4 - north,
		 * 5 - north/south ground, 6 - east/west ground,
		 * 7 - north/south ceiling, 0 - east/west ceiling
		 * 0x8 flags power
		 */
		LEVER,
		/**
		 * 0-3 - wood type, 0x4 - east/west, 0x8 north/south;
		 * if neither 0x4 nor 0x8 are set, wood is up/down; if both are set, wood is all bark
		 */
		WOOD
	};

	/** A mapping of blocks to rotation type for handling rotation. Allows custom blocks to be added. */
	private static final Map<Block, Rotation> blockRotationData = new HashMap<Block, Rotation>();

	/**
	 * Returns the rotation type for the block given, or null if no type is registered
	 */
	public static final Rotation getBlockRotationType(Block block) {
		return blockRotationData.get(block);
	}

	/**
	 * Maps a block to a specified rotation type. Allows custom blocks to rotate with structure.
	 * @param block a valid block
	 * @param rotationType types predefined by enumerated type ROTATION
	 * @return false if a rotation type has already been specified for the given block
	 */
	public static final boolean registerCustomBlockRotation(Block block, Rotation rotationType) {
		return registerCustomBlockRotation(block, rotationType, false);
	}

	/**
	 * Maps a block to a specified rotation type. Allows custom blocks to rotate with structure.
	 * @param block a valid block
	 * @param rotationType types predefined by enumerated type ROTATION
	 * @param override if true, will override the previously set rotation data for specified block
	 * @return false if a rotation type has already been specified for the given block
	 */
	public static final boolean registerCustomBlockRotation(Block block, Rotation rotationType, boolean override) {
		if (blockRotationData.containsKey(block)) {
			LogHelper.warning("Block " + block + " already has a rotation type." + (override ? " Overriding previous data." : ""));
			if (override) {
				blockRotationData.remove(block);
			} else {
				return false;
			}
		}

		blockRotationData.put(block, rotationType);

		return true;
	}

	/** Set rotation data for vanilla blocks */
	static
	{
		blockRotationData.put(Block.anvil, Rotation.ANVIL);

		blockRotationData.put(Block.doorIron, Rotation.DOOR);
		blockRotationData.put(Block.doorWood, Rotation.DOOR);

		blockRotationData.put(Block.bed, Rotation.GENERIC);
		blockRotationData.put(Block.cocoaPlant, Rotation.GENERIC);
		blockRotationData.put(Block.fenceGate, Rotation.GENERIC);
		blockRotationData.put(Block.pumpkin, Rotation.GENERIC);
		blockRotationData.put(Block.pumpkinLantern, Rotation.GENERIC);
		blockRotationData.put(Block.endPortalFrame, Rotation.GENERIC);
		blockRotationData.put(Block.tripWireSource, Rotation.GENERIC);

		blockRotationData.put(Block.chest, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.chestTrapped, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.dispenser, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.dropper, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.enderChest, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.furnaceBurning, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.furnaceIdle, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.hopperBlock, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.ladder, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.signWall, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.pistonBase, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.pistonExtension, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.pistonMoving, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.pistonStickyBase, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.railActivator, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.railDetector, Rotation.PISTON_CONTAINER);
		blockRotationData.put(Block.railPowered, Rotation.PISTON_CONTAINER);

		blockRotationData.put(Block.blockNetherQuartz, Rotation.QUARTZ);

		blockRotationData.put(Block.rail, Rotation.RAIL);

		blockRotationData.put(Block.redstoneComparatorActive, Rotation.REPEATER);
		blockRotationData.put(Block.redstoneComparatorIdle, Rotation.REPEATER);
		blockRotationData.put(Block.redstoneRepeaterActive, Rotation.REPEATER);
		blockRotationData.put(Block.redstoneRepeaterIdle, Rotation.REPEATER);

		blockRotationData.put(Block.signPost, Rotation.SIGNPOST);

		blockRotationData.put(Block.skull, Rotation.SKULL);

		// Mineral stairs:
		blockRotationData.put(Block.stairsBrick, Rotation.STAIRS);
		blockRotationData.put(Block.stairsCobblestone, Rotation.STAIRS);
		blockRotationData.put(Block.stairsNetherBrick, Rotation.STAIRS);
		blockRotationData.put(Block.stairsNetherQuartz, Rotation.STAIRS);
		blockRotationData.put(Block.stairsSandStone, Rotation.STAIRS);
		blockRotationData.put(Block.stairsStoneBrick, Rotation.STAIRS);
		// Wooden stairs
		blockRotationData.put(Block.stairsWoodBirch, Rotation.STAIRS);
		blockRotationData.put(Block.stairsWoodJungle, Rotation.STAIRS);
		blockRotationData.put(Block.stairsWoodOak, Rotation.STAIRS);
		blockRotationData.put(Block.stairsWoodSpruce, Rotation.STAIRS);

		blockRotationData.put(Block.trapdoor, Rotation.TRAPDOOR);

		blockRotationData.put(Block.vine, Rotation.VINE);

		blockRotationData.put(Block.lever, Rotation.LEVER);

		blockRotationData.put(Block.stoneButton, Rotation.WALL_MOUNTED);
		blockRotationData.put(Block.woodenButton, Rotation.WALL_MOUNTED);
		blockRotationData.put(Block.torchRedstoneActive, Rotation.WALL_MOUNTED);
		blockRotationData.put(Block.torchRedstoneIdle, Rotation.WALL_MOUNTED);
		blockRotationData.put(Block.torchWood, Rotation.WALL_MOUNTED);

		blockRotationData.put(Block.wood, Rotation.WOOD);
	}
}

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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.api.gen.ISeedStructure;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Generates Link's House with the front door facing the player
 *
 */
public class LinksHouse implements ISeedStructure
{
	public LinksHouse() {}

	@Override
	public boolean generate(World world, EntityPlayer player, int x, int y, int z, int side) {
		int facing = MathHelper.floor_double((double)((player.rotationYaw * 4F) / 360f) + 0.5D) & 3;
		if (StructureGenUtils.isRotatedAreaClear(world, x, z, 0, 7, y + 1, y + 9, -3, 3, facing)) {
			doGenerate(world, x, y + 1, z, facing);
			return true;
		}
		return false;
	}

	private void doGenerate(World world, int x, int y, int z, int facing) {
		// wood planks/slabs: meta 0 is oak, 1 is spruce
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 6, y, y + 4, -2, 2, facing, Block.planks.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 1, y + 3, -1, 1, facing, 0, 0);
		// wood.blockID beams and posts (spruce up/down: 1, east/west: 5, north/south: 9)
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y, y + 4, -2, -2, facing, Block.wood.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y, y + 4, 2, 2, facing, Block.wood.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y, y + 4, -2, -2, facing, Block.wood.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y, y + 4, 2, 2, facing, Block.wood.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, y + 4, y + 4, -2, -2, facing, Block.wood.blockID, StructureGenUtils.getMetadata(facing, Block.wood, 5));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, y + 4, y + 4, 2, 2, facing, Block.wood.blockID, StructureGenUtils.getMetadata(facing, Block.wood, 5));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y + 4, y + 4, -3, 3, facing, Block.wood.blockID, StructureGenUtils.getMetadata(facing, Block.wood, 9));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y + 4, y + 4, -3, 3, facing, Block.wood.blockID, StructureGenUtils.getMetadata(facing, Block.wood, 9));
		// door is at position clicked, so no offset needed:
		world.setBlock(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(1, 0, facing), Block.doorWood.blockID, StructureGenUtils.getMetadata(facing, Block.doorWood, 0), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 2, z + StructureGenUtils.getOffsetZ(1, 0, facing), Block.doorWood.blockID, StructureGenUtils.getMetadata(facing, Block.doorWood, 8), 2);
		// rear window:
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y + 2, y + 3, 0, 0, facing, Block.glass.blockID, 0);
		// side windows:
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 3, 4, y + 2, y + 3, -2, -2, facing, Block.glass.blockID, 0);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 3, 4, y + 2, y + 3, 2, 2, facing, Block.glass.blockID, 0);
		// make a pyramid of stairs 7 blocks wide at the base, up to 1 at the top
		int minY = y + 5;
		for (int j = 0; j < 3; ++j) {
			// stairs facing to left
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, (j - 3), 0, facing, Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 2));
			// stairs facing to right
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, 0, (3 - j), facing, Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 3));
			// clean up inside attic
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, minY, minY, (j - 2), (2 - j), facing, 0, 0);
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, minY, minY, (j - 2), (2 - j), facing, Block.planks.blockID, 1);
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, minY, minY, (j - 2), (2 - j), facing, Block.planks.blockID, 1);
			// clean up under eaves
			if (j < 2) {
				StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 0, minY, minY, (j - 1), (1 - j), facing, 0, 0);
				StructureGenUtils.rotatedFillWithBlocks(world, x, z, 7, 7, minY, minY, (j - 1), (1 - j), facing, 0, 0);
			}
			++minY; // increment current y level for pyramid
		}
		// final roofing element - slabs
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, 0, 0, facing, Block.woodSingleSlab.blockID, 1);
		// fill lower sides in attic to make room more square
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 5, y + 5, -2, -2, facing, Block.planks.blockID, 1);
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 5, y + 5, 2, 2, facing, Block.planks.blockID, 1);
		// finish under eaves
		world.setBlock(x + StructureGenUtils.getOffsetX(0, -2, facing), y + 5, z + StructureGenUtils.getOffsetZ(0, -2, facing), Block.planks.blockID, 1, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, 2, facing), y + 5, z + StructureGenUtils.getOffsetZ(0, 2, facing), Block.planks.blockID, 1, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(7, -2, facing), y + 5, z + StructureGenUtils.getOffsetZ(7, -2, facing), Block.planks.blockID, 1, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(7, 2, facing), y + 5, z + StructureGenUtils.getOffsetZ(7, 2, facing), Block.planks.blockID, 1, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(0, -1, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 7), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(0, 1, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 6), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(7, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(7, -1, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 7), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(7, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(7, 1, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 6), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(0, 0, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 4), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(7, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(7, 0, facing), Block.stairsWoodSpruce.blockID, StructureGenUtils.getMetadata(facing, Block.stairsWoodSpruce, 5), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 6, z + StructureGenUtils.getOffsetZ(1, 0, facing), Block.glass.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(6, 0, facing), y + 6, z + StructureGenUtils.getOffsetZ(6, 0, facing), Block.glass.blockID, 0, 2);

		// add downstairs decorations
		world.setBlock(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 3, z + StructureGenUtils.getOffsetZ(2, -1, facing), Block.ladder.blockID, StructureGenUtils.getMetadata(facing, Block.ladder, 5), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 4, z + StructureGenUtils.getOffsetZ(2, -1, facing), Block.trapdoor.blockID, StructureGenUtils.getMetadata(facing, Block.trapdoor, 1), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(2, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(2, 0, facing), Block.pressurePlatePlanks.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, -1, facing), Block.workbench.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(5, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, 0, facing), Block.bed.blockID, StructureGenUtils.getMetadata(facing, Block.bed, 0), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(5, 1, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, 1, facing), Block.bed.blockID, StructureGenUtils.getMetadata(facing, Block.bed, 8), 2);

		// add upstairs decorations
		world.setBlock(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 5, z + StructureGenUtils.getOffsetZ(5, 1, facing), Block.web.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(5, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(5, -1, facing), Block.web.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(2, -1, facing), Block.web.blockID, 0, 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(3, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(3, 0, facing), Block.web.blockID, 0, 2);

		// add torches inside and outside
		world.setBlock(x + StructureGenUtils.getOffsetX(2, 1, facing), y + 3, z + StructureGenUtils.getOffsetZ(2, 1, facing), Block.torchWood.blockID, StructureGenUtils.getMetadata(facing, Block.torchWood, 4), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 3, z + StructureGenUtils.getOffsetZ(5, -1, facing), Block.torchWood.blockID, StructureGenUtils.getMetadata(facing, Block.torchWood, 3), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, -1, facing), y + 2, z + StructureGenUtils.getOffsetZ(0, -1, facing), Block.torchWood.blockID, StructureGenUtils.getMetadata(facing, Block.torchWood, 2), 2);
		world.setBlock(x + StructureGenUtils.getOffsetX(0, 1, facing), y + 2, z + StructureGenUtils.getOffsetZ(0, 1, facing), Block.torchWood.blockID, StructureGenUtils.getMetadata(facing, Block.torchWood, 2), 2);

		// add chests + contents (chest meta must be manually set after setting the block, as they auto-rotate upon placement)
		List<ItemStack> contents = new ArrayList<ItemStack>(4);
		// downstairs chest
		int dx = x + StructureGenUtils.getOffsetX(2, 1, facing);
		int dz = z + StructureGenUtils.getOffsetZ(2, 1, facing);
		world.setBlock(dx, y + 1, dz, Block.chest.blockID, 0, 2); // don't care about metadata during first placement
		StructureGenUtils.setMetadata(world, dx, y + 1, dz, StructureGenUtils.getMetadata(facing, Block.chest, 2));
		contents.add(new ItemStack(ZSSItems.swordKokiri));
		contents.add(new ItemStack(ZSSItems.keySmall));
		WorldUtils.addInventoryContentsRandomly(world, dx, y + 1, dz, contents);

		// upstairs chest
		dx = x + StructureGenUtils.getOffsetX(5, 0, facing);
		dz = z + StructureGenUtils.getOffsetZ(5, 0, facing);
		world.setBlock(dx, y + 5, dz, ZSSBlocks.chestLocked.blockID, 0, 2);
		StructureGenUtils.setMetadata(world, dx, y + 5, dz, StructureGenUtils.getMetadata(facing, ZSSBlocks.chestLocked, 4));
		contents.clear();
		contents.add(new ItemStack(ZSSItems.skillOrb, 1, SkillBase.swordBasic.getId()));
		contents.add(new ItemStack(ZSSItems.heartPiece));
		WorldUtils.addInventoryContentsRandomly(world, dx, y + 5, dz, contents);
	}
}

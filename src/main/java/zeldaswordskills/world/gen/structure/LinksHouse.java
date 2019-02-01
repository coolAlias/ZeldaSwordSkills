/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.world.gen.structure;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
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
	public boolean generate(World world, EntityPlayer player, BlockPos pos, EnumFacing face) {
		int facing = MathHelper.floor_double((double)((player.rotationYaw * 4F) / 360f) + 0.5D) & 3;
		if (StructureGenUtils.isRotatedAreaClear(world, pos.getX(), pos.getZ(), 0, 7, pos.getY() + 1, pos.getY() + 9, -3, 3, facing)) {
			doGenerate(world, pos.getX(), pos.getY() + 1, pos.getZ(), facing);
			return true;
		}
		return false;
	}

	private void doGenerate(World world, int x, int y, int z, int facing) {
		// wood planks/slabs: meta 0 is oak, 1 is spruce
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 6, y, y + 4, -2, 2, facing, Blocks.planks.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 1, y + 3, -1, 1, facing, Blocks.air.getDefaultState());
		// log beams and posts (spruce up/down: 1, east/west: 5, north/south: 9)
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y, y + 4, -2, -2, facing, Blocks.log.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y, y + 4, 2, 2, facing, Blocks.log.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y, y + 4, -2, -2, facing, Blocks.log.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y, y + 4, 2, 2, facing, Blocks.log.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, y + 4, y + 4, -2, -2, facing, Blocks.log.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.log, 5)));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, y + 4, y + 4, 2, 2, facing, Blocks.log.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.log, 5)));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, y + 4, y + 4, -3, 3, facing, Blocks.log.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.log, 9)));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y + 4, y + 4, -3, 3, facing, Blocks.log.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.log, 9)));
		// door is at position clicked, so no offset needed:
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(1, 0, facing)), Blocks.oak_door.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.oak_door, 0)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 2, z + StructureGenUtils.getOffsetZ(1, 0, facing)), Blocks.oak_door.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.oak_door, 8)), 2);
		// rear window:
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, y + 2, y + 3, 0, 0, facing, Blocks.glass_pane.getDefaultState());
		// side windows:
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 3, 4, y + 2, y + 3, -2, -2, facing, Blocks.glass_pane.getDefaultState());
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 3, 4, y + 2, y + 3, 2, 2, facing, Blocks.glass_pane.getDefaultState());
		// make a pyramid of stairs 7 blocks wide at the base, up to 1 at the top
		int minY = y + 5;
		for (int j = 0; j < 3; ++j) {
			// stairs facing to left
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, (j - 3), 0, facing, Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 2)));
			// stairs facing to right
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, 0, (3 - j), facing, Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 3)));
			// clean up inside attic
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, minY, minY, (j - 2), (2 - j), facing, Blocks.air.getDefaultState());
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 1, 1, minY, minY, (j - 2), (2 - j), facing, Blocks.planks.getStateFromMeta(1));
			StructureGenUtils.rotatedFillWithBlocks(world, x, z, 6, 6, minY, minY, (j - 2), (2 - j), facing, Blocks.planks.getStateFromMeta(1));
			// clean up under eaves
			if (j < 2) {
				StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 0, minY, minY, (j - 1), (1 - j), facing, Blocks.air.getDefaultState());
				StructureGenUtils.rotatedFillWithBlocks(world, x, z, 7, 7, minY, minY, (j - 1), (1 - j), facing, Blocks.air.getDefaultState());
			}
			++minY; // increment current y level for pyramid
		}
		// final roofing element - slabs
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 0, 7, minY, minY, 0, 0, facing, Blocks.wooden_slab.getStateFromMeta(1));
		// fill lower sides in attic to make room more square
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 5, y + 5, -2, -2, facing, Blocks.planks.getStateFromMeta(1));
		StructureGenUtils.rotatedFillWithBlocks(world, x, z, 2, 5, y + 5, y + 5, 2, 2, facing, Blocks.planks.getStateFromMeta(1));
		// finish under eaves
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, -2, facing), y + 5, z + StructureGenUtils.getOffsetZ(0, -2, facing)), Blocks.planks.getStateFromMeta(1), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, 2, facing), y + 5, z + StructureGenUtils.getOffsetZ(0, 2, facing)), Blocks.planks.getStateFromMeta(1), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(7, -2, facing), y + 5, z + StructureGenUtils.getOffsetZ(7, -2, facing)), Blocks.planks.getStateFromMeta(1), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(7, 2, facing), y + 5, z + StructureGenUtils.getOffsetZ(7, 2, facing)), Blocks.planks.getStateFromMeta(1), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(0, -1, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 7)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(0, 1, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 6)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(7, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(7, -1, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 7)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(7, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(7, 1, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 6)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(0, 0, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 4)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(7, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(7, 0, facing)), Blocks.spruce_stairs.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.spruce_stairs, 5)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(1, 0, facing), y + 6, z + StructureGenUtils.getOffsetZ(1, 0, facing)), Blocks.glass_pane.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(6, 0, facing), y + 6, z + StructureGenUtils.getOffsetZ(6, 0, facing)), Blocks.glass_pane.getDefaultState(), 2);

		// add downstairs decorations
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 3, z + StructureGenUtils.getOffsetZ(2, -1, facing)), Blocks.ladder.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.ladder, 5)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 4, z + StructureGenUtils.getOffsetZ(2, -1, facing)), Blocks.trapdoor.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.trapdoor, 1)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(2, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(2, 0, facing)), Blocks.wooden_pressure_plate.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, -1, facing)), Blocks.crafting_table.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, 0, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, 0, facing)), Blocks.bed.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.bed, 0)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, 1, facing), y + 1, z + StructureGenUtils.getOffsetZ(5, 1, facing)), Blocks.bed.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.bed, 8)), 2);

		// add upstairs decorations
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 5, z + StructureGenUtils.getOffsetZ(5, 1, facing)), Blocks.web.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, 1, facing), y + 6, z + StructureGenUtils.getOffsetZ(5, -1, facing)), Blocks.web.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(2, -1, facing), y + 6, z + StructureGenUtils.getOffsetZ(2, -1, facing)), Blocks.web.getDefaultState(), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(3, 0, facing), y + 7, z + StructureGenUtils.getOffsetZ(3, 0, facing)), Blocks.web.getDefaultState(), 2);

		// add torches inside and outside
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(2, 1, facing), y + 3, z + StructureGenUtils.getOffsetZ(2, 1, facing)), Blocks.torch.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.torch, 4)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(5, -1, facing), y + 3, z + StructureGenUtils.getOffsetZ(5, -1, facing)), Blocks.torch.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.torch, 3)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, -1, facing), y + 2, z + StructureGenUtils.getOffsetZ(0, -1, facing)), Blocks.torch.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.torch, 2)), 2);
		world.setBlockState(new BlockPos(x + StructureGenUtils.getOffsetX(0, 1, facing), y + 2, z + StructureGenUtils.getOffsetZ(0, 1, facing)), Blocks.torch.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.torch, 2)), 2);

		// add chests + contents (chest meta must be manually set after setting the block, as they auto-rotate upon placement)
		List<ItemStack> contents = new ArrayList<ItemStack>(4);
		// downstairs chest
		int dx = x + StructureGenUtils.getOffsetX(2, 1, facing);
		int dz = z + StructureGenUtils.getOffsetZ(2, 1, facing);
		world.setBlockState(new BlockPos(dx, y + 1, dz), Blocks.chest.getStateFromMeta(StructureGenUtils.getMetadata(facing, Blocks.chest, 2)), 2);
		contents.add(new ItemStack(ZSSItems.swordKokiri));
		contents.add(new ItemStack(ZSSItems.keySmall));
		WorldUtils.addInventoryContentsRandomly(world, new BlockPos(dx, y + 1, dz), contents);

		// upstairs chest
		dx = x + StructureGenUtils.getOffsetX(5, 0, facing);
		dz = z + StructureGenUtils.getOffsetZ(5, 0, facing);
		world.setBlockState(new BlockPos(dx, y + 5, dz), ZSSBlocks.chestLocked.getStateFromMeta(StructureGenUtils.getMetadata(facing, ZSSBlocks.chestLocked, 4)), 2);
		contents.clear();
		contents.add(new ItemStack(ZSSItems.skillOrb, 1, SkillBase.swordBasic.getId()));
		contents.add(new ItemStack(ZSSItems.heartPiece));
		WorldUtils.addInventoryContentsRandomly(world, new BlockPos(dx, y + 5, dz), contents);
	}
}

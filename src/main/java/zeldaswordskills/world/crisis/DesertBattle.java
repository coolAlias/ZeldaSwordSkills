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

package zeldaswordskills.world.crisis;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.util.WorldUtils;

public class DesertBattle extends BossBattle {

	public DesertBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	public void beginCrisis(World world) {
		super.beginCrisis(world);
		if (difficulty > 1) {
			scheduleUpdateTick(300 - world.rand.nextInt(100));
			handleDispensers(world, false);
			if (difficulty == 3) {
				setDungeonFloorTo(world, Blocks.soul_sand.getDefaultState(), null);
			}
		}
	}

	@Override
	protected void endCrisis(World world) {
		setDungeonFloorTo(world, Blocks.sandstone.getDefaultState(), Blocks.soul_sand);
		super.endCrisis(world);
	}

	@Override
	protected void onUpdateTick(World world) {
		handleDispensers(world, true);
		scheduleUpdateTick((300 - (difficulty * 50)) - world.rand.nextInt(100));
	}

	/**
	 * Places dispensers in the centers of all four walls and fills them with arrows
	 * @param activate whether the dispensers are being activated (shot) or placed
	 */
	protected void handleDispensers(World world, boolean activate) {
		int j = box.minY + 2;
		for (EnumFacing side : EnumFacing.HORIZONTALS) {
			int minX = (side == EnumFacing.EAST ? box.maxX : side == EnumFacing.WEST ? box.minX : (box.minX + 3));
			int maxX = (side == EnumFacing.SOUTH || side == EnumFacing.NORTH ? box.maxX - 2 : minX + 1);
			int minZ = (side == EnumFacing.SOUTH ? box.maxZ : side == EnumFacing.NORTH ? box.minZ : (box.minZ + 3));
			int maxZ = (side == EnumFacing.EAST || side == EnumFacing.WEST ? box.maxZ - 2 : minZ + 1);
			for (int i = minX; i < maxX; ++i) {
				for (int k = minZ; k < maxZ; ++k) {
					BlockPos pos = new BlockPos(i, j, k);
					if (activate) {
						IBlockState state = world.getBlockState(pos);
						if (state.getBlock() == Blocks.dispenser && world.rand.nextInt(9 - (2 * difficulty)) == 0) { 
							TileEntity te = world.getTileEntity(pos);
							if (te instanceof IInventory) {
								WorldUtils.addItemToInventory(new ItemStack(Items.arrow), (IInventory) te);
								Blocks.dispenser.updateTick(world, pos, state, world.rand);
							}
						}
					} else {
						// TODO check that facing is correct
						world.setBlockState(pos, Blocks.dispenser.getDefaultState().withProperty(BlockDispenser.FACING, side.getOpposite()), 3);
					}
				}
			}
		}
	}
}

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

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.structure.RoomBoss;

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
				setDungeonFloorTo(world, Blocks.soul_sand, 0, null);
			}
		}
	}

	@Override
	protected void endCrisis(World world) {
		setDungeonFloorTo(world, Blocks.sandstone, 0, Blocks.soul_sand);
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
		for (int side = 0; side < 4; ++side) {
			int minX = (side == RoomBoss.EAST ? box.maxX : side == RoomBoss.WEST ? box.minX : (box.minX + 3));
			int maxX = (side == RoomBoss.SOUTH || side == RoomBoss.NORTH ? box.maxX - 2 : minX + 1);
			int minZ = (side == RoomBoss.SOUTH ? box.maxZ : side == RoomBoss.NORTH ? box.minZ : (box.minZ + 3));
			int maxZ = (side == RoomBoss.EAST || side == RoomBoss.WEST ? box.maxZ - 2 : minZ + 1);

			for (int i = minX; i < maxX; ++i) {
				for (int k = minZ; k < maxZ; ++k) {
					if (activate) {
						if (world.getBlock(i, j, k) == Blocks.dispenser && world.rand.nextInt(9 - (2 * difficulty)) == 0) { 
							Blocks.dispenser.updateTick(world, i, j, k, world.rand);
						}
					} else {
						world.setBlock(i, j, k, Blocks.dispenser);
						world.setBlockMetadataWithNotify(i, j, k, RoomBoss.facingToOrientation[(side + 2) % 4], 2);
						TileEntity te = world.getTileEntity(i, j, k);
						if (te instanceof IInventory) {
							WorldUtils.addItemToInventory(new ItemStack(Items.arrow, (difficulty == 3 ? 24 : 16)), (IInventory) te);
						}
					}
				}
			}
		}
	}
}

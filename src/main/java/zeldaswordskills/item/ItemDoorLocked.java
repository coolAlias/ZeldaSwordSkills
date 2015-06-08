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

package zeldaswordskills.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * 
 * Item version of locked doors for use in Creative mode and Creative Tabs.
 *
 */
public class ItemDoorLocked extends ItemModBlock
{
	public ItemDoorLocked(Block block) {
		super(block);
		setMaxDamage(0);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (face != EnumFacing.UP) {
			return false;
		}
		pos = pos.up();
		if (player.canPlayerEdit(pos, face, stack) && player.canPlayerEdit(pos.up(), face, stack)) {
			if (!block.canPlaceBlockAt(world, pos)) {
				return false;
			} else {
				placeDoorBlock(world, pos, stack.getItemDamage());
				--stack.stackSize;
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * Places first the bottom then the top block with correct metadata values
	 */
	private void placeDoorBlock(World world, BlockPos pos, int meta) {
		meta &= 0x7;
		world.setBlockState(pos, block.getStateFromMeta(meta), 2);
		world.setBlockState(pos.up(), block.getStateFromMeta(meta | 0x8), 2);
		world.notifyNeighborsOfStateChange(pos, block);
		world.notifyNeighborsOfStateChange(pos.up(), block);
	}
}

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

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Item version of locked doors for use in Creative mode and Creative Tabs.
 *
 */
public class ItemDoorLocked extends Item
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public ItemDoorLocked(int id) {
		super(id);
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (side != 1) {
			return false;
		} else {
			++y;
			Block block = ZSSBlocks.doorLocked;
			if (player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack)) {
				if (!block.canPlaceBlockAt(world, x, y, z)) {
					return false;
				} else {
					placeDoorBlock(world, x, y, z, stack.getItemDamage(), block);
					--stack.stackSize;
					return true;
				}
			} else {
				return false;
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		for (int i = 0; i < 8; ++i) {
			list.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		return iconArray[par1 % 8];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[8];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9).toLowerCase() + i);
		}
	}

	/**
	 * Places first the bottom then the top block with correct metadata values
	 */
	public static void placeDoorBlock(World world, int x, int y, int z, int meta, Block block) {
		world.setBlock(x, y, z, block.blockID, meta, 2);
		world.setBlock(x, y + 1, z, block.blockID, meta | 0x8, 2);
		world.notifyBlocksOfNeighborChange(x, y, z, block.blockID);
		world.notifyBlocksOfNeighborChange(x, y + 1, z, block.blockID);
	}
}

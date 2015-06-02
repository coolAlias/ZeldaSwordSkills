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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Item version of locked doors for use in Creative mode and Creative Tabs.
 *
 */
public class ItemDoorLocked extends Item implements IUnenchantable
{
	private final Block doorBlock;

	public ItemDoorLocked(Block block) {
		super();
		this.doorBlock = block;
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabKeys);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (side != 1) {
			return false;
		} else {
			++y;
			if (player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack)) {
				if (!doorBlock.canPlaceBlockAt(world, x, y, z)) {
					return false;
				} else {
					placeDoorBlock(world, x, y, z, stack.getItemDamage());
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
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	/**
	 * Places first the bottom then the top block with correct metadata values
	 */
	private void placeDoorBlock(World world, int x, int y, int z, int meta) {
		meta &= 0x7;
		world.setBlock(x, y, z, doorBlock, meta, 2);
		world.setBlock(x, y + 1, z, doorBlock, meta | 0x8, 2);
		world.notifyBlocksOfNeighborChange(x, y, z, doorBlock);
		world.notifyBlocksOfNeighborChange(x, y + 1, z, doorBlock);
	}
}

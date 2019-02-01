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

package zeldaswordskills.item;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.ILiftBlock;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;

/**
 * 
 * Gauntlets that allow the wearer to pick up certain blocks on right-click.
 * 
 * Only solid opaque cubes can be picked up, and each gauntlet is only able
 * to pick up blocks of a certain resistance or less, in addition to blocks
 * implementing the ILiftable interface so long as the gauntlets match or
 * exceed the required strength. Blocks with tile entities cannot be moved.
 *
 */
public class ItemPowerGauntlets extends BaseModItem implements ILiftBlock, IUnenchantable
{
	/** Max resistance that a block may have and still be picked up */
	private final BlockWeight strength;

	public ItemPowerGauntlets(BlockWeight strength) {
		super();
		this.strength = strength;
		setMaxDamage(0);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public BlockWeight getLiftStrength(EntityPlayer player, ItemStack stack, IBlockState state) {
		return strength;
	}

	@Override
	public ItemStack onLiftBlock(EntityPlayer player, ItemStack stack, IBlockState state) {
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}
}

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

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.block.BlockDoorBoss;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BossType;

/**
 * 
 * Item version of boss doors for use in Creative mode and Creative Tabs.
 *
 */
public class ItemDoorBoss extends ItemDoorLocked
{
	public ItemDoorBoss(Block block) {
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name", BossType.byDoorMetadata(stack.getItemDamage()).getDisplayName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		block.getSubBlocks(item, tab, list);
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[BlockDoorBoss.EnumType.values().length];
		for (BlockDoorBoss.EnumType temple : BlockDoorBoss.EnumType.values()) {
			variants[temple.getMetadata()] = ModInfo.ID + ":door_" + temple.getName();
		}
		return variants;
	}
}

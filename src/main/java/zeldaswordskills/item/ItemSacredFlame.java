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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import zeldaswordskills.block.BlockSacredFlame;
import zeldaswordskills.ref.ModInfo;

import com.google.common.base.Function;

/**
 * 
 * For rendering in the inventory as well as implementing uses for each.
 *
 */
public class ItemSacredFlame extends ItemMetadataBlock
{
	public ItemSacredFlame(Block block) {
		super(block, new Function<ItemStack, String>() {
			@Override
			public String apply(ItemStack stack) {
				return BlockSacredFlame.EnumType.byMetadata(stack.getItemDamage()).getName();
			}
		});
	}

	@Override
	public String[] getVariants() {
		String s = getUnlocalizedName();
		s = ModInfo.ID + ":" + s.substring(s.lastIndexOf(".") + 1);
		String[] variants = new String[BlockSacredFlame.EnumType.values().length];
		for (BlockSacredFlame.EnumType flame : BlockSacredFlame.EnumType.values()) {
			variants[flame.getMetadata()] = s + "_" + flame.getName();
		}
		return variants;
	}
}

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

import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Default ItemBlock for blocks with unbreakable varieties
 *
 */
public class ItemBlockUnbreakable extends ItemMetadataBlock
{
	/**
	 * Default naming function appends ".unbreakable" for any damage >= 8
	 */
	public ItemBlockUnbreakable(Block block) {
		super(block, new Function<ItemStack, String>() {
			@Override
			public String apply(ItemStack stack) {
				return stack.getItemDamage() < 8 ? "" : "unbreakable";
			}
		});
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		if (stack.getItemDamage() > 7) {
			list.add(StatCollector.translateToLocal("tooltip.zss.block.unbreakable.desc"));
		} else {
			list.add(StatCollector.translateToLocal("tooltip.zss.block.dungeon.desc.0"));
		}
	}

	/**
	 * Default behavior registers a renderer for each variant, plus the same
	 * renderer for that variant as an unbreakble version (damage | 8).
	 * Note that this limits the maximum variants to 8.
	 * If no variants are returned by {@link #getVariants()}, the unlocalized name is used instead.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerResources() {
		String[] variants = getVariants();
		if (variants == null || variants.length < 1) {
			String name = getUnlocalizedName();
			variants = new String[]{ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1)};
		}
		for (int i = 0; i < variants.length; ++i) {
			ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(variants[i], "inventory"));
			ModelLoader.setCustomModelResourceLocation(this, i | 8, new ModelResourceLocation(variants[i], "inventory"));
		}
	}
}

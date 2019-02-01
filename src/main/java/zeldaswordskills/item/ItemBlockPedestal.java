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

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBlockPedestal extends ItemMetadataBlock {

	@SideOnly(Side.CLIENT)
	private List<ModelResourceLocation> models;

	public ItemBlockPedestal(Block block) {
		super(block);
	}

	/**
	 * Pedestal has two variants, one each using meta 0 and 8
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerResources() {
		String[] variants = getVariants();
		this.models = new ArrayList<ModelResourceLocation>(variants.length);
		for (int i = 0; i < variants.length; ++i) {
			this.models.add(new ModelResourceLocation(variants[i], "inventory"));
		}
		ModelLoader.registerItemVariants(this, this.models.toArray(new ModelResourceLocation[0]));
		ModelLoader.setCustomMeshDefinition(this, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				return models.get(stack.getItemDamage() == 8 ? 1 : 0);
			}
		});
	}
}

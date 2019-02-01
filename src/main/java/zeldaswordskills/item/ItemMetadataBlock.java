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

import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.IBlockItemVariant;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Can be used for any generic block with metadata subtypes
 * If block implements IBlockVariant, those variant names will be used
 *
 */
public class ItemMetadataBlock extends ItemMultiTexture implements IModItem, IUnenchantable
{
	/**
	 * Default constructor with no special naming scheme
	 */
	public ItemMetadataBlock(Block block) {
		this(block, new Function<ItemStack, String>() {
			@Override
			public String apply(ItemStack stack) {
				return "";
			}
		});
	}

	/**
	 * @param nameFunction Function to determine unlocalized name
	 */
	public ItemMetadataBlock(Block block, Function<ItemStack, String> nameFunction) {
		super(block, block, nameFunction);
	}

	/**
	 * @param namesByMeta Array of names used to create a lookup-based name function
	 */
	public ItemMetadataBlock(Block block, final String[] namesByMeta) {
		super(block, block, namesByMeta);
	}

	/**
	 * Returns "tile.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("tile.", "tile.zss.");
	}

	/**
	 * Override ItemMultiTexture's to allow for variants with no custom naming scheme
	 */
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String extra = (String) nameFunction.apply(stack);
		return getUnlocalizedName() + (extra == null || extra.length() < 1 ? "" : "." + extra);
	}

	/**
	 * Default behavior returns NULL to not register any variants. Each variant returned
	 * will automatically have an identical renderer registered for the unbreakable version.
	 * See {@link #registerRenderers(ItemModelMesher)}  
	 */
	@Override
	public String[] getVariants() {
		if (block instanceof IBlockItemVariant) {
			return ((IBlockItemVariant) block).getItemBlockVariants();
		}
		return null;
	}

	/**
	 * Default implementation suggested by {@link IModItem#registerResources()}
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
		}
	}
}

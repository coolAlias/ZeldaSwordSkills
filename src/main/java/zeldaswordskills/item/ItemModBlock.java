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
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.block.IBlockItemVariant;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Default ItemBlock class implementing IModItem
 *
 */
public class ItemModBlock extends ItemBlock implements IModItem, IUnenchantable
{
	private final String[] variants;

	/**
	 * At least one variant needs to be registered at some point
	 */
	public ItemModBlock(Block block) {
		super(block);
		this.variants = null;
	}

	/**
	 * Standard ItemBlock constructor with optional variant names
	 */
	public ItemModBlock(Block block, String... variants) {
		super(block);
		this.variants = variants;
		if (variants.length > 1) {
			setMaxDamage(0);
			setHasSubtypes(true);
		}
	}

	/**
	 * Returns "tile.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("tile.", "tile.zss.");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	/**
	 * Default behavior returns NULL to not register any variants
	 */
	@Override
	public String[] getVariants() {
		if (block instanceof IBlockItemVariant) {
			return ((IBlockItemVariant) block).getItemBlockVariants();
		}
		return variants;
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

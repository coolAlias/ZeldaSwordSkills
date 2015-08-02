/**
    Copyright (C) <2017> <coolAlias>

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

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Class for items that require drinking, such as potions,
 * and leave an empty glass bottle behind when consumed.
 *
 */
public class ItemDrinkable extends Item implements IModItem
{
	public ItemDrinkable() {}

	public ItemDrinkable(String name) {
		this.setUnlocalizedName(name);
	}

	/**
	 * Returns "item.zss.unlocalized_name" for translation purposes
	 */
	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replaceFirst("item.", "item.zss.");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		return stack;
	}

	/**
	 * Override this method and return super at the end to automatically handle adding a glass bottle;
	 * note that this implementation does not decrement the stack size.
	 */
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
		if (!player.capabilities.isCreativeMode) {
			if (stack == null || stack.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}
			player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
		}
		return stack;
	}

	/**
	 * Default behavior returns NULL to not register any variants
	 */
	@Override
	public String[] getVariants() {
		return null;
	}

	/**
	 * Default implementation suggested by {@link IModItem#registerVariants()}
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerVariants() {
		String[] variants = getVariants();
		if (variants != null) {
			ModelBakery.addVariantName(this, variants);
		}
	}

	/**
	 * Register all of this Item's renderers here, including for any subtypes.
	 * Default behavior registers a single inventory-based mesher for each variant
	 * returned by {@link #getVariants() getVariants}.
	 * If no variants are available, "mod_id:" plus the item's unlocalized name is used.
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerRenderers(ItemModelMesher mesher) {
		String[] variants = getVariants();
		if (variants == null || variants.length < 1) {
			String name = getUnlocalizedName();
			variants = new String[]{ModInfo.ID + ":" + name.substring(name.lastIndexOf(".") + 1)};
		}
		for (int i = 0; i < variants.length; ++i) {
			mesher.register(this, i, new ModelResourceLocation(variants[i], "inventory"));
		}
	}
}

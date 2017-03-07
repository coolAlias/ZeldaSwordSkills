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

import java.util.List;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.ref.Config;
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

	public static class ItemLonLonSpecial extends ItemDrinkable
	{
		public ItemLonLonSpecial(String name) {
			super(name);
			setMaxStackSize(1);
			setCreativeTab(ZSSCreativeTabs.tabTools);
		}
		@Override
		public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			info.setCurrentMagic(info.getMaxMagic());
			ZSSEntityInfo.get(player).applyBuff(Buff.UNLIMITED_MAGIC, 1200, 0);
			if (!Config.allowUnlimitedNayru()) {
				info.setFlag(ZSSPlayerInfo.IS_NAYRU_ACTIVE, false);
			}
			return super.onItemUseFinish(stack, world, player);
		}
		@Override
		public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
			return true;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean isHeld) {
			list.add(StatCollector.translateToLocal("tooltip.zss.lon_lon_special.desc.0"));
			list.add(StatCollector.translateToLocal("tooltip.zss.lon_lon_special.desc.1"));
		}
	}
}

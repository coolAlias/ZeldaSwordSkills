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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
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
public class ItemDrinkable extends Item
{
	public ItemDrinkable(String name) {
		super();
		setUnlocalizedName("zss." + name);
		setTextureName(ModInfo.ID + ":" + name);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.drink;
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
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode) {
			if (stack == null || stack.stackSize <= 0) {
				return new ItemStack(Items.glass_bottle);
			}
			player.inventory.addItemStackToInventory(new ItemStack(Items.glass_bottle));
		}
		return stack;
	}

	public static class ItemLonLonSpecial extends ItemDrinkable
	{
		public ItemLonLonSpecial(String name) {
			super(name);
			setMaxStackSize(1);
			setCreativeTab(ZSSCreativeTabs.tabTools);
		}
		@Override
		public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			info.setCurrentMagic(info.getMaxMagic());
			ZSSEntityInfo.get(player).applyBuff(Buff.UNLIMITED_MAGIC, 1200, 0);
			if (!Config.allowUnlimitedNayru()) {
				info.setFlag(ZSSPlayerInfo.IS_NAYRU_ACTIVE, false);
			}
			return super.onEaten(stack, world, player);
		}
		@Override
		public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
			return true;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
			list.add(StatCollector.translateToLocal("tooltip.zss.lon_lon_special.desc.0"));
			list.add(StatCollector.translateToLocal("tooltip.zss.lon_lon_special.desc.1"));
		}
	}

	public static class ItemPotionPurple extends ItemDrinkable
	{
		protected final int restore_hunger;
		protected final float saturation;
		public ItemPotionPurple(String name, int restore_hunger, float saturation) {
			super(name);
			this.restore_hunger = restore_hunger;
			this.saturation = saturation;
			setMaxStackSize(1);
			setCreativeTab(ZSSCreativeTabs.tabTools);
		}
		@Override
		public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			player.getFoodStats().addStats(restore_hunger, saturation);
			return super.onEaten(stack, world, player);
		}
		@Override
		public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
			return true;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public boolean hasEffect(ItemStack stack, int pass) {
			return true;
		}
		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("tooltip.zss.restore_stamina", restore_hunger));
		}
	}
}

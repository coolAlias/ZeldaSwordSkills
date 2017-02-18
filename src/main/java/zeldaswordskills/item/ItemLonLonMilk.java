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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.util.PlayerUtils;

public class ItemLonLonMilk extends ItemDrinkable
{
	@SideOnly(Side.CLIENT)
	private IIcon used_icon;

	protected final float restore_hp;

	public ItemLonLonMilk(String name, int uses, float restore_hp) {
		super(name);
		this.restore_hp = restore_hp;
		this.setMaxDamage(uses);
		setMaxStackSize(1);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (stack.hasTagCompound() && player.worldObj.getWorldTime() > stack.getTagCompound().getLong("expiration")) {
			if (!world.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.lon_lon_milk.expired");
			}
			return stack;
		}
		return (player.getHealth() < player.getMaxHealth() ? super.onItemRightClick(stack, world, player) : stack);
	}

	@Override
	public ItemStack onEaten(ItemStack stack, World world, EntityPlayer player) {
		// note that super.onItemUseFinish is not suitable for multi-use items due to always adding a glass bottle
		player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
		if (!player.capabilities.isCreativeMode) {
			stack.setItemDamage(stack.getItemDamage() + 1);
			if (stack.getItemDamage() >= stack.getMaxDamage()) {
				--stack.stackSize;
			}
		}
		player.heal(restore_hp);
		return (stack.stackSize <= 0 ? new ItemStack(Items.glass_bottle) : stack);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		if (!stack.getTagCompound().hasKey("expiration")) {
			stack.getTagCompound().setLong("expiration", world.getWorldTime() + 24000);
		}
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean isHeld) {
		long expiration = (stack.hasTagCompound() ? stack.getTagCompound().getLong("expiration") : 0);
		if (expiration > 0 && player.worldObj.getWorldTime() > expiration) {
			list.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("tooltip.zss.lon_lon_milk.expired"));
		} else {
			list.add(EnumChatFormatting.GREEN + StatCollector.translateToLocalFormatted("tooltip.zss.restore_hp", restore_hp));
			if (stack.getMaxDamage() > 1) {
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("tooltip.zss.uses", stack.getMaxDamage() - stack.getItemDamage(), stack.getMaxDamage()));
			}
			expiration -= player.worldObj.getWorldTime();
			if (expiration > 0) {
				String time = StringUtils.ticksToElapsedTime((int) expiration);
				list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted("tooltip.zss.lon_lon_milk.expires_in", time));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return (damage > 0 ? used_icon : itemIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		super.registerIcons(register);
		used_icon = register.registerIcon(getIconString() + "_half");
	}
}

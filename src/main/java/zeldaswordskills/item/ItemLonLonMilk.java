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

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

public class ItemLonLonMilk extends ItemDrinkable
{
	protected final int uses;

	protected final float restore_hp;

	public ItemLonLonMilk(String name, int uses, float restore_hp) {
		super(name);
		this.uses = uses;
		this.restore_hp = restore_hp;
		setMaxStackSize(1);
		this.setHasSubtypes(true);
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
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityPlayer player) {
		// note that super.onItemUseFinish is not suitable for multi-use items due to always adding a glass bottle
		player.triggerAchievement(StatList.objectUseStats[Item.getIdFromItem(this)]);
		if (!player.capabilities.isCreativeMode) {
			int used = stack.getItemDamage() + 1;
			if (used >= this.uses) {
				--stack.stackSize;
			} else {
				stack = new ItemStack(this, stack.stackSize, used);
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
	public String[] getVariants() {
		String name = ModInfo.ID + ":lon_lon_milk";
		return new String[]{name, name + "_half"};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
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
}

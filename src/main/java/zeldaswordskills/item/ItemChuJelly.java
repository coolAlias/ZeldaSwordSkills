/**
    Copyright (C) <2014> <coolAlias>

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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityChu.ChuType;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemChuJelly extends Item
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;
	
	private static final Map<ChuType, Item> jellyMap = new EnumMap<ChuType, Item>(ChuType.class);

	public ItemChuJelly(int id) {
		super(id);
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}
	
	/** Safe method for obtaining chu type from the stack, regardless of stack damage value */
	private ChuType getType(ItemStack stack) {
		return ChuType.values()[stack.getItemDamage() % ChuType.values().length];
	}
	
	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			EntityVillager entityVillager = (EntityVillager) entity;
			ZSSVillagerInfo villager = ZSSVillagerInfo.get(entityVillager);
			MerchantRecipeList trades = entityVillager.getRecipes(player);
			ChuType type = getType(stack);
			entityVillager.playLivingSound();
			if (villager != null && trades != null && villager.isChuTrader() && jellyMap.containsKey(type)) {
				if (villager.getJelliesReceived(type) == 0) {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.jelly.first"));
					villager.addJelly(type, 1);
					--stack.stackSize;
				} else if (villager.canSellType(type, stack)) {
					MerchantRecipe trade = new MerchantRecipe(new ItemStack(stack.getItem(), 4, type.ordinal()),
							new ItemStack(Item.emerald, (type.ordinal() + 1) * 8), new ItemStack(jellyMap.get(type)));
					if (MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
						player.worldObj.playSoundAtEntity(player, ModInfo.SOUND_SUCCESS, 1.0F, 1.0F);
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.jelly.new_stock"));
						PlayerUtils.addItemToInventory(player, new ItemStack(jellyMap.get(type)));
					} else {
						player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.jelly.in_stock"));
					}
				} else {
					player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.jelly.need_more"));
				}

				if (stack.stackSize == 0) {
					player.setCurrentItemOrArmor(0, null);
				}
			} else {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.trade.jelly.gross"));
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + stack.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		for (int i = 0; i < ChuType.values().length; ++i) {
			list.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		return iconArray[damage % ChuType.values().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[ChuType.values().length];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9) + i);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.jelly_chu.desc.0"));
	}
	
	public static void initializeJellies() {
		jellyMap.put(ChuType.RED, ZSSItems.potionRed);
		jellyMap.put(ChuType.GREEN, ZSSItems.potionGreen);
		jellyMap.put(ChuType.BLUE, ZSSItems.potionBlue);
		jellyMap.put(ChuType.YELLOW, ZSSItems.potionYellow);
	}
}

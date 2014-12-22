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

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.entity.projectile.EntitySeedShot;
import zeldaswordskills.entity.projectile.EntitySeedShot.SeedType;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Miscellaneous items with no use other than trading should go here.
 *
 */
public class ItemMiscZSS extends Item implements IUnenchantable
{
	/** The price this item will fetch if sold to a villager */
	private final int sell_price;

	public ItemMiscZSS(int price) {
		super();
		setMaxDamage(0);
		sell_price = price;
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (this == ZSSItems.dekuNut) {
			EntitySeedShot seedShot = new EntitySeedShot(world, player, 0.5F, 1, 0).setType(SeedType.DEKU);
			seedShot.setDamage(2.5F);
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			if (!world.isRemote) {
				world.spawnEntityInWorld(seedShot);
			}
		} else if (this == ZSSItems.skillWiper) {
			if (!world.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.skill.reset");
				ZSSPlayerSkills.get(player).resetSkills();
			}
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass().isAssignableFrom(EntityVillager.class)) {
			if (stack.getItem() == ZSSItems.masterOre) {
				handleMasterOre(stack, player, (EntityVillager) entity);
			} else {
				handleGenericTrade(stack, player, (EntityVillager) entity);
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	private void handleMasterOre(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		MerchantRecipeList trades = villager.getRecipes(player);
		if (villager.getProfession() == 3 && trades != null && trades.size() > Config.getFriendTradesRequired()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.smith");
			if (player.inventory.hasItem(ZSSItems.swordMaster)) {
				if (MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(new ItemStack(ZSSItems.masterOre,2), new ItemStack(ZSSItems.swordMaster), new ItemStack(ZSSItems.swordTempered)))) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.new");
					player.triggerAchievement(ZSSAchievements.swordTempered);
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.old");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.unworthy");
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.villager");
		}
	}

	private void handleGenericTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		MerchantRecipeList trades = villager.getRecipes(player);
		if (trades != null && sell_price > 0) {
			MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, sell_price));
			if (player.worldObj.rand.nextFloat() < 0.2F && MerchantRecipeHelper.addToListWithCheck(trades, trade)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sell.0");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.1");
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		}
	}
}

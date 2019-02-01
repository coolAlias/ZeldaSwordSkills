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

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

public class ItemMasterOre extends ItemMiscZSS
{
	public ItemMasterOre(int price) {
		super(price);
	}

	@Override
	protected void handleTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		MerchantRecipeList trades = villager.getRecipes(player);
		if (villager.isChild()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.child");
		} else if (villager.getClass() != EntityVillager.class) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		} else if (EnumVillager.BLACKSMITH.is(villager) && trades != null) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.smith");
			if (player.inventory.hasItem(ZSSItems.swordMaster)) {
				if (MerchantRecipeHelper.addToListWithCheck(trades, new MerchantRecipe(new ItemStack(ZSSItems.masterOre,2), new ItemStack(ZSSItems.swordMaster), new ItemStack(ZSSItems.swordTempered)))) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.new");
					player.triggerAchievement(ZSSAchievements.swordTempered);
				} else {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.old");
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.unworthy");
			}
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.master_ore.villager");
		}
	}
}

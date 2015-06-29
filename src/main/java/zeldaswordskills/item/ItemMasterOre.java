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

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

public class ItemMasterOre extends ItemMiscZSS
{
	public ItemMasterOre(int id, int price) {
		super(id, price);
	}

	@Override
	protected void handleTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		MerchantRecipeList trades = villager.getRecipes(player);
		if (villager.isChild()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.child");
		} else if (!villager.getClass().isAssignableFrom(EntityVillager.class)) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		} else if (villager.getProfession() == 3 && trades != null && trades.size() > Config.getFriendTradesRequired()) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.masterore.smith");
			if (player.inventory.hasItem(ZSSItems.swordMaster.itemID)) {
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
}

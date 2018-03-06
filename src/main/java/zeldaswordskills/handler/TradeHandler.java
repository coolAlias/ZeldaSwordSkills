/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.handler;

import java.util.Random;

import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;

public class TradeHandler implements IVillageTradeHandler
{
	public static void registerTrades() {
		for (int i = 0; i < 5; ++i) {
			VillagerRegistry.instance().registerVillageTradeHandler(i, new TradeHandler());
		}
		ZSSVillagerInfo.initTrades();
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList trades, Random rand) {
		if (villager instanceof EntityGoron && Config.enableTradeBomb()) {
			float bombChance = (EnumVillager.BLACKSMITH.is(villager) ? 0.6F : 0.3F);
			for (BombType bomb : BombType.values()) {
				addTrade(trades, rand, bombChance, new MerchantRecipe(new ItemStack(Items.emerald, 8 + (bomb.ordinal() * 4) + rand.nextInt(6)), new ItemStack(ZSSItems.bomb, 1, bomb.ordinal())));
			}
			return;
		}
	}

	/**
	 * Adds the trade to the list of trades with the given probability
	 */
	public static void addTrade(MerchantRecipeList trades, Random rand, float probability, MerchantRecipe trade) {
		if (rand.nextFloat() < probability) {
			trades.add(trade);
		}
	}
}

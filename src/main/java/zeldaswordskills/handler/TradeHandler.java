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

import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.entity.ZSSVillagerInfo;

public class TradeHandler // TODO implements IVillageTradeHandler
{
	public static void registerTrades() {
		for (int i = 0; i < 5; ++i) {
			// TODO VillagerRegistry.instance().registerVillageTradeHandler(i, new TradeHandler());
		}
		ZSSVillagerInfo.initTrades();
	}

	/*
	// TODO
	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList trades, Random rand) {
		if (villager instanceof EntityGoron && Config.enableTradeBomb()) {
			float bombChance = (villager.getProfession() == EnumVillager.BLACKSMITH.ordinal() ? 0.6F : 0.3F);
			for (BombType bomb : BombType.values()) {
				addTrade(trades, rand, bombChance, new MerchantRecipe(new ItemStack(Items.emerald, 8 + (bomb.ordinal() * 4) + rand.nextInt(6)), new ItemStack(ZSSItems.bomb, 1, bomb.ordinal())));
			}
			return;
		}
		switch(EnumVillager.values()[villager.getProfession()]) {
		case FARMER:
			addTrade(trades, rand, 0.3F, new MerchantRecipe(new ItemStack(Items.emerald, 10), new ItemStack(ZSSItems.tunicHeroBoots)));
			addTrade(trades, rand, 0.3F, new MerchantRecipe(new ItemStack(Items.emerald, 10), new ItemStack(ZSSItems.tunicHeroHelm)));
			break;
		case LIBRARIAN:
			break;
		case PRIEST:
			break;
		case BLACKSMITH:
			addTrade(trades, rand, 0.2F, new MerchantRecipe(new ItemStack(Items.emerald, 16), new ItemStack(ZSSItems.swordKokiri)));
			addTrade(trades, rand, 0.2F, new MerchantRecipe(new ItemStack(Items.emerald, 16), new ItemStack(ZSSItems.shieldDeku)));
			break;
		case BUTCHER:
			addTrade(trades, rand, 0.3F, new MerchantRecipe(new ItemStack(Items.emerald, 20), new ItemStack(ZSSItems.tunicHeroChest)));
			addTrade(trades, rand, 0.3F, new MerchantRecipe(new ItemStack(Items.emerald, 10), new ItemStack(ZSSItems.tunicHeroLegs)));
			break;
		default:
			break;
		}
	}
	 */

	/**
	 * Adds the trade to the list of trades with the given probability
	 */
	public static void addTrade(MerchantRecipeList trades, Random rand, float probability, MerchantRecipe trade) {
		if (rand.nextFloat() < probability) {
			trades.add(trade);
		}
	}
}

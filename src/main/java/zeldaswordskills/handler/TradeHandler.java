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

package zeldaswordskills.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.api.block.IHookable.HookshotType;
import zeldaswordskills.entity.ZSSVillagerInfo;
import zeldaswordskills.item.ItemHookShotUpgrade.UpgradeType;
import zeldaswordskills.item.ZSSItems;

public class TradeHandler // TODO implements IVillageTradeHandler
{
	/** Trade mapping for a single HookShot ItemStack key to a full Extended HookShot trade recipe output */
	public static final Map<List<Integer>, MerchantRecipe> hookshotAddonTrades = new HashMap<List<Integer>, MerchantRecipe>();

	/**
	 * Adds the trade recipe to the map with the AddonType and  ItemStack as key
	 */
	public static final void addTradeToMap(UpgradeType type, ItemStack stack, MerchantRecipe trade) {
		hookshotAddonTrades.put(Arrays.asList(type.ordinal(), Item.getIdFromItem(stack.getItem()), stack.getItemDamage()), trade);
	}

	/**
	 * Returns the appropriate MerchantRecipe for the stack and Addon Type, or null if none was found
	 */
	public static final MerchantRecipe getTrade(UpgradeType type, ItemStack stack) {
		return hookshotAddonTrades.get(Arrays.asList(type.ordinal(), Item.getIdFromItem(stack.getItem()), stack.getItemDamage()));
	}

	public static void registerTrades() {
		for (int i = 0; i < 5; ++i) {
			// TODO VillagerRegistry.instance().registerVillageTradeHandler(i, new TradeHandler());
		}

		addTradeToMap(UpgradeType.EXTENDER, new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT.ordinal()), 
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.EXTENDER.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT_EXT.ordinal())));
		addTradeToMap(UpgradeType.EXTENDER, new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT.ordinal()),
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.EXTENDER.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT_EXT.ordinal())));
		addTradeToMap(UpgradeType.EXTENDER, new ItemStack(ZSSItems.hookshot,1,HookshotType.MULTI_SHOT.ordinal()),
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.MULTI_SHOT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.EXTENDER.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.MULTI_SHOT_EXT.ordinal())));

		addTradeToMap(UpgradeType.CLAW, new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT.ordinal()), 
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.CLAW.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT.ordinal())));
		addTradeToMap(UpgradeType.CLAW, new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT_EXT.ordinal()),
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.WOOD_SHOT_EXT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.CLAW.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT_EXT.ordinal())));

		addTradeToMap(UpgradeType.MULTI, new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT.ordinal()), 
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.MULTI.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.MULTI_SHOT.ordinal())));
		addTradeToMap(UpgradeType.MULTI, new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT_EXT.ordinal()),
				new MerchantRecipe(new ItemStack(ZSSItems.hookshot,1,HookshotType.CLAW_SHOT_EXT.ordinal()),
						new ItemStack(ZSSItems.hookshotUpgrade,1,UpgradeType.MULTI.ordinal()),
						new ItemStack(ZSSItems.hookshot,1,HookshotType.MULTI_SHOT_EXT.ordinal())));

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

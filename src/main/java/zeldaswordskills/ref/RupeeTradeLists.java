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

package zeldaswordskills.ref;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.entity.VanillaRupeeMerchant;
import zeldaswordskills.item.ZSSItems;

public class RupeeTradeLists
{
	/**
	 * Call during FML's post init phase to (re)create the default rupee trade lists.
	 * Should be called after RupeeValueRegistry has been initialized.
	 */
	public static void postInit(Configuration config) {
		Property init = config.get("Trade Lists", "Init Rupee Trade Lists", true, "Set to true to recreate the default rupee trade json files - this will overwrite existing files!");
		if (init.getBoolean()) {
			RupeeTradeLists.createDefaultNpcTradeFiles();
			RupeeTradeLists.createDefaultVanillaTradeFiles();
		}
		init.set(false);
	}

	/**
	 * Creates the default trading lists for Zelda NPCs
	 */
	private static void createDefaultNpcTradeFiles() {
		RupeeTradeList<RupeeTrade> buys = new RupeeTradeList<RupeeTrade>(RupeeTradeList.WILL_BUY);
		RupeeTradeList<RupeeTrade> sells = new RupeeTradeList<RupeeTrade>(RupeeTradeList.FOR_SALE);
	}

	/**
	 * Creates the default trading lists for Vanilla Villagers
	 */
	private static void createDefaultVanillaTradeFiles() {
		RupeeTradeList<RupeeTrade> buys = new RupeeTradeList<RupeeTrade>(RupeeTradeList.WILL_BUY);
		RupeeTradeList<RupeeTrade> sells = new RupeeTradeList<RupeeTrade>(RupeeTradeList.FOR_SALE);
		// BLACKSMITH
		sells.add(new RupeeTrade(new ItemStack(Items.arrow, 10), 15));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.swordKokiri), 50));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.shieldDeku), 100));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.tunicHeroHelm), 15));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.tunicHeroChest), 50));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.tunicHeroLegs), 20));
		sells.add(new RupeeTrade(new ItemStack(ZSSItems.tunicHeroBoots), 15));
		RupeeMerchantHelper.writeTradesToFile(buys, sells, VanillaRupeeMerchant.getDefaultTradeLocation(EnumVillager.BLACKSMITH));
	}
}

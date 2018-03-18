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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.api.entity.merchant.RupeeTradeTemplateList;
import zeldaswordskills.api.item.RupeeValueRegistry;
import zeldaswordskills.entity.VanillaRupeeMerchant;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
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
		RupeeTradeTemplateList buys = new RupeeTradeTemplateList(RupeeTradeList.WILL_BUY);
		RupeeTradeTemplateList sells = new RupeeTradeTemplateList(RupeeTradeList.FOR_SALE);
		// BARNES
		buys.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.gunpowder), 1));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.bomb, 1, BombType.BOMB_STANDARD.ordinal()), 8));
		RupeeMerchantHelper.writeTradesToFile(buys, sells, EntityNpcBarnes.DEFAULT_RUPEE_TRADES);
	}

	/**
	 * Creates the default trading lists for Vanilla Villagers
	 */
	private static void createDefaultVanillaTradeFiles() {
		RupeeTradeTemplateList buys = new RupeeTradeTemplateList(RupeeTradeList.WILL_BUY);
		RupeeTradeTemplateList sells = new RupeeTradeTemplateList(RupeeTradeList.FOR_SALE);
		// BLACKSMITH
		// TODO tested - working
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.arrow, 10), 15));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.swordKokiri), 50));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.shieldDeku), 100));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.tunicHeroHelm), 15));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.tunicHeroChest), 50));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.tunicHeroLegs), 20));
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.tunicHeroBoots), 15));
		RupeeMerchantHelper.writeTradesToFile(buys, sells, VanillaRupeeMerchant.getDefaultTradeLocation(EnumVillager.BLACKSMITH));
		// Test cases for randomized additional trades:
		// 1. Non-existent parent file
		ResourceLocation parent = new ResourceLocation("minecraft", "villager/missing");
		sells = new RupeeTradeTemplateList(RupeeTradeList.FOR_SALE);
		// a. Random enchantments
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.swordOrdon), 150, 0, 15, 0, 0.3F).setEnchanted());
		ItemStack stack = new ItemStack(Items.iron_sword);
		// b. Random enchantments on top of specified enchantment
		stack.addEnchantment(Enchantment.sharpness, 2);
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(stack, 150, 0, 15, 0, 0.3F).setEnchanted());
		RupeeMerchantHelper.writeTradesToFile(null, sells, VanillaRupeeMerchant.getDefaultRandomTradeLocation(EnumVillager.BUTCHER), parent);
		// 2. Valid parent file
		parent = new ResourceLocation("minecraft", "villager/generic_random");
		sells = new RupeeTradeTemplateList(RupeeTradeList.FOR_SALE);
		// a. Random price only; fixed stack size, no enchantments
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.cookie), 4, 0, 0.9F));
		// b. Enchanted not-usually-enchantable item; will it work? it shouldn't
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.iron_ingot), 15, 30, 30, 0, 0.6F));
		// c. Randomized stack size with scaling price
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.carrot), 4, 1, 4, 0, 0.6F));
		// d. Randomized stack size with absolute price; single use trade
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(Items.apple), 4, 1, 4, 1, 0.3F).setPriceAbsolute());
		// Write parent file
		RupeeMerchantHelper.writeTradesToFile(null, sells, parent);
		// 3. Child file with no trades other than what is in parent
		RupeeMerchantHelper.writeTradesToFile(null, null, VanillaRupeeMerchant.getDefaultRandomTradeLocation(EnumVillager.FARMER), parent);
		// 4. Child file with valid parent and at least one trade of its own
		sells = new RupeeTradeTemplateList(RupeeTradeList.FOR_SALE);
		// a. Unenchantable sword with random enchantments - does this break the interface?
		sells.add(RupeeValueRegistry.getRupeeTradeTemplate(new ItemStack(ZSSItems.swordMaster), 200, 50, 50, 1, 0.9F).setEnchanted());
		RupeeMerchantHelper.writeTradesToFile(null, sells, VanillaRupeeMerchant.getDefaultRandomTradeLocation(EnumVillager.PRIEST), parent);
	}
}

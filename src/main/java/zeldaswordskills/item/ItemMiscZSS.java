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

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.util.MerchantRecipeHelper;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Miscellaneous items with no use other than trading should go here.
 * Use an anonymous class to override onItemRightClick if it should do anything.
 *
 */
public class ItemMiscZSS extends BaseModItem implements IUnenchantable
{
	/** The price this item will fetch if sold to a villager */
	private final int sellPrice;

	public ItemMiscZSS(int price) {
		super();
		setMaxDamage(0);
		this.canRepair = false;
		this.sellPrice = price;
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass().isAssignableFrom(EntityVillager.class)) {
			handleTrade(stack, player, (EntityVillager) entity);
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	/**
	 * Called when left-clicking a villager with the item in hand
	 * @param stack The player's currently held item (stack.getItem() is 'this')
	 */
	protected void handleTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		MerchantRecipeList trades = villager.getRecipes(player);
		if (trades != null && sellPrice > 0) {
			MerchantRecipe trade = new MerchantRecipe(stack.copy(), new ItemStack(Items.emerald, sellPrice));
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

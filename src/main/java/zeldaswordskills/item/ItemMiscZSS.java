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

package zeldaswordskills.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.item.IRightClickEntity;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Miscellaneous items with no use other than trading should go here.
 * Use an anonymous class to override onItemRightClick if it should do anything.
 *
 */
public class ItemMiscZSS extends Item implements IRightClickEntity, IUnenchantable
{
	/** The price this item will fetch if sold to a villager */
	protected final int sellPrice;

	public ItemMiscZSS(int price) {
		super();
		setMaxDamage(0);
		this.canRepair = false;
		this.sellPrice = price;
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			handleTrade(stack, player, (EntityVillager) entity);
		}
		return true;
	}

	@Override
	public boolean onRightClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			handleTrade(stack, player, (EntityVillager) entity);
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		itemIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		list.add(StatCollector.translateToLocal("tooltip." + getUnlocalizedName().substring(5) + ".desc.0"));
	}

	/**
	 * Called when left- or right-clicking a villager with the item in hand
	 * @param stack The player's currently held item (stack.getItem() is 'this')
	 */
	protected void handleTrade(ItemStack stack, EntityPlayer player, EntityVillager villager) {
		if (this.sellPrice > 0) {
			RupeeTrade trade = new RupeeTrade(new ItemStack(stack.getItem(), 1, stack.getItemDamage()), this.sellPrice);
			RupeeMerchantHelper.addVillagerRupeeTrade(player, trade, villager, null, EntityVillager.class, 0.2F);
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.generic.sorry.0");
		}
	}
}

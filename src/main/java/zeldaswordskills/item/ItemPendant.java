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

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * The three Pendants of Virtue are required to attain the Master Sword.
 * 
 * The Pendant of Courage (Farore) is found in the Eastern Palace, located in a desert canyon.
 * The Pendant of Power (Din) is found on Death Mountain
 * The Pendant of Wisdom (Nayru) is found in the House of Gales, hidden under a lake
 *
 */
public class ItemPendant extends Item
{
	/** The three Pendants of Virtue */
	public static enum PendantType {POWER,WISDOM,COURAGE};
	
	/** Unlocalized name suffixes */
	private String[] names = {"_power","_wisdom","_courage"};
	
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public ItemPendant(int id) {
		super(id);
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass().isAssignableFrom(EntityVillager.class)) {
			EntityVillager villager = (EntityVillager) entity;
			if (villager.getProfession() == 2) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.priest.0");
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.priest.1");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.villager");
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		return iconArray[par1 % names.length];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + names[stack.getItemDamage() % names.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs tab, List list) {
		for (int i = 0; i < names.length; ++i) {
			list.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[names.length];
		for (int i = 0; i < names.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":pendant" + names[i]);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.pendant.desc.0"));
	}
}

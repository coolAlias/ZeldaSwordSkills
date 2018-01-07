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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSVillagerInfo.EnumVillager;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * The three Pendants of Virtue are required to attain the Master Sword.
 * 
 * The Pendant of Courage (Farore) is found in the Eastern Palace, located in a desert canyon.
 * The Pendant of Power (Din) is found on Death Mountain
 * The Pendant of Wisdom (Nayru) is found in the House of Gales, hidden under a lake
 *
 */
public class ItemPendant extends BaseModItem implements IUnenchantable
{
	/** The three Pendants of Virtue */
	public static enum PendantType {
		POWER("power", 1),
		WISDOM("wisdom", 2),
		COURAGE("courage", 4);
		public final String unlocalizedName;
		/** Bit flag used in Pedestal Block/TileEntity */
		public final int bitFlag;
		private PendantType(String name, int bitFlag) {
			this.unlocalizedName = name;
			this.bitFlag = bitFlag;
		}
		/**
		 * Returns pendant type by damage value (0, 1, or 2), NOT the same as the bit flag
		 */
		public static PendantType byDamage(int damage) {
			return PendantType.values()[damage % PendantType.values().length];
		}
	};

	public ItemPendant() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity instanceof EntityVillager) {
			EntityVillager villager = (EntityVillager) entity;
			if (EnumVillager.PRIEST.is(villager)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.priest.0");
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.priest.1");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.pendant.villager");
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + PendantType.byDamage(stack.getItemDamage()).unlocalizedName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (PendantType type : PendantType.values()) {
			list.add(new ItemStack(item, 1, type.ordinal()));
		}
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[PendantType.values().length];
		for (PendantType type : PendantType.values()) {
			variants[type.ordinal()] = ModInfo.ID + ":pendant_" + type.unlocalizedName;
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.pendant.desc.0"));
	}
}

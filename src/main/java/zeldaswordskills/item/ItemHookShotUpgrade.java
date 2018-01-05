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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * All HookShot upgrades belong to this class. Handles adding tooltip and custom trades.
 *
 */
public class ItemHookShotUpgrade extends BaseModItem implements IUnenchantable
{
	/** Current types of available hookshot upgrades */
	public static enum UpgradeType {
		EXTENDER("extender"),
		CLAW("claw"),
		MULTI("multi");
		public final String unlocalizedName;
		private UpgradeType(String name) {
			this.unlocalizedName = name;
		}
		/**
		 * Return upgrade type from item damage
		 */
		public static UpgradeType fromDamage(int damage) {
			return values()[damage % values().length];
		}
	};

	public ItemHookShotUpgrade() {
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/** Returns this addon's enum Type from stack damage value */
	public UpgradeType getType(int damage) {
		return (damage > -1 ? UpgradeType.values()[damage % UpgradeType.values().length] : UpgradeType.EXTENDER);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.use.fail.0");
		}
		return stack;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (!player.worldObj.isRemote && entity.getClass() == EntityVillager.class) {
			int profession = ((EntityVillager) entity).getProfession();
			PlayerUtils.sendTranslatedChat(player, "chat.zss.hookshot.upgrade." + profession);
		}
		return true;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
		return entity instanceof EntityVillager;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "_" + UpgradeType.fromDamage(stack.getItemDamage()).unlocalizedName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (int i = 0; i < UpgradeType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String[] getVariants() {
		String[] variants = new String[UpgradeType.values().length];
		for (UpgradeType upgrade : UpgradeType.values()) {
			variants[upgrade.ordinal()] = ModInfo.ID + ":hookshot_upgrade_" + upgrade.unlocalizedName; 
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip.zss.hookshot.upgrade." + getType(stack.getItemDamage()).unlocalizedName));
	}
}

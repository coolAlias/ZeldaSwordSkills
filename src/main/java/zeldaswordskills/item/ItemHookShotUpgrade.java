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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

public class ItemHookShotUpgrade extends Item implements IUnenchantable
{
	/** Current types of add-ons available */
	public static enum AddonType {
		EXTENSION("extender"),
		STONECLAW("claw"),
		MULTI("multi");
		public final String unlocalizedName;
		private AddonType(String unlocalizedName) {
			this.unlocalizedName = unlocalizedName;
		}
	};

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public ItemHookShotUpgrade() {
		super();
		setMaxStackSize(1);
		setHasSubtypes(true);
		setCreativeTab(ZSSCreativeTabs.tabTools);
	}

	/** Returns this addon's enum Type from stack damage value */
	public AddonType getType(int damage) {
		return (damage > -1 ? AddonType.values()[damage % AddonType.values().length] : AddonType.EXTENSION);
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
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int type) {
		return iconArray[getType(type).ordinal()];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName() + "." + stack.getItemDamage();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < AddonType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		iconArray = new IIcon[AddonType.values().length];
		for (int i = 0; i < AddonType.values().length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + AddonType.values()[i].unlocalizedName);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean advanced) {
		list.add(StatCollector.translateToLocal("tooltip.zss.hookshot.upgrade." + getType(stack.getItemDamage()).unlocalizedName));
	}
}

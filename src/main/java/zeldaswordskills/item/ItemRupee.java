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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import com.google.common.collect.Maps;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import zeldaswordskills.api.item.IHandlePickup;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;

public class ItemRupee extends Item implements IHandlePickup
{
	public static enum Rupee {
		GREEN_RUPEE("green", 1),
		BLUE_RUPEE("blue", 5),
		YELLOW_RUPEE("yellow", 10),
		RED_RUPEE("red", 20),
		PURPLE_RUPEE("purple", 50),
		SILVER_RUPEE("silver", 100),
		GOLD_RUPEE("gold", 200);
		public final String color;
		public final int value;
		private Rupee(String name, int value) {
			this.color = name;
			this.value = value;
		}
		public static Rupee byDamage(int damage) {
			return Rupee.values()[damage % Rupee.values().length];
		}
		/**
		 * Returns a map containing the least quantity of each Rupee type
		 * required to meet the amount of rupees requested.
		 * All Rupee types are present as map keys; any not needed to fill
		 * the request have a value of 0.
		 */
		public static EnumMap<Rupee, Integer> getRupeeStackSizes(int amount) {
			EnumMap<Rupee, Integer> map = Maps.newEnumMap(Rupee.class);
			for (int i = Rupee.values().length; i > 0; --i) {
				Rupee rupee = Rupee.byDamage(i - 1);
				int n = amount / rupee.value;
				map.put(rupee, n);
				amount -= n * rupee.value;
			}
			return map;
		}
	}

	@SideOnly(Side.CLIENT)
	private List<IIcon> icons;

	@SideOnly(Side.CLIENT)
	private IIcon graySmall, grayBig;

	public ItemRupee() {
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("zss.rupee");
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	@Override
	public boolean onPickupItem(ItemStack stack, EntityPlayer player) {
		int n = ZSSPlayerWallet.get(player).addRupees(stack).stackSize;
		stack.stackSize = (Config.alwaysPickUpRupees() ? 0 : n);
		return true;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Rupee.byDamage(stack.getItemDamage()).color;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return icons.get(damage % icons.size());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconIndex(ItemStack stack) {
		int damage = stack.getItemDamage();
		// Customized item damage from ContainerWallet for 'empty' stack icon
		if (damage >= Rupee.values().length) {
			damage -= Rupee.values().length;
			return damage < Rupee.SILVER_RUPEE.ordinal() ? this.graySmall : this.grayBig;
		}
		return this.getIconFromDamage(damage);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (Rupee rupee : Rupee.values()) {
			list.add(new ItemStack(item, 1, rupee.ordinal()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icons = new ArrayList<IIcon>(Rupee.values().length);
		for (Rupee rupee : Rupee.values()) {
			icons.add(register.registerIcon(ModInfo.ID + ":rupee_" + rupee.color));
		}
		this.graySmall = register.registerIcon(ModInfo.ID + ":rupee_gray_small");
		this.grayBig = register.registerIcon(ModInfo.ID + ":rupee_gray_big");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		int value = Rupee.byDamage(stack.getItemDamage()).value;
		if (value > 1) {
			list.add(StatCollector.translateToLocalFormatted("tooltip.zss.rupee.plural.desc", value));
		} else {
			list.add(StatCollector.translateToLocal("tooltip.zss.rupee.desc"));
		}
	}
}

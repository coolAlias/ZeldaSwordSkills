/**
    Copyright (C) <2014> <coolAlias>

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
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemInstrument extends Item
{
	public static enum Instrument {
		OCARINA("ocarina", GuiHandler.GUI_OCARINA);

		private final String unlocalizedName;

		private final int guiId;

		private Instrument(String name, int guiId) {
			this.unlocalizedName = name;
			this.guiId = guiId;
		}

		public String getUnlocalizedName() {
			return unlocalizedName;
		}

		public int getGuiId() {
			return guiId;
		}
	}

	@SideOnly(Side.CLIENT)
	private List<IIcon> icons;

	public ItemInstrument() {
		super();
		setMaxDamage(0);
		setHasSubtypes(true);
		setUnlocalizedName("zss.instrument");
		setCreativeTab(ZSSCreativeTabs.tabMisc);
	}

	public Instrument getInstrument(ItemStack stack) {
		return Instrument.values()[stack.getItemDamage() % Instrument.values().length];
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) { // instruments have client-side only Guis
			player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		}
		return stack;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Instrument.values()[stack.getItemDamage() % Instrument.values().length].unlocalizedName;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int damage) {
		return icons.get(damage % icons.size());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (Instrument instrument : Instrument.values()) {
			list.add(new ItemStack(item, 1, instrument.ordinal()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister register) {
		icons = new ArrayList<IIcon>(Instrument.values().length);
		for (Instrument instrument : Instrument.values()) {
			icons.add(register.registerIcon(ModInfo.ID + ":" + instrument.getUnlocalizedName()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean par4) {
		list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.instrument." + getInstrument(stack).getUnlocalizedName() + ".desc"));
	}
}

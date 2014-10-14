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

import net.minecraft.block.Block;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.ZeldaSong;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemInstrument extends Item
{
	public static enum Instrument {
		OCARINA_FAIRY("ocarina_fairy", GuiHandler.GUI_OCARINA, false),
		OCARINA_TIME("ocarina_time", GuiHandler.GUI_OCARINA, true);

		private final String unlocalizedName;

		private final int guiId;

		/** Whether songs performed with this instrument are capable of having a real effect */
		private final boolean isPotent;

		private Instrument(String name, int guiId, boolean isPotent) {
			this.unlocalizedName = name;
			this.guiId = guiId;
			this.isPotent = isPotent;
		}

		public String getUnlocalizedName() {
			return unlocalizedName;
		}

		public int getGuiId() {
			return guiId;
		}

		/** Whether songs performed with this instrument are capable of having a real effect */
		public boolean doSongsHaveEffect() {
			return isPotent;
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

	/**
	 * Returns true if a {@link ZeldaSong} played by this stack is capable of having a real effect
	 */
	public boolean doSongsHaveEffect(ItemStack stack) {
		return getInstrument(stack).doSongsHaveEffect();
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) { // instruments have client-side only Guis
			player.openGui(ZSSMain.instance, getInstrument(stack).getGuiId(), world, MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		}
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			if (isScarecrowAt(world, x, y, z) && ZSSPlayerSongs.get(player).canOpenScarecrowGui(true)) {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_SCARECROW, world, x, y, z);
			}
		}
		return true;
	}

	/**
	 * Returns true if the blocks around x/y/z form a scarecrow figure,
	 * assuming that x/y/z is one of the central blocks (not the 'arms')
	 */
	private boolean isScarecrowAt(World world, int x, int y, int z) {
		int i = 0;
		while (i < 2 && world.getBlock(x, y, z) == Blocks.hay_block) {
			++i;
			++y;
		}
		// should now always have the head
		Block block = world.getBlock(x, y, z);
		if (block instanceof BlockPumpkin) {
			--y;
			for (int dy = i; dy < 2; ++dy) {
				if (world.getBlock(x, y - dy, z) != Blocks.hay_block) {
					return false;
				}
			}
			if (world.getBlock(x + 1, y, z) == Blocks.hay_block && world.getBlock(x - 1, y, z) == Blocks.hay_block) {
				return true;
			}
			if (world.getBlock(x, y, z + 1) == Blocks.hay_block && world.getBlock(x, y, z - 1) == Blocks.hay_block) {
				return true;
			}
		}
		return false;
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

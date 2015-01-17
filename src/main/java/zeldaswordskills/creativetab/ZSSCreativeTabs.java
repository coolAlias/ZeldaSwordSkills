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

package zeldaswordskills.creativetab;

import java.util.Collections;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.item.ZSSItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSCreativeTabs
{
	/**
	 * 
	 * Sorts creative tab using {@link ZSSItems#itemstackComparator}, allowing
	 * tabs to be sorted correctly even on old world saves.
	 *
	 */
	public abstract static class ZSSCreativeTab extends CreativeTabs {
		public ZSSCreativeTab(String label) {
			super(label);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllReleventItems(List itemstacks) {
			super.displayAllReleventItems(itemstacks);
			Collections.sort(itemstacks, ZSSItems.itemstackComparator);
		}
	}

	public static CreativeTabs tabBlocks = new ZSSCreativeTab("zss.blocks") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return Item.getItemFromBlock(ZSSBlocks.pedestal);
		}
	};

	public static CreativeTabs tabCombat = new ZSSCreativeTab("zss.combat") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.swordMasterTrue;
		}
	};

	public static CreativeTabs tabTools = new ZSSCreativeTab("zss.tools") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.bombBag;
		}
	};

	public static CreativeTabs tabSkills = new ZSSCreativeTab("zss.skills") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.skillOrb;
		}
	};

	public static CreativeTabs tabMasks = new ZSSCreativeTab("zss.masks") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.maskHawkeye;
		}
	};

	public static CreativeTabs tabMisc = new ZSSCreativeTab("zss.misc") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.pendant;
		}
	};

	public static CreativeTabs tabKeys = new ZSSCreativeTab("zss.keys") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.keySmall;
		}
	};

	public static CreativeTabs tabEggs = new ZSSCreativeTab("zss.eggs") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.eggSpawner;
		}
	};
}

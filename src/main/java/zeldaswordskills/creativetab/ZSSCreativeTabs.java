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
import net.minecraft.util.StatCollector;
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
	
	public static CreativeTabs tabBlocks = new ZSSCreativeTab("zssTabBlocks") {
		@Override
		@SideOnly(Side.CLIENT)
		public int getTabIconItemIndex() {
			return ZSSBlocks.pedestal.blockID;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.block");
		}
	};
	
	public static CreativeTabs tabCombat = new ZSSCreativeTab("zssTabCombat") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.swordMasterTrue;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.combat");
		}
	};

	public static CreativeTabs tabTools = new ZSSCreativeTab("zssTabTools") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.bombBag;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.tools");
		}
	};

	public static CreativeTabs tabSkills = new ZSSCreativeTab("zssTabSkills") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.skillOrb;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.skill");
		}
	};

	public static CreativeTabs tabMasks = new ZSSCreativeTab("zssTabMasks") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.maskHawkeye;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.masks");
		}
	};

	public static CreativeTabs tabMisc = new ZSSCreativeTab("zssTabMisc") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.pendant;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.misc");
		}
	};

	public static CreativeTabs tabKeys = new ZSSCreativeTab("zssTabKeys") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.keySmall;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.keys");
		}
	};

	public static CreativeTabs tabEggs = new ZSSCreativeTab("zssTabEggs") {
		@Override
		@SideOnly(Side.CLIENT)
		public Item getTabIconItem() {
			return ZSSItems.eggSpawner;
		}

		@Override
		public String getTranslatedTabLabel() {
			return StatCollector.translateToLocal("creativetab.zss.eggs");
		}
	};
}

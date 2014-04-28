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

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.StatCollector;
import zeldaswordskills.item.ZSSItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSTabKeys extends CreativeTabs
{
	public ZSSTabKeys(String label) {
		super(label);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getTabIconItemIndex() {
		return ZSSItems.keySmall.itemID;
	}

	@Override
	public String getTranslatedTabLabel() {
		return StatCollector.translateToLocal("creativetab.zss.keys");
	}
}

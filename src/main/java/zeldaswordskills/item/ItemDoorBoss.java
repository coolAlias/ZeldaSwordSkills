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
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.BossType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDoorBoss extends ItemDoorLocked
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public ItemDoorBoss(int id) {
		super(id, ZSSBlocks.doorLocked);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return StatCollector.translateToLocalFormatted(getUnlocalizedName() + ".name",
				BossType.values()[stack.getItemDamage() % BossType.values().length].getDisplayName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int item, CreativeTabs tab, List list) {
		for (int i = 0; i < BossType.values().length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int damage) {
		return iconArray[damage % BossType.values().length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[BossType.values().length];
		for (int i = 0; i < iconArray.length; ++i) {
			iconArray[i] = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9).toLowerCase() + i);
		}
	}
}

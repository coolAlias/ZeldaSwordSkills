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

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSecretStone extends ItemBlockWithMetadata {

	public ItemSecretStone(int id, Block block) {
		super(id, block);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		return StatCollector.translateToLocal(getUnlocalizedName() + (stack.getItemDamage() > 7 ? ".name.unbreakable" : ".name"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack,	EntityPlayer player, List list, boolean isHeld) {
		if (stack.getItemDamage() > 7) {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.block.unbreakable.desc"));
		} else {
			list.add(EnumChatFormatting.ITALIC + StatCollector.translateToLocal("tooltip.zss.block.dungeon.desc.0"));
		}
	}
}

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

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import zeldaswordskills.block.BlockTime;

public class ItemBlockTime extends ItemBlockWithMetadata {

	public ItemBlockTime(int id, Block block) {
		super(id, block);
	}

	@Override
	public String getItemDisplayName(ItemStack stack) {
		String s = BlockTime.names[stack.getItemDamage() % BlockTime.names.length];
		return StatCollector.translateToLocal("tile.zss." + s + ".name");
	}
}

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.OpenGossipStoneEditorPacket;

public class ItemGossipStone extends ItemMetadataBlock {

	public ItemGossipStone(Block block) {
		super(block);
		setMaxStackSize(16);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return StatCollector.translateToLocal(getUnlocalizedName() + (stack.getItemDamage() > 0 ? ".name.unbreakable" : ".name"));
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote || side == 0) {
			return false;
		} else if (!world.getBlock(x, y, z).getMaterial().isSolid()) {
			return false;
		} else {
			switch(side) {
			case 1: ++y; break;
			case 2: --z; break;
			case 3: ++z; break;
			case 4: --x; break;
			case 5: ++x; break;
			}
			Block block = ZSSBlocks.gossipStone;
			if (!player.canPlayerEdit(x, y, z, side, stack)) {
				return false;
			} else if (!block.canPlaceBlockAt(world, x, y, z)) {
				return false;
			} else if (!world.setBlock(x, y, z, block, stack.getItemDamage(), 3)) {
				return false;
			}
			block.onBlockPlacedBy(world, x, y, z, player, stack);
			block.onPostBlockPlaced(world, x, y, z, stack.getItemDamage());
			if (!player.capabilities.isCreativeMode) {
				--stack.stackSize;
			}
			if (player instanceof EntityPlayerMP) {
				PacketDispatcher.sendTo(new OpenGossipStoneEditorPacket(x, y, z), (EntityPlayerMP) player);
			}
			return true;
		}
	}
}

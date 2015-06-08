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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.OpenGossipStoneEditorPacket;

public class ItemGossipStone extends ItemBlockUnbreakable {

	public ItemGossipStone(Block block) {
		super(block);
		setMaxStackSize(16);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (!super.onItemUse(stack, player, world, pos, face, hitX, hitY, hitZ)) {
			return false;
		} else if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new OpenGossipStoneEditorPacket(pos.offset(face)), (EntityPlayerMP) player);
		}
		return true;
	}
}

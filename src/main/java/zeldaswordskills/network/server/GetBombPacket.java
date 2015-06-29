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

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.CustomPacket.CustomServerPacket;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class GetBombPacket extends CustomServerPacket {

	public GetBombPacket() {}

	@Override
	public void write(ByteArrayDataOutput out) throws IOException {}

	@Override
	public void read(ByteArrayDataInput in) throws IOException {}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		ItemStack heldItem = player.getHeldItem();
		ItemStack mask = player.getCurrentArmor(ArmorIndex.WORN_HELM);
		if (mask != null && mask.getItem() == ZSSItems.maskBlast) {
			((ItemMask) mask.getItem()).explode(player, mask, player.worldObj, player.posX, player.posY, player.posZ);
		} else if (player.isSneaking() && heldItem != null && heldItem.getItem() instanceof ItemBombBag) {
			((ItemBombBag) heldItem.getItem()).emptyBag(heldItem, player);
		} else {
			for (ItemStack invStack : player.inventory.mainInventory) {
				if (invStack != null && invStack.getItem() instanceof ItemBombBag) {
					ItemBombBag bombBag = (ItemBombBag) invStack.getItem();
					if (player.capabilities.isCreativeMode || bombBag.removeBomb(invStack)) {
						// TODO attempt to merge stackable items with inventory before dropping
						if (heldItem != null && (heldItem.isStackable() || !player.inventory.addItemStackToInventory(heldItem))) {
							player.dropPlayerItem(heldItem);
						}
						int type = bombBag.getBagBombType(invStack);
						player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.bomb, 1, (type > 0 ? type : 0)));
						break;
					}
				}
			}
		}
	}
}

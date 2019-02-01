/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.server;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

public class GetBombPacket extends AbstractServerMessage<GetBombPacket>
{
	public GetBombPacket() {}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {}

	@Override
	protected void process(EntityPlayer player, Side side) {
		ItemStack heldItem = player.getHeldItem();
		ItemStack mask = player.getCurrentArmor(ArmorIndex.WORN_HELM);
		if (mask != null && mask.getItem() == ZSSItems.maskBlast) {
			((ItemMask) mask.getItem()).explode(player, mask, player.worldObj, player.posX, player.posY, player.posZ);
		} else if (player.isSneaking() && heldItem != null && heldItem.getItem() instanceof ItemBombBag) {
			((ItemBombBag) heldItem.getItem()).emptyBag(heldItem, player);
		} else {
			// TODO should check if held item is a bomb bag first and take bomb from there
			for (ItemStack invStack : player.inventory.mainInventory) {
				if (invStack != null && invStack.getItem() instanceof ItemBombBag) {
					ItemBombBag bombBag = (ItemBombBag) invStack.getItem();
					ItemStack bomb = bombBag.removeBomb(player, invStack);
					if (bomb != null) {
						// TODO attempt to merge stackable items with inventory before dropping
						if (heldItem != null && (heldItem.isStackable() || !player.inventory.addItemStackToInventory(heldItem))) {
							player.dropPlayerItemWithRandomChoice(heldItem, false);
						}
						player.setCurrentItemOrArmor(0, bomb);
						break;
					}
				}
			}
		}
	}
}

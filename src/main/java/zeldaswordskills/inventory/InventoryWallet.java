/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.item.ItemRupee;

public class InventoryWallet extends InventoryBasic
{
	private final ZSSPlayerWallet wallet;

	public InventoryWallet(EntityPlayer player) {
		super("", false, ItemRupee.Rupee.values().length);
		this.wallet = ZSSPlayerWallet.get(player);
	}

	@Override
	public void markDirty() {
		// Recalculate total rupees and sync wallet
		int total = 0;
		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack stack = this.getStackInSlot(i);
			if (stack != null) {
				int value = ItemRupee.Rupee.byDamage(i).value;
				total += stack.stackSize * value;
			}
		}
		this.wallet.setRupees(total);
		super.markDirty();
	}
}

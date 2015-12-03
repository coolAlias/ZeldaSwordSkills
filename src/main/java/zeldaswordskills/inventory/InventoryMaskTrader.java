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

package zeldaswordskills.inventory;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.item.ZSSItems;

public class InventoryMaskTrader extends AbstractInventory
{
	private static ItemStack zoraMask;
	static {
		zoraMask = new ItemStack(ZSSItems.maskZora);
		zoraMask.addEnchantment(Enchantment.respiration, 3);
	}

	private static final ItemStack[] masks = {
		new ItemStack(ZSSItems.maskBunny),
		new ItemStack(ZSSItems.maskCouples),
		new ItemStack(ZSSItems.maskKeaton),
		new ItemStack(ZSSItems.maskScents),
		new ItemStack(ZSSItems.maskSkull),
		new ItemStack(ZSSItems.maskSpooky),
		new ItemStack(ZSSItems.maskTruth),
		new ItemStack(ZSSItems.maskDeku),
		new ItemStack(ZSSItems.maskGoron),
		new ItemStack(ZSSItems.maskGerudo),
		InventoryMaskTrader.zoraMask,
		new ItemStack(ZSSItems.maskFierce)
	};

	public InventoryMaskTrader() {
		inventory = masks;
	}

	@Override
	public String getInventoryName() {
		return "gui.zss.mask_trader.name";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getBorrowedMask() == null;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return false;
	}
}

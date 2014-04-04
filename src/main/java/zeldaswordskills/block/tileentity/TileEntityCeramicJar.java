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

package zeldaswordskills.block.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

/**
 * 
 * A jar that holds one ItemStack and sucks in nearby items if inventory is empty
 *
 */
public class TileEntityCeramicJar extends TileEntityInventory {

	public TileEntityCeramicJar() {
		inventory = new ItemStack[1];
	}

	private boolean shouldUpdate() {
		return (worldObj.getWorldTime() % 64 == 0 && worldObj.rand.nextInt(8) == 0 &&
				worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 16.0D) != null);
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote && getStackInSlot(0) == null && shouldUpdate()) {
			List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getAABBPool().
					getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1).expand(1.0D, 1.0D, 1.0D));
			for (EntityItem item : list) {
				ItemStack stack;
				if (item.getEntityItem().stackSize > 64) {
					stack = item.getEntityItem().splitStack(64);
				} else {
					stack = item.getEntityItem().copy();
					item.setDead();
				}
				setInventorySlotContents(0, stack);
				break;
			}
		}
	}

	@Override
	public String getInvName() {
		return "";
	}

	@Override
	public boolean isInvNameLocalized() {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}
}

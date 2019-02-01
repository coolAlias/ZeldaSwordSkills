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

package zeldaswordskills.block.tileentity;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import zeldaswordskills.ref.Config;

/**
 * 
 * A jar that holds one ItemStack and sucks in nearby items if inventory is empty
 *
 */
public class TileEntityCeramicJar extends TileEntityInventory implements ITickable
{
	public TileEntityCeramicJar() {
		inventory = new ItemStack[1];
	}

	private boolean shouldUpdate() {
		return (worldObj.getTotalWorldTime() % 20 == 0 && worldObj.rand.nextInt(8) == 0 &&
				worldObj.getClosestPlayer(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, 16.0D) != null);
	}

	@Override
	public void update() {
		if (!Config.doJarsUpdate()) {
			return;
		}
		if (!worldObj.isRemote && getStackInSlot(0) == null && shouldUpdate()) {
			BlockPos pos = new BlockPos(getPos().getX() + 1, getPos().getY() + 1, getPos().getZ() + 1);
			List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos(), pos).expand(1.0D, 1.0D, 1.0D));
			for (EntityItem item : list) {
				if (!item.isEntityAlive() || item.cannotPickup()) {
					continue;
				}
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

	@Override
	public void markDirty() {
		super.markDirty();
		worldObj.markBlockForUpdate(getPos());
	}

	@Override
	public Packet<?> getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(getPos(), 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}
}

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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import zeldaswordskills.item.ItemPendant;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.SwordPedestalPacket;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A sword pedestal that first requires all three Pendants of Virtue to unlock.
 *
 */
public class TileEntityPedestal extends TileEntityInventory
{
	/** The sword stored in this pedestal */
	private ItemStack sword;

	/** Used to stop sound from playing when block placed by a player */
	private boolean playSound = true;

	/** Sword's orientation (0 or 1, like anvils) */
	private byte orientation = 0;

	public TileEntityPedestal() {
		inventory = new ItemStack[3];
	}

	/**
	 * Changes sword's orientation by 90 degrees
	 */
	public void changeOrientation() {
		if (hasSword()) {
			orientation = (byte)(orientation == 0 ? 1 : 0);
			sendPacketToAllAround();
		}
	}

	/** Returns on which axis the sword is aligned */
	public byte getOrientation() { return orientation; }

	/** Returns the sword itemstack currently in the pedestal or null */
	public ItemStack getSword() { return sword; }

	/** Returns whether there is a sword currently in this pedestal */
	public boolean hasSword() { return sword != null; }

	/**
	 * Retrieves the sword from the pedestal, if any, spawning the appropriate EntityItem
	 */
	public void retrieveSword() {
		if (sword != null) {
			worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_SWORDSTRIKE, 1.0F, 1.0F);
			WorldUtils.spawnItemWithRandom(worldObj, sword, xCoord, yCoord + 1, zCoord);
			sword = null;
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			sendPacketToAllAround();
		}
	}

	/**
	 * Places a copy of the sword into the pedestal, returning true if successful
	 */
	public boolean setSword(ItemStack stack) {
		if (sword == null && stack != null && stack.getItem() instanceof ItemSword) {
			if (stack.getItem() == ZSSItems.swordGolden && stack.hasTagCompound() &&
					stack.getTagCompound().hasKey("SacredFlames") &&
					stack.getTagCompound().getInteger("SacredFlames") == 0x7)
			{
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_MASTER_SWORD, 1.0F, 1.0F);
				ItemStack master = new ItemStack(ZSSItems.swordMasterTrue);
				master.addEnchantment(Enchantment.sharpness, 5);
				master.addEnchantment(Enchantment.knockback, 2);
				master.addEnchantment(Enchantment.fireAspect, 2);
				master.addEnchantment(Enchantment.looting, 3);
				sword = master;
			} else {
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_SWORDSTRIKE, 1.0F, 1.0F);
				sword = stack.copy();
			}
			orientation = 0;
			worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, worldObj.getBlockId(xCoord, yCoord, zCoord));
			sendPacketToAllAround();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Called from client side packet to update sword and orientation
	 */
	@SideOnly(Side.CLIENT)
	public void setSword(ItemStack stack, byte orientation) {
		this.sword = stack;
		this.orientation = orientation;
	}

	/**
	 * Call when the block is placed by a player; re-sets the inventory contents
	 */
	public void onBlockPlaced() {
		for (int i = 0; i < getSizeInventory(); ++i) {
			inventory[i] = new ItemStack(ZSSItems.pendant,1,i);
		}
		playSound = false;
		onInventoryChanged();
	}

	@Override
	public boolean canUpdate() { return false; }

	@Override
	public void onInventoryChanged() {
		super.onInventoryChanged();
		int meta = 0;
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null) {
				meta |= (i == 2 ? i << 1 : i + 1);
			}
		}

		if (meta == 0x7) {
			meta = 0x8;
			if (playSound) {
				worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_MASTER_SWORD, 1.0F, 1.0F);
				retrieveSword();
			} else {
				playSound = true;
			}
		}

		if (meta != blockMetadata || worldObj.getBlockMetadata(xCoord, yCoord, zCoord) != meta) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta, 2);
		}
	}

	@Override
	public String getInvName() { return ""; }

	@Override
	public boolean isInvNameLocalized() { return true; }

	@Override
	public int getInventoryStackLimit() { return 1; }

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this ? false : player.getDistanceSq((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64.0D;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return (stack != null && stack.getItem() instanceof ItemPendant && stack.getItemDamage() == slot);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		readFromNBT(packet.data);
	}
	
	/** Sends description packet to all around */
	private void sendPacketToAllAround() {
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 64.0D, worldObj.provider.dimensionId,
				new SwordPedestalPacket(xCoord, yCoord, zCoord, sword, orientation).makePacket());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.getBoolean("hasSword")) {
			sword = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("Sword"));
			orientation = (compound.hasKey("orientation") ? compound.getByte("orientation") : 0);
		} else {
			sword = null;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (sword != null) {
			compound.setBoolean("hasSword", true);
			compound.setTag("Sword", sword.writeToNBT(new NBTTagCompound()));
			compound.setByte("orientation", orientation);
		} else {
			compound.setBoolean("hasSword", false);
		}
	}
}

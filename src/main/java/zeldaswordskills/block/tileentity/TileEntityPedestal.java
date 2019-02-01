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

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.item.ItemPendant;
import zeldaswordskills.item.ItemZeldaSword;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.WorldUtils;

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
	private byte orientation;

	public TileEntityPedestal() {
		inventory = new ItemStack[3];
	}

	/**
	 * Returns the amount of redstone power provided by the implanted sword, if any
	 */
	public int getPowerLevel() {
		if (sword != null && sword.getItem() instanceof ItemZeldaSword && ((ItemZeldaSword) sword.getItem()).isMasterSword()) {
			return sword.getItem() == ZSSItems.swordMaster || Config.getMasterSwordsProvidePower() ? 15 : 0;
		}
		return 0;
	}

	/**
	 * Changes sword's orientation by 90 degrees
	 */
	public void changeOrientation() {
		if (hasSword()) {
			orientation = (byte)(orientation == 0 ? 1 : 0);
			worldObj.markBlockForUpdate(getPos());
		}
	}

	/** Returns on which axis the sword is aligned */
	public byte getOrientation() {
		return orientation;
	}

	/** Returns the sword itemstack currently in the pedestal or null */
	public ItemStack getSword() {
		return sword;
	}

	/** Returns whether there is a sword currently in this pedestal */
	public boolean hasSword() {
		return sword != null;
	}

	/**
	 * Retrieves the sword from the pedestal, if any, spawning the appropriate EntityItem
	 */
	public void retrieveSword() {
		if (sword != null) {
			WorldUtils.playSoundAt(worldObj, getPos().getX() + 0.5D, getPos().getY() + 1, getPos().getZ() + 0.5D, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
			WorldUtils.spawnItemWithRandom(worldObj, sword, getPos().getX() + 0.5D, getPos().getY() + 1, getPos().getZ() + 0.5D);
			sword = null;
			worldObj.notifyNeighborsOfStateChange(getPos(), blockType);
			worldObj.markBlockForUpdate(getPos());
		}
	}

	/**
	 * Places a copy of the sword into the pedestal, returning true if successful
	 * @param player may be null
	 */
	public boolean setSword(ItemStack stack, EntityPlayer player) {
		if (sword == null && stack != null && WeaponRegistry.INSTANCE.isSword(stack.getItem())) {
			if (stack.getItem() == ZSSItems.swordGolden && stack.hasTagCompound() &&
					stack.getTagCompound().hasKey("SacredFlames") &&
					stack.getTagCompound().getInteger("SacredFlames") == 0x7)
			{
				worldObj.playSoundEffect(getPos().getX() + 0.5D, getPos().getY() + 1, getPos().getZ() + 0.5D, Sounds.MASTER_SWORD, 1.0F, 1.0F);
				ItemStack master = new ItemStack(ZSSItems.swordMasterTrue);
				master.addEnchantment(Enchantment.sharpness, 5);
				master.addEnchantment(Enchantment.knockback, 2);
				master.addEnchantment(Enchantment.fireAspect, 2);
				master.addEnchantment(Enchantment.looting, 3);
				sword = master;
				if (player != null) {
					player.triggerAchievement(ZSSAchievements.swordTrue);
				}
			} else {
				WorldUtils.playSoundAt(worldObj, getPos().getX() + 0.5D, getPos().getY() + 1, getPos().getZ() + 0.5D, Sounds.SWORD_STRIKE, 0.4F, 0.5F);
				sword = stack.copy();
			}
			if (player != null) {
				orientation = (byte)(EnumFacing.fromAngle(player.rotationYaw).getAxis() == EnumFacing.Axis.Z ? 0 : 1);
			}
			worldObj.notifyNeighborsOfStateChange(pos, blockType);
			worldObj.markBlockForUpdate(getPos());
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Call when the block is placed by a player; re-sets the inventory contents
	 */
	public void onBlockPlaced() {
		for (int i = 0; i < getSizeInventory(); ++i) {
			inventory[i] = new ItemStack(ZSSItems.pendant,1,i);
		}
		playSound = false;
		markDirty();
	}

	@Override
	public void markDirty() {
		super.markDirty();
		int meta = 0;
		for (int i = 0; i < getSizeInventory(); ++i) {
			if (getStackInSlot(i) != null) {
				meta |= (i == 2 ? i << 1 : i + 1);
			}
		}
		if (meta == 0x7) {
			meta = 0x8;
			if (playSound) {
				worldObj.playSoundEffect(getPos().getX() + 0.5D, getPos().getY() + 1, getPos().getZ() + 0.5D, Sounds.MASTER_SWORD, 1.0F, 1.0F);
				retrieveSword();
				EntityPlayer player = worldObj.getClosestPlayer(getPos().getX() + 0.5D, getPos().getY() + 0.5D, getPos().getZ() + 0.5D, 8.0D);
				if (player != null) {
					player.triggerAchievement(ZSSAchievements.swordMaster);
				}
			} else {
				playSound = true;
			}
		}
		if (meta != getBlockMetadata() || blockType.getMetaFromState(worldObj.getBlockState(pos)) != meta) {
			worldObj.setBlockState(pos, blockType.getStateFromMeta(meta), 2);
		}
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(getPos()) != this ? false : player.getDistanceSqToCenter(getPos()) <= 64.0D;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return (stack != null && stack.getItem() instanceof ItemPendant && stack.getItemDamage() == slot);
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

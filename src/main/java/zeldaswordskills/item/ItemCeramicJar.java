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

package zeldaswordskills.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.entity.projectile.EntityCeramicJar;

public class ItemCeramicJar extends ItemModBlock implements IUnenchantable
{
	public ItemCeramicJar(Block block) {
		super(block);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		player.swingItem();
		EntityCeramicJar jar = new EntityCeramicJar(world, player);
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("jarStack")) {
			jar.setStack(ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("jarStack")));
		}
		if (!world.isRemote) {
			world.spawnEntityInWorld(jar);
		}

		if (!player.capabilities.isCreativeMode) {
			--stack.stackSize;
		}
		return stack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() && super.onItemUse(stack, player, world, pos, face, hitX, hitY, hitZ)) {
			if (stack.hasTagCompound() && stack.getTagCompound().hasKey("jarStack")) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof IInventory) {
					((IInventory) te).setInventorySlotContents(0, ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("jarStack")));
				}
			}

			return true;
		}
		return false;
	}
}

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

package zeldaswordskills.item.dispenser;

import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import zeldaswordskills.item.ItemCustomEgg;

/**
 * 
 * Dispenser behavior for ItemCustomEgg class and sub-classes
 *
 */
public class BehaviorDispenseCustomMobEgg extends BehaviorDefaultDispenseItem {
	@Override
	public ItemStack dispenseStack(IBlockSource block, ItemStack stack) {
		EnumFacing facing = BlockDispenser.getFacing(block.getBlockMetadata());
		double dx = block.getX() + facing.getFrontOffsetX();
        double dy = ((float) block.getBlockPos().getY() + 0.2F);
        double dz= block.getZ() + facing.getFrontOffsetZ();
		Entity entity = ((ItemCustomEgg) stack.getItem()).spawnCreature(block.getWorld(), stack.getItemDamage(), dx, dy, dz);
		if (entity instanceof EntityLivingBase && stack.hasDisplayName()) {
			((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
		}
		stack.splitStack(1);
		return stack;
	}
}

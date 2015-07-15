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

package zeldaswordskills.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ZSSItems;

public class RecipeCombineBombBag implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		ItemStack bag = null;
		boolean flag = false;
		int found = 0;
		for (int i = 0; i < grid.getSizeInventory(); ++i) {
			ItemStack stack = grid.getStackInSlot(i);
			if (stack != null) {
				if (++found > 2) {
					return false;
				} else if (stack.getItem() instanceof ItemBombBag) {
					if (bag == null) {
						bag = stack;
					} else {
						flag = ((ItemBombBag) bag.getItem()).areMatchingTypes(bag, stack, false);
					}
				}
			}
		}
		return found == 2 && flag;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting grid) {
		ItemStack bag = null;
		for (int i = 0; i < grid.getSizeInventory(); ++i) {
			ItemStack stack = grid.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof ItemBombBag) {
				if (bag == null) {
					bag = stack.copy();
				} else if (((ItemBombBag) bag.getItem()).canCombine(bag, stack)) {
					ItemBombBag bombBag = (ItemBombBag) bag.getItem();
					int capacity = bombBag.getCapacity(bag, true);
					capacity += ((ItemBombBag) stack.getItem()).getCapacity(stack, true);
					bombBag.setCapacity(bag, capacity);
					if (bombBag.getBagBombType(bag) < 0) {
						bombBag.setBagBombType(bag, ((ItemBombBag) stack.getItem()).getBagBombType(stack));
					}
					bombBag.addBombs(bag, ((ItemBombBag) stack.getItem()).getBombsHeld(stack));
					return bag;
				}
			}
		}
		return null;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ZSSItems.bombBag);
	}
}

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

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.item.ItemBombBag;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;

public class RecipeBagToBombArrows implements IRecipe {

	public RecipeBagToBombArrows() {
		RecipeSorter.register(ModInfo.ID + ":bagtobombarrows", RecipeBagToBombArrows.class, RecipeSorter.Category.SHAPELESS, "after:minecraft:shapeless");
	}

	@Override
	public boolean matches(InventoryCrafting grid, World world) {
		boolean hasArrows = false;
		boolean hasBag = false;
		int found = 0;
		for (int i = 0; i < grid.getSizeInventory(); ++i) {
			ItemStack stack = grid.getStackInSlot(i);
			if (stack != null) {
				++found;
				if (!hasArrows) {
					hasArrows = stack.getItem() == Items.arrow;
				}
				if (!hasBag) {
					hasBag = stack.getItem() instanceof ItemBombBag;
				}
			}
		}
		return found == 2 && hasBag && hasArrows;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting grid) {
		int arrows = 0;
		int bombs = 0;
		int bombType = -1;
		for (int i = 0; i < grid.getSizeInventory(); ++i) {
			ItemStack stack = grid.getStackInSlot(i);
			if (stack != null) {
				if (stack.getItem() == Items.arrow) {
					arrows = stack.stackSize;
				} else if (stack.getItem() instanceof ItemBombBag) {
					bombs = ((ItemBombBag) stack.getItem()).getBombsHeld(stack);
					bombType = ((ItemBombBag) stack.getItem()).getBagBombType(stack);
				}
			}
		}
		// Desired behavior is one arrow output per click until either arrows or bombs in bag are depleted
		if (arrows > 0 && bombs > 0) {
			return new ItemStack(getArrowForType(bombType));
		}
		return null;
	}

	@Override
	public int getRecipeSize() {
		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(ZSSItems.arrowBomb);
	}

	private Item getArrowForType(int index) {
		BombType type = (index < 0 ? BombType.BOMB_STANDARD : BombType.values()[index % BombType.values().length]);
		switch (type) {
		case BOMB_FIRE: return ZSSItems.arrowBombFire;
		case BOMB_WATER: return ZSSItems.arrowBombWater;
		default: return ZSSItems.arrowBomb;
		}
	}

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting grid) {
		ItemStack[] stacks = new ItemStack[grid.getSizeInventory()];
		for (int i = 0; i < grid.getSizeInventory(); ++i) {
			ItemStack stack = grid.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof ItemBombBag) {
				((ItemBombBag) stack.getItem()).removeBomb(stack);
				stacks[i] = stack;
			} else {
				stacks[i] = null;
			}
		}
		return stacks;
	}
}

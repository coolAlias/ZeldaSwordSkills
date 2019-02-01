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

package zeldaswordskills.api.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * 
 * Base registry class for {@link ICyclableItem} items that cycle between ItemStacks of some sort.
 * 
 */
public class ItemMode
{
	protected final List<ItemStack> modes = new ArrayList<ItemStack>();

	public ItemMode() {}

	/**
	 * Registers the ItemStack mode to this registry
	 * @param stack Null may be added but should be done so only once
	 */
	public void register(ItemStack stack) {
		this.modes.add(stack);
	}

	/**
	 * Returns the current number of registered modes in this registry
	 */
	public int size() {
		return this.modes.size();
	}

	/**
	 * Returns a copy of the ItemStack for the current index or null if none registered
	 * @param index the current index, usually as retrieved from {@link ICyclableItem#getCurrentMode(ItemStack, EntityPlayer)}
	 */
	public ItemStack getStack(int index) {
		if (index < 0 || index > this.modes.size() - 1) {
			return null;
		}
		ItemStack stack = this.modes.get(index);
		return (stack == null ? null : stack.copy());
	}

	/**
	 * Returns the index of the next mode, looping if required
	 * @param index the current index, usually as retrieved from {@link ICyclableItem#getCurrentMode(ItemStack, EntityPlayer)}
	 */
	public int next(int index) {
		index = (index < this.modes.size() - 1 ? index + 1 : 0);
		return index;
	}

	/**
	 * Returns the index of the previous mode, looping if required
	 * @param index the current index, usually as retrieved from {@link ICyclableItem#getCurrentMode(ItemStack, EntityPlayer)}
	 */
	public int prev(int index) {
		index = (index > 0 ? index - 1 : this.modes.size() - 1);
		return index;
	}

	/**
	 * 
	 * Generic modal registry for cycling ItemStacks tied to some sort of class, such as a projectile
	 * 
	 */
	public static class ItemModeEntity<T> extends ItemMode
	{
		protected final Map<Pair<Item, Integer>, Class<? extends T>> registry = new HashMap<Pair<Item, Integer>, Class<? extends T>>();

		/**
		 * Unsupported method - use {@link #register(ItemStack, Class)} instead
		 */
		@Override
		public void register(ItemStack stack) {}

		/**
		 * Registers the Item and damage combination to the specified class
		 * @param stack May be null to add a 'no mode selected' option, but should be added only once
		 * @param clazz Null is allowed
		 */
		public void register(ItemStack stack, Class<? extends T> clazz) {
			this.modes.add(stack);
			if (stack != null) {
				this.registry.put(Pair.of(stack.getItem(), stack.getItemDamage()), clazz);
			}
		}

		/**
		 * Returns true if this registry contains an entry for the itemstack
		 */
		public boolean contains(ItemStack stack) {
			return (stack == null ? false : this.registry.containsKey(Pair.of(stack.getItem(), stack.getItemDamage())));
		}

		/**
		 * Returns the class registered for the provided ItemStack, if any
		 */
		public Class<? extends T> getEntityClass(ItemStack stack) {
			if (stack == null) { return null; }
			Pair<Item, Integer> key = Pair.of(stack.getItem(), stack.getItemDamage());
			return (this.registry.containsKey(key) ? this.registry.get(key) : null);
		}
	}
}

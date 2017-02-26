/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.api.item;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * 
 * Predicates for comparing ItemStacks that assume {@link Predicates#notNull} will
 * be checked first, i.e. none of the default implementations check for null inputs.
 *
 */
public abstract class ItemStackPredicate implements Predicate<ItemStack>
{
	protected final ItemStack stack;

	/**
	 * @param stack The base line to be compared against
	 */
	public ItemStackPredicate(ItemStack stack) {
		this.stack = stack.copy();
	}

	/**
	 * Returns a Predicate that returns true when an input's Item matches the stack's, ignoring everything else.
	 */
	public static Predicate<ItemStack> get(ItemStack stack) {
		return ItemStackPredicate.get(stack, false, false, false);
	}

	/**
	 * Returns composite ItemStack Predicate that returns true when an ItemStack matches
	 * the criteria; at a minimum, non-null with an Item matching the comparison stack.
	 * 
	 * @param stack The stack to compare against
	 * @param matchDamage True to require an exact item damage match unless the stack to compare against uses {@link OreDictionary#WILDCARD_VALUE}
	 * @param matchNbt True to add {@link ItemStackNbtPredicate}
	 * @param matchQty True to add {@link ItemStackQuantityPredicate}
	 * @return
	 */
	public static Predicate<ItemStack> get(ItemStack stack, boolean matchDamage, boolean matchNbt, boolean matchQty) {
		List<Predicate<ItemStack>> predicates = Lists.newArrayList();
		predicates.add(Predicates.<ItemStack>notNull());
		predicates.add(new ItemStackItemPredicate(stack));
		// Only compare stack damage if the stack to compare against does not use the wildcard value
		if (matchDamage && stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) {
			predicates.add(new ItemStackDamagePredicate(stack));
		}
		if (matchQty) {
			predicates.add(new ItemStackQuantityPredicate(stack));
		}
		// Add NBT check last to allow simpler predicates to short-circuit whenever possible
		if (matchNbt) {
			predicates.add(new ItemStackNbtPredicate(stack));
		}
		return Predicates.and(predicates);
	}

	/**
	 * 
	 * Predicate returns true if the stack's item matches that of the input.
	 *
	 */
	public static class ItemStackItemPredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackItemPredicate} */
		public ItemStackItemPredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return this.stack.getItem() == input.getItem();
		}
	}

	/**
	 * 
	 * Returns true if the stack's damage matches that of the input.
	 *
	 */
	public static class ItemStackDamagePredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackDamagePredicate} */
		public ItemStackDamagePredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return this.stack.getItemDamage() == input.getItemDamage();
		}
	}

	/**
	 * 
	 * Returns true if the stack either does not have subtypes or its damage value matches the input.
	 *
	 */
	public static class ItemStackSubtypePredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackSubtypePredicate} */
		public ItemStackSubtypePredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return !this.stack.getHasSubtypes() || this.stack.getItemDamage() == input.getItemDamage();
		}
	}

	/**
	 * 
	 * Returns true if the stack's damage uses the {@link OreDictionary#WILDCARD_VALUE} value
	 *
	 */
	public static class ItemStackWildCardPredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackWildCardPredicate} */
		public ItemStackWildCardPredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return this.stack.getItemDamage() == OreDictionary.WILDCARD_VALUE;
		}
	}

	/**
	 * 
	 * Returns true if the input's damage uses the {@link OreDictionary#WILDCARD_VALUE} value
	 *
	 */
	public static class WildCardPredicate implements Predicate<ItemStack>
	{
		@Override
		public boolean apply(ItemStack input) {
			return input.getItemDamage() == OreDictionary.WILDCARD_VALUE;
		}
	}

	/**
	 * 
	 * Returns true if the stack's stack size matches that of the input exactly.
	 *
	 */
	public static class ItemStackQuantityPredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackQuantityPredicate} */
		public ItemStackQuantityPredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return this.stack.stackSize == input.stackSize;
		}
	}

	/**
	 * 
	 * Returns true if the stack's stack size is at least that of the input.
	 *
	 */
	public static class ItemStackMinQtyPredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackMinQtyPredicate} */
		public ItemStackMinQtyPredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			return this.stack.stackSize >= input.stackSize;
		}
	}

	/**
	 * 
	 * Returns true if the stack's NBT matches that of the input according to
	 * {@link INbtComparable} if appropriate or {@link ItemStack#areItemStackTagsEqual} if not.
	 *
	 */
	public static class ItemStackNbtPredicate extends ItemStackPredicate
	{
		/** See {@link ItemStackNbtPredicate} */
		public ItemStackNbtPredicate(ItemStack stack) {
			super(stack);
		}
		@Override
		public boolean apply(ItemStack input) {
			if (!this.stack.hasTagCompound() && !input.hasTagCompound()) {
				return true;
			} else if (this.stack.getItem() instanceof INbtComparable) {
				return ((INbtComparable) this.stack.getItem()).areTagsEquivalent(this.stack, input);
			}
			return ItemStack.areItemStackTagsEqual(this.stack, input);
		}
	}
}

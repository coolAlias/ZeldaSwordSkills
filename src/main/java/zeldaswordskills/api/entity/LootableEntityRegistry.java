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

package zeldaswordskills.api.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * 
 * Registry of entities (and their loot!) that are able to be looted with a Whip.
 *
 */
public class LootableEntityRegistry {

	/** Map of entity class to list of potential ItemStack drops */
	private static final Map<Class<? extends Entity>, List<ItemStack>> entityLoot = new HashMap<Class <? extends Entity>, List<ItemStack>>();

	/** Map of entity class to chance item may be looted */
	private static final Map<Class<? extends Entity>, Float> lootChance = new HashMap<Class <? extends Entity>, Float>();

	private static final Random rand = new Random();

	/**
	 * Attempts to add the entity with the desired loot without replacing any existing entry.
	 * Entities that implement {@link IEntityLootable} do not benefit when added to this list.
	 * Best to use this method from FMLPostInitializationEvent, and no earlier than FMLInitializationEvent.
	 * @param chance	Value between 0.0F and 1.0F, inclusive, used to determine if the loot will drop
	 * @param loot		ItemStack(s) that may be dropped by this entity
	 * @return			True if added, false if the entity class already has an entry
	 */
	public static boolean addLootableEntity(Class<? extends Entity> entity, float chance, ItemStack... loot) {
		return addLootableEntity(entity, chance, Arrays.asList(loot));
	}

	/**
	 * Attempts to add the entity with the desired loot without replacing any existing entry.
	 * Entities that implement {@link IEntityLootable} do not benefit when added to this list.
	 * Best to use this method from FMLPostInitializationEvent, and no earlier than FMLInitializationEvent.
	 * @param chance	Value between 0.0F and 1.0F, inclusive, used to determine if the loot will drop
	 * @param loot		List of possible ItemStack loot drops
	 * @return			True if added, false if the entity class already has an entry
	 */
	public static boolean addLootableEntity(Class<? extends Entity> entity, float chance, List<ItemStack> loot) {
		if (entityLoot.containsKey(entity)) {
			return false;
		}
		entityLoot.put(entity, loot);
		lootChance.put(entity, chance);
		return true;
	}

	/**
	 * Returns a copy of one of the possible loot ItemStacks for the entity class or null if the entity does not have an entry
	 */
	public static ItemStack getEntityLoot(Class<? extends Entity> entity) {
		List<ItemStack> loot = entityLoot.get(entity);
		if (loot != null && loot.size() > 0) {
			ItemStack stack = loot.get(rand.nextInt(loot.size()));
			return (stack == null ? null : stack.copy());
		}
		return null;
	}

	/**
	 * Returns the chance that special loot will drop when this entity is looted,
	 * which is always zero for entities with no special drop.
	 */
	public static float getEntityLootChance(Class<? extends Entity> entity) {
		return (lootChance.containsKey(entity) ? lootChance.get(entity) : 0.0F);
	}
}

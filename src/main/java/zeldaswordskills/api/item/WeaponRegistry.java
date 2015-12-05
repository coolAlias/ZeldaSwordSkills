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

package zeldaswordskills.api.item;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.registry.GameRegistry;
import zeldaswordskills.ZSSMain;

/**
 * 
 * Allows items from other mods to be registered as generic weapons or as swords.
 * Items that rely on NBT or damage value to determine their weapon status should
 * NOT be registered in this way - use {@link IWeapon} instead.
 * 
 * Note also that some skills require blocking to activate, so any item which cannot
 * be used to block will not be able to trigger those skills even if registered.
 *
 */
public class WeaponRegistry
{
	/** FML Inter-Mod Communication key for registering items as swords */
	public static final String IMC_SWORD_KEY = "ZssRegisterSword";

	/** FML Inter-Mod Communication key for registering items as non-sword weapons */
	public static final String IMC_WEAPON_KEY = "ZssRegisterWeapon";

	private final Set<Item> swords = new HashSet<Item>();

	private final Set<Item> weapons = new HashSet<Item>();

	private final Set<Item> forbidden_swords = new HashSet<Item>();

	private final Set<Item> forbidden_weapons = new HashSet<Item>();

	public static final WeaponRegistry INSTANCE = new WeaponRegistry();

	private WeaponRegistry() {}

	/**
	 * Returns true if the item is registered as a sword or extends ItemSword.
	 */
	public boolean isSword(Item item) {
		return !isSwordForbidden(item) && (item instanceof ItemSword || swords.contains(item));
	}

	/**
	 * Returns true if the item is forbidden either as a sword or a weapon (if it's not a weapon, it's not a sword).
	 * Recommended to use this method instead of {@link #isSword} when implementing {@link IWeapon#isSword}.
	 */
	public boolean isSwordForbidden(Item item) {
		return forbidden_swords.contains(item) || isWeaponForbidden(item);
	}

	/**
	 * Returns true if the item is registered as a non-sword weapon or extends ItemSword
	 */
	public boolean isWeapon(Item item) {
		return !isWeaponForbidden(item) && (item instanceof ItemSword || weapons.contains(item));
	}

	/**
	 * Returns true if the item is forbidden as a weapon.
	 * Recommended to use this method instead of {@link #isWeapon} when implementing {@link IWeapon#isWeapon}.
	 */
	public boolean isWeaponForbidden(Item item) {
		return forbidden_weapons.contains(item);
	}

	/**
	 * If the message key is either {@link #IMC_SWORD_KEY} or {@link #IMC_WEAPON_KEY}
	 * and the message contains an ItemStack, the stack will be registered appropriately.
	 */
	public void processMessage(FMLInterModComms.IMCMessage msg) {
		if (!msg.isItemStackMessage()) {
			return;
		} else if (msg.key.equalsIgnoreCase(IMC_SWORD_KEY)) {
			registerSword("IMC", msg.getSender(), msg.getItemStackValue().getItem());
		} else if (msg.key.equalsIgnoreCase(IMC_WEAPON_KEY)) {
			registerWeapon("IMC", msg.getSender(), msg.getItemStackValue().getItem());
		}
	}

	/**
	 * Registers an array of named items either as swords or generic weapons
	 * @param names Must be in the format 'modid:registered_item_name'
	 * @param origin Information about the origin of the names array, e.g. "Config" or "Command"
	 * @param isSword True to register the items as swords, or false for generic melee weapons
	 */
	public void registerItems(String[] names, String origin, boolean isSword) {
		processArray(names, origin, isSword, true);
	}

	/**
	 * Forbids an array of named items either from the swords or generic weapons registry
	 * @param names Must be in the format 'modid:registered_item_name'
	 * @param origin Information about the origin of the names array, e.g. "Config" or "Command"
	 * @param isSword True to forbid the items as swords, or false for generic melee weapons
	 */
	public void forbidItems(String[] names, String origin, boolean isSword) {
		processArray(names, origin, isSword, false);
	}

	private void processArray(String[] names, String origin, boolean isSword, boolean register) {
		for (String s : names) {
			String[] parts = parseString(s);
			if (parts != null) {
				Item item = GameRegistry.findItem(parts[0], parts[1]);
				if (item == null) {
					ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] %s:%s could not be found - the mod may not be installed, or you may have typed it incorrectly", origin, parts[0], parts[1]));
				} else if (isSword) {
					if (register) {
						registerSword(origin, parts[0], item);
					} else {
						removeSword(origin, parts[0], item);
					}
				} else if (register) {
					registerWeapon(origin, parts[0], item);
				} else {
					removeWeapon(origin, parts[0], item);
				}
			}
		}
	}

	/**
	 * Registers an item as a sword: it may be used to activate sword-specific skills, cut grass, etc.
	 * Item will be removed from the forbidden sword list if present.
	 * @param origin String containing information about origins of registration, e.g. "IMC"
	 * @param modid String modid of mod to which the Item belongs
	 * @param item Item to add to the sword registry
	 * @return true if item was successfully added
	 */
	public boolean registerSword(String origin, String modid, Item item) {
		boolean added = false;
		if (weapons.contains(item)) {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] CONFLICT: %s:%s cannot be registered as a sword - it is already registered as a non-sword weapon", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		} else if (swords.add(item)) {
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] Registered %s:%s as a sword", origin, modid, item.getUnlocalizedName().replace("item.", "")));
			added = true;
		} else {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] %s:%s has already been registered as a sword", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		forbidden_swords.remove(item);
		return added;
	}

	/**
	 * Registers an item as a generic melee weapon: it may be used to activate all skills that do not specifically require a sword.
	 * Item will be removed from the forbidden weapons list if present.
	 * @param origin String containing information about origins of registration, e.g. "IMC"
	 * @param modid String modid of mod to which the Item belongs
	 * @param item Item to add to the weapon registry
	 * @return true if item was successfully added
	 */
	public boolean registerWeapon(String origin, String modid, Item item) {
		boolean added = false;
		if (swords.contains(item)) {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] CONFLICT: %s:%s cannot be registered as a weapon - it is already registered as a sword", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		} else if (weapons.add(item)) {
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] Registered %s:%s as a non-sword weapon", origin, modid, item.getUnlocalizedName().replace("item.", "")));
			added = true;
		} else {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] %s:%s has already been registered as a weapon", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		forbidden_weapons.remove(item);
		return added;
	}

	/**
	 * Removes the item from the SWORD registry if present and adds it to the forbidden swords list
	 * @return true if item was both present and removed from the SWORD list
	 */
	public boolean removeSword(String origin, String modid, Item item) {
		boolean added = false; // prevent too much log spam
		if (forbidden_swords.add(item)) {
			added = true;
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] %s:%s added to FORBIDDEN swords list", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		if (swords.remove(item)) {
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] Removed %s:%s from list of registered SWORDS", origin, modid, item.getUnlocalizedName().replace("item.", "")));
			return true;
		} else if (!added) {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] Could not remove %s:%s - it was not registered as a sword", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		return added;
	}

	/**
	 * Removes the item from the WEAPON registry if present and adds it to the forbidden weapons list
	 * @return true if item was both present and removed from the WEAPON list
	 */
	public boolean removeWeapon(String origin, String modid, Item item) {
		boolean added = false; // prevent too much log spam
		if (forbidden_weapons.add(item)) {
			added = true;
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] %s:%s added to FORBIDDEN weapons list", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		if (weapons.remove(item)) {
			ZSSMain.logger.info(String.format("[WeaponRegistry] [%s] Removed %s:%s from list of registered WEAPONS", origin, modid, item.getUnlocalizedName().replace("item.", "")));
			return true;
		} else if (!added) {
			ZSSMain.logger.warn(String.format("[WeaponRegistry] [%s] Could not remove %s:%s - it was not registered as a non-sword weapon", origin, modid, item.getUnlocalizedName().replace("item.", "")));
		}
		return added;
	}

	/**
	 * Parses a String into an array containing the mod_id and item_name, or NULL if format was invalid
	 * @param itemid Expected format is 'modid:registered_item_name'
	 */
	public static String[] parseString(String itemid) {
		String[] parts = itemid.split(":");
		if (parts.length == 2) {
			return parts;
		}
		ZSSMain.logger.error(String.format("[WeaponRegistry] String must be in the format 'modid:registered_item_name', received: %s", itemid));
		return null;
	}
}

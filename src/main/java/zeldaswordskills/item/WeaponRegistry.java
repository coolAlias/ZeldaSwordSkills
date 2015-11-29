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

package zeldaswordskills.item;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.ISkillItem;
import zeldaswordskills.api.item.ISword;

/**
 * 
 * Allows items from other mods to be registered as generic weapons or as swords.
 * Items that rely on NBT or damage value to determine their weapon status should
 * NOT be registered in this way - use {@link ISword} or {@link ISkillItem} instead.
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

	public static final WeaponRegistry INSTANCE = new WeaponRegistry();

	private WeaponRegistry() {}

	/**
	 * Returns true if the item is registered as a sword
	 */
	public boolean isSword(Item item) {
		return item != null && swords.contains(item);
	}

	/**
	 * Returns true if the item is registered as a non-sword weapon
	 */
	public boolean isWeapon(Item item) {
		return item != null && weapons.contains(item);
	}

	/**
	 * If the message key is either {@link #IMC_SWORD_KEY} or {@link #IMC_WEAPON_KEY}
	 * and the message contains an ItemStack, the stack will be registered appropriately.
	 */
	public void processMessage(FMLInterModComms.IMCMessage msg) {
		if (!msg.isItemStackMessage()) {
			return;
		} else if (msg.key.equalsIgnoreCase(IMC_SWORD_KEY)) {
			registerSword(msg.getSender(), msg.getItemStackValue());
		} else if (msg.key.equalsIgnoreCase(IMC_WEAPON_KEY)) {
			registerWeapon(msg.getSender(), msg.getItemStackValue());
		}
	}

	private void registerSword(String sender, ItemStack stack) {
		if (weapons.contains(stack.getItem())) {
			ZSSMain.logger.warn(String.format("[Message from %s] CONFLICT: %s cannot be registered as a sword - it is already registered as a non-sword weapon", sender, stack.getUnlocalizedName()));
		} else if (swords.add(stack.getItem())) {
			ZSSMain.logger.info(String.format("[Message from %s] Registered %s as a sword", sender, stack.getUnlocalizedName()));
		} else {
			ZSSMain.logger.warn(String.format("[Message from %s] %s has already been registered as a sword", sender, stack.getUnlocalizedName()));
		}
	}

	private void registerWeapon(String sender, ItemStack stack) {
		if (swords.contains(stack.getItem())) {
			ZSSMain.logger.warn(String.format("[Message from %s] CONFLICT: %s cannot be registered as a weapon - it is already registered as a sword", sender, stack.getUnlocalizedName()));
		} else if (weapons.add(stack.getItem())) {
			ZSSMain.logger.info(String.format("[Message from %s] Registered %s as a non-sword weapon", sender, stack.getUnlocalizedName()));
		} else {
			ZSSMain.logger.warn(String.format("[Message from %s] %s has already been registered as a weapon", sender, stack.getUnlocalizedName()));
		}
	}
}

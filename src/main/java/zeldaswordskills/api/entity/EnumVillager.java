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

package zeldaswordskills.api.entity;

import net.minecraft.entity.passive.EntityVillager;

/**
 * 
 * Helper class for making working with vanilla villager professions more readable.
 *
 */
public enum EnumVillager {
	FARMER("farmer"),
	LIBRARIAN("librarian"),
	PRIEST("priest"),
	BLACKSMITH("blacksmith"),
	BUTCHER("butcher");
	public final String unlocalizedName;
	private EnumVillager(String name) {
		this.unlocalizedName = name;
	}
	/** Return the EnumVillager type based on the villager's profession, or null for non-vanilla professions */
	public static EnumVillager get(EntityVillager villager) {
		int i = villager.getProfession();
		if (i < 0 || i > EnumVillager.values().length) {
			return null;
		}
		return EnumVillager.values()[i];
	}
	/**
	 * Returns true if the villager's profession is this one
	 */
	public boolean is(EntityVillager villager) {
		return villager.getProfession() == this.ordinal();
	}
}

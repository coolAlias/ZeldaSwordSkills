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

package zeldaswordskills.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;

public class VillagerDescription
{
	/** Name of villager interested in this trade */
	public final String villagerName;

	/** Class of villager interested in this trade */
	public final Class<? extends Entity> villagerClass;

	/** Profession of villager interested in this trade; -1 if irrelevant */
	public final int profession;

	/** True to require child version of this villager entity (only if villagerClass extends EntityAgeable) */
	public final boolean isChild;

	/**
	 * Creates a villager description for an adult with no profession requirement
	 */
	public VillagerDescription(String villagerName, Class<? extends Entity> villagerClass) {
		this(villagerName, villagerClass, -1, false);
	}

	/**
	 * Creates a villager description for an adult with a profession requirement
	 */
	public VillagerDescription(String villagerName, Class<? extends Entity> villagerClass, int profession) {
		this(villagerName, villagerClass, profession, false);
	}

	/**
	 * Create a villager description with the following requirements:
	 * @param villagerName	Required villager name, case-sensitive
	 * @param villagerClass Required villager class, checked using identity rather than instanceof
	 * @param profession    Required EntityVillager profession id, or -1 for no requirement
	 * @param isChild       True if the villager must be a child, false for an adult
	 */
	public VillagerDescription(String villagerName, Class<? extends Entity> villagerClass, int profession, boolean isChild) {
		this.villagerName = villagerName;
		this.villagerClass = villagerClass;
		this.profession = profession;
		this.isChild = isChild;
	}

	/**
	 * Returns true if the entity matches this description exactly, including child status
	 */
	public boolean matches(Entity entity) {
		return matches(entity, true);
	}

	/**
	 * Returns true if the entity matches this description exactly
	 * @param checkChild false to ignore isChild status - check {@link #matchChild} separately
	 */
	public boolean matches(Entity entity, boolean checkChild) {
		if (!matchClassAndName(entity) || !matchProfession(entity)) {
			return false;
		}
		return !checkChild || matchChild(entity);
	}

	/**
	 * Returns true if the entity's class and custom name tag match the required values
	 */
	public boolean matchClassAndName(Entity entity) {
		return (entity.getClass() == villagerClass && villagerName.equals(entity.getCustomNameTag()));
	}

	/**
	 * Returns true if the description's and entity's isChild status match exactly
	 */
	public boolean matchChild(Entity entity) {
		boolean isChildEntity = (entity instanceof EntityAgeable && ((EntityAgeable) entity).isChild());
		return (isChild && isChildEntity) || (!isChild && !isChildEntity);
	}

	/**
	 * Returns true if no profession is required or the entity has the correct profession
	 */
	public boolean matchProfession(Entity entity) {
		if (profession != -1) {
			return (entity instanceof EntityVillager && ((EntityVillager) entity).getProfession() == profession);
		}
		return true;
	}
}

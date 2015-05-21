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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * 
 * Interface usable by both Item and Entity classes to modify disarm chances.
 *
 */
public interface IParryModifier {

	/**
	 * Modifier applied to the chance of being disarmed when attacking an entity
	 * @param entity	Attacking entity who may be disarmed: either the instance of IParryModifier, or the entity wielding the stack
	 * @param stack		ItemStack which may be disarmed: either the instance of IParryModifier, or the item held by the entity
	 * @return	Positive value decreases the likelihood of being disarmed (e.g. 0.5F reduces likelihood by 50%)
	 */
	public float getOffensiveModifier(EntityLivingBase entity, ItemStack stack);

	/**
	 * Modifier applied to the chance to disarm an opponent when defending against an attack
	 * @param entity	Defending entity attempting to disarm: either the instance of IParryModifier, or the entity wielding the stack
	 * @param stack		ItemStack being used to disarm: either the instance of IParryModifier, or the item held by the entity
	 * @return	Positive value increases the likelihood of disarming the attacker (e.g. 0.5F increases likelihood by 50%)
	 */
	public float getDefensiveModifier(EntityLivingBase entity, ItemStack stack);

}

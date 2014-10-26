/**
    Copyright (C) <2014> <coolAlias>

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

import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.skills.sword.BackSlice;

/**
 * 
 * Specifically for entities with special responses to {@link BackSlice} 
 *
 */
public interface IEntityBackslice {

	/**
	 * Called when an entity is struck in the back by a player using a skill such as {@link BackSlice}
	 * to determine if the attack damage should be modified by the player's skill
	 * @param attacker	Attacking player
	 * @return			True to allow damage multiplier, false for standard attack damage
	 */
	public boolean allowDamageMultiplier(EntityPlayer player);

	/**
	 * Return true for default handling of disarmorment, or false to prevent any further processing
	 * (random note: this is a deliberate misspelling of 'disarmament')
	 * @param damage Damage has already been modified by {@link #onBackDamaged} at this point
	 */
	public boolean allowDisarmorment(EntityPlayer player, float damage);

	/**
	 * Called when an entity is damaged in the back by a player using a skill such as {@link BackSlice}
	 * Any special results of being attacked in the back should be handled here.
	 * For custom handling of disarmorment, {@link BackSlice#getDisarmorChance} is available.
	 * @param attacker	Attacking player
	 * @param level		Level of the skill used to cause back damage
	 * @param damage	Amount of damage to be inflicted after skill modifier has been applied (if allowed)
	 * @return			New amount of damage to be dealt; 0 will cancel the damage
	 */
	public float onBackSliced(EntityPlayer attacker, int level, float damage);

}

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

package zeldaswordskills.entity.ai;

import net.minecraft.entity.EntityLivingBase;

/**
 * 
 * Allows entity to be notified from EntityAIUseMagic when casting begins, for animating
 *
 */
// TODO make IMagicUser the base, then have IRangedMagicUser and IPassiveMagicUser (for self-centered spells)
// or just require IMagicUser to implement both types of methods, which would be determined
// based on AI tasks added (i.e. EntityAIRangedCaster would call ranged method)
public interface IMagicUser {

	/**
	 * Called from {@link EntityAIRangedMagic} when a spell is about to begin charging.
	 * Allows entity to determine which spell should be cast and set the cast time
	 * appropriately, as well as set animation flags and such.
	 * @param target	Will be null if called from EntityAIPassiveMagic, or the current target for EntityAIRangedMagic
	 * @return			The time it will take for the spell being cast to fully charge;
	 * 					returning 0 will prevent casting a spell at this time
	 */
	public int beginSpellCasting(EntityLivingBase target);

	/**
	 * Called after charging a spell from EntityAIPassiveMagic for spells that target the self
	 */
	// TODO EntityAIPassiveMagic would need to query entity for shouldExecute and continueExecuting,
	// as the AI will not know if effect(s) is already active or not
	// TODO make part of IPassiveMagicUser, which also has methods for interacting with EntityAIPassiveMagic
	public void castPassiveSpell();

	/**
	 * Called after spell done charging to actually cast it
	 * @param target	The current AI target
	 * @param range		Range factor between 0F and 1F; reaches 1F when the caster
	 * 					is the minimum distance or greater away from the target and
	 * 					approaches zero as caster nears the target's position.
	 * @return			Return the next maximum time before another spell may be cast
	 */
	public void castRangedSpell(EntityLivingBase target, float range);

	/**
	 * Called from {@link EntityAIRangedMagic} when the task is reset
	 */
	public void stopCasting();
}

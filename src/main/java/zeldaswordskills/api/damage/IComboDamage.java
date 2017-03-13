/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.api.damage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

/**
 * 
 * Controls whether a {@link DamageSource} should have its damage applied to a Combo.
 *
 */
public interface IComboDamage {

	/**
	 * Return true if this damage source counts as Combo damage
	 */
	boolean isComboDamage(EntityPlayer player);

	/**
	 * 
	 * Provide further control over how this {@link DamageSource} interacts with Combos.
	 * Note that none of these methods will be called if {@link #isComboDamage} returns false.
	 *
	 */
	public static interface IComboDamageFull extends IComboDamage {

		/**
		 * Return true to increase the current Combo's hit count when this damage is applied
		 */
		boolean increaseComboCount(EntityPlayer player);

		/**
		 * Only called if {@link #increaseComboCount} returns false.
		 * @return True to add this damage to the previous combo hit, if any
		 */
		boolean applyDamageToPrevious(EntityPlayer player);

		/**
		 * Return true to play the default combo hit sound or false to use {@link #getHitSound}
		 */
		boolean playDefaultSound(EntityPlayer player);

		/**
		 * Return the sound to play on a successful hit, or null to not play any sound.
		 * Only used if {@link #playDefaultSound} returns false.
		 */
		String getHitSound(EntityPlayer player);

	}
}

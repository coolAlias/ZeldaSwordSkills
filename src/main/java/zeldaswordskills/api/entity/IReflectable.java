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

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zeldaswordskills.item.ItemZeldaShield;

/**
 * 
 * For projectiles that need special handling when blocked by Zelda Shields.
 * TODO make more generic:
 *  - when struck by projectile while any IReflector item is in use
 *  - or at least all Zelda Shields should be able to ineract with this interface (e.g. Deku Shield vs. Octorok rocks)
 *
 */
public interface IReflectable extends IProjectile {

	/**
	 * Called when the projectile is blocked with a {@link ItemZeldaShield mirror shield}
	 * @param shield Shield stack used to block the attack
	 * @param player Defending player that is reflecting the projectile
	 * @param source The original source of damage; {@link DamageSource#getSourceOfDamage} is the original
	 *               projectile instance and {@link DamageSource#getEntity} should be the original shooter
	 * @return Chance that the projectile will be reflected, between 0.0F and 1.0F inclusive
	 */
	float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source);

	/**
	 * Called just before setting the reflected projectile instance's trajectory to control accuracy
	 * @param shield Shield stack used to block the attack
	 * @param player Defending player that is reflecting the projectile
	 * @param source The original source of damage; {@link DamageSource#getSourceOfDamage} is the original
	 *               projectile instance and {@link DamageSource#getEntity} should be the original shooter
	 * @return Projectile's variance from a straight trajectory, with 0 being dead on and 20+ being wildly inaccurate
	 * <br>Returning a negative value will result in the default randomized variance being used.
	 */
	float getReflectedWobble(ItemStack shield, EntityPlayer player, DamageSource source);

	/**
	 * Called right before spawning the reflected projectile; this is a good time to call #setDead on the
	 * original projectile (see DamageSource param) and to set the new projectile's shooter to the player
	 * @param shield Shield stack used to block the attack
	 * @param player Defending player that is reflecting the projectile
	 * @param source The original source of damage; {@link DamageSource#getSourceOfDamage} is the original
	 *               projectile instance and {@link DamageSource#getEntity} should be the original shooter
	 */
	void onReflected(ItemStack shield, EntityPlayer player, DamageSource source);

	/**
	 * 
	 * Interface for reflectable projectiles that keep track of the original shooting entity
	 *
	 */
	public static interface IReflectableOrigin extends IReflectable
	{
		/**
		 * Return the original shooter of this projectile, if any
		 */
		Entity getReflectedOriginEntity();
	}
}

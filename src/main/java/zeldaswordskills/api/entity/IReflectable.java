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

import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 
 * For projectiles that need special handling when reflected with the Mirror Shield.
 *
 */
public interface IReflectable extends IProjectile {

	/**
	 * Called when the projectile is blocked with a Mirror Shield
	 * @param shield	Mirror Shield stack used to block the attack
	 * @param player	Defending player that is reflecting the projectile
	 * @param shooter	Shooter of the projectile, possibly null
	 * @return Chance that the projectile will be reflected, between 0.0F and 1.0F inclusive
	 */
	public float getReflectChance(ItemStack mirrorShield, EntityPlayer player, Entity shooter);

	/**
	 * Called using the new projectile instance, just before it is spawned into the world
	 * @param shield	Mirror Shield stack used to block the attack
	 * @param player	Defending player that is reflecting the projectile
	 * @param shooter	Shooter of the original projectile, possibly null
	 * @param oldEntity	Original projectile entity
	 */
	public void onReflected(ItemStack mirrorShield, EntityPlayer player, Entity shooter, Entity oldEntity);
}

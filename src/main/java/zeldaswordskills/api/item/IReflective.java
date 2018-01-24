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

package zeldaswordskills.api.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import zeldaswordskills.api.entity.IReflectable;

/**
 * 
 * Specifically for shields that can reflect {@link IReflectable} projectiles.
 * 
 */
public interface IReflective
{
	/**
	 * True if the stack is a Mirror Shield, meaning it can reflect just about everything.
	 */
	boolean isMirrorShield(ItemStack shield);

	/**
	 * The chance this shield has of reflecting a projectile damage source back to its initiator.
	 * <br>Called automatically only for non-{@link IReflectable} projectiles; IReflectables call this method at their discretion.
	 * <br>WARNING: Do not call {@link IReflectable#getReflectChance(ItemStack, EntityPlayer, DamageSource, float) IReflectable#getReflectChance} chance from here unless you want an infinite loop.
	 * @param shield The IReflective shield stack
	 * @param source Will only be called for non-explosive projectile damage sources
	 * @param damage amount of damage the attack would cause if not reflected
	 * @return a value between 0.0F and 1.0F, inclusive
	 */
	float getReflectChance(ItemStack shield, EntityPlayer player, DamageSource source, float damage);

	/**
	 * Called after reflecting a projectile
	 * @param shield The IReflective shield stack
	 * @param source Will only be called for non-explosive projectile damage sources
	 * @param damage amount of damage the attack would have caused if it was not reflected
	 */
	void onReflected(ItemStack shield, EntityPlayer player, DamageSource source, float damage);

}

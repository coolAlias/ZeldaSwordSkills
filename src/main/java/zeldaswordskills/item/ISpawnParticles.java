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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * Interface that allows an Item class to spawn particles with a custom algorithm,
 * the advantage of which is that these particles will be spawned in such a way that
 * all nearby players will see them.
 * 
 * To take advantage of this feature, an ISpawnParticlesPacket must be sent to all
 * players around the position at which to spawn the particles, otherwise the interface
 * method will never be called.
 *
 */
public interface ISpawnParticles {

	/**
	 * Method that is called on each client world upon receiving an ISpawnParticlesPacket
	 * Any particles to be spawned should be spawned from this method.
	 * @param player the player that caused the particles to be spawned (not guaranteed to still be holding the ISpawnParticles item)
	 * @param stack the ItemStack originally used to spawn the particles
	 * @param x the instigating player's X position
	 * @param y the instigating player's Y position
	 * @param z the instigating player's Z position
	 * @param r typically used as the radius in which to spawn particles
	 */
	@SideOnly(Side.CLIENT)
	void spawnParticles(World world, EntityPlayer player, ItemStack stack, double x, double y, double z, float r);

}

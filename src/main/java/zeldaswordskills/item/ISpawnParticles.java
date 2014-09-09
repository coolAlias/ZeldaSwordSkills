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

package zeldaswordskills.item;

import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	 * @param x the x coordinate, typically based on the instigating player's position
	 * @param y the y coordinate, typically based on the instigating player's position
	 * @param z the z coordinate, typically based on the instigating player's position
	 * @param r typically used as the radius in which to spawn particles
	 * @param lookVector the normalized look vector from the original player who caused the particles
	 */
	@SideOnly(Side.CLIENT)
	public void spawnParticles(World world, double x, double y, double z, float r, Vec3 lookVector);

}

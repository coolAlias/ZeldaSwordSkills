/**
    Copyright (C) <20143> <coolAlias>

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

package zeldaswordskills.skills;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import zeldaswordskills.network.CustomPacket.ProtocolException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Interface for skills that are able to lock on to a target, switch targets, etc. Only one
 * such skill should be active at a time.
 *
 */
public interface ILockOnTarget
{	
	/** Returns true if player currently targeting an entity */
	public boolean isLockedOn();
	
	/** Returns entity currently locked on to, or null if not locked on */
	public Entity getCurrentTarget();
	
	/** Should only use on the server side; client side should throw an exception */
	public void setCurrentTarget(Side side, Entity entity) throws ProtocolException;
	
	/** Should find and return the next valid target or null */
	@SideOnly(Side.CLIENT)
	public void getNextTarget(EntityPlayer player);
	
}

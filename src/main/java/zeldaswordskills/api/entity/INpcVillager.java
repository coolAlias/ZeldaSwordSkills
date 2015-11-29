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

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.eventhandler.Event.Result;

public interface INpcVillager {

	/**
	 * Called on both sides when a player right-click interacts with a villager or
	 * sub-class thereof. Note that this entity has had its position and name tag
	 * set already, and this is the final condition before a villager is converted
	 * (i.e. it is safe to consume items at this point).
	 * 
	 * @return DEFAULT to allow the event or calling method to continue processing without converting
	 *         ALLOW   to cancel any further processing and signify that the villager was successfully converted
	 *         DENY    to cancel any further processing and signify that the villager was NOT converted
	 */
	Result canInteractConvert(EntityPlayer player, EntityVillager villager);

	/**
	 * Called on both sides when a player left-clicks a villager or sub-class thereof
	 * with an ItemTreasure. Note that this entity has had its position and name tag
	 * set already, and this is the final condition before a villager is converted
	 * (i.e. it is safe to consume items at this point).
	 * 
	 * @return DEFAULT to allow the event or calling method to continue processing without converting
	 *         ALLOW   to cancel any further processing and signify that the villager was successfully converted
	 *         DENY    to cancel any further processing and signify that the villager was NOT converted
	 */
	Result canLeftClickConvert(EntityPlayer player, EntityVillager villager);

	/**
	 * Called on the server after spawning the newly converted Npc
	 */
	void onConverted(EntityPlayer player);

}

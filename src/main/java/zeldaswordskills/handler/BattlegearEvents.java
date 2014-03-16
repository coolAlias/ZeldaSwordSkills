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

package zeldaswordskills.handler;

import mods.battlegear2.api.PlayerEventChild.ShieldBlockEvent;
import net.minecraftforge.event.ForgeSubscribe;
import zeldaswordskills.item.ItemZeldaShield;

/**
 * 
 * Specifically for handling Battlegear2 events
 *
 */
public class BattlegearEvents {

	@ForgeSubscribe
	public void onBlocked(ShieldBlockEvent event) {
		if (event.shield.getItem() instanceof ItemZeldaShield) {
			((ItemZeldaShield) event.shield.getItem()).onBlock(event.entityPlayer, event.shield, event.source, event.ammount);
			event.performAnimation = false;
			event.damageShield = false;
		}
	}
}

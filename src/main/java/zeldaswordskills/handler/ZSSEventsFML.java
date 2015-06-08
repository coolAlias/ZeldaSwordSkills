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

package zeldaswordskills.handler;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncConfigPacket;

/**
 * 
 * Various events on the FML event bus
 *
 */
public class ZSSEventsFML {

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event) {
		Item item = (event.crafting == null ? null : event.crafting.getItem());
		if (item instanceof ItemInstrument && ((ItemInstrument) item).getInstrument(event.crafting) == ItemInstrument.Instrument.OCARINA_FAIRY) {
			event.player.triggerAchievement(ZSSAchievements.ocarinaCraft);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		ZSSPlayerInfo.get(event.player).onPlayerLoggedIn();
		if (event.player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncConfigPacket(), (EntityPlayerMP) event.player);
		}
	}
}

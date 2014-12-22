package zeldaswordskills.handler;

import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.item.ZSSItems;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

/**
 * 
 * Various events on the FML event bus
 *
 */
public class ZSSEventsFML {

	@SubscribeEvent
	public void onItemCrafted(ItemCraftedEvent event) {
		if (event.crafting != null && event.crafting.getItem() == ZSSItems.instrument
				&& ((ItemInstrument) event.crafting.getItem()).getInstrument(event.crafting) == ItemInstrument.Instrument.OCARINA_FAIRY)
		{
			event.player.triggerAchievement(ZSSAchievements.ocarinaCraft);
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerLoggedInEvent event) {
		ZSSPlayerInfo.get(event.player).onPlayerLoggedIn();
	}
}

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

package zeldaswordskills.handler;

import mods.battlegear2.api.PlayerEventChild.ShieldBlockEvent;
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.ISpecialBow;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import zeldaswordskills.api.item.IReflective;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.item.ItemZeldaArrow;
import zeldaswordskills.item.ItemZeldaShield;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * Specifically for handling Battlegear2 events
 *
 */
public class BattlegearEvents {

	@SubscribeEvent
	public void onBlocked(ShieldBlockEvent event) {
		// Event only fires after BG2 has determined a successful block, i.e. player is blocking, opponent is in front, etc.
		// Ignore ZSSPlayerInfo#canBlock() - only BG2-compatible shields post this event, meaning they use the stamina bar
		boolean wasReflected = false;
		if (ZSSCombatEvents.wasProjectileReflected(event.shield, event.entityPlayer, event.source, event.ammount)) {
			wasReflected = true;
			if (event.shield.getItem() instanceof IReflective) {
				((IReflective) event.shield.getItem()).onReflected(event.shield, event.entityPlayer, event.source, event.ammount);
			} else {
				ZSSPlayerInfo.get(event.entityPlayer).onAttackBlocked(event.shield, event.ammount);
			}
		}
		if (event.shield.getItem() instanceof ItemZeldaShield) {
			event.ammountRemaining = ((ItemZeldaShield) event.shield.getItem()).onBlock(event.entityPlayer, event.shield, event.source, event.ammount, wasReflected);
			event.performAnimation = false;
			event.damageShield = false;
		}
		// Event can't be directly canceled, so set amount remaining to 0 after processing #onBlock for ZSS shields
		if (wasReflected) {
			event.ammountRemaining = 0.0F;
		}
	}

	/**
	 * Returns the currently selected quiver arrow, or null if empty or no quiver
	 * @param quiver if null, current quiver retrieved using {@link QuiverArrowRegistry#getArrowContainer}
	 */
	public static ItemStack getQuiverArrow(ItemStack bow, EntityPlayer player) {
		return getQuiverArrow(bow, QuiverArrowRegistry.getArrowContainer(bow, player), player);
	}

	/**
	 * Returns the currently selected quiver arrow, or null if quiver is null or does not contain arrows
	 */
	public static ItemStack getQuiverArrow(ItemStack bow, ItemStack quiver, EntityPlayer player) {
		if (quiver != null) {
			int slot = ((IArrowContainer2) quiver.getItem()).getSelectedSlot(quiver);
			return ((IArrowContainer2) quiver.getItem()).getStackInSlot(quiver, slot);
		}
		return null;
	}

	/**
	 * Required to prevent nocking unusable ZSS arrows in vanilla and other bows
	 * (merely cosmetic, as they can not be fired by the default fire handler)
	 */
	@SubscribeEvent(priority=EventPriority.HIGH)
	public void preArrowNock(ArrowNockEvent event) {
		// ISpecialBows should determine nocking result on their own
		if (!(event.result.getItem() instanceof ISpecialBow)) {
			ItemStack arrow = getQuiverArrow(event.result, event.entityPlayer);
			if (arrow != null && arrow.getItem() instanceof ItemZeldaArrow) {
				event.setCanceled(!event.entityPlayer.capabilities.isCreativeMode);
			}
		}
	}
}

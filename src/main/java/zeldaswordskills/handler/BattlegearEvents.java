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
import mods.battlegear2.api.quiver.IArrowContainer2;
import mods.battlegear2.api.quiver.QuiverArrowRegistry;
import mods.battlegear2.enchantments.BaseEnchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import zeldaswordskills.item.ItemHeroBow;
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
		if (event.shield.getItem() instanceof ItemZeldaShield) {
			((ItemZeldaShield) event.shield.getItem()).onBlock(event.entityPlayer, event.shield, event.source, event.ammount);
			event.performAnimation = false;
			event.damageShield = false;
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
	 * Required to prevent unshootable arrows from being nocked and drawn (merely cosmetic, as
	 * they could not be fired by the fire handler), as well as for the Hero Bow to be able to
	 * nock arrows from inventory.
	 * Canceling will prevent BG2 processing
	 */
	@SubscribeEvent(priority=EventPriority.HIGH)
	public void preArrowNock(ArrowNockEvent event) {
		// For quivers: prevent nocking of unusable arrows, so player does not think it is usable
		// No quiver: allow custom bow to determine if it can be nocked, e.g. look for valid arrow in inventory
		ItemStack arrow = getQuiverArrow(event.result, event.entityPlayer);
		if (arrow != null) { // cancel event if arrow can not be nocked:
			if (event.result.getItem() instanceof ItemHeroBow) {
				if (((ItemHeroBow) event.result.getItem()).canShootArrow(event.entityPlayer, event.result, arrow)) {
					event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result) - EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.effectId, event.result) * 20000);
				}
				event.setCanceled(true);
			} else if (arrow.getItem() instanceof ItemZeldaArrow && !event.entityPlayer.capabilities.isCreativeMode) {
				// Does this ever happen???
				//LogHelper.info("Attempted to nock Zelda Arrow from quiver with vanilla bow - canceling");
				event.setCanceled(true); // allow API for other bows to shoot zelda arrows?
			}
		} else if (event.result.getItem() instanceof ItemHeroBow) {
			if (((ItemHeroBow) event.result.getItem()).nockArrowFromInventory(event.result, event.entityPlayer)) {
				event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result) - EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.effectId, event.result) * 20000);
			}
			event.setCanceled(true);
		}
	}

	/**
	 * Only required to allow custom fire handler to handle vanilla arrow; once API
	 * changes to allow ordered fire handler lists, this will be redundant.
	 */
	@SubscribeEvent(priority=EventPriority.HIGH)
	public void preArrowLoose(ArrowLooseEvent event) {
		ItemStack quiverStack = QuiverArrowRegistry.getArrowContainer(event.bow, event.entityPlayer);
		ItemStack arrowStack = getQuiverArrow(event.bow, quiverStack, event.entityPlayer);
		if (arrowStack != null) { // quiverStack implicitly checked by getQuiverArrow
			if (event.bow.getItem() instanceof ItemHeroBow) {
				((ItemHeroBow) event.bow.getItem()).bg2FireArrow(event, quiverStack, arrowStack);
				event.setCanceled(true);
			}
		}
	}
}

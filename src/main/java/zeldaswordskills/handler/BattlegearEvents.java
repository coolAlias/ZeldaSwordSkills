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
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import zeldaswordskills.item.ItemHeroBow;
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
	private static ItemStack getQuiverArrow(ItemStack bow, ItemStack quiver, EntityPlayer player) {
		if (quiver != null) {
			int slot = ((IArrowContainer2) quiver.getItem()).getSelectedSlot(quiver);
			return ((IArrowContainer2) quiver.getItem()).getStackInSlot(quiver, slot);
		}
		return null;
	}

	/**
	 * Fake event listener to handle arrow nock events, since BG2's handler is set
	 * to receive canceled -.-
	 * In order to allow quiver use, must not post the event to the event bus.
	 * @return True if event is 'canceled'
	 */
	public static boolean preArrowNock(ArrowNockEvent event) {
		boolean isCanceled = false;
		ItemStack arrow = getQuiverArrow(event.result, event.entityPlayer);
		if (arrow != null) { // cancel event if arrow can not be nocked:
			if (event.result.getItem() instanceof ItemHeroBow) {
				if (((ItemHeroBow) event.result.getItem()).canShootArrow(event.entityPlayer, event.result, arrow)) {
					event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result) - EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.effectId, event.result) * 20000);
				}
				isCanceled = true;
			} // cannot handle vanilla bow or other cases since this is not the real event...
		} else if (event.result.getItem() instanceof ItemHeroBow) {
			// standard nock must set an item and cancel to prevent BG2 processing
			if (((ItemHeroBow) event.result.getItem()).nockArrowFromInventory(event.result, event.entityPlayer)) {
				event.entityPlayer.setItemInUse(event.result, event.result.getItem().getMaxItemUseDuration(event.result) - EnchantmentHelper.getEnchantmentLevel(BaseEnchantment.bowCharge.effectId, event.result) * 20000);
			}
			isCanceled = true;
		}
		return isCanceled;
	}

	/**
	 * Fake event listener to handle arrow loose events, since BG2's handler is set
	 * to receive canceled -.-
	 * In order to allow quiver use, must not post the event to the event bus.
	 * @return True if event is 'canceled'
	 */
	public static boolean preArrowLoose(ArrowLooseEvent event) {
		boolean isCanceled = false;
		ItemStack quiverStack = QuiverArrowRegistry.getArrowContainer(event.bow, event.entityPlayer);
		ItemStack arrowStack = getQuiverArrow(event.bow, quiverStack, event.entityPlayer);
		if (arrowStack != null) { // quiverStack implicitly checked by getQuiverArrow
			if (event.bow.getItem() instanceof ItemHeroBow) {
				((ItemHeroBow) event.bow.getItem()).bg2FireArrow(event, quiverStack, arrowStack);
				isCanceled = true;
			}
			// cannot handle vanilla bow or other cases since this is not the real event...
			// BG2 BUG: Arrows shot from vanilla bow + quiver while in Creative Mode are added to the inventory when picked up
		}
		return isCanceled;
	}
}

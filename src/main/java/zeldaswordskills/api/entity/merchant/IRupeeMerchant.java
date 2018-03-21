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

package zeldaswordskills.api.entity.merchant;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;

/**
 * 
 * Players can interact with any Entity implementing this interface to open
 * up the default rupee trading interface and make purchases using rupees.
 * 
 * If the Entity is also an {@link IMerchant}, the IMerchant trading interface
 * is given preference; players must sneak-interact to open the rupee interface.
 * 
 */
public interface IRupeeMerchant {

	EntityPlayer getRupeeCustomer();

	/**
	 * Set the merchant's customer; called prior to opening the GUI
	 */
	void setRupeeCustomer(EntityPlayer player);

	/**
	 * Called after setting the customer to open the appropriate GUI screen; use {@link EntityPlayer#openGui} with appropriate arguments.
	 * @param getItemsToSell whether to fetch the list of trades the merchant is selling (true) or buying (false)
	 */
	void openRupeeGui(EntityPlayer player, boolean getItemsToSell);

	/**
	 * Requests the merchant's rupee trading list for either buying or selling.
	 * Note that this method is called extremely frequently, especially while trading;
	 * any player-specific customizations should be handled in {@link #getCustomizedRupeeTrades(EntityPlayer, boolean)}. 
	 * @param getItemsToSell whether to fetch the list of trades the merchant is selling (true) or buying (false)
	 * @return the list of RupeeTrades this merchant has available given the current context
	 */
	RupeeTradeList<RupeeTrade> getRupeeTrades(boolean getItemsToSell);

	/**
	 * Requests the merchant's rupee trading list for either buying or selling, customized
	 * for the given player. This is a suitable time to ensure trade lists are populated.
	 * <br><br>
	 * This should only be called when the current rupee customer is null to prevent
	 * modifying the trade list while another player is using it.
	 * @param player The player interacting with the merchant
	 * @param getItemsToSell getItemsToSell whether to fetch the list of trades the merchant is selling (true) or buying (false)
	 * @return the list of RupeeTrades this merchant has available given the current context, customized for the player
	 */
	RupeeTradeList<RupeeTrade> getCustomizedRupeeTrades(EntityPlayer player, boolean getItemsToSell);

	/**
	 * Used client side when opening the trading interface and server side when loading default trade lists.
	 * @param getItemsToSell whether to fetch the list of trades the merchant is selling (true) or buying (false)
	 */
	void setRupeeTrades(RupeeTradeList<RupeeTrade> trades, boolean getItemsToSell);

	/**
	 * Called when the current customer buys or sells an item for rupees; client side calls are
	 * mainly to increment the trade's number of times used so it can display properly in the GUI.
	 * @param getItemsToSell whether to fetch the list of trades the merchant is selling (true) or buying (false)
	 */
	void useRupeeTrade(RupeeTrade trade, boolean getItemsToSell);

	/**
	 * Called during EntityInteractEvent when the player interacts with this merchant.
	 * <br>This is a good time to ensure trade lists are populated.
	 * <br>Ideally returns the same value on both client and server, but only the server result is important.
	 * @return Result determining how to proceed with opening of the rupee trading interface:
	 * <br>DEFAULT allows the rupee trading interface to open provided typical merchant conditions
	 *     are met (e.g. merchant is alive); IMerchants will DENY unless the player is sneaking.
	 * <br>DENY prevents the interface from opening
	 * <br>ALLOW allows the interface to open with no further checks
	 */
	Result onInteract(EntityPlayer player);

	/**
	 * Called after processing {@link #onInteract(EntityPlayer)} to determine whether the EntityInteractEvent should be canceled
	 * @param player the player for whom the GUI may have opened
	 * @param result the Result from {@link #onInteract(EntityPlayer)}; note that the Result may
	 *               have been changed to e.g. DENY by the interaction Event if the GUI did not open.
	 * @return true to cancel the interaction event; false to allow it to continue to e.g. Entity#interact 
	 */
	boolean wasInteractionHandled(EntityPlayer player, Result result);

}

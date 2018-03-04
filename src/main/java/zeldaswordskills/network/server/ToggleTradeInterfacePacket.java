/**
    Copyright (C) <2018> <coolAlias>

    This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
    you can redistribute it and/or modify it under the terms of the GNU
    General Public License as published by the Free Software Foundation,
    either version 3 of the License, or (at your option) any later version.

    This program is distributed buffer the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.network.server;

import java.io.IOException;
import java.lang.reflect.Field;

import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.entity.VanillaRupeeMerchant;
import zeldaswordskills.inventory.ContainerRupeeMerchant;
import zeldaswordskills.network.AbstractMessage.AbstractServerMessage;

/**
 * 
 * Handles request to toggle currently open trading interface between IMerchant and IRupeeMerchant
 *
 */
public class ToggleTradeInterfacePacket extends AbstractServerMessage<ToggleTradeInterfacePacket>
{
	public ToggleTradeInterfacePacket() {}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (player.openContainer instanceof ContainerRupeeMerchant) {
			ContainerRupeeMerchant container = (ContainerRupeeMerchant) player.openContainer;
			IMerchant merchant = this.getVanillaMerchant(container.getMerchant());
			if (merchant != null && merchant.getCustomer() == null) {
				MerchantRecipeList trades = merchant.getRecipes(player);
				if (trades != null && !trades.isEmpty()) {
					String name = (merchant instanceof EntityLiving ? ((EntityLiving) merchant).getCustomNameTag() : "");
					// Vanilla #displayGUI methods assign player.openContainer directly without
					// calling Container#onContainerClosed on the currently open container because
					// they don't expect one; ContainerRupeeMerchant needs to be closed properly.
					container.onContainerClosed(player);
					merchant.setCustomer(player);
					player.displayGUIMerchant(merchant, name);
				}
			}
		} else if (player.openContainer instanceof ContainerMerchant) {
			// Opening rupee trading interface from vanilla trading interface
			IMerchant vanillaMerchant = ToggleTradeInterfacePacket.getMerchantFromContainer((ContainerMerchant) player.openContainer);
			IRupeeMerchant merchant = RupeeMerchantHelper.getRupeeMerchant(vanillaMerchant);
			if (merchant != null && vanillaMerchant.getCustomer() == player) {
				// Set vanilla rupee merchant's customer (uses vanilla merchant's) to null temporarily
				vanillaMerchant.setCustomer(null);
				// Make sure rupee trade lists are populated and customized, since
				// IRupeeMerchant#onInteract will not be called in this case
				RupeeTradeList<RupeeTrade> trades = RupeeMerchantHelper.getRupeeTrades(merchant, true, player);
				boolean hasTrades = (trades != null && !trades.isEmpty());
				boolean getItemsToSell = hasTrades; // true if there are items to sell, false if not and should try items to buy
				if (!hasTrades) {
					trades = RupeeMerchantHelper.getRupeeTrades(merchant, false, player);
					hasTrades = (trades != null && !trades.isEmpty());
				}
				// Re-set customer if the current gui should not close
				if (!hasTrades || (!RupeeMerchantHelper.openRupeeMerchantGui(merchant, player, getItemsToSell) && player.openContainer instanceof ContainerMerchant)) {
					vanillaMerchant.setCustomer(player);
				}
			}
		}
	}

	protected IMerchant getVanillaMerchant(IRupeeMerchant merchant) {
		if (merchant instanceof IMerchant) {
			return (IMerchant) merchant;
		} else if (merchant instanceof VanillaRupeeMerchant) {
			return ((VanillaRupeeMerchant) merchant).getVillager();
		}
		return null;
	}

	/** Accessible reference to {@code ContainerMerchant#theMerchant */
	private static Field merchant;
	public static IMerchant getMerchantFromContainer(ContainerMerchant container) {
		if (merchant == null) {
			merchant = ReflectionHelper.findField(ContainerMerchant.class, "field_75178_e", "theMerchant");
		}
		try {
			return (IMerchant) merchant.get(container);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

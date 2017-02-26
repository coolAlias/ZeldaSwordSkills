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

package zeldaswordskills.network.client;

import java.io.IOException;

import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeTradeList;
import zeldaswordskills.client.gui.GuiRupeeMerchant;
import zeldaswordskills.network.AbstractMessage.AbstractClientMessage;

public class SyncRupeeMerchantPacket extends AbstractClientMessage<SyncRupeeMerchantPacket>
{
	private int windowId;

	private NBTTagCompound compound;

	public SyncRupeeMerchantPacket() {}

	public SyncRupeeMerchantPacket(EntityPlayerMP player, IRupeeMerchant merchant) {
		this.windowId = player.currentWindowId;
		RupeeTradeList buys = merchant.getRupeeTrades(false);
		RupeeTradeList sells = merchant.getRupeeTrades(true);
		this.compound = new NBTTagCompound();
		if (buys != null) {
			this.compound.setTag("buys", buys.writeToNBT());
		}
		if (sells != null) {
			this.compound.setTag("sells", sells.writeToNBT());
		}
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		this.windowId = buffer.readInt();
		this.compound = buffer.readNBTTagCompoundFromBuffer();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		buffer.writeInt(this.windowId);
		buffer.writeNBTTagCompoundToBuffer(this.compound);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui instanceof GuiRupeeMerchant && this.windowId == player.openContainer.windowId) {
			IRupeeMerchant merchant = ((GuiRupeeMerchant) gui).getMerchant();
			if (this.compound.hasKey("buys", Constants.NBT.TAG_COMPOUND)) {
				RupeeTradeList buys = new RupeeTradeList((NBTTagCompound) this.compound.getTag("buys"));
				merchant.setRupeeTrades(buys, false);
			}
			if (this.compound.hasKey("sells", Constants.NBT.TAG_COMPOUND)) {
				RupeeTradeList sells = new RupeeTradeList((NBTTagCompound) this.compound.getTag("sells"));
				merchant.setRupeeTrades(sells, true);
			}
		} else {
			ZSSMain.logger.error("Failed to sync rupee trades; current gui: " + (gui == null ? "null" : gui.getClass().getSimpleName()));
			ZSSMain.logger.error(String.format("Expected window id: %d; Current window id: %d", this.windowId, player.openContainer.windowId));
		}
	}
}

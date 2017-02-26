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

package zeldaswordskills.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemWalletUpgrade;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.OpenGuiPacket;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public final class GuiButtonWallet extends GuiButton implements IGuiButtonPostDraw
{
	private static final ResourceLocation SLOT_BG = new ResourceLocation(ModInfo.ID, "textures/gui/slot.png");
	private static final int BG_ICON_SIZE = 18;
	private static final int ICON_SIZE = 16;
	private ZSSPlayerWallet wallet;

	public GuiButtonWallet(int id, int x, int y) {
		super(id, x, y, BG_ICON_SIZE, BG_ICON_SIZE, "");
		this.width = GuiButtonWallet.BG_ICON_SIZE;
		this.height = GuiButtonWallet.BG_ICON_SIZE;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			if (mc.currentScreen == null || mc.currentScreen.getClass() != GuiWallet.class) {
				PacketDispatcher.sendToServer(new OpenGuiPacket(GuiHandler.GUI_WALLET));
			}
			return true;
		}
		return false;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible && mc.thePlayer != null) {
			if (this.wallet == null) {
				this.wallet = ZSSPlayerWallet.get(mc.thePlayer);
			}
			this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int hoverState = this.getHoverState(this.field_146123_n);
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(SLOT_BG);
			RenderHelperQ.drawTexturedRect(this.xPosition, this.yPosition, 0, 0, BG_ICON_SIZE, BG_ICON_SIZE, BG_ICON_SIZE, BG_ICON_SIZE);
			// Highlight when mouse over slot
			if (hoverState == 2) {
				GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glColorMask(true, true, true, false);
				this.drawGradientRect(this.xPosition + 1, this.yPosition + 1, this.xPosition + 17, this.yPosition + 17, -2130706433, -2130706433);
				GL11.glPopAttrib();
			}
			// Draw wallet icon
			ResourceLocation walletIcon = ItemWalletUpgrade.TEXTURES.get(this.wallet.getWallet().icon_index);
			mc.getTextureManager().bindTexture(walletIcon);
			RenderHelperQ.drawTexturedRect(this.xPosition + 1, this.yPosition + 1, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
			GL11.glPopAttrib();
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	@Override
	public void postDraw(GuiScreen gui, int mouseX, int mouseY) {
		// Calculate hover directly since #getHoverState seems to be restricted to coordinates within the GuiScreen
		if (this.wallet != null && mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
			this.renderToolTip(gui, mouseX, mouseY);
		}
	}

	private void renderToolTip(GuiScreen screen, int x, int y) {
		List<String> list = new ArrayList<String>();
		list.add(StatCollector.translateToLocal(this.wallet.getWallet().getUnlocalizedName()));
		if (this.wallet.getCapacity() > 0) {
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("tooltip.zss.wallet.desc.0"));
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("tooltip.zss.wallet.desc.1"));
			list.add(EnumChatFormatting.BLUE + StatCollector.translateToLocalFormatted("tooltip.zss.wallet.rupees", this.wallet.getRupees(), this.wallet.getCapacity()));
		} else {
			list.add(StatCollector.translateToLocal("tooltip.zss.wallet.none.desc"));
		}
		RenderHelperQ.drawHoveringText(screen, list, x, y);
	}
}

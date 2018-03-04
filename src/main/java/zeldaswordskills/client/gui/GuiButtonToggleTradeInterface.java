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
import net.minecraft.util.StatCollector;
import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.server.ToggleTradeInterfacePacket;

/**
 * 
 * Button used to toggle between IMerchant and IRupeeMerchant trading interfaces.
 *
 */
@SideOnly(Side.CLIENT)
public final class GuiButtonToggleTradeInterface extends GuiButton implements IGuiButtonPostDraw
{
	private final boolean isRupeeGui;

	/**
	 * @param isRupeeGui Whether the currently displayed GUI is the rupee trading GUI 
	 */
	public GuiButtonToggleTradeInterface(int id, boolean isRupeeGui, int x, int y) {
		super(id, x, y, 17, 18, "");
		this.isRupeeGui = isRupeeGui;
		this.width = 17;
		this.height = 18;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			// Unable to do any sanity checks here without an IRupeeMerchant instance,
			// and unable to have one of those because GuiMerchant's IMerchant is a fake.
			PacketDispatcher.sendToServer(new ToggleTradeInterfacePacket());
			return true;
		}
		return false;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(GuiRupeeMerchant.TEXTURE);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int u = (this.isRupeeGui ? 175 + this.width : 175);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, u, 52, this.width, this.height);
		}
	}

	@Override
	public void postDraw(GuiScreen gui, int mouseX, int mouseY) {
		// Calculate hover directly since #getHoverState seems to be restricted to coordinates within the GuiScreen
		if (mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height) {
			this.renderToolTip(gui, mouseX, mouseY);
		}
	}

	private void renderToolTip(GuiScreen screen, int x, int y) {
		List<String> list = new ArrayList<String>();
		String s = (this.isRupeeGui ? "gui.zss.button.toggle_rupee_gui.tooltip" : "gui.zss.button.toggle_merchant_gui.tooltip");
		list.add(StatCollector.translateToLocal(s));
		RenderHelperQ.drawHoveringText(screen, list, x, y);
	}
}

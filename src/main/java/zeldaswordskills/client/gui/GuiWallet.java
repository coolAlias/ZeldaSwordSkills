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

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.inventory.ContainerWallet;
import zeldaswordskills.ref.ModInfo;

public class GuiWallet extends GuiContainer
{
	private static final ResourceLocation TEXTURE = new ResourceLocation(ModInfo.ID, "textures/gui/gui_wallet.png");

	private final ZSSPlayerWallet wallet;

	private GuiButton btnBack;

	public GuiWallet(EntityPlayer player) {
		super(new ContainerWallet(player));
		this.wallet = ZSSPlayerWallet.get(player);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		String s = StatCollector.translateToLocal("gui.done");
		int stringWidth = 10 + this.mc.fontRenderer.getStringWidth(s);
		this.btnBack = new GuiButton(0, (this.width - stringWidth) / 2, this.guiTop + 30, stringWidth, 20, s);
		this.buttonList.add(this.btnBack);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == this.btnBack.id) {
			this.mc.displayGuiScreen(new GuiInventory(this.mc.thePlayer));
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialRenderTick){
		super.drawScreen(mouseX, mouseY, partialRenderTick);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = I18n.format(this.wallet.getWallet().getUnlocalizedName());
		this.fontRendererObj.drawString(name, this.xSize / 2 - this.fontRendererObj.getStringWidth(name) / 2, 10, 0X000000);
		String s = this.wallet.getRupees() + " / " + this.wallet.getCapacity();
		this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 12 + this.fontRendererObj.FONT_HEIGHT, 0XFFFFFF);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialRenderTick, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}

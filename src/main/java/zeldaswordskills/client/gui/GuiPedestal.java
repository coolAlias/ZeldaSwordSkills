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

package zeldaswordskills.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.inventory.container.ContainerPedestal;
import zeldaswordskills.lib.ModInfo;

public class GuiPedestal extends GuiContainer
{
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID + ":textures/gui/gui_pedestal.png");

	private final TileEntityPedestal pedestal;

	public GuiPedestal(InventoryPlayer inv, TileEntityPedestal pedestal) {
		super(new ContainerPedestal(inv, pedestal));
		this.pedestal = pedestal;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String s = pedestal.isInvNameLocalized() ? pedestal.getInvName() : I18n.getString(pedestal.getInvName());
		this.fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.getString("container.inventory"), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		int k = (width - xSize) / 2;
		int l = (height - ySize) / 2;
		drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
		int meta = pedestal.worldObj.getBlockMetadata(pedestal.xCoord, pedestal.yCoord, pedestal.zCoord);
		boolean flag = (meta & 0x8) == 0x8;
		if (flag || (meta & 0x1) == 0x1) {
			drawTexturedModalRect(k + 57, l + 7, 176, 0, 62, 31);
		}
		if (flag || (meta & 0x2) == 0x2) {
			drawTexturedModalRect(k + 26, l + 38, 176, 0, 62, 31);
		}
		if (flag || (meta & 0x4) == 0x4) {
			drawTexturedModalRect(k + 88, l + 38, 176, 0, 62, 31);
		}
	}
}

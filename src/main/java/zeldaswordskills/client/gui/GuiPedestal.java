/**
    Copyright (C) <2017> <coolAlias>

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

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.inventory.ContainerPedestal;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class GuiPedestal extends GuiContainer
{
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_pedestal.png");

	private final TileEntityPedestal pedestal;

	public GuiPedestal(EntityPlayer player, TileEntityPedestal pedestal) {
		super(new ContainerPedestal(player, pedestal));
		this.pedestal = pedestal;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String s = pedestal.hasCustomInventoryName() ? pedestal.getInventoryName() : I18n.format(pedestal.getInventoryName());
		fontRendererObj.drawString(s, xSize / 2 - fontRendererObj.getStringWidth(s) / 2, 6, 4210752);
		fontRendererObj.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		int meta = pedestal.getWorldObj().getBlockMetadata(pedestal.xCoord, pedestal.yCoord, pedestal.zCoord);
		boolean flag = (meta & 0x8) == 0x8;
		if (flag || (meta & 0x1) == 0x1) {
			drawTexturedModalRect(guiLeft + 57, guiTop + 7, 176, 0, 62, 31);
		}
		if (flag || (meta & 0x2) == 0x2) {
			drawTexturedModalRect(guiLeft + 26, guiTop + 38, 176, 0, 62, 31);
		}
		if (flag || (meta & 0x4) == 0x4) {
			drawTexturedModalRect(guiLeft + 88, guiTop + 38, 176, 0, 62, 31);
		}
	}
}

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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.ref.Config;

@SideOnly(Side.CLIENT)
public class GuiItemModeOverlay extends AbstractGuiOverlay
{
	private static final int ICON_SIZE = 18;
	private ItemStack stack;

	public GuiItemModeOverlay(Minecraft mc) {
		super(mc);
		this.width = ICON_SIZE;
		this.height = ICON_SIZE;
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.itemModeHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.itemModeVAlign;
	}

	@Override
	public boolean shouldRender() {
		this.stack = this.mc.thePlayer.getHeldItem();
		return Config.isItemModeEnabled && this.stack != null && this.stack.getItem() instanceof ICyclableItem;
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.setPosX(resolution, this.getOffsetX(DEFAULT_PADDING) + Config.itemModeOffsetX);
		this.setPosY(resolution, this.getOffsetY(DEFAULT_PADDING) + Config.itemModeOffsetY);
	}

	@Override
	protected void render(ScaledResolution resolution) {
		this.stack = ((ICyclableItem) this.stack.getItem()).getRenderStackForMode(this.stack, this.mc.thePlayer);
		if (this.stack != null) {
			int xPos = this.getLeft();
			int yPos = this.getTop();
			GlStateManager.pushAttrib();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			// alpha test and blend needed due to vanilla or Forge rendering bug
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			FontRenderer font = this.stack.getItem().getFontRenderer(this.stack);
			if (font == null) font = this.mc.fontRendererObj;
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.stack, xPos, yPos);
			String text = (this.stack.stackSize == 1 ? null : String.valueOf(this.stack.stackSize));
			this.mc.getRenderItem().renderItemOverlayIntoGUI(font, this.stack, xPos, yPos, text);
			GlStateManager.popAttrib();
		}
	}
}

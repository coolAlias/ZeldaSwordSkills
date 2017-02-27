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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import zeldaswordskills.item.ICyclableItem;
import zeldaswordskills.ref.Config;

@SideOnly(Side.CLIENT)
public class GuiItemModeOverlay extends AbstractGuiOverlay
{
	private static final int ICON_SIZE = 18;
	private final RenderItem itemRender;
	private ItemStack stack;

	public GuiItemModeOverlay(Minecraft mc) {
		super(mc);
		this.itemRender = new RenderItem();
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
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_LIGHTING);
			// alpha test and blend needed due to vanilla or Forge rendering bug
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glTranslatef(0.0F, 0.0F, 32.0F);
			FontRenderer font = this.stack.getItem().getFontRenderer(this.stack);
			if (font == null) font = this.mc.fontRenderer;
			this.itemRender.renderItemAndEffectIntoGUI(font, this.mc.getTextureManager(), this.stack, xPos, yPos);
			String text = (this.stack.stackSize == 1 ? null : String.valueOf(this.stack.stackSize));
			this.itemRender.renderItemOverlayIntoGUI(font, this.mc.getTextureManager(), this.stack, xPos, yPos, text);
			GL11.glPopAttrib();
		}
	}
}

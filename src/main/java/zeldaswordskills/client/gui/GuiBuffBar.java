/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.gui;

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;

/**
 * 
 * Displays active buffs when toggled on or key is held
 *
 */
@SideOnly(Side.CLIENT)
public class GuiBuffBar extends AbstractGuiOverlay
{
	/** Number of icons per row in the texture sheet */
	private static final int ICONS_PER_ROW = 8;
	private static final int ICON_SIZE = 18;
	private static final int ICON_SPACING = ICON_SIZE + DEFAULT_PADDING;
	private static final ResourceLocation BUFF_ICONS = new ResourceLocation(ModInfo.ID, "textures/gui/bufficons.png");
	/** Currently rendering Buffs; set each render cycle during {@link #shouldRender()} */
	protected Collection<BuffBase> buffs;

	public GuiBuffBar(Minecraft mc) {
		super(mc);
	}

	/** Number of buffs to display per row or column */
	private int buffsPerRow() {
		return Config.buffBarMaxIcons;
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.buffBarHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.buffBarVAlign;
	}

	@Override
	public boolean allowMergeX(boolean rendered) {
		return !Config.isBuffBarHorizontal || this.buffs.size() < 4;
	}

	@Override
	public boolean shouldRender() {
		this.buffs = ZSSEntityInfo.get(mc.thePlayer).getActiveBuffs();
		return Config.isBuffBarEnabled && !this.buffs.isEmpty();
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		int n = 0;
		for (Iterator<BuffBase> iterator = this.buffs.iterator(); iterator.hasNext();) {
			BuffBase buff = iterator.next();
			if (buff.getBuff().hasIcon()) { ++n; }
		}
		if (Config.isBuffBarHorizontal) {
			this.width = Math.min(n, this.buffsPerRow()) * ICON_SPACING;
			this.height = ((this.buffsPerRow() + n - 1) / this.buffsPerRow()) * ICON_SPACING;
		} else {
			this.width = ((this.buffsPerRow() + n - 1) / this.buffsPerRow()) * ICON_SPACING;
			this.height = Math.min(n, this.buffsPerRow()) * ICON_SPACING;
		}
		// First one's free. Thanks CL4P-TP.
		this.width -= DEFAULT_PADDING;
		this.height -= DEFAULT_PADDING;
		this.setPosX(resolution, this.getOffsetX(DEFAULT_PADDING) + Config.buffBarOffsetX);
		this.setPosY(resolution, this.getOffsetY(DEFAULT_PADDING) + Config.buffBarOffsetY);
	}

	@Override
	protected void render(ScaledResolution resolution) {
		int xPos = (this.getHorizontalAlignment() == HALIGN.RIGHT ? this.getRight() - ICON_SIZE : this.getLeft());
		int yPos = (this.getVerticalAlignment() == VALIGN.BOTTOM ? this.getBottom() - ICON_SIZE : this.getTop());
		int origX = xPos, origY = yPos; // Save original x and y positions
		int dx = this.getOffsetX(ICON_SPACING);
		int dy = this.getOffsetY(ICON_SPACING);
		int offsetX = 0, offsetY = 0;
		int n = 0;
		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BUFF_ICONS);
		for (Iterator<BuffBase> iterator = this.buffs.iterator(); iterator.hasNext(); offsetX = dx, offsetY = dy, n++) {
			BuffBase buff = iterator.next();
			if (!buff.getBuff().hasIcon()) {
				continue;
			}
			int index = buff.getIconIndex();
			if (n > 0 && n % this.buffsPerRow() == 0) {
				xPos = (Config.isBuffBarHorizontal ? origX : xPos + dx);
				yPos = (Config.isBuffBarHorizontal ? yPos + dy : origY);
			} else {
				xPos += (Config.isBuffBarHorizontal ? offsetX : 0);
				yPos += (Config.isBuffBarHorizontal ? 0 : offsetY);
			}
			drawTexturedModalRect(xPos, yPos, index % ICONS_PER_ROW * ICON_SIZE,
					index / ICONS_PER_ROW * ICON_SIZE, ICON_SIZE, ICON_SIZE);
			if (buff.displayArrow()) {
				drawTexturedModalRect(xPos, yPos, buff.isDebuff() ? ICON_SIZE : 0, 0, ICON_SIZE, ICON_SIZE);
			}
		}
		GlStateManager.popAttrib();
	}
}

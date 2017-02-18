/**
    Copyright (C) <2015> <coolAlias>

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

import java.util.Collection;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Displays active buffs when toggled on or key is held
 *
 */
@SideOnly(Side.CLIENT)
public class GuiBuffBar extends Gui implements IGuiOverlay
{
	private final Minecraft mc;

	/** Buff icons texture sheet */
	private final ResourceLocation textures;

	private static final int ICON_SIZE = 18;
	public static final int ICON_SPACING = ICON_SIZE + 2;
	private static final int ICONS_PER_ROW = 8;

	public GuiBuffBar(Minecraft mc) {
		super();
		this.mc = mc;
		this.textures = new ResourceLocation(ModInfo.ID, "textures/gui/bufficons.png");
	}

	@Override
	public boolean shouldRender() {
		return Config.isBuffBarEnabled && !ZSSEntityInfo.get(mc.thePlayer).getActiveBuffs().isEmpty();
	}

	@Override
	public void renderOverlay(ScaledResolution resolution) {
		int xPos = Config.isBuffBarLeft ? 2 : resolution.getScaledWidth() - (ICON_SPACING + 2);
		int yPos = 2;
		// Adjust for Magic Meter
		if ((Config.isMagicMeterEnabled || Config.isMagicMeterTextEnabled) && Config.isMagicMeterTop && Config.isBuffBarLeft == Config.isMagicMeterLeft) {
			if ((Config.isBuffBarLeft ? GuiMagicMeter.getLeftX(resolution) < ICON_SPACING : GuiMagicMeter.getRightX(resolution) > xPos) && GuiMagicMeter.getBottomY(resolution) > yPos) {
				if (Config.isMagicMeterHorizontal) { // move down
					yPos = GuiMagicMeter.getBottomY(resolution) + (Config.isMagicMeterTextEnabled ? 0 : 2);
				} else { // move right or left
					xPos = (Config.isBuffBarLeft ? GuiMagicMeter.getRightX(resolution) + (Config.isMagicMeterTextEnabled ? 0 : 2) : GuiMagicMeter.getLeftX(resolution) - ICON_SPACING); 
				}
			}
		}
		int offset = 0;
		int increment = Config.isBuffBarHorizontal && !Config.isBuffBarLeft ? -ICON_SPACING : ICON_SPACING;
		Collection<BuffBase> buffs = ZSSEntityInfo.get(mc.thePlayer).getActiveBuffs();
		if (!buffs.isEmpty()) {
			GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(textures);
			for (Iterator<BuffBase> iterator = buffs.iterator(); iterator.hasNext(); offset = increment) {
				BuffBase buff = iterator.next();
				if (!buff.getBuff().hasIcon()) {
					continue;
				}
				int index = buff.getIconIndex();
				xPos += (Config.isBuffBarHorizontal ? offset : 0);
				yPos += (Config.isBuffBarHorizontal ? 0 : offset);
				drawTexturedModalRect(xPos, yPos, index % ICONS_PER_ROW * ICON_SIZE,
						index / ICONS_PER_ROW * ICON_SIZE, ICON_SIZE, ICON_SIZE);
				if (buff.displayArrow()) {
					drawTexturedModalRect(xPos, yPos, buff.isDebuff() ? ICON_SIZE : 0, 0, ICON_SIZE, ICON_SIZE);
				}
			}
			GL11.glPopAttrib();
		}
	}
}

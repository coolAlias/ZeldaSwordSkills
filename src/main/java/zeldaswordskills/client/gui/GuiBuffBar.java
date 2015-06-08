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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
public class GuiBuffBar extends Gui
{
	/** Whether the buff bar should be displayed */
	public static boolean shouldDisplay;

	private final Minecraft mc;

	/** Buff icons texture sheet */
	private final ResourceLocation textures;

	private static final int ICON_SIZE = 18;
	private static final int ICON_SPACING = ICON_SIZE + 2;
	private static final int ICONS_PER_ROW = 8;

	public GuiBuffBar() {
		super();
		shouldDisplay = Config.isBuffBarEnabled();
		this.mc = Minecraft.getMinecraft();
		this.textures = new ResourceLocation(ModInfo.ID, "textures/gui/bufficons.png");
	}

	@SubscribeEvent
	public void onRenderExperienceBar(RenderGameOverlayEvent.Post event) {
		if (!shouldDisplay || event.type != ElementType.EXPERIENCE) {
			return;
		}
		int xPos = Config.isBuffBarLeft() ? 2 : event.resolution.getScaledWidth() - (ICON_SPACING + 2);
		int yPos = 2;
		int offset = 0;
		int increment = Config.isBuffBarHorizontal() && !Config.isBuffBarLeft() ? -ICON_SPACING : ICON_SPACING;
		Collection<BuffBase> collection = ZSSEntityInfo.get(mc.thePlayer).getActiveBuffsMap().values();
		if (!collection.isEmpty()) {
			GlStateManager.pushAttrib();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableLighting();
			// alpha test and blend needed due to vanilla or Forge rendering bug
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			mc.getTextureManager().bindTexture(textures);
			for (Iterator<BuffBase> iterator = ZSSEntityInfo.get(mc.thePlayer).getActiveBuffsMap().values().iterator();
					iterator.hasNext(); offset = increment)
			{
				BuffBase buff = iterator.next();
				int index = buff.getIconIndex();
				xPos += (Config.isBuffBarHorizontal() ? offset : 0);
				yPos += (Config.isBuffBarHorizontal() ? 0 : offset);
				drawTexturedModalRect(xPos, yPos, index % ICONS_PER_ROW * ICON_SIZE,
						index / ICONS_PER_ROW * ICON_SIZE, ICON_SIZE, ICON_SIZE);
				if (buff.displayArrow()) {
					drawTexturedModalRect(xPos, yPos, buff.isDebuff() ? ICON_SIZE : 0, 0, ICON_SIZE, ICON_SIZE);
				}
			}
			GlStateManager.popAttrib();
		}
	}
}

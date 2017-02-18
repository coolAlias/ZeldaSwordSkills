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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;

@SideOnly(Side.CLIENT)
public class GuiMagicMeter extends Gui implements IGuiOverlay
{
	private static final ResourceLocation textureHorizontal = new ResourceLocation(ModInfo.ID, "textures/gui/magic_meter_horizontal.png");
	private static final ResourceLocation textureVertical = new ResourceLocation(ModInfo.ID, "textures/gui/magic_meter_vertical.png");
	private static int MAX_WIDTH;
	private static int NUM_INCREMENTS = 2;
	private static float INCREMENT;
	public static final int METER_HEIGHT = 9;

	/**
	 * Call this method if Config settings change while in game.
	 * Sets the maximum width of the magic meter.
	 * @param value Clamped between 25 and 100
	 */
	public static void setMaxWidth(int value) {
		MAX_WIDTH = MathHelper.clamp_int(value, 25, 100);
		INCREMENT = (float) MAX_WIDTH / (float) NUM_INCREMENTS;
	}

	/**
	 * Call this method if Config settings change while in game.
	 * Sets the number of increments required to max out the magic meter.
	 * @param value Clamped between 1 and 10
	 */
	public static void setNumIncrements(int value) {
		NUM_INCREMENTS = MathHelper.clamp_int(value, 1, 10);
		INCREMENT = (float) MAX_WIDTH / (float) NUM_INCREMENTS;
	}

	/**
	 * Returns the x coordinate of the left-most edge of the Magic Meter
	 */
	public static int getLeftX(ScaledResolution resolution) {
		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode) {
			return 0;
		}
		return (Config.isMagicMeterCenteredX ? resolution.getScaledWidth() / 2 : Config.isMagicMeterLeft ? 0 : resolution.getScaledWidth() - (Config.isMagicMeterEnabled ? (Config.isMagicMeterHorizontal ? MAX_WIDTH : METER_HEIGHT) : 0)) + Config.magicMeterOffsetX;
	}

	/**
	 * Returns the x coordinate of the right-most edge of the Magic Meter
	 */
	public static int getRightX(ScaledResolution resolution) {
		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode) {
			return 0;
		}
		int left = getLeftX(resolution);
		return (Config.isMagicMeterEnabled ? (Config.isMagicMeterHorizontal ? left + MAX_WIDTH : left + METER_HEIGHT) : left);
	}

	/**
	 * Returns the y coordinate of the top-most edge of the Magic Meter including the text element, if any
	 */
	public static int getTopY(ScaledResolution resolution) {
		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode) {
			return 0;
		}
		int y = (Config.isMagicMeterTop ? 0 : resolution.getScaledHeight() - (Config.isMagicMeterEnabled ? (Config.isMagicMeterHorizontal ? METER_HEIGHT : MAX_WIDTH + 6) : 0)) + Config.magicMeterOffsetY;
		if (Config.isMagicMeterTextEnabled && !Config.isMagicMeterTop) {
			y -= (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + (Config.isMagicMeterEnabled ? 2 : 0));
		}
		return y;
	}

	/**
	 * Returns the y coordinate of the bottom-most edge of the Magic Meter including the text element, if any
	 */
	public static int getBottomY(ScaledResolution resolution) {
		if (Minecraft.getMinecraft().thePlayer != null && Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode) {
			return 0;
		}
		int y = (Config.isMagicMeterTop ? (Config.isMagicMeterEnabled ? (Config.isMagicMeterHorizontal ? METER_HEIGHT : MAX_WIDTH + 6) : 0) : resolution.getScaledHeight()) + Config.magicMeterOffsetY;
		if (Config.isMagicMeterTextEnabled && Config.isMagicMeterTop) {
			y += (Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + (Config.isMagicMeterEnabled ? 2 : 0));
		}
		return y;
	}

	private final Minecraft mc;

	public GuiMagicMeter(Minecraft mc) {
		this.mc = mc;
		GuiMagicMeter.setMaxWidth(Config.magicMeterWidth);
		GuiMagicMeter.setNumIncrements(Config.magicMeterIncrements);
	}

	@Override
	public boolean shouldRender() {
		if (mc.thePlayer.capabilities.isCreativeMode) {
			return false;
		}
		return (Config.isMagicMeterEnabled || Config.isMagicMeterTextEnabled) && ZSSPlayerInfo.get(mc.thePlayer).getMaxMagic() > 0;
	}

	@Override
	public void renderOverlay(ScaledResolution resolution) {
		ZSSPlayerInfo info = ZSSPlayerInfo.get(mc.thePlayer);
		boolean unlimited = ZSSEntityInfo.get(mc.thePlayer).isBuffActive(Buff.UNLIMITED_MAGIC);
		float maxMana = info.getMaxMagic();
		int width = MathHelper.clamp_int(MathHelper.floor_float((maxMana / 50) * INCREMENT), MathHelper.floor_float(INCREMENT), MAX_WIDTH);
		int current = MathHelper.floor_float((info.getCurrentMagic() / maxMana) * width);
		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (unlimited) {
			GlStateManager.color(0.5F, 0.5F, 1.0F, 1.0F);
		} else {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		int xPos = (Config.isMagicMeterCenteredX ? resolution.getScaledWidth() / 2 : Config.isMagicMeterLeft ? 0 : resolution.getScaledWidth()) + Config.magicMeterOffsetX;
		int yPos = (Config.isMagicMeterTop ? 0 : resolution.getScaledHeight()) + Config.magicMeterOffsetY;
		if (Config.isMagicMeterEnabled) {
			if (Config.isMagicMeterHorizontal) {
				xPos -= (Config.isMagicMeterLeft ? 0 : width + 6);
				yPos -= (Config.isMagicMeterTop ? 0 : METER_HEIGHT);
				mc.getTextureManager().bindTexture(textureHorizontal);
				RenderHelperQ.drawTexturedRect(xPos, yPos, 0, 0, 3 + width, METER_HEIGHT, 106, 12);
				RenderHelperQ.drawTexturedRect(xPos + 3 + width, yPos, 103, 0, 3, METER_HEIGHT, 106, 12);
				RenderHelperQ.drawTexturedRect(xPos + 3 + (Config.isMagicMeterLeft ? 0 : width - current), yPos + 3, 0, METER_HEIGHT, current, 3, 106, 12);
				xPos += (Config.isMagicMeterLeft ? 0 : (width + 6));
				yPos += (Config.isMagicMeterTop ? 11 : -2);
			} else {
				xPos -= (Config.isMagicMeterLeft ? 0 : METER_HEIGHT);
				yPos -= (Config.isMagicMeterTop ? 0 : width + 6);
				mc.getTextureManager().bindTexture(textureVertical);
				RenderHelperQ.drawTexturedRect(xPos, yPos, 0, 0, METER_HEIGHT, 3 + width, 12, 106);
				RenderHelperQ.drawTexturedRect(xPos, yPos + 3 + width, 0, 103, METER_HEIGHT, 3, 12, 106);
				RenderHelperQ.drawTexturedRect(xPos + 3, yPos + 3 + (width - current), METER_HEIGHT, 0, 3, current, 12, 106);
				xPos += (Config.isMagicMeterLeft ? 0 : METER_HEIGHT);
				yPos += (Config.isMagicMeterTop ? width + 8 : -(width + 8));
			}
		}
		if (Config.isMagicMeterTextEnabled) {
			String mp = "MP " + (int) Math.ceil(info.getCurrentMagic()) + "/" + (int) Math.ceil(info.getMaxMagic());
			xPos -= (Config.isMagicMeterLeft ? 0 : mc.fontRendererObj.getStringWidth(mp));
			yPos -= (Config.isMagicMeterTop ? 0 : mc.fontRendererObj.FONT_HEIGHT);
			mc.fontRendererObj.drawString(mp, xPos, yPos, 0xFFFFFF, true);
		}
		GlStateManager.popAttrib();
	}
}

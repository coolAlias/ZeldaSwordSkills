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

import net.minecraft.client.Minecraft;
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
public class GuiMagicMeter extends AbstractGuiOverlay
{
	protected static final ResourceLocation HORIZONTAL_BAR = new ResourceLocation(ModInfo.ID, "textures/gui/magic_meter_horizontal.png");
	protected static final ResourceLocation VERTICAL_BAR = new ResourceLocation(ModInfo.ID, "textures/gui/magic_meter_vertical.png");
	public static final int PADDING = 1;
	public static final int METER_HEIGHT = 9;
	private static int NUM_INCREMENTS = 2;
	private static int MAX_WIDTH;
	private static float INCREMENT;
	private ZSSPlayerInfo info;
	/** The width (or height if vertical) of the inner portion of the mana bar */
	protected int inner_bar;

	/**
	 * Call this method if Config settings change while in game.
	 * Sets the maximum width of the magic meter.
	 * @param value Clamped between 25 and 100
	 */
	public static void setMaxWidth(int value) {
		MAX_WIDTH = MathHelper.clamp_int(value, 25, 100);
		INCREMENT = (float) MAX_WIDTH / (float) NUM_INCREMENTS;
	}
	
	public static int getMaxWidth(){return MAX_WIDTH;}
	
	/**
	 * Call this method if Config settings change while in game.
	 * Sets the number of increments required to max out the magic meter.
	 * @param value Clamped between 1 and 10
	 */
	public static void setNumIncrements(int value) {
		NUM_INCREMENTS = MathHelper.clamp_int(value, 1, 10);
		INCREMENT = (float) MAX_WIDTH / (float) NUM_INCREMENTS;
	}

	public static int getNumIncrements(){return NUM_INCREMENTS;}
	
	public static float getIncrementLength(){return INCREMENT;}

	public GuiMagicMeter(Minecraft mc) {
		super(mc);
		GuiMagicMeter.setMaxWidth(Config.magicMeterWidth);
		GuiMagicMeter.setNumIncrements(Config.magicMeterIncrements);
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return Config.magicMeterHAlign;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return Config.magicMeterVAlign;
	}

	@Override
	public boolean allowMergeX(boolean rendered) {
		return !Config.isMagicMeterHorizontal;
	}

	@Override
	public boolean shouldRender() {
		if (!Config.isMagicMeterEnabled || this.mc.thePlayer.capabilities.isCreativeMode) {
			return false;
		}
		this.info = ZSSPlayerInfo.get(this.mc.thePlayer);
		return this.info.getMaxMagic() > 0;
	}

	/** Returns the current max magic value */
	protected float getMaxMagic() {
		return (this.info == null ? 0F : this.info.getMaxMagic());
	}

	/** Returns the current magic value */
	protected float getCurrentMagic() {
		return (this.info == null ? 0F : this.info.getCurrentMagic());
	}

	/** True if the player's mana is currently unlimited */
	protected boolean isUnlimited() {
		return (this.mc.thePlayer == null ? false : ZSSEntityInfo.get(this.mc.thePlayer).isBuffActive(Buff.UNLIMITED_MAGIC));
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.inner_bar = MathHelper.clamp_int(MathHelper.floor_float((this.getMaxMagic() / 50) * INCREMENT), MathHelper.floor_float(INCREMENT), MAX_WIDTH);
		if (Config.isMagicMeterHorizontal) {
			this.width = MAX_WIDTH; // so offsets work the same for bars of differing sizes
			this.height = METER_HEIGHT;
			int offsetX = Config.magicMeterOffsetX;
			if (this.getHorizontalAlignment() == HALIGN.RIGHT) {
				offsetX += (MAX_WIDTH - this.inner_bar - 6);
			} else if (this.getHorizontalAlignment() == HALIGN.CENTER) {
				if (offsetX == 0) { // should be perfectly centered
					this.width = this.inner_bar + 6;
				} else if (!Config.isMagicBarLeft) { // this allows 'centered' bar to be left- or right-aligned as well
					offsetX += (MAX_WIDTH - this.inner_bar - 6);
				}
			}
			this.setPosX(resolution, offsetX);
			this.setPosY(resolution, Config.magicMeterOffsetY);
			this.width = this.inner_bar + 6; // actual rendering width
		} else {
			this.width = METER_HEIGHT;
			this.height = MAX_WIDTH; // so offsets work the same for bars of differing sizes
			int offsetY = Config.magicMeterOffsetY;
			if (this.getVerticalAlignment() == VALIGN.BOTTOM) {
				offsetY += (MAX_WIDTH - this.inner_bar - 6);
			} else if (this.getVerticalAlignment() == VALIGN.CENTER) {
				this.height = this.inner_bar + 6; // bar will be centered
			}
			this.setPosX(resolution, Config.magicMeterOffsetX);
			this.setPosY(resolution, offsetY);
			this.height = this.inner_bar + 6; // actual rendering height
		}
	}

	@Override
	protected void render(ScaledResolution resolution) {
		int xPos = this.getLeft();
		int yPos = this.getTop();
		int current = MathHelper.floor_float((this.getCurrentMagic() / this.getMaxMagic()) * this.inner_bar);
		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		if (this.isUnlimited()) {
			GlStateManager.color(0.5F, 0.5F, 1.0F, 1.0F);
		} else {
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		if (Config.isMagicMeterHorizontal) {
			this.mc.getTextureManager().bindTexture(HORIZONTAL_BAR);
			RenderHelperQ.drawTexturedRect(xPos, yPos, 0, 0, 3 + this.inner_bar, METER_HEIGHT, 106, 12);
			RenderHelperQ.drawTexturedRect(xPos + 3 + this.inner_bar, yPos, 103, 0, 3, METER_HEIGHT, 106, 12);
			RenderHelperQ.drawTexturedRect(xPos + 3 + (Config.isMagicBarLeft ? 0 : this.inner_bar - current), yPos + 3, 0, METER_HEIGHT, current, 3, 106, 12);
		} else {
			this.mc.getTextureManager().bindTexture(VERTICAL_BAR);
			RenderHelperQ.drawTexturedRect(xPos, yPos, 0, 0, METER_HEIGHT, 3 + this.inner_bar, 12, 106);
			RenderHelperQ.drawTexturedRect(xPos, yPos + 3 + this.inner_bar, 0, 103, METER_HEIGHT, 3, 12, 106);
			RenderHelperQ.drawTexturedRect(xPos + 3, yPos + 3 + (Config.isMagicBarLeft ? (this.inner_bar - current) : 0), METER_HEIGHT, 0, 3, current, 12, 106);
		}
		GlStateManager.popAttrib();
	}
}

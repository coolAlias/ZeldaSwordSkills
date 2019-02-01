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
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.ref.Config;

public class GuiMagicMeterText extends AbstractGuiOverlay
{
	protected final GuiMagicMeter meter;
	private ZSSPlayerInfo info;
	protected String text;

	public GuiMagicMeterText(Minecraft mc, GuiMagicMeter meter) {
		super(mc);
		this.meter = meter;
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return this.meter.getHorizontalAlignment();
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return this.meter.getVerticalAlignment();
	}

	@Override
	public boolean shouldRender() {
		if (!Config.isMagicMeterTextEnabled || this.mc.thePlayer.capabilities.isCreativeMode) {
			return false;
		}
		this.info = ZSSPlayerInfo.get(this.mc.thePlayer);
		return this.info.getMaxMagic() > 0;
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.text = StatCollector.translateToLocalFormatted("gui.zss.magic_meter.text", (int) Math.ceil(this.info.getCurrentMagic()), (int) Math.ceil(this.info.getMaxMagic()));
		this.width = this.mc.fontRendererObj.getStringWidth(this.text);
		this.height = this.mc.fontRendererObj.FONT_HEIGHT - DEFAULT_PADDING; // font height seems to include some empty space - remove it
		if (Config.isMagicMeterEnabled) {
			this.x = (this.getHorizontalAlignment() == HALIGN.LEFT ? this.meter.getLeft() + GuiMagicMeter.PADDING : this.meter.getRight() - this.width - GuiMagicMeter.PADDING);
			if (Config.isMagicMeterHorizontal && Config.magicMeterOffsetX == 0 && this.getHorizontalAlignment() == HALIGN.CENTER) {
				this.x += (this.width / 2) - (this.meter.getWidth() / 2); // perfectly centered
			}
			this.y = (this.getVerticalAlignment() == VALIGN.BOTTOM ? this.meter.getTop() - this.mc.fontRendererObj.FONT_HEIGHT : this.meter.getBottom() + GuiMagicMeter.PADDING);
		} else {
			this.setPosX(resolution, Config.magicMeterOffsetX);
			this.setPosY(resolution, Config.magicMeterOffsetY);
		}
	}

	@Override
	protected void render(ScaledResolution resolution) {
		this.mc.fontRendererObj.drawString(this.text, this.getLeft(), this.getTop(), 0xFFFFFF, true);
	}
}

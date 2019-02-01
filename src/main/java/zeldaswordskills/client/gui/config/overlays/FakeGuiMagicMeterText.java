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

package zeldaswordskills.client.gui.config.overlays;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.GuiMagicMeter;
import zeldaswordskills.client.gui.GuiMagicMeterText;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.StringUtils;

public final class FakeGuiMagicMeterText extends GuiMagicMeterText implements IOverlayButton {

	private final String CATEGORY = "magic meter";

	private final Property isMagicMeterTextEnabled = Config.config.get(CATEGORY, "Display Current Magic Points", false);
	private final Property magicMeterHAlign = Config.config.get(CATEGORY, "Magic Meter X Alignment", "center");
	private final Property magicMeterVAlign = Config.config.get(CATEGORY, "Magic Meter Y Alignment", "bottom");
	private final Property magicMeterOffsetX = Config.config.get(CATEGORY, "Magic Meter X Offset", 47);
	private final Property magicMeterOffsetY = Config.config.get(CATEGORY, "Magic Meter Y Offset", -40);

	public FakeGuiMagicMeterText(Minecraft mc, FakeGuiMagicMeter meter) {
		super(mc, meter);
	}

	@Override
	public boolean shouldRender() {
		return Config.isMagicMeterTextEnabled;
	}

	@Override
	public boolean setShouldRender() {
		Config.isMagicMeterTextEnabled = !Config.isMagicMeterTextEnabled;
		isMagicMeterTextEnabled.set(Config.isMagicMeterTextEnabled);
		return shouldRender();
	}

	@Override
	protected void setup(ScaledResolution resolution) {
		this.text = StatCollector.translateToLocalFormatted("gui.zss.magic_meter.text", (int) Math.ceil(Config.getMaxMagicPoints()), (int) Math.ceil(Config.getMaxMagicPoints()));
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

	@Override
	public String getDisplayName() {
		return StringUtils.translateKey(this.getLangKey() + ".text.title");
	}

	@Override
	public String getLangKey() {
		return "config.zss.magic_meter";
	}

	@Override
	public List<Property> getPanelInfo() {
		List<Property> props = new ArrayList<Property>();
		props.add(magicMeterHAlign);
		props.add(magicMeterVAlign);
		props.add(magicMeterOffsetX);
		props.add(magicMeterOffsetY);
		return props;
	}

	@Override
	public boolean renderOverlayBorder() {
		return false;
	}

	@Override
	public void adjustOverlay(char typedChar, int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_R:
			this.resetOverlay();
			break;
		default:
			this.handleAlignment(keyCode);
			this.handleOffset(keyCode);
			break;
		}
	}

	@Override
	public void resetOverlay() {
		Config.magicMeterHAlign = HALIGN.fromString(magicMeterHAlign.setToDefault().getString());
		Config.magicMeterVAlign = VALIGN.fromString(magicMeterVAlign.setToDefault().getString());
		Config.magicMeterOffsetX = magicMeterOffsetX.setToDefault().getInt();
		Config.magicMeterOffsetY = magicMeterOffsetY.setToDefault().getInt();
	}

	@Override
	public void handleAlignment(int keyCode) {
		if (!Config.isMagicMeterEnabled) {
			// If the Magic Meter is enabled, this should huddle it, otherwise it needs access to the HAlign and VAlign
			switch (keyCode) {
			case Keyboard.KEY_LEFT:
				Config.magicMeterHAlign = Config.magicMeterHAlign.prev();
				Config.magicMeterOffsetX = magicMeterOffsetX.setValue(0).getInt();
				magicMeterHAlign.set(Config.magicMeterHAlign.toString());
				break;
			case Keyboard.KEY_RIGHT:
				Config.magicMeterHAlign = Config.magicMeterHAlign.next();
				Config.magicMeterOffsetX = magicMeterOffsetX.setValue(0).getInt();
				magicMeterHAlign.set(Config.magicMeterHAlign.toString());
				break;
			case Keyboard.KEY_UP:
				Config.magicMeterVAlign = Config.magicMeterVAlign.prev();
				Config.magicMeterOffsetY = magicMeterOffsetY.setValue(0).getInt();
				magicMeterVAlign.set(Config.magicMeterVAlign.toString());
				break;
			case Keyboard.KEY_DOWN:
				Config.magicMeterVAlign = Config.magicMeterVAlign.next();
				Config.magicMeterOffsetY = magicMeterOffsetY.setValue(0).getInt();
				magicMeterVAlign.set(Config.magicMeterVAlign.toString());
				break;
			}
		}
	}

	@Override
	public void handleOffset(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_W:
			if (-(Config.magicMeterOffsetY - 1) < mc.currentScreen.height / 4 && this.getTop() > 0) {
				Config.magicMeterOffsetY -= 1;
				magicMeterOffsetY.set(Config.magicMeterOffsetY);
			}
			break;
		case Keyboard.KEY_S:
			if (Config.magicMeterOffsetY + 1 < mc.currentScreen.height / 4 && this.getBottom() < mc.currentScreen.height) {
				Config.magicMeterOffsetY += 1;
				magicMeterOffsetY.set(Config.magicMeterOffsetY);
			}
			break;
		case Keyboard.KEY_A:
			if (-(Config.magicMeterOffsetX - 1) < mc.currentScreen.width / 4 && this.getLeft() > 0) {
				Config.magicMeterOffsetX -= 1;
				magicMeterOffsetX.set(Config.magicMeterOffsetX);
			}
			break;
		case Keyboard.KEY_D:
			if (Config.magicMeterOffsetX + 1 < mc.currentScreen.width / 4 && this.getRight() < mc.currentScreen.width) {
				Config.magicMeterOffsetX += 1;
				magicMeterOffsetX.set(Config.magicMeterOffsetX);
			}
			break;
		}
	}
}

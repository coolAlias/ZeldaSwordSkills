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
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.GuiMagicMeter;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.StringUtils;

public final class FakeGuiMagicMeter extends GuiMagicMeter implements IOverlayButton {

	private final String CATEGORY = "magic meter";

	private final Property isMagicMeterEnabled = Config.config.get(CATEGORY, "Display Magic Meter", true);
	private final Property magicMeterHAlign = Config.config.get(CATEGORY, "Magic Meter X Alignment", "center");
	private final Property magicMeterVAlign = Config.config.get(CATEGORY, "Magic Meter Y Alignment", "bottom");
	private final Property magicMeterOffsetX = Config.config.get(CATEGORY, "Magic Meter X Offset", 47);
	private final Property magicMeterOffsetY = Config.config.get(CATEGORY, "Magic Meter Y Offset", -40);
	private final Property isMagicMeterHorizontal = Config.config.get(CATEGORY, "Magic Meter Displays Horizontally", true);
	private final Property isMagicBarLeft = Config.config.get(CATEGORY, "Drain Magic Bar To the Bottom/Left", true);
	private final Property magicMeterWidth = Config.config.get(CATEGORY, "Magic Meter Width", 75);
	private final Property magicMeterIncrements = Config.config.get(CATEGORY, "Number of Meter Increments", 2);

	public FakeGuiMagicMeter(Minecraft mc) {
		super(mc);
	}

	@Override
	public boolean shouldRender() {
		return Config.isMagicMeterEnabled;
	}

	@Override
	public boolean setShouldRender() {
		Config.isMagicMeterEnabled = !Config.isMagicMeterEnabled;
		isMagicMeterEnabled.set(Config.isMagicMeterEnabled);
		return shouldRender();
	}

	@Override
	protected float getMaxMagic() {
		return 50.0F;
	}

	@Override
	protected float getCurrentMagic() {
		return this.getMaxMagic();
	}

	@Override
	protected boolean isUnlimited() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return StringUtils.translateKey(this.getLangKey() + ".title");
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
		props.add(isMagicMeterHorizontal);
		props.add(isMagicBarLeft);
		props.add(magicMeterWidth);
		props.add(magicMeterIncrements);
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
		case Keyboard.KEY_TAB:
			Config.isMagicBarLeft = !Config.isMagicBarLeft;
			isMagicBarLeft.set(Config.isMagicBarLeft);
			break;
		case Keyboard.KEY_SLASH:
			Config.isMagicMeterHorizontal = !Config.isMagicMeterHorizontal;
			isMagicMeterHorizontal.set(Config.isMagicMeterHorizontal);
			break;
		case Keyboard.KEY_LBRACKET:
			if (Config.magicMeterIncrements > 1) {
				Config.magicMeterIncrements -= 1;
				GuiMagicMeter.setNumIncrements(Config.magicMeterIncrements);
				magicMeterIncrements.set(Config.magicMeterIncrements);
			}
			break;
		case Keyboard.KEY_RBRACKET:
			if (Config.magicMeterIncrements < 10) {
				Config.magicMeterIncrements += 1;
				GuiMagicMeter.setNumIncrements(Config.magicMeterIncrements);
				magicMeterIncrements.set(Config.magicMeterIncrements);
			}
			break;
		case Keyboard.KEY_ADD:
			if (Config.magicMeterWidth < 100) {
				Config.magicMeterWidth += 1;
				GuiMagicMeter.setMaxWidth(Config.magicMeterWidth);
				magicMeterWidth.set(Config.magicMeterWidth);
			}
			break;
		case Keyboard.KEY_SUBTRACT:
			if (Config.magicMeterWidth > 25) {
				Config.magicMeterWidth -= 1;
				GuiMagicMeter.setMaxWidth(Config.magicMeterWidth);
				magicMeterWidth.set(Config.magicMeterWidth);
			}
			break;
		default:
			handleAlignment(keyCode);
			handleOffset(keyCode);
			break;
		}
	}

	@Override
	public void resetOverlay() {
		Config.magicMeterHAlign = HALIGN.fromString(magicMeterHAlign.setToDefault().getString());
		Config.magicMeterVAlign = VALIGN.fromString(magicMeterVAlign.setToDefault().getString());
		Config.magicMeterOffsetX = magicMeterOffsetX.setToDefault().getInt();
		Config.magicMeterOffsetY = magicMeterOffsetY.setToDefault().getInt();
		Config.isMagicMeterHorizontal = isMagicMeterHorizontal.setToDefault().getBoolean();
		Config.isMagicBarLeft = isMagicMeterHorizontal.setToDefault().getBoolean();
		Config.magicMeterWidth = magicMeterWidth.setToDefault().getInt();
		GuiMagicMeter.setMaxWidth(Config.magicMeterWidth);
		Config.magicMeterIncrements = magicMeterIncrements.setToDefault().getInt();
		GuiMagicMeter.setNumIncrements(Config.magicMeterIncrements);
	}

	@Override
	public void handleAlignment(int keyCode) {
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
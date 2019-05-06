/**
    Copyright (C) <2019> <coolAlias>

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

package zeldaswordskills.client.gui.config.overlays;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.ComboOverlay;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.ref.Config;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.StringUtils;

public final class FakeComboOverlay extends ComboOverlay implements IOverlayButton {

	private final String CATEGORY = "combo hud";

	private final Property isComboHudEnabled = Config.config.get(CATEGORY, "Display Combo Counter", true);
	private final Property hitsToDisplay = Config.config.get(CATEGORY, "Hits to Display in Combo HUD", 3);
	private final Property comboHudHAlign = Config.config.get(CATEGORY, "Combo HUD X Alignment", "left");
	private final Property comboHudVAlign = Config.config.get(CATEGORY, "Combo HUD Y Alignment", "top");
	private final Property comboHudOffsetX = Config.config.get(CATEGORY, "Combo HUD X Offset", 0);
	private final Property comboHudOffsetY = Config.config.get(CATEGORY, "Combo HUD Y Offset", 0);

	public FakeComboOverlay(Minecraft mc) {
		super(mc);
		this.combo = new Combo(null, SkillBase.swordBasic, 3, 1000);
		this.combo.add(null, null, 5.0F);
		this.combo.add(null, null, 5.0F);
	}

	@Override
	public boolean shouldRender() {
		this.displayStartTime = Minecraft.getSystemTime();
		return Config.isComboHudEnabled;
	}

	@Override
	public boolean setShouldRender() {
		Config.isComboHudEnabled = !Config.isComboHudEnabled;
		isComboHudEnabled.set(Config.isComboHudEnabled);
		return shouldRender();
	}

	@Override
	public String getDisplayName() {
		return StringUtils.translateKey(this.getLangKey() + ".title");
	}

	@Override
	public String getLangKey() {
		return "config.zss.combo_hud";
	}

	@Override
	public List<Property> getPanelInfo() {
		List<Property> props = new ArrayList<Property>();
		props.add(comboHudHAlign);
		props.add(comboHudVAlign);
		props.add(comboHudOffsetX);
		props.add(comboHudOffsetY);
		props.add(hitsToDisplay);
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
		case Keyboard.KEY_ADD:
			if (Config.hitsToDisplay < 12) {
				Config.hitsToDisplay += 1;
				hitsToDisplay.set(Config.hitsToDisplay);
			}
			break;
		case Keyboard.KEY_SUBTRACT:
			if (Config.hitsToDisplay > 1) {
				Config.hitsToDisplay -= 1;
				hitsToDisplay.set(Config.hitsToDisplay);
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
		Config.hitsToDisplay = hitsToDisplay.setToDefault().getInt();
		Config.comboHudHAlign = HALIGN.fromString(comboHudHAlign.setToDefault().getString());
		Config.comboHudVAlign = VALIGN.fromString(comboHudVAlign.setToDefault().getString());
		Config.comboHudOffsetX = comboHudOffsetX.setToDefault().getInt();
		Config.comboHudOffsetY = comboHudOffsetY.setToDefault().getInt();
	}

	@Override
	public void handleAlignment(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_LEFT:
			Config.comboHudHAlign = Config.comboHudHAlign.prev();
			Config.comboHudOffsetX = comboHudOffsetX.setValue(0).getInt();
			comboHudHAlign.set(Config.comboHudHAlign.toString());
			break;
		case Keyboard.KEY_RIGHT:
			Config.comboHudHAlign = Config.comboHudHAlign.next();
			Config.comboHudOffsetX = comboHudOffsetX.setValue(0).getInt();
			comboHudHAlign.set(Config.comboHudHAlign.toString());
			break;
		case Keyboard.KEY_UP:
			Config.comboHudVAlign = Config.comboHudVAlign.prev();
			Config.comboHudOffsetY = comboHudOffsetY.setValue(0).getInt();
			comboHudVAlign.set(Config.comboHudVAlign.toString());
			break;
		case Keyboard.KEY_DOWN:
			Config.comboHudVAlign = Config.comboHudVAlign.next();
			Config.comboHudOffsetY = comboHudOffsetY.setValue(0).getInt();
			comboHudVAlign.set(Config.comboHudVAlign.toString());
			break;
		}
	}

	@Override
	public void handleOffset(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_W:
			if (-(Config.comboHudOffsetX - 1) < mc.currentScreen.height / 4 && this.getTop() > 0) {
				Config.comboHudOffsetY -= 1;
				comboHudOffsetY.set(Config.comboHudOffsetX);
			}
			break;
		case Keyboard.KEY_S:
			if (Config.comboHudOffsetY + 1 < mc.currentScreen.height / 4 && this.getBottom() < mc.currentScreen.height) {
				Config.comboHudOffsetY += 1;
				comboHudOffsetY.set(Config.comboHudOffsetY);
			}
			break;
		case Keyboard.KEY_A:
			if (-(Config.comboHudOffsetX - 1) < mc.currentScreen.width / 4 && this.getLeft() > 0) {
				Config.comboHudOffsetX -= 1;
				comboHudOffsetX.set(Config.comboHudOffsetX);
			}
			break;
		case Keyboard.KEY_D:
			if (Config.comboHudOffsetX + 1 < mc.currentScreen.width / 4 && this.getRight() < mc.currentScreen.width) {
				Config.comboHudOffsetX += 1;
				comboHudOffsetX.set(Config.comboHudOffsetX);
			}
			break;
		default: break;
		}
	}
}
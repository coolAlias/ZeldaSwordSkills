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
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.StringUtils;

public final class FakeGuiBuffBar extends GuiBuffBar implements IOverlayButton {

	private final String CATEGORY = "buff bar hud";

	private final Property isBuffBarEnabled = Config.config.get(CATEGORY, "Buff Bar Displays at All Times", true);
	private final Property buffBarMaxIcons = Config.config.get(CATEGORY, "Number of Buffs per Row/Column", 5);
	private final Property isBuffBarHorizontal = Config.config.get(CATEGORY, "Display Buff Bar Horizontally", true);
	private final Property buffBarHAlign = Config.config.get(CATEGORY, "Buff HUD X Alignment", "right");
	private final Property buffBarVAlign = Config.config.get(CATEGORY, "Buff HUD Y Alignment", "top");
	private final Property buffBarOffsetX = Config.config.get(CATEGORY, "Buff HUD X Offset", 0);
	private final Property buffBarOffsetY = Config.config.get(CATEGORY, "Buff HUD Y Offset", 0);

	public FakeGuiBuffBar(Minecraft mc) {
		super(mc);
		this.buffs = new ArrayList<BuffBase>();
		// 10 buffs, since the max per row is 10
		this.buffs.add(new BuffBase(Buff.ATTACK_UP, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.DEFENSE_UP, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.EVADE_UP, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_COLD, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_FIRE, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_HOLY, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_MAGIC, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_QUAKE, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_SHOCK, Integer.MAX_VALUE, 0));
		this.buffs.add(new BuffBase(Buff.RESIST_STUN, Integer.MAX_VALUE, 0));
	}

	@Override
	public boolean shouldRender() {
		return Config.isBuffBarEnabled;
	}

	@Override
	public boolean setShouldRender() {
		Config.isBuffBarEnabled = !Config.isBuffBarEnabled;
		isBuffBarEnabled.set(Config.isBuffBarEnabled);
		return shouldRender();
	}

	@Override
	public String getDisplayName() {
		return StringUtils.translateKey(this.getLangKey() + ".title");
	}

	@Override
	public String getLangKey() {
		return "config.zss.buff_bar";
	}

	@Override
	public List<Property> getPanelInfo() {
		List<Property> props = new ArrayList<Property>();
		props.add(buffBarHAlign);
		props.add(buffBarVAlign);
		props.add(buffBarOffsetX);
		props.add(buffBarOffsetY);
		props.add(isBuffBarHorizontal);
		props.add(buffBarMaxIcons);
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
		case Keyboard.KEY_SUBTRACT:
			if (Config.buffBarMaxIcons > 1) {
				Config.buffBarMaxIcons -= 1;
				buffBarMaxIcons.set(Config.buffBarMaxIcons);
			}
			break;
		case Keyboard.KEY_ADD:
			if (Config.buffBarMaxIcons < 10) {
				Config.buffBarMaxIcons += 1;
				buffBarMaxIcons.set(Config.buffBarMaxIcons);
			}
			break;
		case Keyboard.KEY_SLASH:
			Config.isBuffBarHorizontal = !Config.isBuffBarHorizontal;
			isBuffBarHorizontal.set(Config.isBuffBarHorizontal);
			break;
		default:
			handleAlignment(keyCode);
			handleOffset(keyCode);
			break;
		}
	}

	@Override
	public void resetOverlay() {
		Config.buffBarMaxIcons = buffBarMaxIcons.setToDefault().getInt();
		Config.isBuffBarHorizontal = isBuffBarHorizontal.setToDefault().getBoolean();
		Config.buffBarHAlign = HALIGN.fromString(buffBarHAlign.setToDefault().getString());
		Config.buffBarVAlign = VALIGN.fromString(buffBarVAlign.setToDefault().getString());
		Config.buffBarOffsetX = buffBarOffsetX.setToDefault().getInt();
		Config.buffBarOffsetY = buffBarOffsetY.setToDefault().getInt();
	}

	@Override
	public void handleAlignment(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_LEFT:
			Config.buffBarHAlign = Config.buffBarHAlign.prev();
			Config.buffBarOffsetX = buffBarOffsetX.setValue(0).getInt();
			buffBarHAlign.set(Config.buffBarHAlign.toString());
			break;
		case Keyboard.KEY_RIGHT:
			Config.buffBarHAlign = Config.buffBarHAlign.next();
			Config.buffBarOffsetX = buffBarOffsetX.setValue(0).getInt();
			buffBarHAlign.set(Config.buffBarHAlign.toString());
			break;
		case Keyboard.KEY_UP:
			Config.buffBarVAlign = Config.buffBarVAlign.prev();
			Config.buffBarOffsetY = buffBarOffsetY.setToDefault().getInt();
			buffBarVAlign.set(Config.buffBarVAlign.toString());
			break;
		case Keyboard.KEY_DOWN:
			Config.buffBarVAlign = Config.buffBarVAlign.next();
			Config.buffBarOffsetY = buffBarOffsetY.setValue(0).getInt();
			buffBarVAlign.set(Config.buffBarVAlign.toString());
			break;
		default: break;
		}
	}

	@Override
	public void handleOffset(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_W:
			if (-(Config.buffBarOffsetY - 1) < mc.currentScreen.height / 4 && this.getTop() > 0) {
				Config.buffBarOffsetY -= 1;
				buffBarOffsetY.set(Config.buffBarOffsetY);
			}
			break;
		case Keyboard.KEY_S:
			if (Config.buffBarOffsetY + 1 < mc.currentScreen.height / 4 && this.getBottom() < mc.currentScreen.height) {
				Config.buffBarOffsetY += 1;
				buffBarOffsetY.set(Config.buffBarOffsetY);
			}
			break;
		case Keyboard.KEY_A:
			if (-(Config.buffBarOffsetX - 1) < mc.currentScreen.width / 4 && this.getLeft() > 0) {
				Config.buffBarOffsetX -= 1;
				buffBarOffsetX.set(Config.buffBarOffsetX);
			}
			break;
		case Keyboard.KEY_D:
			if (Config.buffBarOffsetX + 1 < mc.currentScreen.width / 4 && this.getRight() < mc.currentScreen.width) {
				Config.buffBarOffsetX += 1;
				buffBarOffsetX.set(Config.buffBarOffsetX);
			}
			break;
		default: break;
		}
	}
}

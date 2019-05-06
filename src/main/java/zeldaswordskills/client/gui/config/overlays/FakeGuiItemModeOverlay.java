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
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.GuiItemModeOverlay;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.StringUtils;

public final class FakeGuiItemModeOverlay extends GuiItemModeOverlay implements IOverlayButton {

	private final String CATEGORY = "item mode hud";

	private final Property isItemModeEnabled = Config.config.get(CATEGORY, "Display Item Mode HUD", true);
	private final Property itemModeHAlign = Config.config.get(CATEGORY, "Item Mode HUD X Alignment", "left");
	private final Property itemModeVAlign = Config.config.get(CATEGORY, "Item Mode HUD Y Alignment", "top");
	private final Property itemModeOffsetX = Config.config.get(CATEGORY, "Item Mode HUD X Offset", 0);
	private final Property itemModeOffsetY = Config.config.get(CATEGORY, "Item Mode HUD Y Offset", 0);
	private final ItemStack renderStack;

	public FakeGuiItemModeOverlay(Minecraft mc) {
		super(mc);
		this.renderStack = new ItemStack(Items.arrow, 64);
	}

	@Override
	public boolean shouldRender() {
		return Config.isItemModeEnabled;
	}

	@Override
	public boolean setShouldRender() {
		Config.isItemModeEnabled = !Config.isItemModeEnabled;
		isItemModeEnabled.set(Config.isItemModeEnabled);
		return shouldRender();
	}

	@Override
	protected ItemStack getStackToRender() {
		return this.renderStack;
	}

	@Override
	public String getDisplayName() {
		return StringUtils.translateKey(this.getLangKey() + ".title");
	}

	@Override
	public String getLangKey() {
		return "config.zss.item_mode";
	}

	@Override
	public List<Property> getPanelInfo() {
		List<Property> props = new ArrayList<Property>();
		props.add(itemModeHAlign);
		props.add(itemModeVAlign);
		props.add(itemModeOffsetX);
		props.add(itemModeOffsetY);
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
		Config.itemModeHAlign = HALIGN.fromString(itemModeHAlign.setToDefault().getString());
		Config.itemModeVAlign = VALIGN.fromString(itemModeVAlign.setToDefault().getString());
		Config.itemModeOffsetX = itemModeOffsetX.setToDefault().getInt();
		Config.itemModeOffsetY = itemModeOffsetY.setToDefault().getInt();
	}

	@Override
	public void handleAlignment(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_UP:
			Config.itemModeVAlign = Config.itemModeVAlign.prev();
			Config.itemModeOffsetY = itemModeOffsetY.setValue(0).getInt();
			itemModeVAlign.set(Config.itemModeVAlign.toString());
			break;
		case Keyboard.KEY_DOWN:
			Config.itemModeVAlign = Config.itemModeVAlign.next();
			Config.itemModeOffsetY = itemModeOffsetY.setValue(0).getInt();
			itemModeVAlign.set(Config.itemModeVAlign.toString());
			break;
		case Keyboard.KEY_LEFT:
			Config.itemModeHAlign = Config.itemModeHAlign.prev();
			Config.itemModeOffsetX = itemModeOffsetX.setValue(0).getInt();
			itemModeHAlign.set(Config.itemModeHAlign.toString());
			break;
		case Keyboard.KEY_RIGHT:
			Config.itemModeHAlign = Config.itemModeHAlign.next();
			Config.itemModeOffsetX = itemModeOffsetX.setValue(0).getInt();
			itemModeHAlign.set(Config.itemModeHAlign.toString());
			break;
		}
	}

	@Override
	public void handleOffset(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_W:
			if (-(Config.itemModeOffsetY - 1) < mc.currentScreen.height / 4 && this.getTop() > 0) {
				Config.itemModeOffsetY -= 1;
				itemModeOffsetY.set(Config.itemModeOffsetY);
			}
			break;
		case Keyboard.KEY_S:
			if (Config.itemModeOffsetY + 1 < mc.currentScreen.height / 4 && this.getBottom() < mc.currentScreen.height) {
				Config.itemModeOffsetY += 1;
				itemModeOffsetY.set(Config.itemModeOffsetY);
			}
			break;
		case Keyboard.KEY_A:
			if (-(Config.itemModeOffsetX - 1) < mc.currentScreen.width / 4 && this.getLeft() > 0) {
				Config.itemModeOffsetX -= 1;
				itemModeOffsetX.set(Config.itemModeOffsetX);
			}
			break;
		case Keyboard.KEY_D:
			if (Config.itemModeOffsetX + 1 < mc.currentScreen.width / 4 && this.getRight() < mc.currentScreen.width) {
				Config.itemModeOffsetX += 1;
				itemModeOffsetX.set(Config.itemModeOffsetX);
			}
			break;
		}
	}
}
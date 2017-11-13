package zeldaswordskills.client.gui.config.overlays;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.GuiEndingBlowOverlay;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.ref.Config;

public final class FakeGuiEndingBlowOverlay extends GuiEndingBlowOverlay implements IOverlayButton {

	private final String CATEGORY = "ending blow hud";

	private final Property endingBlowHudHAlign = Config.config.get(CATEGORY, "Ending Blow HUD X Alignment", "center");
	private final Property endingBlowHudVAlign = Config.config.get(CATEGORY, "Ending Blow HUD Y Alignment", "top");
	private final Property endingBlowHudOffsetX = Config.config.get(CATEGORY, "Ending Blow HUD X Offset", 0);
	private final Property endingBlowHudOffsetY = Config.config.get(CATEGORY, "Ending Blow HUD Y Offset", 30);

	public FakeGuiEndingBlowOverlay(Minecraft mc) {
		super(mc);
		this.displayStartTime = Minecraft.getSystemTime();
	}

	@Override
	public boolean shouldRender() {
		return Config.isEndingBlowHudEnabled;
	}

	@Override
	public Map<String, String> getPanelInfo() {
		Map<String, String> info = new LinkedHashMap<String, String>();
		info.put("Ending Blow HAlign", "Left/Right Arrow Keys \u2190 \u2192");
		info.put("Ending Blow VAlign", "Up/Down Arrow Keys \u2191 \u2193");
		info.put("X Axis Offset", "A and D Keys");
		info.put("Y Axis Offset", "W and S Keys");
		return info;
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
		Config.endingBlowHudHAlign = HALIGN.fromString(endingBlowHudHAlign.setToDefault().getString());
		Config.endingBlowHudVAlign = VALIGN.fromString(endingBlowHudVAlign.setToDefault().getString());
		Config.endingBlowHudOffsetX = endingBlowHudOffsetX.setToDefault().getInt();
		Config.endingBlowHudOffsetY = endingBlowHudOffsetY.setToDefault().getInt();
	}

	@Override
	public void handleAlignment(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_UP:
			Config.endingBlowHudVAlign = Config.endingBlowHudVAlign.prev();
			Config.endingBlowHudOffsetY = endingBlowHudOffsetY.setValue(0).getInt();
			endingBlowHudVAlign.set(Config.endingBlowHudVAlign.toString());
			break;
		case Keyboard.KEY_DOWN:
			Config.endingBlowHudVAlign = Config.endingBlowHudVAlign.next();
			Config.endingBlowHudOffsetY = endingBlowHudOffsetY.setValue(0).getInt();
			endingBlowHudVAlign.set(Config.endingBlowHudVAlign.toString());
			break;
		case Keyboard.KEY_LEFT:
			Config.endingBlowHudHAlign = Config.endingBlowHudHAlign.prev();
			Config.endingBlowHudOffsetX = endingBlowHudOffsetX.setValue(0).getInt();
			endingBlowHudHAlign.set(Config.endingBlowHudHAlign.toString());
			break;
		case Keyboard.KEY_RIGHT:
			Config.endingBlowHudHAlign = Config.endingBlowHudHAlign.next();
			Config.endingBlowHudOffsetX = endingBlowHudOffsetX.setValue(0).getInt();
			endingBlowHudHAlign.set(Config.endingBlowHudHAlign.toString());
			break;
		}
	}

	@Override
	public void handleOffset(int keyCode) {
		switch (keyCode) {
		case Keyboard.KEY_W:
			if (-(Config.endingBlowHudOffsetY - 1) < mc.currentScreen.height / 4 && this.getTop() > 0) {
				Config.endingBlowHudOffsetY -= 1;
				endingBlowHudOffsetY.set(Config.endingBlowHudOffsetY);
			}
			break;
		case Keyboard.KEY_S:
			if (Config.endingBlowHudOffsetY + 1 < mc.currentScreen.height / 4 && this.getBottom() < mc.currentScreen.height) {
				Config.endingBlowHudOffsetY += 1;
				endingBlowHudOffsetY.set(Config.endingBlowHudOffsetY);
			}
			break;
		case Keyboard.KEY_A:
			if (-(Config.endingBlowHudOffsetX - 1) < mc.currentScreen.width / 4 && this.getLeft() > 0) {
				Config.endingBlowHudOffsetX -= 1;
				endingBlowHudOffsetX.set(Config.endingBlowHudOffsetX);
			}
			break;
		case Keyboard.KEY_D:
			if (Config.endingBlowHudOffsetX + 1 < mc.currentScreen.width / 4 && this.getRight() < mc.currentScreen.width) {
				Config.endingBlowHudOffsetX += 1;
				endingBlowHudOffsetX.set(Config.endingBlowHudOffsetX);
			}
			break;
		}
	}
}
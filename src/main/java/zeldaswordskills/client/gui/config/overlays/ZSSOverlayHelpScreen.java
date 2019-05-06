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

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.util.StringUtils;

public class ZSSOverlayHelpScreen extends GuiScreen {

	private final GuiZSSFakeScreen parentScreen;
	
	private final List<String> propNames = new ArrayList<String>();
	private final List<String> controlEntries = new ArrayList<String>();
	
	private static final int SLOT_SIZE = 14;

	public ZSSOverlayHelpScreen(GuiZSSFakeScreen parent, IOverlayButton overlay) {
		this.parentScreen = parent;
		this.mc = parent.mc;
		List<Property> props = overlay.getPanelInfo();
		for (Property prop : props) {
			String name = StringUtils.translateKey(prop.getLanguageKey());
			String controls = StringUtils.translateKey(prop.getLanguageKey() + ".controls");
			this.propNames.add(name);
			this.controlEntries.add(controls);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.parentScreen.drawScreen(mouseX, mouseY, partialTicks);// Otherwise the background comes out solid black
		Gui.drawRect(0, 0,this.width, this.height, 0x80000000);
		GlStateManager.enableAlpha();
		int startY = this.height / 2 - this.getSize() * SLOT_SIZE / 2;
		for(int i = 0; i < this.getSize(); i++) {
			this.drawSlot(i, this.width / 4, startY + (i * SLOT_SIZE));
		}
	}

	private int getSize() {
		return this.propNames.size();
	}

	private void drawSlot(int slotIndex, int x, int y) {
		String name = this.propNames.get(slotIndex);
		String control = this.controlEntries.get(slotIndex);
		this.drawCenteredString(fontRendererObj, name, x, y, 0xFFFFFFFF);
		this.drawCenteredString(fontRendererObj, control, x + this.width / 2, y, 0xFFFFFFFF);
	}
	
	@Override
	protected void keyTyped(char charTyped, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_H) {
			mc.displayGuiScreen(this.parentScreen);
		}
	}
}

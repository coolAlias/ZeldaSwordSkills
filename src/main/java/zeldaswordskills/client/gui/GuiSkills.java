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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;

import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.inventory.ContainerSkills;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.skills.SkillBase;

public class GuiSkills extends GuiContainer
{
	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_skills.png");
	/** Number of lines of text that can be drawn on the screen */
	private static final int MAX_LINES = 11;
	/** Currently selected skill for displaying a description */
	private SkillBase currentSkill = null;
	/** The description to display */
	private final List<String> desc = new ArrayList<String>(50);
	/** Current y position at which to draw text; coordinates set as though gui was entire screen */
	private int textY;
	/** Current position of the scroll bar, as a float (0 is top, 1 is bottom) */
	private float scrollY;
	/** True if the scroll bar is being dragged */
	private boolean isScrolling;
	/** Whether left mouse button is held down */
	private boolean wasClicking;
	/** The number of lines in the current skill's description */
	private int numLines;
	/** Flag for whether current font is in unicode, to prevent overriding the font */
	private boolean isUnicode;
	/** Tracks current mouseX, used for rendering player model rotation. Defined as float, passed as int */
	private float xSize_lo;
	/** Tracks current mouseY, used for rendering player model rotation. Defined as float, passed as int. */
	private float ySize_lo;

	public GuiSkills(EntityPlayer player) {
		super(new ContainerSkills(player));
		this.xSize = 281;
		this.ySize = 180;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		xSize_lo = mouseX;
		ySize_lo = mouseY;
		boolean flag = Mouse.isButtonDown(0);
		int x1 = guiLeft + 259;
		int y1 = guiTop + 61;
		if (!wasClicking && flag && isMouseInRegion(mouseX, mouseY, x1, x1 + 3, y1, y1 + 88)) {
			isScrolling = needsScrollBar();
		}
		if (!flag) {
			isScrolling = false;
		}
		wasClicking = flag;
		if (isScrolling) {
			scrollY = ((float)(mouseY - y1) - 3.0F) / 81.0F;
			clampScrollBar();
		}
	}

	/** Returns true if the mouse is within the real-screen coordinates specified */
	private boolean isMouseInRegion(int mouseX, int mouseY, int x1, int x2, int y1, int y2) {
		return (mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (needsScrollBar()) {
			GlStateManager.pushAttrib();
			GlStateManager.enableBlend();
			mc.renderEngine.bindTexture(texture);
			RenderHelperQ.drawTexturedRect(259, 55, 282, 0, 3, 5, 285, 180);
			RenderHelperQ.drawTexturedRect(259, 150, 282, 5, 3, 5, 285, 180);
			RenderHelperQ.drawTexturedRect(260, 61, 283, 17, 1, 88, 285, 180);
			RenderHelperQ.drawTexturedRect(259, 61 + (int)(scrollY * 81), 282, 10, 3, 7, 285, 180);
			GlStateManager.popAttrib();
		}
		String s = (currentSkill != null ? currentSkill.getDisplayName().toUpperCase() : StatCollector.translateToLocal("skill.zss.gui.description"));
		isUnicode = fontRendererObj.getUnicodeFlag();
		fontRendererObj.setUnicodeFlag(true);
		fontRendererObj.drawString(s, 158, 38, 4210752);
		if (currentSkill != null) {
			s = currentSkill.getLevelDisplay(false);
			fontRendererObj.drawString(s, 262 - fontRendererObj.getStringWidth(s), 38, 4210752);
		}
		refreshDescription();
		textY = 38 + (fontRendererObj.FONT_HEIGHT * 2);
		int start = (needsScrollBar() ? (int)(scrollY * (numLines - MAX_LINES)) : 0);
		for (int i = start; i < desc.size() && i < (MAX_LINES + start); ++i) {
			fontRendererObj.drawString(desc.get(i), 158, textY, 4210752);
			textY += fontRendererObj.FONT_HEIGHT;
		}
		fontRendererObj.setUnicodeFlag(isUnicode);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		RenderHelperQ.drawTexturedRect(texture, guiLeft, guiTop, 0, 0, xSize, ySize, 284, 180);
		GuiInventory.drawEntityOnScreen(guiLeft + 73, guiTop + 105, 30, guiLeft + 73 - xSize_lo, guiTop + 55 - ySize_lo, mc.thePlayer);
	}

	/**
	 * Refreshes the description either when empty or the skill has changed
	 */
	private void refreshDescription() {
		if (!desc.isEmpty()) {
			return;
		}
		if (currentSkill != null) {
			desc.add(StatCollector.translateToLocal("skill.zss.gui.summary"));
			currentSkill.addInformation(desc, mc.thePlayer);
			desc.add("");
			desc.add(StatCollector.translateToLocal("skill.zss.gui.activation"));
			desc.addAll(fontRendererObj.listFormattedStringToWidth(currentSkill.getActivationDisplay(), 101));
			desc.add("");
		}
		desc.add(StatCollector.translateToLocal("skill.zss.gui.description"));
		String[] temp = (currentSkill != null ? currentSkill.getFullDescription().split("\\\\n") : StatCollector.translateToLocal("skill.zss.gui.explanation").split("\\\\n"));
		for (String s : temp) {
			desc.addAll(fontRendererObj.listFormattedStringToWidth(s, 101));
			desc.add("");
		}
		numLines = desc.size();
	}

	private boolean needsScrollBar() {
		return numLines > MAX_LINES;
	}

	/**
	 * Clamps scroll bar position value between 0.0F and 1.0F
	 */
	private void clampScrollBar() {
		if (scrollY < 0.0F) {
			scrollY = 0.0F;
		}
		if (scrollY > 1.0F) {
			scrollY = 1.0F;
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		if (needsScrollBar()) {
			int i = Mouse.getEventDWheel();
			if (i != 0) {
				if (i < 0) {
					scrollY += 1.0F / (float)(numLines - MAX_LINES);
				} else {
					scrollY -= 1.0F / (float)(numLines - MAX_LINES);
				}
			}
			clampScrollBar();
		}
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int lastButtonClicked, long timeSinceMouseClick) {
		// do nothing
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int which) {
		Slot slot = this.getSlotAtPosition(mouseX, mouseY);
		if (slot != null && slot.getStack() != null) {
			int id = (slot.getStack().getItemDamage() % SkillBase.getNumSkills());
			if (currentSkill == null || currentSkill.getId() != id) {
				scrollY = 0.0F;
				// clear the current description so it refreshes next time the screen draws
				desc.clear();
			}
			currentSkill = ZSSPlayerSkills.get(mc.thePlayer).getPlayerSkill((byte) id);
		}
	}

	/**
	 * Returns the slot at the given coordinates or null if there is none. (copied from GuiContainer)
	 */
	private Slot getSlotAtPosition(int x, int y) {
		for (int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = (Slot) inventorySlots.inventorySlots.get(k);
			if (isPointInRegion(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, x, y)) {
				return slot;
			}
		}
		return null;
	}

	@Override
	protected void keyTyped(char c, int key) {
		if (key == 1 || key == mc.gameSettings.keyBindInventory.getKeyCode() || key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_SKILLS_GUI].getKeyCode()) {
			mc.thePlayer.closeScreen();
		}
	}
}

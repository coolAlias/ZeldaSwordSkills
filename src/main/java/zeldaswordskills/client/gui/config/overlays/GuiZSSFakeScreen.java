/**
    Copyright (C) <2017> <coolAlias>

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import zeldaswordskills.client.gui.IGuiOverlay;
import zeldaswordskills.client.gui.config.GuiConfigZeldaSwordSkills;
import zeldaswordskills.ref.ModInfo;

/**
 * This screen provides dummy versions of Zelda Sword Skills' in-game overlays that can be edited with this
 * interface. The keys for controlling the interfaces are relatively the same; WASD controls integer offset
 * for any overlay, and the arrow keys control {@code HALIGN} and {@code VALIGN}. Press 'H' when an overlay
 * is highlighted for specific help.
 * 
 * @author Spitfyre03
 */
public final class GuiZSSFakeScreen extends GuiScreen {

	protected int previousWidth = 0;

	/** The background image for this screen */
	public static final ResourceLocation ZSS_FAKE_GUI = new ResourceLocation(ModInfo.ID, "textures/gui/fake_gui.png");

	/** The parent screen. The only time this Screen is constructed is from {@link GuiConfigZeldaSwordSkills} or {@link GuiHUDPanel} */
	protected GuiConfigZeldaSwordSkills parent;

	protected final List<IOverlayButton> overlays = new ArrayList<IOverlayButton>();

	private IOverlayButton activeElement;
	private OverlayToggleList toggleList;

	private long startTime = 0;
	private final int DISPLAY_TIME = 3000;

	public GuiZSSFakeScreen(GuiConfigZeldaSwordSkills parent) {
		this.parent = parent;
		this.mc = Minecraft.getMinecraft();

		// TODO perhaps draw a hotbar and experience bar above all these to prevent them from rendering in that space
		FakeGuiMagicMeter magicMeterDummy = new FakeGuiMagicMeter(mc);
		overlays.add(magicMeterDummy);
		overlays.add(new FakeGuiMagicMeterText(mc, magicMeterDummy));
		overlays.add(new FakeGuiBuffBar(mc));
		overlays.add(new FakeGuiItemModeOverlay(mc));
		overlays.add(new FakeComboOverlay(mc));
		overlays.add(new FakeGuiEndingBlowOverlay(mc));

		// Set up the OverlayToggleList
		int maxWidth = 0;

		for (IOverlayButton b : this.overlays) {
			String name = b.getName();
			int entryWidth = this.mc.fontRendererObj.getStringWidth(name);
			if (entryWidth > maxWidth) {
				maxWidth = entryWidth;
			}
		}
		// Pad 5 on both sides of the button width to give the longest entry a little room to write
		toggleList = new OverlayToggleList(this, maxWidth + 10);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		// The done button
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 25, I18n.format("gui.done")));
		this.startTime = Minecraft.getSystemTime();// For the Help pop-up
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Adjusting the Minecraft screen size (e.g. fullscreen to minimized, or by click-and-drag on
		// the borders creates a render issue with the background and buttons. This fixes that issue
		if (previousWidth != width) {
			this.initGui();
			previousWidth = width;
		}

		// Draws the background
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(ZSS_FAKE_GUI);
		GuiScreen.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, mc.currentScreen.width, mc.currentScreen.height, mc.currentScreen.width, mc.currentScreen.height);

		// Render the overlays
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		ScaledResolution res = new ScaledResolution(this.mc);
		List<IGuiOverlay> rendered = new ArrayList<IGuiOverlay>();
		for (IOverlayButton overlay : this.overlays) {
			if (overlay.shouldRender() && this.renderElement(overlay, res, rendered, overlay.equals(this.activeElement))) {
				rendered.add(overlay);
			}
		}
		rendered.clear();

		// Draws the Done button and Help hint
		if (Minecraft.getSystemTime() - startTime < DISPLAY_TIME && this.mc.currentScreen.equals(this)) {
			String help = StatCollector.translateToLocal("config.zss.overlays.help");
			int width = mc.fontRendererObj.getStringWidth(help);
			int textPadding = 5;

			this.drawRect(this.width / 2 - (width / 2 + textPadding), this.height / 2 - (mc.fontRendererObj.FONT_HEIGHT / 2 + textPadding), this.width / 2 + (width / 2 + textPadding), this.height / 2 + (mc.fontRendererObj.FONT_HEIGHT / 2 + textPadding), 0x80000000);
			this.drawCenteredString(mc.fontRendererObj, help, this.width / 2, this.height / 2 - mc.fontRendererObj.FONT_HEIGHT / 2, 0xFFFFFFFF);
		}
		GlStateManager.disableAlpha();
		GlStateManager.disableBlend();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected boolean renderElement(IOverlayButton overlay, ScaledResolution res, List<IGuiOverlay> rendered, boolean isActive) {
		if (overlay.renderOverlay(res, rendered)) {
			if (isActive) {
				if (!overlay.renderOverlayBorder()) {
					this.renderOverlayBorder(overlay);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Renders the default rectangular border around the overlay
	 */
	protected void renderOverlayBorder(IGuiOverlay overlay) {
		this.drawHorizontalLine(overlay.getLeft() - 1, overlay.getRight(), overlay.getTop() - 1, 0xFF000000);
		this.drawHorizontalLine(overlay.getLeft() - 1, overlay.getRight(), overlay.getBottom(), 0xFF000000);
		this.drawVerticalLine(overlay.getLeft() - 1, overlay.getTop() - 1, overlay.getBottom(), 0xFF000000);
		this.drawVerticalLine(overlay.getRight(), overlay.getTop() - 1, overlay.getBottom(), 0xFF000000);
	}

	/**
	 * Fired when a key is typed. If an overlay element is in focus, this method passes through to that overlay so
	 * that key press behaviors are handled internally. Also passes up to {@code GuiScreen} so that other methods, such as
	 * {@link #actionPerformed(GuiButton) actionPerformed} get called
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_Z) {
			this.mc.displayGuiScreen(this.toggleList);
		}
		else if (keyCode == Keyboard.KEY_ESCAPE) {
			this.mc.displayGuiScreen(this.parent);
		}
		else if (this.activeElement != null) {
			if (keyCode == Keyboard.KEY_H) {
				mc.displayGuiScreen(new ZSSOverlayHelpScreen(this, activeElement));
			}
			activeElement.adjustOverlay(typedChar, keyCode);
		}
		else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/** Used to designate the overlay focus of the {@code GuiZSSFakeScreen} */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		for (IOverlayButton o : overlays) {
			if (isWithinOverlay(mouseX, mouseY, o)) {
				this.activeElement = o;
				return;
			} else {
				this.activeElement = null;
			}
		}
		this.toggleList.handleMouseInput();
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/** Called when the done button is pressed. Posts a {@link OnConfigChangedEvent} */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 1) {
			ConfigChangedEvent event = new OnConfigChangedEvent(ModInfo.ID, ModInfo.CONFIG_PATH, mc.theWorld != null, false);
			MinecraftForge.EVENT_BUS.post(event);
			this.mc.displayGuiScreen(parent);
		}
		super.actionPerformed(button);
	}

	private boolean isWithinOverlay(int mouseX, int mouseY, IOverlayButton overlay) {
		return mouseX >= overlay.getLeft() && mouseX <= overlay.getRight() && mouseY >= overlay.getTop() && mouseY <= overlay.getBottom();
	}
}

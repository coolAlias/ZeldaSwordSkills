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

package zeldaswordskills.client.gui.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import zeldaswordskills.client.gui.ComboOverlay;
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.client.gui.GuiEndingBlowOverlay;
import zeldaswordskills.client.gui.GuiItemModeOverlay;
import zeldaswordskills.client.gui.GuiMagicMeter;
import zeldaswordskills.client.gui.GuiMagicMeterText;
import zeldaswordskills.client.gui.IGuiOverlay;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.skills.Combo;
import zeldaswordskills.skills.SkillBase;

/**
 * This screen provides dummy versions of Zelda Sword Skills' in-game overlays that can be edited with this
 * interface. The keys for controlling the interfaces are relatively the same; WASD controls integer offset
 * for any overlay, and the arrow keys control {@code HALIGN} and {@code VALIGN}.
 * 
 * @author Spitfyre03
 */
public final class GuiZSSFakeScreen extends GuiScreen {

	protected int previousWidth = 0;

	/** The background image for this screen */
	public static final ResourceLocation ZSS_FAKE_GUI = new ResourceLocation(ModInfo.ID, "textures/gui/fake_gui.png");

	/** The parent screen. The only time this Screen is constructed is from {@link GuiConfigZeldaSwordSkills} or {@link GuiHUDPanel} */
	protected GuiConfigZeldaSwordSkills parent;

	private final List<IOverlayButton> overlays = new ArrayList<IOverlayButton>();

	private IOverlayButton activeElement;

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
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();

		// The done button
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height - 25, I18n.format("gui.done")));
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
		ScaledResolution res = new ScaledResolution(this.mc);
		List<IGuiOverlay> rendered = new ArrayList<IGuiOverlay>();
		for (IOverlayButton overlay : this.overlays) {
			if (overlay.shouldRender() && this.renderElement(overlay, res, rendered, overlay.equals(this.activeElement))) {
				rendered.add(overlay);
			}
		}
		rendered.clear();

		// Draws the Done button
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected boolean renderElement(IOverlayButton overlay, ScaledResolution res, List<IGuiOverlay> rendered, boolean isActive) {
		if (overlay.renderOverlay(res, rendered)) {
			if (isActive) {
				overlay.renderInfoPanel();
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
		if (keyCode == Keyboard.KEY_Z) {}
		// TODO open panel
		if (this.activeElement != null) {
			activeElement.adjustOverlay(typedChar, keyCode);
		}
		super.keyTyped(typedChar, keyCode);
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

	private final class FakeGuiMagicMeter extends GuiMagicMeter implements IOverlayButton {

		private final String CATEGORY = "magic meter";

		private final Property magicMeterHAlign = Config.config.get(CATEGORY, "Magic Meter Horizontal Alignment", "center");
		private final Property magicMeterVAlign = Config.config.get(CATEGORY, "Magic Meter Vertical Alignment", "bottom");
		private final Property magicMeterOffsetX = Config.config.get(CATEGORY, "Magic Meter Horizontal Offset", 47);
		private final Property magicMeterOffsetY = Config.config.get(CATEGORY, "Magic Meter Vertical Offset", -40);
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
		public void renderInfoPanel() {
			// TODO Unsupported
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

	private final class FakeGuiMagicMeterText extends GuiMagicMeterText implements IOverlayButton {

		private final String CATEGORY = "magic meter";

		private final Property magicMeterHAlign = Config.config.get(CATEGORY, "Magic Meter Horizontal Alignment", "center");
		private final Property magicMeterVAlign = Config.config.get(CATEGORY, "Magic Meter Vertical Alignment", "bottom");
		private final Property magicMeterOffsetX = Config.config.get(CATEGORY, "Magic Meter Horizontal Offset", 47);
		private final Property magicMeterOffsetY = Config.config.get(CATEGORY, "Magic Meter Vertical Offset", -40);

		public FakeGuiMagicMeterText(Minecraft mc, FakeGuiMagicMeter meter) {
			super(mc, meter);
		}

		@Override
		public boolean shouldRender() {
			return Config.isMagicMeterTextEnabled;
		}

		@Override
		protected void setup(ScaledResolution resolution) {
			int current = MathHelper.floor_float(this.meter.getNumIncrements() <= 1 ? 1 : this.meter.getNumIncrements() >= 10 ? 10 : ((this.meter.getNumIncrements() - 1) / this.meter.getNumIncrements())) * Config.getMaxMagicPoints();
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
		public void renderInfoPanel() {
			// TODO Unsupported
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

	private final class FakeGuiBuffBar extends GuiBuffBar implements IOverlayButton {

		private final GuiBuffBar bar;

		private final String CATEGORY = "buff bar hud";

		private final Property buffBarMaxIcons = Config.config.get(CATEGORY, "Number of Icons to Display on Buff", 5);
		private final Property isBuffBarHorizontal = Config.config.get(CATEGORY, "Display Buff Bar Horizontally", true);
		private final Property buffBarHAlign = Config.config.get(CATEGORY, "Buff HUD X-axis Alignment", "right");
		private final Property buffBarVAlign = Config.config.get(CATEGORY, "Buff HUD Y-axis Alignment", "top");
		private final Property buffBarOffsetX = Config.config.get(CATEGORY, "Buff HUD X Offset", 0);
		private final Property buffBarOffsetY = Config.config.get(CATEGORY, "Buff HUD Y Offset", 0);

		public FakeGuiBuffBar(Minecraft mc) {
			super(mc);
			this.bar = new GuiBuffBar(mc);
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
		public void renderInfoPanel() {
			// TODO Unsupported
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

	private final class FakeGuiItemModeOverlay extends GuiItemModeOverlay implements IOverlayButton {

		private final String CATEGORY = "item mode hud";

		private final Property itemModeHAlign = Config.config.get(CATEGORY, "Item Mode HUD X-axis Alignment", "left");
		private final Property itemModeVAlign = Config.config.get(CATEGORY, "Item Mode HUD Y-axis Alignment", "top");
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
		protected ItemStack getStackToRender() {
			return this.renderStack;
		}

		@Override
		public void renderInfoPanel() {
			// TODO Unsupported
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

	private final class FakeComboOverlay extends ComboOverlay implements IOverlayButton {

		private final String CATEGORY = "combo hud";

		private final Property hitsToDisplay = Config.config.get(CATEGORY, "Hits to Display in Combo HUD", 3);
		private final Property comboHudHAlign = Config.config.get(CATEGORY, "Combo HUD X-axis Alignment", "left");
		private final Property comboHudVAlign = Config.config.get(CATEGORY, "Combo HUD Y-axis Alignment", "top");
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
		public void renderInfoPanel() {
			// TODO Unsupported
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
					Config.hitsToDisplay -= 1;
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

	private final class FakeGuiEndingBlowOverlay extends GuiEndingBlowOverlay implements IOverlayButton {

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
		public void renderInfoPanel() {
			// TODO Unsupported
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
}

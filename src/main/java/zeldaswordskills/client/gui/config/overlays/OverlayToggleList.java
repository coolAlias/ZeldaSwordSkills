package zeldaswordskills.client.gui.config.overlays;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiUtils;

public class OverlayToggleList extends GuiScreen {

	/** The owning screen of this slot*/
	private final GuiZSSFakeScreen parent;
	/**The list of overlays to include in this ToggleList*/
	protected final List<IOverlayButton> overlays = new ArrayList<IOverlayButton>();

	/**The width of the longest overlay title*/
	private final int MAX_ENTRY_WIDTH;
	/**The height of each slot. Text height is 9, and padding for entries is 3, top and bottom*/
	private final int SLOT_HEIGHT = 15;
	private final int SLOT_PADDING = 3;

	public OverlayToggleList(GuiZSSFakeScreen parentScreen, int slotWidth) {
		this.parent = parentScreen;
		MAX_ENTRY_WIDTH = slotWidth;
		this.overlays.addAll(parentScreen.overlays);
	}

	@Override
	public void initGui() {
		int x = (this.width / 2) - (MAX_ENTRY_WIDTH / 2);
		int currentHeight = (this.height / 2) - ((this.overlays.size() * SLOT_HEIGHT / 2) + ((this.overlays.size() - 1) * SLOT_PADDING));// size() - 1 to cut off the padding that would come after the last entry
		for (int i = 0; i < this.overlays.size(); i++) {
			IOverlayButton overlay = this.overlays.get(i);
			this.buttonList.add(new OverlayToggleButton(i, x, currentHeight, MAX_ENTRY_WIDTH, SLOT_HEIGHT, overlay));
			currentHeight += SLOT_HEIGHT + SLOT_PADDING;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.parent.drawScreen(mouseX, mouseY, partialTicks);
		Gui.drawRect(0, 0, this.width, this.height, 0xA0000000);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	protected void drawSlot(int slotIndex, int x, int y) {
		String overlayTitle = overlays.get(slotIndex).getDisplayName();
		this.mc.currentScreen.drawCenteredString(fontRendererObj, overlayTitle, x, y, 0xFFFFFFFF);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_Z || keyCode == Keyboard.KEY_ESCAPE) {
			this.mc.displayGuiScreen(this.parent);
		}
	}

	private class OverlayToggleButton extends GuiButtonExt {

		private final IOverlayButton overlay;

		public OverlayToggleButton(int buttonId, int x, int y, int widthIn, int heightIn, IOverlayButton overlay) {
			super(buttonId, x, y, widthIn, heightIn, overlay.getDisplayName());
			this.overlay = overlay;
			this.packedFGColour = overlay.shouldRender() ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
		}

		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				this.packedFGColour = this.overlay.setShouldRender() ? GuiUtils.getColorCode('2', true) : GuiUtils.getColorCode('4', true);
				return true;
			}
			else {
				return false;
			}
		}
	}
}
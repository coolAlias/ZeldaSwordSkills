package zeldaswordskills.client.gui.config.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

public class ZSSOverlayHelpScreen extends GuiScreen {

	private final GuiZSSFakeScreen parentScreen;
	
	private final List<String> controlEntries = new ArrayList<String>();
	private final List<String> keyEntries = new ArrayList<String>();
	
	private static final int SLOT_SIZE = 14;

	public ZSSOverlayHelpScreen(GuiZSSFakeScreen parent, IOverlayButton overlay) {
		this.parentScreen = parent;
		this.mc = parent.mc;
		Map<String, String> controlsMap = overlay.getPanelInfo();
		for(String key : controlsMap.keySet()){
			this.controlEntries.add(key);
			this.keyEntries.add(controlsMap.get(key));
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		this.parentScreen.drawScreen(mouseX, mouseY, partialTicks);// Otherwise the background comes out solid black
		this.drawRect(0, 0,this.width, this.height, 0x80000000);
		GlStateManager.enableAlpha();
		int startY = this.height / 2 - this.getSize() * 7;// * 14 (text height is 9 and padding is 5) / 2 (to center on the y-axis)
		for(int i = 0; i < this.getSize(); i++){
			this.drawSlot(i, this.width / 4, startY + (i * SLOT_SIZE));
		}
	}

	private int getSize() {
		return this.controlEntries.size();
	}

	private void drawSlot(int slotIndex, int x, int y) {
		String control = this.controlEntries.get(slotIndex);
		String key = this.keyEntries.get(slotIndex);
		this.drawCenteredString(fontRendererObj, control, x, y, 0xFFFFFFFF);
		int keyLength = key.length();
		this.drawCenteredString(fontRendererObj, key, x + this.width / 2, y, 0xFFFFFFFF);
	}
	
	@Override
	protected void keyTyped(char charTyped, int keyCode){
		if(keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_H){
			mc.displayGuiScreen(this.parentScreen);
		}
	}
}

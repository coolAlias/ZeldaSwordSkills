package zeldaswordskills.client.gui.config.overlays;

import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.IGuiOverlay;

/**
 * This interface provides methods used by dummy overlays to be rendered to Zelda Sword Skills' HUD Editing
 * interface. All classes that implement this interface should ideally declare local Property variables that
 * are refreshed in {@link #adjustOverlay(char, int) adjustOverlay}
 */
public interface IOverlayButton extends IGuiOverlay {

	/** This method is used rendering the panel containing the information specific to an overlay */
	void renderInfoPanel();

	/**
	 * By default, a rectangular border will be rendered; if a more specialized
	 * border is required, render it in this method.
	 * @return true if the overlay doesn't need the default border
	 */
	boolean renderOverlayBorder();

	/**
	 * Defines the behaviors of pressed keys for the overlay, when in focus. In this method, overlays should set their
	 * configurations values based on change, and set the corresponding {@link Property} values for the configuration file
	 */
	void adjustOverlay(char typedChar, int keyCode);

	void resetOverlay();

	void handleAlignment(int keyCode);

	void handleOffset(int keyCode);
}

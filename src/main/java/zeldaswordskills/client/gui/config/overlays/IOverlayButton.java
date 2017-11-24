package zeldaswordskills.client.gui.config.overlays;

import java.util.List;

import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.IGuiOverlay;

/**
 * This interface provides methods used by dummy overlays to be rendered to Zelda Sword Skills' HUD Editing
 * interface. All classes that implement this interface should ideally declare local Property variables that
 * are refreshed in {@link #adjustOverlay(char, int) adjustOverlay}
 */
public interface IOverlayButton extends IGuiOverlay {

	/**
	 * @return the translated title for an overlay, or the English default if no translation is present
	 */
	String getDisplayName();

	/**
	 * @return the language key used by this overlay
	 */
	String getLangKey();

	/**
	 * Provides the list of Properties used by this overlay so that {@code ZSSOverlayHelpScreen} can provide translated
	 * names and controls for the overlay's settings
	 * 
	 */
	List<Property> getPanelInfo();

	/**
	 * @return whether or not the overlay is currently enabled after being set
	 */
	boolean setShouldRender();

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

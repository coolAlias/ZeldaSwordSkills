package zeldaswordskills.client.gui.config;

import java.util.List;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.common.config.Property;
import zeldaswordskills.client.gui.IGuiOverlay;

/**
 * This interface provides methods used by dummy overlays to be rendered to Zelda Sword Skills' HUD Editing
 * interface. All classes that implement this interface should ideally declare local Property variables that
 * are refreshed in {@link #adjustOverlay(char, int) adjustOverlay}
 */
public interface IOverlayButton extends IGuiOverlay {

	/** This is the encompassing method of rendering the overlay and all of its accessories */
	boolean renderElement(ScaledResolution res, List<IGuiOverlay> rendered, boolean isActive);

	/** This method is used rendering the panel containing the information specific to an overlay */
	void renderInfoPanel();

	/**
	 * Renders the black-line border around an overlay. All overlays should essentially have the same code that draws
	 * black lines from corner to corner, unless an overlay is irregularly shaped and it is imperative that negative
	 * space be ignored
	 */
	void renderOverlayBorder();

	/**
	 * Defines the behaviors of pressed keys for the overlay, when in focus. In this method, overlays should set their
	 * configurations values based on change, and set the corresponding {@link Property} values for the configuration file
	 */
	void adjustOverlay(char typedChar, int keyCode);

	void resetOverlay();

	void handleAlignment(int keyCode);

	void handleOffset(int keyCode);
}

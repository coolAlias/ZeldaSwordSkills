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

package zeldaswordskills.client.gui;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

@SideOnly(Side.CLIENT)
public abstract class AbstractGuiOverlay extends Gui implements IGuiOverlay
{
	/** Default padding between elements */
	protected static final int DEFAULT_PADDING = 2;

	protected final Minecraft mc;

	/** Max height and width for combining elements; based on ScaledResolution */
	protected int maxH, maxW;

	/** Left-most x, top-most y, width and height are usually re-set each time the overlay renders */
	protected int x, y, width, height;

	public AbstractGuiOverlay(Minecraft mc) {
		this.mc = mc;
	}

	@Override
	public int getLeft() {
		return this.x;
	}

	@Override
	public int getRight() {
		return this.x + this.getWidth();
	}

	@Override
	public int getTop() {
		return this.y;
	}

	@Override
	public int getBottom() {
		return this.y + this.getHeight();
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public HALIGN getHorizontalAlignment() {
		return HALIGN.LEFT;
	}

	@Override
	public VALIGN getVerticalAlignment() {
		return VALIGN.TOP;
	}

	@Override
	public boolean allowMergeX(boolean rendered) {
		return true;
	}

	/**
	 * Sets up and dynamically adjusts element before calling {@link #render(ScaledResolution)}
	 */
	@Override
	public boolean renderOverlay(ScaledResolution resolution, List<IGuiOverlay> overlays) {
		this.maxW = resolution.getScaledWidth() / 3;
		this.maxH = resolution.getScaledHeight() / 2;
		this.setup(resolution);
		for (IGuiOverlay overlay : overlays) {
			if (this.intersectsWith(overlay) && !this.coalesce(overlay, resolution)) {
				return false; // failed to render
			}
		}
		this.render(resolution);
		return true;
	}

	/**
	 * Called prior to {@link IGuiOverlay#renderOverlay} and after {@link IGuiOverlay#shouldRender()}.
	 * Set this element's x, y, width and height so they can be properly adjusted later.
	 */
	protected abstract void setup(ScaledResolution resolution);

	/**
	 * Do the actual rendering for this element
	 */
	protected abstract void render(ScaledResolution resolution);

	/**
	 * Returns {@link HALIGN#getOffset(int)}
	 */
	protected int getOffsetX(int offset) {
		return this.getHorizontalAlignment().getOffset(offset);
	}

	/**
	 * Returns {@link VALIGN#getOffset(int)}
	 */
	protected int getOffsetY(int offset) {
		return this.getVerticalAlignment().getOffset(offset);
	}

	/**
	 * Sets this element's X position based on its width, alignment and the provided offset
	 * @param offset Positive values move the element right, negative values move it left
	 */
	protected void setPosX(ScaledResolution resolution, int offset) {
		switch (this.getHorizontalAlignment()) {
		case LEFT:
			this.x = offset;
			break;
		case CENTER:
			this.x = ((resolution.getScaledWidth() / 2) - (this.getWidth() / 2)) + offset; 
			break;
		case RIGHT:
			this.x = resolution.getScaledWidth() - this.getWidth() + offset;
			break;
		}
	}

	/**
	 * Sets this element's Y position based on its height, alignment and the provided offset
	 * @param offset Positive values move the element down, negative values move it up
	 */
	protected void setPosY(ScaledResolution resolution, int offset) {
		switch (this.getVerticalAlignment()) {
		case TOP:
			this.y = offset;
			break;
		case CENTER:
			this.y = ((resolution.getScaledHeight() / 2) - (this.getHeight() / 2)) + offset;
			break;
		case BOTTOM:
			this.y = resolution.getScaledHeight() - this.getHeight() + offset;
			break;
		}
	}

	/**
	 * Returns true if this element intersects with the passed in overlay
	 */
	public boolean intersectsWith(IGuiOverlay overlay) {
		return this.overlapsX(overlay) && this.overlapsY(overlay);
	}

	/**
	 * Returns true if this element has any overlap with the given overlay on the x axis.
	 * This does not imply that the element intersects with the overlay.
	 */
	public boolean overlapsX(IGuiOverlay overlay) {
		if (this.getLeft() > overlay.getRight() || this.getRight() < overlay.getLeft()) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if this element has any overlap with the given overlay on the y axis.
	 * This does not imply that the element intersects with the overlay.
	 */
	public boolean overlapsY(IGuiOverlay overlay) {
		if (this.getTop() > overlay.getBottom() || this.getBottom() < overlay.getTop()) {
			return false;
		}
		return true;
	}

	/**
	 * Return true if this element can render on the same line as the existing overlay
	 */
	public boolean canCombineX(IGuiOverlay overlay) {
		if (!this.allowMergeX(false) || !overlay.allowMergeX(true)) {
			return false;
		} else if (this.getHorizontalAlignment() != overlay.getHorizontalAlignment()) {
			return false; // different horizontal alignments
		} else if (!this.compareHeight(overlay)) {
			return false; // element heights too different
		}
		switch (this.getHorizontalAlignment()) {
		case LEFT: // left-most 1/3 of screen
			return (overlay.getRight() + this.getWidth() + DEFAULT_PADDING) < this.maxW;
		case CENTER: // center 1/3 of screen
			return (overlay.getRight() + this.getWidth() + DEFAULT_PADDING) < (this.maxW * 2);
		case RIGHT: // right-most 1/3 of screen, would shift left
			return (overlay.getLeft() - this.getWidth() - DEFAULT_PADDING) > (this.maxW * 2);
		}
		return false;
	}

	/**
	 * Returns true if the this element's height is similar enough to the overlay's height
	 */
	protected boolean compareHeight(IGuiOverlay overlay) {
		return this.getHeight() <= overlay.getHeight();
	}

	/**
	 * Adjusts this element's position so it does not overlap with the given overlay
	 */
	protected boolean coalesce(IGuiOverlay overlay, ScaledResolution resolution) {
		if (this.canCombineX(overlay)) {
			if (!this.shiftX(overlay, resolution)) {
				return this.shiftY(overlay, resolution);
			}
		} else if (!this.shiftY(overlay, resolution)) {
			return this.shiftX(overlay, resolution); // try shifting on X anyway
		}
		return true;
	}

	/**
	 * Adjust the x position of this element so it no longer collides with the given overlay
	 * @param overlay Element with collision on the x-axis
	 * @return true if this element's x position was adjusted
	 */
	protected boolean shiftX(IGuiOverlay overlay, ScaledResolution resolution) {
		switch (this.getHorizontalAlignment()) {
		case LEFT:
			// Fall-through
		case CENTER:
			if (overlay.getRight() + this.getWidth() + DEFAULT_PADDING > resolution.getScaledWidth()) {
				return false; // would shift fully or partially off screen
			}
			this.x = overlay.getRight() + DEFAULT_PADDING;
			break;
		case RIGHT:
			if (overlay.getLeft() - (this.getWidth() + DEFAULT_PADDING) < 0) {
				return false; // would shift fully or partially off screen
			}
			this.x = overlay.getLeft() - (this.getWidth() + DEFAULT_PADDING);
			break;
		}
		return true;
	}

	/**
	 * Adjust the y position of this element so it no longer collides with the given overlay
	 * @param overlay Element with collision on the y-axis
	 * @return true if this element's y position was adjusted
	 */
	protected boolean shiftY(IGuiOverlay overlay, ScaledResolution resolution) {
		switch (this.getVerticalAlignment()) {
		case TOP:
			// Fall-through
		case CENTER:
			if (overlay.getBottom() + this.getHeight() + DEFAULT_PADDING > (this.maxH * 2)) {
				return false; // would shift fully or partially off screen
			}
			this.y = overlay.getBottom() + DEFAULT_PADDING;
			break;
		case BOTTOM:
			if (overlay.getTop() - (this.getHeight() + DEFAULT_PADDING) < 0) {
				return false; // would shift fully or partially off screen
			}
			this.y = overlay.getTop() - (this.getHeight() + DEFAULT_PADDING);
			break;
		}
		return true;
	}
}

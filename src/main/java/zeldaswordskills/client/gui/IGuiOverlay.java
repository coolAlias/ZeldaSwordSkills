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

import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IGuiOverlay
{
	/** Horizontal alignments */
	public static enum HALIGN {
		LEFT, CENTER, RIGHT;
		public HALIGN next() {
			return HALIGN.values()[(this.ordinal() + 1) % HALIGN.values().length];
		}
		public HALIGN prev() {
			int i = (this.ordinal() < 1 ? HALIGN.values().length : this.ordinal());
			return HALIGN.values()[(i - 1) % HALIGN.values().length];
		}
		/** Returns the offset such that it will behave similarly for all alignments */
		public int getOffset(int offset) {
			return this == RIGHT ? -offset : offset;
		}
		/** Returns a HALIGN based on the string; if no match is found, LEFT is returned */
		public static HALIGN fromString(String s) {
			s = s.toLowerCase();
			return (s.equals("center") ? CENTER : s.equals("right") ? RIGHT : LEFT);
		}
	}

	/** Vertical alignments */
	public static enum VALIGN {
		TOP, CENTER, BOTTOM;
		public VALIGN next() {
			return VALIGN.values()[(this.ordinal() + 1) % VALIGN.values().length];
		}
		public VALIGN prev() {
			int i = (this.ordinal() < 1 ? VALIGN.values().length : this.ordinal());
			return VALIGN.values()[(i - 1) % VALIGN.values().length];
		}
		/** Returns the offset such that it will behave similarly for all alignments */
		public int getOffset(int offset) {
			return this == BOTTOM ? -offset : offset;
		}
		/** Returns a VALIGN based on the string; if no match is found, TOP is returned */
		public static VALIGN fromString(String s) {
			s = s.toLowerCase();
			return (s.equals("center") ? CENTER : s.equals("bottom") ? BOTTOM : TOP);
		}
	}

	/**
	 * Return true if the overlay should render
	 */
	boolean shouldRender();

	/**
	 * Render this overlay onto the screen
	 * @param resolution Provided by RenderGameOverlayEvent.Post
	 * @param overlays   List of overlays that have already rendered
	 * @return true if the element rendered, false if it didn't
	 */
	@SideOnly(Side.CLIENT)
	boolean renderOverlay(ScaledResolution resolution, List<IGuiOverlay> overlays);

	/** Return the element's left-most coordinate */
	int getLeft();

	/** Return the element's right-most coordinate */
	int getRight();

	/** Return the element's top-most coordinate */
	int getTop();

	/** Return the element's bottom-most coordinate */
	int getBottom();

	/** Return the element's total width */
	int getWidth();

	/** Return the element's total height */
	int getHeight();

	/** Return this element's horizontal alignment */
	HALIGN getHorizontalAlignment();

	/** Return this element's vertical alignment */
	VALIGN getVerticalAlignment();

	/**
	 * Return true if this element may share the same horizontal line with other elements
	 * @param rendered true if this element has already rendered
	 */
	boolean allowMergeX(boolean rendered);

}

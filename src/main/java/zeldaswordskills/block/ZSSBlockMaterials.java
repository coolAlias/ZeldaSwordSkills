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

package zeldaswordskills.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class ZSSBlockMaterials
{
	/** Clay-colored material which can be harvested in adventure mode (allows player to left-click) */
	public static final Material adventureClay = new AdventureExemptMaterial(MapColor.clayColor);

	/** Air-like material in that pistons can move over it, but cannot move it */
	public static final Material sacredFlame = new AdventureExemptMaterial(MapColor.airColor).setNoPushMobility();

	/** Rock-colored material which can be harvested in adventure mode (allows player to left-click) */
	public static final Material adventureStone = new AdventureExemptMaterial(MapColor.stoneColor).setRequiresTool();

	/** Material used by Wooden Pegs */
	public static final Material pegWoodMaterial = new AdventureExemptMaterial(MapColor.woodColor).setRequiresTool().setImmovableMobility();

	/** Material used by Rusty Pegs */
	public static final Material pegRustyMaterial = new AdventureExemptMaterial(MapColor.ironColor).setRequiresTool().setImmovableMobility();

	/**
	 * Base block Material which is adventure-mode exempt (left-clicks are passed to block)
	 * Also wraps the various setters so they can be used in the ZSSBlockMaterials class.
	 */
	public static class AdventureExemptMaterial extends Material {
		public AdventureExemptMaterial(MapColor color) {
			super(color);
			this.setAdventureModeExempt();
		}

		@Override
		protected AdventureExemptMaterial setNoPushMobility() {
			super.setNoPushMobility();
			return this;
		}

		@Override
		protected AdventureExemptMaterial setImmovableMobility() {
			super.setImmovableMobility();
			return this;
		}

		@Override
		protected AdventureExemptMaterial setRequiresTool() {
			super.setRequiresTool();
			return this;
		}

		@Override
		protected AdventureExemptMaterial setBurning() {
			super.setBurning();
			return this;
		}
	}
}

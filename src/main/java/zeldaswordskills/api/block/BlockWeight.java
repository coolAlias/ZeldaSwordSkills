/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.api.block;

/**
 * 
 * An enumeration of block "weights", representing how difficult they are
 * to lift or smash. The weights are based on block resistance values for
 * all solid, breakable normal cubes.
 *
 */
public enum BlockWeight {
	/** Used mainly for items that cannot affect vanilla blocks but should still have some interaction */
	VERY_LIGHT(0.0F),
	/** Most basic ore blocks, dirt, etc. are in this category, but not stone or logs */
	LIGHT(5.0F),
	/** Nearly all normal blocks, including stone, blocks of gold, etc. */
	MEDIUM(10.0F),
	/** The only vanilla block heavier than this is obsidian */
	HEAVY(15.0F),
	/** The heaviest category containing vanilla blocks */
	VERY_HEAVY(2000.0F),
	/** A category above the heaviest vanilla blocks */
	EXTREME_I(5000.0F),
	/** A category above the category above the heaviest vanilla blocks */
	EXTREME_II(10000.0F),
	/** Category for blocks that are impossible to move or smash */
	IMPOSSIBLE(6000000.0F); // value of bedrock

	/** The weight of block that can be lifted with this strength */
	public final float weight;
	private BlockWeight(float f) {
		weight = f;
	}
	
	/** Returns the next heavier BlockWeight, or IMPOSSIBLE if maxed */
	public BlockWeight next() {
		return (this != IMPOSSIBLE ? values()[this.ordinal() + 1] : IMPOSSIBLE);
	}
	
	/** Returns the previous lighter BlockWeight, or VERY_LIGHT if already at the minimum */
	public BlockWeight prev() {
		return (this != VERY_LIGHT ? values()[this.ordinal() - 1] : VERY_LIGHT);
	}
	
	/** Returns the vanilla post-setResistance bedrock resistance value (6000000.0F * 3.0F) */
	public static float getMaxResistance() {
		return IMPOSSIBLE.weight * 3.0F;
	}
}

/**
    Copyright (C) <2018> <coolAlias>

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

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import zeldaswordskills.entity.projectile.EntityWhip;
import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * 
 * Interface for blocks that interact in specific ways with {@link EntityWhip}
 *
 */
public interface IWhipBlock
{
	public static enum WhipType {
		/** Standard whip can latch on to all 'post'-like blocks: fences, logs, levers, ladders, signs, etc. */
		WHIP_SHORT(50, false),
		/** Same as the standard whip, but with twice the range */
		WHIP_LONG(150, true),
		/** The magic whip can latch on to any solid surface and has the same range as the long whip */
		WHIP_MAGIC(300, true);

		/** The default rupee value of an ItemBomb of this bomb type */
		public final int defaultRupeeValue;

		private final boolean isExtended;

		private WhipType(int defaultRupeeValue, boolean isExtended) {
			this.defaultRupeeValue = defaultRupeeValue;
			this.isExtended = isExtended;
		}

		public boolean isExtended() {
			return isExtended;
		}
	}

	/**
	 * Return true if the type of whip is able to break this specific block.
	 * This method is never called if canGrabBlock returns true, as the whip
	 * can not both attach to and break a block at the same time.
	 * @param thrower Not likely to be null, but it is conceivable
	 * @param side	The side of the block that was hit
	 */
	boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side);

	/**
	 * Return true if the type of whip is able to attach to this specific block.
	 * @param thrower Not likely to be null, but it is conceivable
	 * @param side	The side of the block that was hit
	 */
	boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side);

	/**
	 * Allows for special interactions, such as pulling levers, rather than swinging.
	 * This method is called every tick before swing motion is applied.
	 * @param whip	Use {@link EntityWhip#setDead() setDead} to kill the entity if necessary
	 * @param ticksInGround	Useful if the action requires a certain amount of time to perform
	 * @return ALLOW to swing, DENY to not swing, and DEFAULT to use standard behavior.
	 */
	Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround);

}

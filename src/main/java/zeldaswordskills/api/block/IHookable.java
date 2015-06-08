/**
    Copyright (C) <2015> <coolAlias>

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

import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

/**
 * 
 * Interface for any block that wants to return a different material type
 * than its own for purposes of determining which hookshots can attach to it
 *
 */
public interface IHookable
{
	public enum HookshotType {
		/** Can grapple wood and break glass */
		WOOD_SHOT(false),
		/** Extended version of WOOD_SHOT */
		WOOD_SHOT_EXT(true),
		/** Can grapple stone and iron grates, breaks glass and wood */
		CLAW_SHOT(false),
		/** Extended version of CLAW_SHOT */
		CLAW_SHOT_EXT(true),
		/** Can grapple a wide variety of materials, only breaks glass */
		MULTI_SHOT(false),
		/** Extended version of MULTI_SHOT */
		MULTI_SHOT_EXT(true);

		private final boolean isExtended;

		private HookshotType(boolean isExtended) {
			this.isExtended = isExtended;
		}

		public boolean isExtended() {
			return isExtended;
		}

		/**
		 * Returns the base hookshot type: e.g. 'MULTI_SHOT' for both
		 * regular and extended multi-shot type hookshots
		 */
		public HookshotType getBaseType() {
			switch(this) {
			case WOOD_SHOT_EXT: return WOOD_SHOT;
			case CLAW_SHOT_EXT: return CLAW_SHOT;
			case MULTI_SHOT_EXT: return MULTI_SHOT;
			default: return this;
			}
		}
	}

	/**
	 * Return true if the type of hookshot is able to break this specific block,
	 * regardless of block material and/or configuaration settings.
	 * This method is never called if canGrabBlock returns true, as the hookshot
	 * can not both attach to and break a block at the same time.
	 * 
	 * Note that blocks destroyed by hookshots do NOT drop any items.
	 * 
	 * @param type	The type of hookshot attempting to destroy the block
	 * @param face	The face of the block that was hit
	 * @return	Result.DEFAULT to use the standard hookshot mechanics
	 * 			Result.ALLOW will allow the block to be destroyed
	 * 			Result.DENY will prevent the block from being destroyed
	 */
	Result canDestroyBlock(HookshotType type, World world, BlockPos pos, EnumFacing face);

	/**
	 * Return true if the type of hookshot is able to attach to this specific block.
	 * @param type	The type of hookshot attempting to grapple the block
	 * @param face	The face of the block that was hit
	 * @return	Result.DEFAULT to use the standard hookshot mechanics
	 * 			Result.ALLOW will allow the hookshot to attach to the block
	 * 			Result.DENY will prevent the hookshot from attaching to the block
	 */
	Result canGrabBlock(HookshotType type, World world, BlockPos pos, EnumFacing face);

	/**
	 * Returns the Material type that should be used to determine which, if
	 * any, hookshots can attach to this block or, if it can't attach, whether
	 * this block will be destroyed by the hookshot upon impact. Only used when
	 * {@link #canDestroyBlock} and {@link #canGrabBlock} return {@link Result#DEFAULT}
	 * @param type the type of hookshot attempting to grapple the block
	 * @param face	The face of the block that was hit
	 */
	Material getHookableMaterial(HookshotType type, World world, BlockPos pos, EnumFacing face);

}

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

package zeldaswordskills.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import zeldaswordskills.item.ItemHeldBlock;


/**
 * 
 * Interface for blocks that can be lifted with any item that implements the
 * ILiftBlock interface.
 * 
 * Only solid opaque cubes without tile entities may be lifted, and vanilla blocks
 * may not be lifted in Adventure Mode or if the player is otherwise unable to edit
 * the world. ILiftable blocks are not restricted by editing permissions.
 *
 */
public interface ILiftable {

	/**
	 * Returns the block's weight for purposes of lifting, allowing
	 * otherwise unbreakable blocks to be handled efficiently
	 * @param stack the itemstack used to lift the block
	 * @param state the current block state
	 * @param face the face (side) of the block the player is trying to lift
	 * @return return null to use the block's explosion resistance as its weight
	 */
	BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face);

	/**
	 * Called after the block is picked up by a player, but before it is set to air,
	 * allowing the ItemHeldBlock stack's NBT tag to be manipulated, e.g. by storing a
	 * tile entity inside. See {@link ItemHeldBlock#getBlockStack} for more information.
	 * @param stack the ItemHeldBlock stack containing the ILiftBlock stack used to pick up the block
	 * @param state the current block state of the block before it was picked up
	 */
	void onLifted(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state);

	/**
	 * Called when a liftable block is placed from the held block stack, allowing tile entity
	 * data previously stored in the stack's NBT to be handled, among other things
	 * @param stack the ItemHeldBlock that was created when the block was lifted
	 * @param state the block state returned from the block's onBlockPlaced method
	 */
	void onHeldBlockPlaced(World world, ItemStack stack, BlockPos pos, IBlockState state);

}

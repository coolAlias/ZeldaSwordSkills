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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;


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
	 * @param meta the block metadata
	 */
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, int meta);

	/**
	 * Called after the block is picked up by a player, but before it is set to air,
	 * allowing tile entity data to be stored in the player's held item stack
	 * @param stack the stack used to lift the block is always an ILiftBlock
	 * @param meta the metadata of the block before it was picked up
	 */
	public void onLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int meta);

	/**
	 * Called when a liftable block is placed from the held block stack, allowing tile entity
	 * data previously stored in the stack's NBT to be handled, among other things
	 * @param stack the ItemHeldBlock that was created when the block was lifted
	 * @param meta the metadata returned from the block's onBlockPlaced method
	 */
	public void onHeldBlockPlaced(World world, ItemStack stack, int x, int y, int z, int meta);

}

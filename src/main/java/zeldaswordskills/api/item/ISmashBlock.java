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

package zeldaswordskills.api.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.api.block.BlockWeight;

/**
 * 
 * Interface for items that can smash (typically destroy) blocks immediately upon
 * left-clicking the block. Blocks that do not implement ISmashable cannot be
 * destroyed if the player is not allowed to edit the world; otherwise any
 * solid opaque normal cube can be smashed if the item's strength is greater
 * than or equal to the associated BlockWeight.
 * 
 * ISmashable blocks can define custom behaviors using the onSmashed method, but all
 * other blocks that meet the smash criteria will simply be destroyed.
 * 
 * Note that in Adventure Mode, any Item implementing ISmashBlock should override
 * the canHarvestBlock method to ensure that it will be able to affect all blocks
 * that implement ISmashable, just in case the block does not use an Adventure Mode
 * exempt block material:
 * 
 * @Override
 * public boolean canHarvestBlock(Block block) {
 * 	return block instanceof ISmashable;
 * }
 *
 */
public interface ISmashBlock {
	
	/**
	 * Returns the strength of this item for the purpose of smashing blocks
	 * Player-, ItemStack-, and Block- sensitive
	 * @param block the block that was struck
	 * @param meta the metadata of the block struck
	 */
	public BlockWeight getSmashStrength(EntityPlayer player, ItemStack stack, Block block, int meta);
	
	/**
	 * Called after the player has attempted to smash a block with the stack,
	 * making it ideal for damaging the stack, for example
	 * @param meta the metadata of the block before it was smashed
	 * @param wasSmashed true if the smash was considered successful
	 */
	public void onBlockSmashed(EntityPlayer player, ItemStack stack, Block block, int meta, boolean wasSmashed);

}

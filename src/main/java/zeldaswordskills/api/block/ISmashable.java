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
import net.minecraftforge.fml.common.eventhandler.Event.Result;

/**
 * 
 * Interface for blocks that are smashable, even in Adventure Mode. Note that in order
 * for your block to be smashable in Adventure Mode, it is advisable to create a custom
 * Material that is adventure mode exempt, though Hammers will be able to affect any
 * ISmashable block regardless of Material.
 * 
 * Vanilla blocks are only smashable if the player is allowed to edit the world, the block
 * is solid and breakable (e.g. not bedrock), and the item used has sufficient strength to
 * smash the block based on its block resistance value.
 * 
 * Vanilla blocks with tile entities cannot be smashed, but ISmashable blocks may choose
 * to allow and handle tile entities.
 * 
 * Note that unless handled specifically in onSmashed, smashed blocks do not drop any items.
 *
 */
public interface ISmashable {

	/**
	 * Returns the weight of this block for the purpose of determining
	 * whether it can be smashed or not by the item used
	 * @param stack the itemstack used to smash the block
	 * @param state the current block state
	 * @param face the face of the block that was hit
	 */
	BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face);
	
	/**
	 * This method is called right before the block smash calculations occur,
	 * allowing custom handling of the event by the block in question.
	 * @param stack the stack's item will always be an instance of ISmashBlock
	 * @param face the face of the block that was hit
	 * @return
	 * DEFAULT proceeds with the default smash calculations and possible destruction of the block struck
	 * ALLOW prevents further processing and indicates a successful smash
	 * DENY prevents further processing and indicates a failed smash attempt
	 */
	Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face);

}

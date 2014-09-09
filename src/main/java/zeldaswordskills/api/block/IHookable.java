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

import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import zeldaswordskills.api.item.HookshotType;
import cpw.mods.fml.common.eventhandler.Event.Result;

/**
 * 
 * Interface for any block that wants to return a different material type
 * than its own for purposes of determining which hookshots can attach to it
 *
 */
public interface IHookable {

	/**
	 * Return true to always allow hookshots to attach, regardless of block material
	 * @param type the type of hookshot attempting to grapple the block
	 */
	public boolean canAlwaysGrab(HookshotType type, World world, int x, int y, int z);

	/**
	 * Return true to allow hookshots to destroy the block, regardless of block material or
	 * configuration settings; passes x, y, z in case tile entity will affect the outcome
	 * @param type the type of hookshot attempting to destroy the block
	 * @return	Result.DEFAULT to use the standard hookshot mechanics
	 * 			Result.ALLOW will allow the block to be destroyed
	 * 			Result.DENY will prevent the block from being destroyed
	 */
	public Result canDestroyBlock(HookshotType type, World world, int x, int y, int z);

	/**
	 * Returns the Material type that should be used to determine which, if
	 * any, hookshots can attach to this block or, if it can't attach, whether
	 * this block will be destroyed by the hookshot upon impact
	 * @param type the type of hookshot attempting to grapple the block
	 */
	public Material getHookableMaterial(HookshotType type, World world, int x, int y, int z);

}

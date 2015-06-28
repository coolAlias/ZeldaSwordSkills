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

package zeldaswordskills.api.gen;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * 
 * A structure that may be generated during regular game-play, e.g. via right-click.
 * 
 */
public interface ISeedStructure {

	/**
	 * Attempt to generate the structure at the coordinates of the block clicked
	 * @param pos	The position of the block clicked, so may want to generate at pos.up()
	 * @param face	The face of the block clicked
	 * @return		True if the structure generated successfully
	 */
	boolean generate(World world, EntityPlayer player, BlockPos pos, EnumFacing face);

}

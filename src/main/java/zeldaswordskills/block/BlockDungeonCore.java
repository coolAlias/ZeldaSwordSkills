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

package zeldaswordskills.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;

/**
 * 
 * This block is the core of the dungeon, providing the tile entity that verifies the
 * integrity of the structure and 'disables' indestructible blocks in addition to
 * playing the medley when breached.
 * 
 * Uses the same bit flag as {@link #BlockDungeonStone}
 *
 */
public class BlockDungeonCore extends BlockDungeonStone
{
	public BlockDungeonCore(Material material) {
		super(material);
		setHardness(1.5F);
		setResistance(10.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDungeonCore();
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) < 0x8 ? blockHardness : -1.0F);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block oldBlock, int oldMeta) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonCore) {
			((TileEntityDungeonCore) te).onBlockBroken();
		}
		super.breakBlock(world, x, y, z, oldBlock, oldMeta);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (world.getBlockMetadata(x, y, z) > 7) {
			super.onBlockClicked(world, x, y, z, player);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityDungeonCore) {
				((TileEntityDungeonCore) te).setSpawner();
			}
		}
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
	}
}

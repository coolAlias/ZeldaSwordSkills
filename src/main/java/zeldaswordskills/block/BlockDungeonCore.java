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

package zeldaswordskills.block;

import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;

/**
 * 
 * This block is the core of the dungeon, providing the tile entity that verifies the
 * integrity of the structure and 'disables' indestructible blocks in addition to
 * playing the medley when breached.
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
	public float getBlockHardness(World world, BlockPos pos) {
		return (((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue() ? -1.0F : blockHardness);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDungeonCore) {
			((TileEntityDungeonCore) te).onBlockBroken();
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		// super plays the break sound and prints a message: only call if unbreakable
		if (((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue()) {
			super.onBlockClicked(world, pos, player);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityDungeonCore) {
				((TileEntityDungeonCore) te).setSpawner();
			}
		}
		super.onBlockPlacedBy(world, pos, state, entity, stack);
	}

	@Override
	public IBlockState getDefaultRenderState(boolean isUnbreakable) {
		return Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, isUnbreakable ? BlockStone.EnumType.GRANITE_SMOOTH : BlockStone.EnumType.GRANITE);
	}
}

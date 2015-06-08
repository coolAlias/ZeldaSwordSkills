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

import net.minecraft.block.Block;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;

public class BlockBar extends BlockRotatedPillar implements IWhipBlock
{
	public BlockBar(Material material) {
		super(material);
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundTypeWood);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return face.getAxis() != ((EnumFacing.Axis) world.getBlockState(pos).getValue(AXIS));
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		return Result.DEFAULT;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return super.onBlockPlaced(world, pos, face, hitX, hitY, hitZ, meta, placer).withProperty(AXIS, face.getAxis());
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbor) {
		boolean drop = false;
		switch ((EnumFacing.Axis) world.getBlockState(pos).getValue(AXIS)) {
		case X:
			drop = (!world.isSideSolid(pos.east(), EnumFacing.WEST) && !world.isSideSolid(pos.west(), EnumFacing.EAST));
			break;
		case Y:
			drop = (!world.isSideSolid(pos.up(), EnumFacing.DOWN) && !world.isSideSolid(pos.down(), EnumFacing.UP));
			break;
		case Z:
			drop = (!world.isSideSolid(pos.north(), EnumFacing.SOUTH) && !world.isSideSolid(pos.south(), EnumFacing.NORTH));
			break;
		}
		if (drop && !world.isRemote) {
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		switch ((EnumFacing.Axis) world.getBlockState(pos).getValue(AXIS)) {
		case X:
			setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
			break;
		case Y:
			setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 1.0F, 0.75F);
			break;
		case Z:
			setBlockBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 1.0F);
			break;
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.0F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(AXIS, EnumFacing.Axis.values()[meta % EnumFacing.Axis.values().length]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing.Axis) state.getValue(AXIS)).ordinal();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, AXIS);
	}
}

/**
    Copyright (C) <2017> <coolAlias>

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
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityInscription;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.BlockRotationData;

/**
 * 
 * A block which can be set to teach any {@link AbstractZeldaSong} so
 * long as {@link AbstractZeldaSong#canLearnFromInscription} returns true.
 *
 */
public class BlockSongInscription extends Block implements ITileEntityProvider, IVanillaRotation
{
	/** The direction the inscription faces is opposite of the block face to which it is attached */
	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	/** One pixel's thickness */
	private static final float px1 = (1.0F / 16.0F);

	/** Two pixel's thickness */
	private static final float px2 = (1.0F / 8.0F);

	public BlockSongInscription() {
		super(Material.iron);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
		setBlockBounds(px2, 0.0F, px2, 1.0F - px2, px1, 1.0F - px2);
		setHardness(50.0F);
		setResistance(2000.0F);
		setStepSound(soundTypeMetal);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityInscription();
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
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	/*
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}
	 */

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityInscription) {
			return ((TileEntityInscription) te).onActivated(player);
		}
		return false;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		for (EnumFacing face : EnumFacing.values()) {
			// TODO many vanilla blocks pass 'true' as 3rd parameter
			if (world.isSideSolid(pos.offset(face), face.getOpposite(), true)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		// TODO vanilla button also passes 'true' here
		return world.isSideSolid(pos.offset(face.getOpposite()), face) ? getDefaultState().withProperty(FACING, face) : getDefaultState().withProperty(FACING, EnumFacing.UP);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SongName")) {
			AbstractZeldaSong song = ZeldaSongs.getSongByName(stack.getTagCompound().getString("SongName"));
			if (song != null) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityInscription) {
					((TileEntityInscription) te).setSong(song);
				}
			}
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(px2, 0.0F, px2, 1.0F - px2, px1, 1.0F - px2);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		float f = 1.0F - px2;
		switch(world.getBlockState(pos).getValue(FACING)) {
		case DOWN: setBlockBounds(px2, 1.0F - px1, px2, f, 1.0F, f); break;
		case UP: setBlockBounds(px2, 0.0F, px2, f, px1, f); break;
		case NORTH: setBlockBounds(px2, px2, 1.0F - px1, f, f, 1.0F); break;
		case SOUTH: setBlockBounds(px2, px2, 0.0F, f, f, px1); break;
		case WEST: setBlockBounds(1.0F - px1, px2, px2, 1.0F, f, f); break;
		case EAST: setBlockBounds(0.0F, px2, px2, px1, f, f); break;
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbor) {
		EnumFacing face = state.getValue(FACING);
		if (!world.isSideSolid(pos.offset(face.getOpposite()), face)) { // TODO , true) is used in many vanilla blocks
			dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, FACING);
	}

	@Override
	public BlockRotationData.Rotation getRotationPattern() {
		return BlockRotationData.Rotation.PISTON_CONTAINER;
	}
}

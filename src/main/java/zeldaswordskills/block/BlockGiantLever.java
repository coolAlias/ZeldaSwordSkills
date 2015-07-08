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

import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Lever that may only be switched on by using a whip.
 * Unbreakable unless it is generating power.
 *
 */
public class BlockGiantLever extends BlockLever implements IWhipBlock
{
	public BlockGiantLever() {
		super();
		setHardness(1.0F);
		setStepSound(soundTypeWood);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.125F, 0.0F, 0.125F, 0.875F, 0.8F, 0.875F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, BlockLever.EnumOrientation.NORTH).withProperty(POWERED, Boolean.valueOf(false)));
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		EnumFacing facing = ((BlockLever.EnumOrientation) world.getBlockState(pos).getValue(FACING)).getFacing();
		return face != facing.getOpposite();
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		if (ticksInGround > (40 - (whip.getType().ordinal() * 5))) {
			WorldUtils.activateButton(world, world.getBlockState(pos), pos);
			whip.setDead();
		}
		return Result.DENY;
	}

	/**
	 * This is slightly shorter than the actual block bounds so players can walk through the lever portion
	 */
	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		switch ((BlockLever.EnumOrientation) state.getValue(FACING)) {
		case EAST: return new AxisAlignedBB(pos.getX() + 0.0F, pos.getY() + 0.125F, pos.getZ() + 0.2F, pos.getX() + 0.375F, pos.getY() + 0.875F, pos.getZ() + 0.8F);
		case WEST: return new AxisAlignedBB(pos.getX() + 0.625F, pos.getY() + 0.125F, pos.getZ() + 0.2F, pos.getX() + 1.0F, pos.getY() + 0.875F, pos.getZ() + 0.8F);
		case SOUTH: return new AxisAlignedBB(pos.getX() + 0.2F, pos.getY() + 0.125F, pos.getZ() + 0.0F, pos.getX() + 0.8F, pos.getY() + 0.875F, pos.getZ() + 0.375F);
		case NORTH: return new AxisAlignedBB(pos.getX() + 0.2F, pos.getY() + 0.125F, pos.getZ() + 0.625F, pos.getX() + 0.8F, pos.getY() + 0.875F, pos.getZ() + 1.0F);
		case UP_X: return new AxisAlignedBB(pos.getX() + 0.125F, pos.getY() + 0.0F, pos.getZ() + 0.2F, pos.getX() + 0.875F, pos.getY() + 0.375F, pos.getZ() + 0.8F);
		case UP_Z: return new AxisAlignedBB(pos.getX() + 0.2F, pos.getY() + 0.0F, pos.getZ() + 0.125F, pos.getX() + 0.8F, pos.getY() + 0.375F, pos.getZ() + 0.875F);
		case DOWN_X: return new AxisAlignedBB(pos.getX() + 0.125F, pos.getY() + 0.625F, pos.getZ() + 0.2F, pos.getX() + 0.875F, pos.getY() + 1.0F, pos.getZ() + 0.8F);
		case DOWN_Z: return new AxisAlignedBB(pos.getX() + 0.2F, pos.getY() + 0.625F, pos.getZ() + 0.125F, pos.getX() + 0.8F, pos.getY() + 1.0F, pos.getZ() + 0.875F);
		}
		return null;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		switch ((BlockLever.EnumOrientation) world.getBlockState(pos).getValue(FACING)) {
		case EAST: setBlockBounds(0.0F, 0.125F, 0.2F, 0.8F, 0.875F, 0.8F); break;
		case WEST: setBlockBounds(0.2F, 0.125F, 0.2F, 1.0F, 0.875F, 0.8F); break;
		case SOUTH: setBlockBounds(0.2F, 0.125F, 0.0F, 0.8F, 0.875F, 0.8F); break;
		case NORTH: setBlockBounds(0.2F, 0.125F, 0.2F, 0.8F, 0.875F, 1.0F); break;
		case UP_X: setBlockBounds(0.125F, 0.0F, 0.2F, 0.875F, 0.8F, 0.8F); break;
		case UP_Z: setBlockBounds(0.2F, 0.0F, 0.125F, 0.8F, 0.8F, 0.875F); break;
		case DOWN_X: setBlockBounds(0.125F, 0.2F, 0.2F, 0.875F, 1.0F, 0.8F); break;
		case DOWN_Z: setBlockBounds(0.2F, 0.2F, 0.125F, 0.8F, 1.0F, 0.875F); break;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		// cannot be activated normally
		return false;
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos) {
		return (((Boolean) world.getBlockState(pos).getValue(POWERED)).booleanValue() ? blockHardness : -1);
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return ((Boolean) world.getBlockState(pos).getValue(POWERED)).booleanValue();
	}

	// TODO remove if Mojang's stupid code ever gets fixed
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (((Boolean) world.getBlockState(pos).getValue(POWERED)).booleanValue()) {
			super.onBlockExploded(world, pos, explosion);
		}
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getExplosionResistance(world, pos, entity, explosion);
		}
		return (((Boolean) state.getValue(POWERED)).booleanValue() ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}
}

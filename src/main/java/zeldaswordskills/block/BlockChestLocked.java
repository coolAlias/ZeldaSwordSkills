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

package zeldaswordskills.block;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BlockRotationData;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * A chest which cannot be broken nor its inventory interacted with until it
 * is unlocked with a 'small key.'
 * 
 * While in Creative Mode, right-click to open up a normal GUI for easy
 * manipulation of the locked chest contents.
 *
 */
public class BlockChestLocked extends Block implements ITileEntityProvider, IVanillaRotation
{
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	/** Prevents inventory from dropping when block is changed to a normal chest */
	private static boolean keepInventory;

	public BlockChestLocked() {
		super(Material.wood);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeWood);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityChestLocked();
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

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!keepInventory) {
			WorldUtils.dropContainerBlockInventory(world, pos);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof IInventory)) {
			return false;
		}
		if (player.capabilities.isCreativeMode && !player.isSneaking()) {
			player.displayGUIChest((IInventory) te);
			return true;
		} else if (canUnlock(player)) {
			convertToChest((IInventory) te, world, pos);
			WorldUtils.playSoundAtEntity(player, Sounds.LOCK_CHEST, 0.4F, 0.5F);
			return true;
		} else {
			WorldUtils.playSoundAtEntity(player, Sounds.LOCK_RATTLE, 0.4F, 0.5F);
		}
		return false;
	}

	/**
	 * Converts this locked chest block into a vanilla chest
	 */
	protected void convertToChest(IInventory inv, World world, BlockPos pos) {
		EnumFacing facing = world.getBlockState(pos).getValue(FACING);
		keepInventory = true;
		world.setBlockState(pos, Blocks.chest.getDefaultState());
		keepInventory = false;
		// If there is not an adjacent chest, make sure the new chest has the same facing
		boolean isChest = world.getBlockState(pos.east()).getBlock() == Blocks.chest;
		if (!isChest) {
			isChest = world.getBlockState(pos.west()).getBlock() == Blocks.chest;
		}
		if (!isChest) {
			isChest = world.getBlockState(pos.north()).getBlock() == Blocks.chest;
		}
		if (!isChest) {
			isChest = world.getBlockState(pos.south()).getBlock() == Blocks.chest;
		}
		if (!isChest) {
			world.setBlockState(pos, Blocks.chest.getDefaultState().withProperty(BlockChest.FACING, facing), 3);
		}
		// copy the old inventory to the new chest
		TileEntity chest = world.getTileEntity(pos);
		if (chest instanceof TileEntityChest) {
			IInventory inv2 = (IInventory) chest;
			for (int i = 0; i < inv.getSizeInventory() && i < inv2.getSizeInventory(); ++i) {
				inv2.setInventorySlotContents(i, inv.getStackInSlot(i));
			}
		}
	}

	private boolean canUnlock(EntityPlayer player) {
		ItemStack key = player.getHeldItem();
		if (key != null) {
			if (key.getItem() == ZSSItems.keySmall) {
				return PlayerUtils.consumeHeldItem(player, ZSSItems.keySmall, 1);
			} else if (key.getItem() == ZSSItems.keySkeleton) {
				key.damageItem(1, player);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		IBlockState state_north = world.getBlockState(pos.north());
		IBlockState state_south = world.getBlockState(pos.south());
		IBlockState state_west = world.getBlockState(pos.west());
		IBlockState state_east = world.getBlockState(pos.east());
		EnumFacing facing = state.getValue(FACING);
		if (state_north.getBlock().isFullBlock() && !state_south.getBlock().isFullBlock()) {
			facing = EnumFacing.SOUTH;
		}
		if (state_south.getBlock().isFullBlock() && !state_north.getBlock().isFullBlock()) {
			facing = EnumFacing.NORTH;
		}
		if (state_west.getBlock().isFullBlock() && !state_east.getBlock().isFullBlock()) {
			facing = EnumFacing.EAST;
		}
		if (state_east.getBlock().isFullBlock() && !state_west.getBlock().isFullBlock()) {
			facing = EnumFacing.WEST;
		}
		world.setBlockState(pos, state.withProperty(FACING, facing), 3);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		EnumFacing facing = EnumFacing.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		state = state.withProperty(FACING, facing);
		world.setBlockState(pos, state, 3);
	}

	/**
	 * Copied from BlockChest - call any time the chest is placed without knowing where it should face
	 */
	public IBlockState correctFacing(World world, BlockPos pos, IBlockState state) {
		EnumFacing facing = null;
		Iterator<EnumFacing> iterator = EnumFacing.Plane.HORIZONTAL.iterator();
		while (iterator.hasNext()) {
			EnumFacing facing1 = iterator.next();
			IBlockState iblockstate1 = world.getBlockState(pos.offset(facing1));
			if (iblockstate1.getBlock() == this) {
				return state;
			}
			if (iblockstate1.getBlock().isFullBlock()) {
				if (facing != null) {
					facing = null;
					break;
				}
				facing = facing1;
			}
		}
		if (facing != null) {
			return state.withProperty(FACING, facing.getOpposite());
		} else {
			EnumFacing facing2 = state.getValue(FACING);
			if (world.getBlockState(pos.offset(facing2)).getBlock().isFullBlock()) {
				facing2 = facing2.getOpposite();
			}
			if (world.getBlockState(pos.offset(facing2)).getBlock().isFullBlock()) {
				facing2 = facing2.rotateY();
			}
			if (world.getBlockState(pos.offset(facing2)).getBlock().isFullBlock()) {
				facing2 = facing2.getOpposite();
			}
			return state.withProperty(FACING, facing2);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.getFront(meta);
		if (facing.getAxis() == EnumFacing.Axis.Y) {
			facing = EnumFacing.NORTH;
		}
		return getDefaultState().withProperty(FACING, facing);
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

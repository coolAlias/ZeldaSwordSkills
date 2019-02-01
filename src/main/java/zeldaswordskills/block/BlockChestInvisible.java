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

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;

public class BlockChestInvisible extends BlockChestLocked implements ICustomStateMapper, ISongBlock {

	public BlockChestInvisible() {
		super();
		setBlockBounds(0.475F, 0.0F, 0.475F, 0.525F, 0.05F, 0.525F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
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
		if (player.capabilities.isCreativeMode) {
			player.displayGUIChest((IInventory) te);
			return true;
		}
		return false;
	}

	@Override
	public boolean onSongPlayed(World world, BlockPos pos, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		if (power > 4 && song == ZeldaSongs.songZeldasLullaby && affected == 0) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory) {
				convertToChest((IInventory) te, world, pos);
				world.playSoundAtEntity(player, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		double dx = (double) pos.getX() + rand.nextFloat();
		double dy = (double) pos.getY() + 0.1D + (rand.nextFloat() * 0.5D);
		double dz = (double) pos.getZ() + rand.nextFloat();
		world.spawnParticle(EnumParticleTypes.SUSPENDED_DEPTH, dx, dy, dz, 0.0D, 0.0D, 0.0D);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(ModInfo.ID + ":chest_invisible");
			}
		};
	}
}

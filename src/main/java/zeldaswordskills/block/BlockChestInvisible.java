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

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockChestInvisible extends BlockChestLocked implements ISongBlock {

	public BlockChestInvisible() {
		super();
		setBlockBounds(0.475F, 0.0F, 0.475F, 0.525F, 0.05F, 0.525F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityChestInvisible();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		TileEntity te = world.getTileEntity(x, y, z);
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
	public boolean onSongPlayed(World world, int x, int y, int z, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		if (power > 4 && song == ZeldaSongs.songZeldasLullaby && affected == 0) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof IInventory) {
				convertToChest((IInventory) te, world, x, y, z);
				world.playSoundAtEntity(player, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		double dx = (double) x + rand.nextFloat();
		double dy = (double) y + 0.1D + (rand.nextFloat() * 0.5D);
		double dz = (double) z + rand.nextFloat();
		world.spawnParticle("depthsuspend", dx, dy, dz, 0.0D, 0.0D, 0.0D);
	}

	public static class TileEntityChestInvisible extends TileEntityChestLocked {
		public TileEntityChestInvisible() {
			super();
		}
	}
}

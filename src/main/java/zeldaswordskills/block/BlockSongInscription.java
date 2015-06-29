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

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import zeldaswordskills.block.tileentity.TileEntityInscription;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.SideHit;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A block which can be set to teach any {@link AbstractZeldaSong} so
 * long as {@link AbstractZeldaSong#canLearnFromInscription} returns true.
 *
 */
public class BlockSongInscription extends BlockContainer
{
	/** One pixel's thickness */
	private static final float px1 = (1.0F / 16.0F);

	/** Two pixel's thickness */
	private static final float px2 = (1.0F / 8.0F);

	public BlockSongInscription(int id) {
		super(id, Material.iron);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, px1, 1.0F);
		setHardness(50.0F);
		setResistance(2000.0F);
		setStepSound(soundMetalFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityInscription();
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityInscription) {
			return ((TileEntityInscription) te).onActivated(player);
		}
		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		switch(side) {
		case 2: return 3;
		case 3: return 2;
		case 4: return 5;
		case 5: return 4;
		case SideHit.TOP: return 0;
		case SideHit.BOTTOM: return 1;
		}
		return meta;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SongName")) {
			AbstractZeldaSong song = ZeldaSongs.getSongByName(stack.getTagCompound().getString("SongName"));
			if (song != null) {
				TileEntity te = world.getBlockTileEntity(x, y, z);
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
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float f = 1.0F - px2;
		switch(world.getBlockMetadata(x, y, z)) {
		case 1: setBlockBounds(px2, 1.0F - px1, px2, f, 1.0F, f); break;
		case 2: setBlockBounds(px2, px2, 0.0F, f, f, px1); break;
		case 3: setBlockBounds(px2, px2, 1.0F - px1, f, f, 1.0F); break;
		case 4: setBlockBounds(0.0F, px2, px2, px1, f, f); break;
		case 5: setBlockBounds(1.0F - px1, px2, px2, 1.0F, f, f); break;
		default: setBlockBounds(px2, 0.0F, px2, f, px1, f); break;
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighbor) {
		int meta = world.getBlockMetadata(x, y, z);
		int x1 = x;
		int y1 = y;
		int z1 = z;
		ForgeDirection side = null;
		switch(meta) {
		case 0: --y1; side = ForgeDirection.UP; break;
		case 1: ++y1; side = ForgeDirection.DOWN; break;
		case 2: --z1; side = ForgeDirection.SOUTH; break;
		case 3: ++z1; side = ForgeDirection.NORTH; break;
		case 4: --x1; side = ForgeDirection.EAST; break;
		case 5: ++x1; side = ForgeDirection.WEST; break;
		}
		if (!world.isBlockSolidOnSide(x1, y1, z1, side)) {
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		blockIcon = register.registerIcon(ModInfo.ID + ":" + getUnlocalizedName().substring(9));
	}
}

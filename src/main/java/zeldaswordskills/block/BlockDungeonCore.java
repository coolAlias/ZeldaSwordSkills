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

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.Config;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * This block is the core of the dungeon, providing the tile entity that verifies the
 * integrity of the structure and 'disables' indestructible blocks in addition to
 * playing the medley when breached.
 * 
 * Uses the same metadata values as BlockSecretStone:
 * Metadata bits 0x0 to 0x7 designate block texture
 * Metadata bit 0x8 flags whether the block is completely indestructible or not
 *
 */
public class BlockDungeonCore extends BlockContainer
	implements IDungeonBlock, ITileEntityProvider
{
	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public BlockDungeonCore(int id, Material material) {
		super(id, material);
		setHardness(1.5F);
		setResistance(10.0F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}
	
	@Override
	public int getMobilityFlag() { return 2; }

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) { return false; }

	@Override
	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
		return world.getBlockMetadata(x, y, z) < 0x8;
	}

	@Override
	public int idDropped(int meta, Random rand, int fortune) {
		return BlockSecretStone.getIdFromMeta(meta);
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		if (world.getBlockMetadata(x, y, z) < 0x8) {
			return blockHardness;
		} else {
			return -1.0F; // not breakable by tool
		}
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		if (world.getBlockMetadata(x, y, z) < 0x8) {
			return getExplosionResistance(entity);
		} else {
			return BlockWeight.IMPOSSIBLE.weight * 3.0F;
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int oldId, int oldMeta) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonCore) {
			((TileEntityDungeonCore) te).onBlockBroken();
		}
		super.breakBlock(world, x, y, z, oldId, oldMeta);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (entity instanceof EntityPlayer && ((EntityPlayer) entity).isSneaking()) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityDungeonCore) {
				((TileEntityDungeonCore) te).setSpawner();
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityDungeonCore();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTabToDisplayOn() {
		return (Config.isDungeonCoreTabEnabled() ? super.getCreativeTabToDisplayOn() : null);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int id, CreativeTabs tab, List list) {
		for (int i = 0; i < BlockSecretStone.names.length; ++i) {
			list.add(new ItemStack(id, 1, i));
			list.add(new ItemStack(id, 1, i | 0x8));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return iconArray[(meta & ~0x8) % BlockSecretStone.names.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[BlockSecretStone.names.length];
		for (int i = 0; i < BlockSecretStone.names.length; ++i) {
			iconArray[i] = register.registerIcon(BlockSecretStone.names[i]);
		}
	}
}

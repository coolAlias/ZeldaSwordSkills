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

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * The pedestal for the Master Sword is completely unbreakable so long as a sword
 * remains set in the block.
 * 
 * Meta & 0x1 is for the Pendant of Courage
 * Meta & 0x2 is for the Pendant of Power
 * Meta & 0x4 is for the Pendant of Wisdom
 * Meta & 0x8 flags that the sword has been taken
 *
 */
public class BlockPedestal extends BlockContainer
{
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;
	@SideOnly(Side.CLIENT)
	private IIcon[] iconSide;

	public BlockPedestal() {
		super(Material.rock);
		setHardness(1.5F);
		setResistance(15.0F);
		setLightLevel(0.5F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPedestal();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return meta == 0x8;
	}

	@Override
	public float getBlockHardness(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) == 0x8 ? blockHardness : -1.0F);
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return (world.getBlockMetadata(x, y, z) == 0x8 ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}

	// TODO remove when vanilla explosion resistance bug is fixed
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		if (world.getBlockMetadata(x, y, z) == 0x8) {
			super.onBlockExploded(world, x, y, z, explosion);
		}
	}

	@Override
	public int damageDropped(int meta) {
		return (meta == 0x8 ? 0x8 : 0x0);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() || !(world.getTileEntity(x, y, z) instanceof TileEntityPedestal)) {
			return false;
		}
		if (!world.isRemote) {
			TileEntityPedestal te = (TileEntityPedestal) world.getTileEntity(x, y, z);
			if (!te.hasSword() && player.getHeldItem() != null && WeaponRegistry.INSTANCE.isSword(player.getHeldItem().getItem())) {
				te.setSword(player.getHeldItem(), player);
				player.setCurrentItemOrArmor(0, null);
			} else if (world.getBlockMetadata(x, y, z) == 0x8 && te.hasSword()) {
				te.retrieveSword();
			} else {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_PEDESTAL, world, x, y, z);
			}
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).changeOrientation();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (stack.getItemDamage() == 0x8) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).onBlockPlaced();
			}
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if (meta != 0x8) {
			WorldUtils.dropContainerBlockInventory(world, x, y, z);
		} else if (meta == 0x8) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).retrieveSword();
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityPedestal) {
			return ((TileEntityPedestal) te).getPowerLevel();
		}
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 8));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 ? iconBottom : side == 1 ? iconTop : iconSide[Math.min(meta, 7)]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		iconTop = register.registerIcon(ModInfo.ID + ":pedestal_top");
		iconBottom = register.registerIcon(ModInfo.ID + ":pedestal_bottom");
		iconSide = new IIcon[8];
		for (int i = 0; i < iconSide.length; ++i) {
			iconSide[i] = register.registerIcon(ModInfo.ID + ":pedestal_side_" + i);
		}
	}
}

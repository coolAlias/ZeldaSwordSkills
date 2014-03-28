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

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	private Icon iconTop;
	@SideOnly(Side.CLIENT)
	private Icon iconBottom;
	@SideOnly(Side.CLIENT)
	private Icon[] iconSide;

	public BlockPedestal(int id) {
		super(id, Material.rock);
		setHardness(1.5F);
		setResistance(15.0F);
		setLightValue(0.5F);
		setStepSound(soundStoneFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityPedestal();
	}
	
	@Override
	public boolean renderAsNormalBlock() { return false; }

	@Override
	public boolean isOpaqueCube() { return false; }
	
	@Override
	public int getMobilityFlag() { return 2; }
	
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
	
	@Override
	public int damageDropped(int meta) {
		return (meta == 0x8 ? 0x8 : 0x0);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() || !(world.getBlockTileEntity(x, y, z) instanceof TileEntityPedestal)) {
			return false;
		}
		if (!world.isRemote) {
			TileEntityPedestal te = (TileEntityPedestal) world.getBlockTileEntity(x, y, z);
			if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemSword && !te.hasSword()) {
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
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).changeOrientation();
			}
		}
	}
	
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (stack.getItemDamage() == 0x8) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).onBlockPlaced();
			}
		}
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		if (meta != 0x8) {
			WorldUtils.dropContainerBlockInventory(world, x, y, z);
		} else if (meta == 0x8) {
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).retrieveSword();
			}
		}
		super.breakBlock(world, x, y, z, id, meta);
	}
	
	@Override
	public boolean canProvidePower() { return true; }
	
	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te instanceof TileEntityPedestal) {
			ItemStack sword = ((TileEntityPedestal) te).getSword();
			if (sword != null && sword.getItem() == ZSSItems.swordMaster) {
				return 15;
			}
		}
		return 0;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void getSubBlocks(int id, CreativeTabs tab, List list) {
		list.add(new ItemStack(id, 1, 0));
		list.add(new ItemStack(id, 1, 8));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return (side == 0 ? iconBottom : side == 1 ? iconTop : iconSide[Math.min(meta, 7)]);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconTop = register.registerIcon(ModInfo.ID + ":pedestal_top");
		iconBottom = register.registerIcon(ModInfo.ID + ":pedestal_bottom");
		iconSide = new Icon[8];
		for (int i = 0; i < iconSide.length; ++i) {
			iconSide[i] = register.registerIcon(ModInfo.ID + ":pedestal_side_" + i);
		}
	}
}

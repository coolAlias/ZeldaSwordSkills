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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.client.render.block.RenderTileEntityPedestal;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * The pedestal for the Master Sword is completely unbreakable so long as a sword
 * remains set in the block.
 * 
 * Outputs a redstone signal when any kind of Master Sword is held within.
 *
 */
public class BlockPedestal extends Block implements IBlockItemVariant, ICustomStateMapper, ISpecialRenderer, ITileEntityProvider
{
	public static final int ALL_PENDANTS = 7;
	/**
	 * Bit 1 is for the Pendant of Courage
	 * Bit 2 is for the Pendant of Power
	 * Bit 4 is for the Pendant of Wisdom
	 */
	public static final PropertyInteger PENDANTS = PropertyInteger.create("pendants", 0, ALL_PENDANTS);
	/**
	 * Whether the pedestal is unlocked, i.e. swords may be removed (locked = 0, unlocked = 8)
	 * While locked, the block may not be broken, but pendants may be added and removed at will
	 */
	public static final PropertyBool UNLOCKED = PropertyBool.create("unlocked");

	public BlockPedestal() {
		super(Material.rock);
		setHardness(1.5F);
		setResistance(15.0F);
		setLightLevel(0.5F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
		setDefaultState(blockState.getBaseState().withProperty(PENDANTS, Integer.valueOf(0)).withProperty(UNLOCKED, Boolean.valueOf(false)));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPedestal();
	}

	@Override
	public boolean isFullCube() {
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
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return ((Boolean) world.getBlockState(pos).getValue(UNLOCKED)).booleanValue();
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getBlockHardness(world, pos);
		}
		return (((Boolean) state.getValue(UNLOCKED)).booleanValue() ? blockHardness : -1.0F);
	}

	// TODO remove if Mojang's stupid code ever gets fixed
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (((Boolean) world.getBlockState(pos).getValue(UNLOCKED)).booleanValue()) {
			super.onBlockExploded(world, pos, explosion);
		}
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getExplosionResistance(world, pos, entity, explosion);
		}
		return (((Boolean) state.getValue(UNLOCKED)).booleanValue() ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((Boolean) state.getValue(UNLOCKED)).booleanValue() ? 0x8 : 0x0;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (player.isSneaking() || !(world.getTileEntity(pos) instanceof TileEntityPedestal)) {
			return false;
		}
		if (!world.isRemote) {
			TileEntityPedestal te = (TileEntityPedestal) world.getTileEntity(pos);
			if (!te.hasSword() && player.getHeldItem() != null && WeaponRegistry.INSTANCE.isSword(player.getHeldItem().getItem())) {
				te.setSword(player.getHeldItem(), player);
				player.setCurrentItemOrArmor(0, null);
			} else if (((Boolean) state.getValue(UNLOCKED)).booleanValue() && te.hasSword()) {
				te.retrieveSword();
			} else {
				player.openGui(ZSSMain.instance, GuiHandler.GUI_PEDESTAL, world, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).changeOrientation();
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		if (stack.getItemDamage() == 0x8) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).onBlockPlaced();
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (((Boolean) state.getValue(UNLOCKED)).booleanValue()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityPedestal) {
				((TileEntityPedestal) te).retrieveSword();
			}
		} else {
			WorldUtils.dropContainerBlockInventory(world, pos);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int getWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing face) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityPedestal) {
			return ((TileEntityPedestal) te).getPowerLevel();
		}
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 8));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		Integer pendants = Integer.valueOf((meta & 0x7));
		return getDefaultState().withProperty(PENDANTS, pendants).withProperty(UNLOCKED, Boolean.valueOf(meta > 0x7));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((Integer) state.getValue(PENDANTS)).intValue();
		if (((Boolean)state.getValue(UNLOCKED)).booleanValue()) {
			i |= 0x8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, PENDANTS, UNLOCKED);
	}

	/**
	 * Only two item variants - no pendants and all pendants
	 */
	@Override
	public String[] getItemBlockVariants() {
		return new String[]{ModInfo.ID + ":pedestal_0", ModInfo.ID + ":pedestal_7"};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		// can't use this because meta 8 needs to be remapped to 7:
		// return (new StateMap.Builder()).addPropertiesToIgnore(UNLOCKED).build();
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				String resource = getUnlocalizedName();
				resource = resource.substring(resource.lastIndexOf(".") + 1) + "#" + PENDANTS.getName() + "=";
				int pendants = ((Boolean) state.getValue(UNLOCKED)).booleanValue() ? ALL_PENDANTS : ((Integer) state.getValue(PENDANTS)).intValue();
				return new ModelResourceLocation(ModInfo.ID + ":" + resource + pendants);
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSpecialRenderer() {
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPedestal.class, new RenderTileEntityPedestal());
	}
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.block.tileentity.TileEntityDungeonStone;
import zeldaswordskills.client.ISwapModel;
import zeldaswordskills.client.render.block.ModelDungeonBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ItemDungeonBlock;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * A simple block that renders as nearly any texture and can only be destroyed by explosions
 *
 */
public class BlockDungeonStone extends Block implements ICustomStateMapper, IDungeonBlock, IExplodable, ISmashable, ISwapModel, ITileEntityProvider
{
	public static final PropertyBool UNBREAKABLE = PropertyBool.create("unbreakable");
	/** Stores the block state that will be rendered */
	public static final UnlistedRenderBlock RENDER_BLOCK = new UnlistedRenderBlock();

	public BlockDungeonStone(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(6.0F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setDefaultState(blockState.getBaseState().withProperty(UNBREAKABLE, Boolean.FALSE));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDungeonStone();
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (state.getValue(UNBREAKABLE).booleanValue() ? BlockWeight.IMPOSSIBLE : BlockWeight.VERY_HEAVY);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return Result.DEFAULT;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return !world.getBlockState(pos).getValue(UNBREAKABLE).booleanValue();
	}

	@Override
	public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
		return false;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess World, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getBlock().getMetaFromState(state);
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		IBlockState renderState = ((IExtendedBlockState) state).getValue(RENDER_BLOCK);
		if (renderState != null) {
			int meta = renderState.getBlock().getMetaFromState(renderState);
			drops.add(new ItemStack(renderState.getBlock(), 1, meta));
		}
		return drops;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (Config.showSecretMessage) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.secret");
			}
			world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
	}

	// this may not even be necessary, since these blocks will only ever be placed by a player
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDungeonStone) {
			TileEntityDungeonStone stone = (TileEntityDungeonStone) te;
			if (stone.getRenderState() == null) {
				stone.setRenderState(getDefaultRenderState(((Boolean) state.getValue(UNBREAKABLE)).booleanValue()));
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDungeonStone && stack != null && stack.getItem() instanceof ItemDungeonBlock) {
			IBlockState renderState = ((ItemDungeonBlock) stack.getItem()).getBlockStateFromStack(stack);
			if (renderState.getBlock() instanceof BlockDungeonStone) {
				renderState = ((BlockDungeonStone) renderState.getBlock()).getDefaultRenderState(stack.getItemDamage() > 7);
			}
			((TileEntityDungeonStone) te).setRenderState(renderState);
		}
	}

	// TODO remove if Mojang's stupid code ever gets fixed
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (!world.getBlockState(pos).getValue(UNBREAKABLE).booleanValue()) {
			super.onBlockExploded(world, pos, explosion);
		}
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getExplosionResistance(world, pos, entity, explosion);
		}
		return (state.getValue(UNBREAKABLE).booleanValue() ? BlockWeight.getMaxResistance() : getExplosionResistance(entity));
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		return true; // doesn't matter as this block is never used as a door anyway
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(UNBREAKABLE, Boolean.valueOf((meta & 0x8) > 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(UNBREAKABLE).booleanValue() ? 0x8 : 0x0;
	}

	@Override
	protected BlockState createBlockState() {
		// These actually have to be explicitly called as 'new IProperty[]' - cannot simply be listed as arguments
		return new ExtendedBlockState(this, new IProperty[] {UNBREAKABLE}, new IUnlistedProperty[] {RENDER_BLOCK});
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDungeonStone && state instanceof IExtendedBlockState) {  // avoid crash in case of mismatch
			IExtendedBlockState extended = (IExtendedBlockState) state;
			IBlockState renderState = ((TileEntityDungeonStone) te).getRenderState();
			if (renderState != null) {
				return extended.withProperty(RENDER_BLOCK, renderState);
			}
		}
		return state;
	}

	/**
	 * Return the default render block state for the normal or unbreakable version
	 */
	public IBlockState getDefaultRenderState(boolean isUnbreakable) {
		return Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, isUnbreakable ? BlockStone.EnumType.ANDESITE_SMOOTH : BlockStone.EnumType.ANDESITE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityDungeonStone) {
			return ((TileEntityDungeonStone) te).getRenderState().getBlock().colorMultiplier(world, pos, renderPass);
		}
		return super.colorMultiplier(world, pos, renderPass);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		// CUTOUT_MIPPED allows both grass-like blocks and solids to render perfectly fine
		// TRANSLUCENT works for the above as well as ice, but results in x-ray issues and is more expensive anyway
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 8));
	}

	/**
	 * Always returns the same base texture, since ISmartModel will handle the actual render state
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		return new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return ModelDungeonBlock.resource;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Collection<ModelResourceLocation> getDefaultResources() {
		return Lists.newArrayList(ModelDungeonBlock.resource);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends IBakedModel> getNewModel() {
		return ModelDungeonBlock.class;
	}

	public static class UnlistedRenderBlock implements IUnlistedProperty<IBlockState>
	{
		@Override
		public String getName() {
			return "UnlistedPropertyRenderBlock";
		}

		@Override
		public boolean isValid(IBlockState value) {
			return true;
		}

		@Override
		public Class<IBlockState> getType() {
			return IBlockState.class;
		}

		@Override
		public String valueToString(IBlockState value) {
			return value.toString();
		}
	}
}

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

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.item.ISacredFlame;
import zeldaswordskills.block.tileentity.TileEntitySacredFlame;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Colored flames representing Din (0x1), Farore (0x2), and Nayru (0x4).
 * 
 * Bit 0x8 determines whether the flame has been extinguished or is still active.
 *
 */
public class BlockSacredFlame extends Block implements ITileEntityProvider
{
	public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockSacredFlame.EnumType.class);
	public static final PropertyBool EXTINGUISHED = PropertyBool.create("extinguished");

	public BlockSacredFlame() {
		super(ZSSBlockMaterials.sacredFlame);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setLightLevel(1.0F);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySacredFlame();
	}

	@Override
	public int damageDropped(IBlockState state) {
		return ((BlockSacredFlame.EnumType) state.getValue(VARIANT)).getMetadata();
	}

	@Override
	public boolean isCollidable() {
		return true;
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
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ISacredFlame) {
			BlockSacredFlame.EnumType flame = ((BlockSacredFlame.EnumType) state.getValue(VARIANT));
			boolean isActive = !((Boolean) state.getValue(EXTINGUISHED)).booleanValue();
			if (((ISacredFlame) stack.getItem()).onActivatedSacredFlame(stack, world, player, flame, isActive)) {
				extinguishFlame(world, pos);
				return true;
			}
		} else if (!world.isRemote) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
		}
		return false;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		IBlockState state = world.getBlockState(pos);
		ItemStack stack = player.getHeldItem();
		if (stack != null) {
			BlockSacredFlame.EnumType flame = ((BlockSacredFlame.EnumType) state.getValue(VARIANT));
			boolean isActive = !((Boolean) state.getValue(EXTINGUISHED)).booleanValue();
			if (stack.getItem() instanceof ISacredFlame) {
				if (((ISacredFlame) stack.getItem()).onClickedSacredFlame(stack, world, player, flame, isActive)) {
					extinguishFlame(world, pos);
				}
			} else if (world.isRemote) {
				;
			} else if (stack.getItem() == Items.arrow && isActive) {
				int n = stack.stackSize;
				player.setCurrentItemOrArmor(0, new ItemStack(flame == BlockSacredFlame.EnumType.DIN ? ZSSItems.arrowFire :
					flame == BlockSacredFlame.EnumType.NAYRU ? ZSSItems.arrowIce : ZSSItems.arrowLight, n));
				world.playSoundAtEntity(player, Sounds.SUCCESS_MAGIC, 1.0F, 1.0F);
				if (Config.getArrowsConsumeFlame() && world.rand.nextInt(80) < n) {
					extinguishFlame(world, pos);
				}
			} else if (stack.getItem() == ZSSItems.crystalSpirit && isActive) {
				switch(flame) {
				case DIN: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalDin)); break;
				case FARORE: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalFarore)); break;
				case NAYRU: player.setCurrentItemOrArmor(0, new ItemStack(ZSSItems.crystalNayru)); break;
				}
				world.playSoundAtEntity(player, Sounds.FLAME_ABSORB, 1.0F, 1.0F);
				extinguishFlame(world, pos);
			} else if (isActive) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.random");
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.sacred_flame.inactive");
			}
		}
	}

	/**
	 * Extinguishes the flames at this location, setting the block to air if flames are not renewable
	 */
	protected void extinguishFlame(World world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (Config.getSacredFlameRefreshRate() > 0 && te instanceof TileEntitySacredFlame) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(EXTINGUISHED, Boolean.valueOf(true)), 3);
			((TileEntitySacredFlame) te).extinguish();
		} else {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return world.isSideSolid(pos.down(), EnumFacing.UP);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockSacredFlame.EnumType flame : BlockSacredFlame.EnumType.values()) {
			list.add(new ItemStack(item, 1, flame.getMetadata()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		BlockSacredFlame.EnumType type = BlockSacredFlame.EnumType.byMetadata(meta);
		return getDefaultState().withProperty(VARIANT, type).withProperty(EXTINGUISHED, Boolean.valueOf((meta & 0x8) > 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((BlockSacredFlame.EnumType) state.getValue(VARIANT)).getMetadata();
		if (((Boolean) state.getValue(EXTINGUISHED)).booleanValue()) {
			i |= 0x8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT, EXTINGUISHED);
	}

	public static enum EnumType implements IStringSerializable {
		DIN("din", 0, 1),
		FARORE("farore", 1, 2),
		NAYRU("nayru", 2, 4);
		private final String unlocalizedName;
		private final int meta;
		private final int bit;
		private EnumType(String name, int meta, int bit) {
			this.unlocalizedName = name;
			this.meta = meta;
			this.bit = bit;
		}

		@Override
		public String getName() {
			return this.unlocalizedName;
		}

		public int getMetadata() {
			return this.meta;
		}

		public static EnumType byMetadata(int meta) {
			return EnumType.values()[meta & 3];
		}

		/**
		 * Returns the flame's bit value; used for storage such as in the Golden Sword
		 */
		public int getBit() {
			return bit;
		}
	}
}

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
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.EntityInvulnerableItem;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BlockRotationData;
import zeldaswordskills.util.PlayerUtils;

public class BlockAncientTablet extends Block implements IBlockItemVariant, ICustomStateMapper, IVanillaRotation, IWhipBlock
{
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockAncientTablet.EnumType.class);

	public BlockAncientTablet(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setBlockBounds(0.125F, 0.0F, 0.375F, 0.875F, 0.9375F, 0.625F);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.SOUTH).withProperty(VARIANT, BlockAncientTablet.EnumType.BOMBOS));
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return (face.getAxis().isHorizontal());
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		return Result.DEFAULT;
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
	public int damageDropped(IBlockState state) {
		// item version has damage values 0-2 based on variant ordinal
		return ((BlockAncientTablet.EnumType) state.getValue(VARIANT)).ordinal();
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		if (world.getLightFromNeighbors(pos) < 8) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.dark");
		} else if (player.getHeldItem() == null || player.getHeldItem().getItem() != ZSSItems.bookMudora) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.unknown");
		} else if (!PlayerUtils.hasMasterSword(player)) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.text." + ((EnumType) state.getValue(VARIANT)).getName());
		} else {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.text." + ((EnumType) state.getValue(VARIANT)).getName());
			for (int i = 1; i < 6; ++i) {
				if (!world.isAirBlock(pos.up(i))) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.ancient_tablet.confined");
					return false; // must be at least 5 blocks clear for lightning and item spawn
				}
			}
			world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ()));
			world.scheduleBlockUpdate(pos, this, 5, 1);
			return true;
		}
		return false;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing face, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumFacing facing = EnumFacing.fromAngle(placer.rotationYaw);
		meta = (meta < 4 ? (meta << 2) : meta); // item damage uses type ordinal, i.e. 0-2
		return super.onBlockPlaced(world, pos, face, hitX, hitY, hitZ, meta, placer).withProperty(FACING, facing);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		int i = ((BlockAncientTablet.EnumType) state.getValue(VARIANT)).ordinal();
		EntityItem item = new EntityInvulnerableItem(world, pos.getX() + 0.5D, pos.getY() + 5.5D, pos.getZ() + 0.5D, new ItemStack(ZSSItems.medallion, 1, i));
		item.setDefaultPickupDelay();
		world.spawnEntityInWorld(item);
		world.destroyBlock(pos, false);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		if (((EnumFacing) world.getBlockState(pos).getValue(FACING)).getAxis() == Axis.X) {
			setBlockBounds(0.375F, 0.0F, 0.125F, 0.625F, 0.9375F, 0.875F);
		} else {
			setBlockBounds(0.125F, 0.0F, 0.375F, 0.875F, 0.9375F, 0.625F);
		}
	}

	@Override
	public String[] getItemBlockVariants() {
		String[] variants = new String[BlockAncientTablet.EnumType.values().length];
		for (BlockAncientTablet.EnumType type : BlockAncientTablet.EnumType.values()) {
			variants[type.ordinal()] = ModInfo.ID + ":ancient_tablet_" + type.getName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockAncientTablet.EnumType variant : BlockAncientTablet.EnumType.values()) {
			list.add(new ItemStack(item, 1, variant.ordinal()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, (meta & 1) > 0 ? EnumFacing.EAST : EnumFacing.SOUTH).withProperty(VARIANT, BlockAncientTablet.EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return (((EnumFacing) state.getValue(FACING)).getAxis() == Axis.X ? 1 : 0) + ((EnumType) state.getValue(VARIANT)).getMetadata();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, FACING, VARIANT);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		return (new StateMap.Builder()).ignore(VARIANT).build();
	}

	@Override
	public BlockRotationData.Rotation getRotationPattern() {
		return BlockRotationData.Rotation.WOOD; // bit4 = E/W, bit8 = N/S
	}

	public static enum EnumType implements IStringSerializable {
		BOMBOS(0, "bombos", -1),
		ETHER(4, "ether", 30),
		QUAKE(8, "quake", 30);
		private final int meta;
		private final String name;
		private final int itemUseDuration;

		private EnumType(int meta, String name, int maxUseDuration) {
			this.meta = meta;
			this.name = name;
			this.itemUseDuration = maxUseDuration;
		}

		public int getMetadata() {
			return this.meta;
		}

		@Override
		public String getName() {
			return this.name;
		}

		public String getDisplayName() {
			return StatCollector.translateToLocal("zss." + getName() + ".name");
		}

		/**
		 * Number of ticks medallion must be used before effect occurs
		 * @return -1 if the medallion may not be used, 0 if the effect is instant, or the duration required
		 */
		public int getItemUseDuration() {
			return itemUseDuration;
		}

		/**
		 * Return block variant by metadata (0, 4, 8) or damage (0, 1, 2) value
		 */
		public static EnumType byMetadata(int meta) {
			return EnumType.values()[(meta > 3 ? (meta >> 2) : meta) % EnumType.values().length];
		}
	}
}

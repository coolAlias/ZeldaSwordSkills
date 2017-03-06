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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;

public class BlockTime extends Block implements IBlockItemVariant, IDungeonBlock, ISongBlock
{
	public static final PropertyEnum<BlockTime.EnumType> VARIANT = PropertyEnum.create("variant", BlockTime.EnumType.class);
	/** Whether the block is currently ethereal or not */
	public static final PropertyBool ETHEREAL = PropertyBool.create("ethereal");

	public BlockTime() {
		super(Material.rock);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockTime.EnumType.TIME).withProperty(ETHEREAL, Boolean.FALSE));
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer() {
		return EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean isBlockSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		return !world.getBlockState(pos).getValue(ETHEREAL).booleanValue();
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean isHoldingBoat) {
		return !state.getValue(ETHEREAL).booleanValue();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return (state.getValue(ETHEREAL).booleanValue() ? null : super.getCollisionBoundingBox(world, pos, state));
	}

	@Override
	public boolean isReplaceable(World world, BlockPos pos) {
		return (world.getBlockState(pos).getValue(ETHEREAL).booleanValue() ? true : super.isReplaceable(world, pos));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos).getValue(ETHEREAL).booleanValue()) {
			setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		} else {
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	@Override
	public void setBlockBoundsForItemRender() {
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public boolean onSongPlayed(World world, BlockPos pos, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		if (power > 4) {
			IBlockState state = world.getBlockState(pos);
			if (song == state.getValue(VARIANT).getRequiredSong()) {
				world.setBlockState(pos, state.withProperty(ETHEREAL, !state.getValue(ETHEREAL)), 2);
				if (affected == 0) {
					world.playSoundAtEntity(player, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getItemBlockVariants() {
		String[] variants = new String[BlockTime.EnumType.values().length];
		for (BlockTime.EnumType type : BlockTime.EnumType.values()) {
			variants[type.getMetadata()] = ModInfo.ID + ":" + type.getName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (BlockTime.EnumType variant : BlockTime.EnumType.values()) {
			list.add(new ItemStack(item, 1, variant.getMetadata()));
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		IBlockState expected = getStateFromMeta(meta);
		return state.getValue(VARIANT) == expected.getValue(VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		BlockTime.EnumType type = BlockTime.EnumType.byMetadata(meta);
		return getDefaultState().withProperty(VARIANT, type).withProperty(ETHEREAL, Boolean.valueOf((meta & 0x8) > 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(VARIANT).getMetadata();
		if (state.getValue(ETHEREAL).booleanValue()) {
			i |= 0x8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT, ETHEREAL);
	}

	public static enum EnumType implements IStringSerializable {
		TIME(0, "time_block", ZeldaSongs.songTime),
		ROYAL(1, "royal_block", ZeldaSongs.songZeldasLullaby);
		private final int meta;
		private final String name;
		private final AbstractZeldaSong requiredSong;

		private EnumType(int meta, String name, AbstractZeldaSong requiredSong) {
			this.meta = meta;
			this.name = name;
			this.requiredSong = requiredSong;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns the song required to toggle this block's solid/ethereal state
		 */
		public AbstractZeldaSong getRequiredSong() {
			return requiredSong;
		}

		@Override
		public String getName() {
			return this.name;
		}

		/**
		 * Return block variant by metadata value
		 */
		public static EnumType byMetadata(int meta) {
			return EnumType.values()[(meta & 0x7) % EnumType.values().length];
		}
	}
}

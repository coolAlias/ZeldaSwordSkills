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

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.PlayerUtils;

public class BlockDoorBoss extends BlockDoorLocked
{
	/** The boss type associated with this door */
	public static final PropertyEnum<BlockDoorBoss.EnumType> BOSS_TYPE = PropertyEnum.create("boss_type", BlockDoorBoss.EnumType.class);

	public BlockDoorBoss(Material material) {
		super(material);
	}

	@Override
	protected boolean canUnlock(EntityPlayer player, IBlockState state) {
		int meta = state.getValue(BOSS_TYPE).getMetadata();
		return PlayerUtils.consumeHeldItem(player, ZSSItems.keyBig, meta, 1) || PlayerUtils.consumeHeldItem(player, ZSSItems.keySkeleton, 0, 1);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
		return new ItemStack(this, 1, world.getBlockState(pos).getValue(BOSS_TYPE).getMetadata());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (BlockDoorBoss.EnumType variant : BlockDoorBoss.EnumType.values()) {
			list.add(new ItemStack(item, 1, variant.getMetadata()));
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		if (state.getBlock() != this) {
			return false;
		}
		return state.getValue(BOSS_TYPE) == BlockDoorBoss.EnumType.byMetadata(meta);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return super.getStateFromMeta(meta).withProperty(BOSS_TYPE, BlockDoorBoss.EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = state.getValue(BOSS_TYPE).getMetadata();
		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			i |= 8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, BOSS_TYPE, HALF);
	}

	public static enum EnumType implements IStringSerializable {
		DESERT("temple_desert", 1),
		EARTH("temple_earth", 6),
		FIRE("temple_fire", 0),
		FOREST("temple_forest", 2),
		ICE("temple_ice", 3),
		WATER("temple_water", 4),
		WIND("temple_wind", 5);
		private final String templeName;
		private final int meta;
		private static final EnumType[] META_LOOKUP = new EnumType[EnumType.values().length];
		private EnumType(String templeName, int meta) {
			this.templeName = templeName;
			this.meta = meta;
		}

		/**
		 * Returns the unlocalized name of the associated temple, e.g. "temple_fire"
		 */
		@Override
		public String getName() {
			return this.templeName;
		}

		/**
		 * Returns the metadata value belonging to a {key : door : temple} trio
		 */
		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns door temple type by metadata value
		 */
		public static EnumType byMetadata(int meta) {
			meta &= 0x7;
			return (meta > -1 && meta < EnumType.META_LOOKUP.length ? EnumType.META_LOOKUP[meta] : EnumType.DESERT);
		}

		static {
			for (EnumType type : EnumType.values()) {
				EnumType.META_LOOKUP[type.meta] = type; 
			}
		}
	}
}

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
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;

public class BlockHeavy extends Block implements IBlockItemVariant, IDungeonBlock, ILiftable, ISmashable
{
	public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockHeavy.EnumType.class);

	/**
	 * An indestructible block that can only be moved with special items
	 */
	public BlockHeavy(Material material) {
		super(material);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockHeavy.EnumType.LIGHT));
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return ((BlockHeavy.EnumType) state.getValue(VARIANT)).getWeight();
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state) {
		BlockWeight weight = ((BlockHeavy.EnumType) state.getValue(VARIANT)).getWeight();
		if (weight.compareTo(BlockWeight.LIGHT) > 0) { // i.e. at least MEDIUM
			player.triggerAchievement(ZSSAchievements.movingBlocks);
			if (weight.compareTo(BlockWeight.HEAVY) > 0) { // i.e. at least VERY HEAVY
				player.triggerAchievement(ZSSAchievements.heavyLifter);
			}
		}
	}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, BlockPos pos, IBlockState state) {}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		BlockWeight weight = ((BlockHeavy.EnumType) state.getValue(VARIANT)).getWeight();
		return (stack.getItem() == ZSSItems.hammerMegaton && PlayerUtils.hasItem(player, ZSSItems.gauntletsGolden) ? weight : weight.next());
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return Result.DEFAULT;
	}

	@Override
	public String[] getItemBlockVariants() {
		String[] variants = new String[BlockHeavy.EnumType.values().length];
		for (BlockHeavy.EnumType type : BlockHeavy.EnumType.values()) {
			variants[type.getMetadata()] = ModInfo.ID + ":" + type.getName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockHeavy.EnumType variant : BlockHeavy.EnumType.values()) {
			list.add(new ItemStack(item, 1, variant.getMetadata()));
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		IBlockState expected = getStateFromMeta(meta);
		return ((BlockHeavy.EnumType) state.getValue(VARIANT)) == ((BlockHeavy.EnumType) expected.getValue(VARIANT));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(VARIANT, BlockHeavy.EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumType) state.getValue(VARIANT)).getMetadata();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT);
	}

	public static enum EnumType implements IStringSerializable {
		LIGHT(0, "barrier_light", BlockWeight.MEDIUM),
		HEAVY(1, "barrier_heavy", BlockWeight.VERY_HEAVY);
		private final int meta;
		private final String name;
		private final BlockWeight weight;

		private EnumType(int meta, String name, BlockWeight weight) {
			this.meta = meta;
			this.name = name;
			this.weight = weight;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns the 'weight' of the block for lifting and smashing
		 */
		public BlockWeight getWeight() {
			return weight;
		}

		@Override
		public String getName() {
			return this.name;
		}

		/**
		 * Return block variant by metadata value
		 */
		public static EnumType byMetadata(int meta) {
			return EnumType.values()[meta % EnumType.values().length];
		}
	}
}

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
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

public class BlockQuakeStone extends Block implements IBlockItemVariant, IDungeonBlock, IQuakeBlock, ISmashable
{
	public static final PropertyEnum<BlockQuakeStone.EnumType> VARIANT = PropertyEnum.create("variant", BlockQuakeStone.EnumType.class);

	public BlockQuakeStone() {
		super(ZSSBlockMaterials.adventureStone);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockQuakeStone.EnumType.COBBLE));
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public void handleQuakeEffect(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		world.destroyBlock(pos, true);
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (Config.allowMegaSmashQuakeStone() && PlayerUtils.hasItem(player, ZSSItems.gauntletsGolden) ? BlockWeight.VERY_HEAVY : BlockWeight.IMPOSSIBLE);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return Result.DEFAULT;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(state.getValue(VARIANT).getDroppedBlock());
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (!world.isRemote) {
				world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
			} else if (Config.showSecretMessage) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.secret");
			}
		}
	}

	@Override
	public String[] getItemBlockVariants() {
		String[] variants = new String[BlockQuakeStone.EnumType.values().length];
		for (BlockQuakeStone.EnumType type : BlockQuakeStone.EnumType.values()) {
			variants[type.getMetadata()] = ModInfo.ID + ":quake_stone_" + type.getName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (BlockQuakeStone.EnumType type : BlockQuakeStone.EnumType.values()) {
			list.add(new ItemStack(item, 1, type.getMetadata()));
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		IBlockState expected = getStateFromMeta(meta);
		return state.getValue(VARIANT) == expected.getValue(VARIANT);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(VARIANT, BlockQuakeStone.EnumType.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT);
	}

	public static enum EnumType implements IStringSerializable
	{
		COBBLE(0, "cobble"),
		MOSSY(1, "mossy");
		private final int meta;
		private final String unlocalizedName;

		private EnumType(int meta, String unlocalizedName) {
			this.meta = meta;
			this.unlocalizedName = unlocalizedName;
		}

		@Override
		public String toString() {
			return this.unlocalizedName;
		}

		@Override
		public String getName() {
			return this.unlocalizedName;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns the block dropped when this type is broken
		 */
		public Block getDroppedBlock() {
			switch (this) {
			case MOSSY: return Blocks.mossy_cobblestone;
			default: return Blocks.cobblestone;
			}
		}

		public static BlockQuakeStone.EnumType byMetadata(int meta) {
			return EnumType.values()[Math.max(0, meta) % EnumType.values().length];
		}
	}
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * A block that can either only be destroyed by explosions, or can not be destroyed at all
 *
 */
public class BlockSecretStone extends Block implements IBlockItemVariant, ICustomStateMapper, IDungeonBlock, IExplodable, ILiftable, ISmashable
{
	public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockSecretStone.EnumType.class);
	public static final PropertyBool UNBREAKABLE = PropertyBool.create("unbreakable");

	public BlockSecretStone(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(6.0F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, BlockSecretStone.EnumType.STONE).withProperty(UNBREAKABLE, Boolean.FALSE));
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() || !Config.canLiftSecretStone() ? BlockWeight.IMPOSSIBLE : null);
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag != null) {
			Block block = ((EnumType) state.getValue(VARIANT)).getDroppedBlock();
			tag.setInteger("blockId", Block.getIdFromBlock(block));
			tag.setInteger("metadata", 0);
		}
	}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, BlockPos pos, IBlockState state) {}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? BlockWeight.IMPOSSIBLE : BlockWeight.VERY_HEAVY);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return Result.DEFAULT;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return !((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue();
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
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(((EnumType) state.getValue(VARIANT)).getDroppedBlock());
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World) world).rand : RANDOM;
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		drops.add(new ItemStack(getItemDropped(state, rand, fortune)));
		return drops;
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

	// TODO remove if Mojang's stupid code ever gets fixed
	@Override
	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (!((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue()) {
			super.onBlockExploded(world, pos, explosion);
		}
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity entity, Explosion explosion) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getExplosionResistance(world, pos, entity, explosion);
		}
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? BlockWeight.getMaxResistance() : getExplosionResistance(entity));
	}

	@Override
	public String[] getItemBlockVariants() {
		String[] variants = new String[BlockSecretStone.EnumType.values().length];
		for (BlockSecretStone.EnumType type : BlockSecretStone.EnumType.values()) {
			variants[type.getMetadata()] = "minecraft:" + type.getUnlocalizedName();
		}
		return variants;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		BlockSecretStone.EnumType types[] = BlockSecretStone.EnumType.values();
		for (int i = 0; i < types.length; ++i) {
			list.add(new ItemStack(item, 1, types[i].getMetadata()));
			list.add(new ItemStack(item, 1, types[i].getMetadata() | 0x8));
		}
	}

	@Override
	public boolean isSameVariant(World world, BlockPos pos, IBlockState state, int meta) {
		IBlockState expected = getStateFromMeta(meta);
		return ((BlockSecretStone.EnumType) state.getValue(VARIANT)) == ((BlockSecretStone.EnumType) expected.getValue(VARIANT));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		BlockSecretStone.EnumType type = BlockSecretStone.EnumType.byMetadata(meta);
		return getDefaultState().withProperty(VARIANT, type).withProperty(UNBREAKABLE, Boolean.valueOf((meta & 0x8) > 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((EnumType) state.getValue(VARIANT)).getMetadata();
		if (((Boolean) state.getValue(UNBREAKABLE)).booleanValue()) {
			i |= 0x8;
		}
		return i;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, VARIANT, UNBREAKABLE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IStateMapper getCustomStateMap() {
		return (new StateMap.Builder()).addPropertiesToIgnore(UNBREAKABLE).build();
	}

	/**
	 * Helper method returns the variant's dropped block based on metadata
	 */
	public static Block getDroppedBlock(int meta) {
		return ((BlockSecretStone.EnumType) ZSSBlocks.secretStone.getStateFromMeta(meta).getValue(VARIANT)).getDroppedBlock();
	}

	public static enum EnumType implements IStringSerializable {
		STONE(0, 0, "stone"),
		SANDSTONE(1, 1, "sandstone"),
		NETHER_BRICK(2, 6, "nether_brick"),
		STONE_BRICK(3, 5, "stonebrick"),
		MOSSY_COBBLE(4, 3, "mossy_cobblestone"),
		ICE(5, 7, "ice"),
		COBBLESTONE(6, 3, "cobblestone"),
		END_STONE(7, 7, "end_stone");
		private final int meta;
		private final int slab;
		private final String name;
		private final String unlocalizedName;

		private EnumType(int meta, int slab, String name) {
			this(meta, slab, name, name);
		}

		private EnumType(int meta, int slab, String name, String unlocalizedName) {
			this.meta = meta;
			this.slab = slab;
			this.name = name;
			this.unlocalizedName = unlocalizedName;
		}

		public int getMetadata() {
			return this.meta;
		}

		/**
		 * Returns the block dropped when this type is broken
		 */
		public Block getDroppedBlock() {
			switch (meta) {
			case 0: return Blocks.stone;
			case 1: return Blocks.sandstone;
			case 2: return Blocks.nether_brick;
			case 3: return Blocks.stonebrick;
			case 4: return Blocks.mossy_cobblestone;
			case 5: return Blocks.ice;
			case 6: return Blocks.cobblestone; // return Blocks.quartz_block;
			case 7: return Blocks.end_stone;
			default: return Blocks.stone;
			}
		}

		/**
		 * Returns the slab of the appropriate type with default position
		 */
		public IBlockState getSlab() {
			return Blocks.stone_slab.getStateFromMeta(slab);
		}

		/**
		 * Returns the stair block to use for this type
		 */
		public Block getStairBlock() {
			switch (meta) {
			case 0:
			case 3: return Blocks.stone_brick_stairs;
			case 1: return Blocks.sandstone_stairs;
			case 2: return Blocks.nether_brick_stairs;
			case 5:
			case 7: return Blocks.quartz_stairs;
			default: return Blocks.stone_stairs;
			}
		}

		@Override
		public String toString() {
			return this.name;
		}

		/**
		 * Returns the EnumType associated with this metadata (bit8 is ignored)
		 */
		public static EnumType byMetadata(int meta) {
			return EnumType.values()[meta & 0x7];
		}

		@Override
		public String getName() {
			return this.name;
		}

		public String getUnlocalizedName() {
			return this.unlocalizedName;
		}
	}
}

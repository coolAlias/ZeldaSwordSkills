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
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.StringUtils;
import zeldaswordskills.util.TimedChatDialogue;

public class BlockGossipStone extends Block implements IHookable, ILiftable, ISmashable, ISongBlock
{
	public static final PropertyBool UNBREAKABLE = PropertyBool.create("unbreakable");

	public BlockGossipStone() {
		super(ZSSBlockMaterials.adventureStone);
		setHardness(10.0F);
		setHarvestLevel("pickaxe", 2);
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setDefaultState(blockState.getBaseState().withProperty(UNBREAKABLE, Boolean.FALSE));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityGossipStone();
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Result.DENY;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Result.DEFAULT;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Material.rock;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? BlockWeight.IMPOSSIBLE : BlockWeight.MEDIUM);
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityGossipStone) {
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			String msg = ((TileEntityGossipStone) te).getMessage();
			stack.getTagCompound().setString("TegsMessage", msg);
		}
	}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, BlockPos pos, IBlockState state) {
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("TegsMessage")) {
			ZSSMain.logger.warn("Held GossipBlock stack had an invalid NBT tag: unable to set message.");
			return;
		}
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityGossipStone) {
			String msg = stack.getTagCompound().getString("TegsMessage");
			((TileEntityGossipStone) te).setMessage(msg);
		}
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? BlockWeight.IMPOSSIBLE : BlockWeight.VERY_HEAVY);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return (((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue() ? Result.DENY : Result.DEFAULT);
	}

	@Override
	public boolean onSongPlayed(World world, BlockPos pos, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		TileEntity te = world.getTileEntity(pos);
		return (te instanceof TileEntityGossipStone && ((TileEntityGossipStone) te).onSongPlayed(player, song, power, affected));
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getBlock().getMetaFromState(state); // allows pickBlock to work, and unbreakable version won't be dropping anyway
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
		return !((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue();
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
	public float getBlockHardness(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) {
			return state.getBlock().getBlockHardness(world, pos);
		}
		return (((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? -1.0F : blockHardness);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		TileEntity te = world.getTileEntity(pos);
		if (!world.isRemote && te instanceof TileEntityGossipStone) {
			ItemStack helm = player.getEquipmentInSlot(ArmorIndex.EQUIPPED_HELM);
			if (helm != null && helm.getItem() == ZSSItems.maskTruth) {
				String msg = ((TileEntityGossipStone) te).getMessage();
				if (msg.startsWith("chat.")) {
					PlayerUtils.sendTranslatedChat(player, msg);
				} else {
					String messages[] = StringUtils.wrapString(msg, TileEntityGossipStone.LINE_LENGTH, 5);
					new TimedChatDialogue(player, messages);
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.gossip_stone.silent");
			}
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (world.isRemote) {
			return;
		}
		if (PlayerUtils.isSword(player.getHeldItem())) {
			long time = world.getWorldTime();
			long days = (time / 24000L);
			long current = (time + 6000L) % 24000L; // 0 is 6:00 am, 18000 is midnight, so add 6000
			int h = (int)(current / 1000L);
			int m = (int)((current % 1000L) * 3 / 50); // 1000 ticks divided by 60 minutes = 16 and 2/3
			PlayerUtils.sendTranslatedChat(player, "chat.zss.block.gossip_stone.time", String.format("%02d", h), String.format("%02d", m), days);
		} else if (((Boolean) world.getBlockState(pos).getValue(UNBREAKABLE)).booleanValue() && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(UNBREAKABLE, Boolean.valueOf((meta & 0x8) > 0));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((Boolean) state.getValue(UNBREAKABLE)).booleanValue() ? 0x8 : 0x0;
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, UNBREAKABLE);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 8));
	}
}

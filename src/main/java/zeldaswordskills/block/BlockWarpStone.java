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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.PlayerUtils;

public class BlockWarpStone extends Block implements ILiftable, ISmashable
{
	public static final PropertyEnum WARP_SONG = PropertyEnum.create("warp_song", BlockWarpStone.EnumWarpSong.class);

	public BlockWarpStone() {
		super(Material.rock);
		setHardness(50.0F);
		setResistance(2000.0F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getBlock().getMetaFromState(state);
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return BlockWeight.IMPOSSIBLE;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		return Result.DENY;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return BlockWeight.IMPOSSIBLE;
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state) {}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, BlockPos pos, IBlockState state) {}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemInstrument) {
			BlockWarpStone.EnumWarpSong warpSong = (BlockWarpStone.EnumWarpSong) state.getValue(WARP_SONG);
			AbstractZeldaSong song = warpSong.getWarpSong();
			ZSSPlayerSongs songs = ZSSPlayerSongs.get(player);
			if (!world.isRemote) {
				if (song != null) {// && songs.isSongKnown(song)) { // otherwise have to click again after learning the song
					songs.onActivatedWarpStone(pos, warpSong);
					PlayerUtils.sendFormattedChat(player, "chat.zss.block.warp_stone.activate", new ChatComponentTranslation(song.getTranslationString()), pos.getX(), pos.getY(), pos.getZ());
				}
			} else if (!player.isSneaking()) {
				if (song != null) {
					songs.songToLearn = song;
					player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, pos.getX(), pos.getY(), pos.getZ());
				} else {
					ZSSMain.logger.warn(String.format("Warp stone at %d/%d/%d had invalid metadata: did not return a song!", pos.getX(), pos.getY(), pos.getZ()));
				}
			}
			return true;
		} else {
			// TODO play failure sound
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (BlockWarpStone.EnumWarpSong song : BlockWarpStone.EnumWarpSong.values()) {
			list.add(new ItemStack(item, 1, song.getMetadata()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(WARP_SONG, BlockWarpStone.EnumWarpSong.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumWarpSong) state.getValue(WARP_SONG)).getMetadata();
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, WARP_SONG);
	}

	public static enum EnumWarpSong implements IStringSerializable {
		FOREST(0, ZeldaSongs.songWarpForest),
		FIRE(1, ZeldaSongs.songWarpFire),
		WATER(2, ZeldaSongs.songWarpWater),
		SPIRIT(3, ZeldaSongs.songWarpSpirit),
		SHADOW(4, ZeldaSongs.songWarpShadow),
		LIGHT(5, ZeldaSongs.songWarpLight),
		ORDER(6, ZeldaSongs.songWarpOrder);
		private final int meta;
		private final AbstractZeldaSong song;
		private EnumWarpSong(int meta, AbstractZeldaSong song) {
			this.meta = meta;
			this.song = song;
		}

		@Override
		public String getName() {
			return song.getUnlocalizedName();
		}

		@Override
		public String toString() {
			return song.getDisplayName();
		}

		public int getMetadata() {
			return this.meta;
		}

		public AbstractZeldaSong getWarpSong() {
			return this.song;
		}

		public static EnumWarpSong byMetadata(int meta) {
			return EnumWarpSong.values()[meta & EnumWarpSong.values().length];
		}

		/**
		 * Return the EnumWarpSong for the given song, or null if not found
		 */
		public static EnumWarpSong bySong(AbstractZeldaSong song) {
			for (EnumWarpSong type : EnumWarpSong.values()) {
				if (type.getWarpSong() == song) {
					return type;
				}
			}
			return null;
		}
	}
}

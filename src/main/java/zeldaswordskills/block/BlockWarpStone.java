/**
    Copyright (C) <2014> <coolAlias>

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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWarpStone extends Block implements ILiftable, ISmashable
{
	public static final Map<Integer, ZeldaSong> warpBlockSongs = new HashMap<Integer, ZeldaSong>();
	public static final Map<ZeldaSong, Integer> reverseLookup = new EnumMap<ZeldaSong, Integer>(ZeldaSong.class);

	public BlockWarpStone() {
		super(Material.rock);
		setHardness(50.0F);
		setResistance(2000.0F);
		setStepSound(soundTypeStone);
		setBlockTextureName(ModInfo.ID + ":warp_stone");
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return false;
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float dropChance, int fortune) {}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta) {
		return BlockWeight.IMPOSSIBLE;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		return Result.DENY;
	}

	@Override
	public BlockWeight getLiftWeight(EntityPlayer player, ItemStack stack, int meta) {
		return BlockWeight.IMPOSSIBLE;
	}

	@Override
	public void onLifted(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int meta) {}

	@Override
	public void onHeldBlockPlaced(World world, ItemStack stack, int x, int y, int z, int meta) {}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		int meta = world.getBlockMetadata(x, y, z);
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemInstrument) {
			ZeldaSong song = warpBlockSongs.get(meta);
			ZSSPlayerSongs songs = ZSSPlayerSongs.get(player);
			if (!world.isRemote) {
				if (song != null) {// && songs.isSongKnown(song)) { // otherwise have to click again after learning the song
					songs.onActivatedWarpStone(x, y, z, meta);
					PlayerUtils.sendChat(player, StatCollector.translateToLocalFormatted("chat.zss.block.warp_stone.activate", song.toString(), x, y, z));
				}
			} else if (!player.isSneaking()) {
				if (song != null) {
					songs.songToLearn = song;
					player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, x, y, z);
				} else {
					LogHelper.warning("Warp stone at " + x + "/" + y + "/" + z + " had invalid metadata: did not return a song!");
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
		for (int i = 0; i < warpBlockSongs.size(); ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	private static void addSongMapping(int meta, ZeldaSong song) {
		warpBlockSongs.put(meta, song);
		reverseLookup.put(song, meta);
	}

	static {
		int i = 0;
		addSongMapping(i++, ZeldaSong.FOREST_MINUET);
		addSongMapping(i++, ZeldaSong.FIRE_BOLERO);
		addSongMapping(i++, ZeldaSong.WATER_SERENADE);
		addSongMapping(i++, ZeldaSong.SPIRIT_REQUIEM);
		addSongMapping(i++, ZeldaSong.SHADOW_NOCTURNE);
		addSongMapping(i++, ZeldaSong.LIGHT_PRELUDE);
		addSongMapping(i++, ZeldaSong.ORDER_OATH);
	}
}

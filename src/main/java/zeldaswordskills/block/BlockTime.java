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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockTime extends Block implements IDungeonBlock, ISongBlock
{
	/** Unlocalized names of all song blocks */
	public static final String[] names = {"time_block","royal_block"};

	/** Song required to manipulate each block, based on metadata less bit8 */
	private static final Map<Integer, AbstractZeldaSong> requiredSongs = new HashMap<Integer, AbstractZeldaSong>();

	@SideOnly(Side.CLIENT)
	private Icon[] iconEnd;

	@SideOnly(Side.CLIENT)
	private Icon[] iconFace;

	public BlockTime(int id) {
		super(id, Material.rock);
		disableStats();
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundStoneFootstep);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canCollideCheck(int meta, boolean isHoldingBoat) {
		return meta < 0x8;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) > 0x7 ? null : super.getCollisionBoundingBoxFromPool(world, x, y, z));
	}

	@Override
	public boolean isBlockReplaceable(World world, int x, int y, int z) {
		return (world.getBlockMetadata(x, y, z) > 0x7 ? true : super.isBlockReplaceable(world, x, y, z));
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		if (world.getBlockMetadata(x, y, z) > 0x7) {
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
	public boolean onSongPlayed(World world, int x, int y, int z, EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		if (power > 4) {
			int meta = world.getBlockMetadata(x, y, z);
			if (song == requiredSongs.get((meta & ~0x8))) {
				world.setBlockMetadataWithNotify(x, y, z, (meta < 0x8 ? (meta | 0x8) : (meta & ~0x8)), 2);
				if (affected == 0) {
					world.playSoundAtEntity(player, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int item, CreativeTabs tab, List list) {
		for (int i = 0; i < names.length; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		int i = (meta & ~0x8);
		return (side < 2 ? iconEnd[i] : iconFace[i]);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconFace = new Icon[names.length];
		iconEnd = new Icon[names.length];
		for (int i = 0; i < names.length; ++i) {
			iconEnd[i] = register.registerIcon(ModInfo.ID + ":" + names[i]);
			iconFace[i] = register.registerIcon(ModInfo.ID + ":" + names[i] + "_face");
		}
	}

	static {
		requiredSongs.put(0, ZeldaSongs.songTime);
		requiredSongs.put(1, ZeldaSongs.songZeldasLullaby);
	}
}

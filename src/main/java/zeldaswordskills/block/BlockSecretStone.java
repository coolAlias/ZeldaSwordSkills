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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A block that can either only be destroyed by explosions, or can not be destroyed at all
 * 
 * Metadata bits 0x0 to 0x7 designate block texture
 * Metadata bit 0x8 flags whether the block is completely indestructible or not
 *
 */
public class BlockSecretStone extends Block implements IDungeonBlock, IExplodable, ISmashable
{
	/** List of all currently available secret blocks */
	public static final String[] names = {"stone","sandstone_normal","nether_brick","stonebrick","cobblestone_mossy","ice","cobblestone","end_stone"}; // 6 = "quartz_block_chiseled"

	/** Slab metadata values associated with each stone type */
	private static final int[] slabs = {0,1,6,5,3,7,3,7}; // [6] = 7

	@SideOnly(Side.CLIENT)
	private IIcon[] iconArray;

	public BlockSecretStone(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(6.0F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta) {
		return (meta < 0x8 ? BlockWeight.VERY_HEAVY : BlockWeight.IMPOSSIBLE);
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		return Result.DEFAULT;
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return world.getBlockMetadata(x, y, z) < 0x8;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return false;
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return Item.getItemFromBlock(getBlockFromMeta(meta));
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (!world.isRemote) {
				world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
			} else if (Config.showSecretMessage()) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.secret");
			}
		}
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		// TODO BUG from vanilla Entity.getExplosionResistance passes (x, x, y) as position parameters
		return (world.getBlockMetadata(x, y, z) < 0x8 ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}

	// TODO remove when vanilla explosion resistance bug is fixed
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		if (world.getBlockMetadata(x, y, z) < 0x8) {
			super.onBlockExploded(world, x, y, z, explosion);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < names.length; ++i) {
			list.add(new ItemStack(item, 1, i));
			list.add(new ItemStack(item, 1, i | 0x8));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return iconArray[(meta & ~0x8) % names.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		iconArray = new IIcon[names.length];
		for (int i = 0; i < names.length; ++i) {
			iconArray[i] = register.registerIcon(names[i]);
		}
	}

	/**
	 * Returns the block ID associated with the given metadata value; bit8 is ignored
	 */
	public static Block getBlockFromMeta(int meta) {
		switch(meta & ~0x8) {
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
	 * Returns the stair block ID associated with the given metadata value; bit8 is ignored
	 */
	public static Block getStairsFromMeta(int meta) {
		switch(meta & ~0x8) {
		case 0: return Blocks.stone_brick_stairs;
		case 4:
		case 6: return Blocks.stone_stairs;
		case 1: return Blocks.sandstone_stairs;
		case 2: return Blocks.nether_brick_stairs;
		case 3: return Blocks.stone_brick_stairs;
		case 5:
			//case 6:
		case 7: return Blocks.quartz_stairs;
		default: return Blocks.stone_stairs;
		}
	}

	/**
	 * Returns the slab block metadata associated with the given metadata value; bit8 is ignored
	 */
	public static int getSlabTypeFromMeta(int meta) {
		return slabs[meta % 8];
	}
}

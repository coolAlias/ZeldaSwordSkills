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

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.Event.Result;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.Sounds;
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
	public static final String[] names = {"stone","sandstone_normal","nether_brick","stonebrick","cobblestone_mossy","ice","quartz_block_chiseled","end_stone"};

	/** Slab metadata values associated with each stone type */
	private static final int[] slabs = {0,1,6,5,3,7,7,7};

	@SideOnly(Side.CLIENT)
	private Icon[] iconArray;

	public BlockSecretStone(int id, Material material) {
		super(id, material);
		setBlockUnbreakable();
		setResistance(6.0F);
		setStepSound(soundStoneFootstep);
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
	public boolean canEntityDestroy(World world, int x, int y, int z, Entity entity) {
		return world.getBlockMetadata(x, y, z) < 0x8;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z) {
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
	public int idDropped(int meta, Random rand, int fortune) {
		return getIdFromMeta(meta);
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (Config.showSecretMessage()) {
				player.addChatMessage(StatCollector.translateToLocal("chat.zss.block.secret"));
			}
			world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
	}

	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		return (world.getBlockMetadata(x, y, z) < 0x8 ? getExplosionResistance(entity) : BlockWeight.getMaxResistance());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(int id, CreativeTabs tab, List list) {
		for (int i = 0; i < names.length; ++i) {
			list.add(new ItemStack(id, 1, i));
			list.add(new ItemStack(id, 1, i | 0x8));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int meta) {
		return iconArray[(meta & ~0x8) % names.length];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		iconArray = new Icon[names.length];
		for (int i = 0; i < names.length; ++i) {
			iconArray[i] = register.registerIcon(names[i]);
		}
	}

	/**
	 * Returns the block ID associated with the given metadata value; bit8 is ignored
	 */
	public static int getIdFromMeta(int meta) {
		switch(meta & ~0x8) {
		case 0: return Block.stone.blockID;
		case 1: return Block.sandStone.blockID;
		case 2: return Block.netherBrick.blockID;
		case 3: return Block.stoneBrick.blockID;
		case 4: return Block.cobblestoneMossy.blockID;
		case 5: return Block.ice.blockID;
		case 6: return Block.blockNetherQuartz.blockID;
		case 7: return Block.whiteStone.blockID;
		default: return 0;
		}
	}

	/**
	 * Returns the stair block ID associated with the given metadata value; bit8 is ignored
	 */
	public static int getStairIdFromMeta(int meta) {
		switch(meta & ~0x8) {
		case 0: return Block.stairsStoneBrick.blockID;
		case 4: return Block.stairsCobblestone.blockID;
		case 1: return Block.stairsSandStone.blockID;
		case 2: return Block.stairsNetherBrick.blockID;
		case 3: return Block.stairsStoneBrick.blockID;
		case 5:
		case 6:
		case 7: return Block.stairsNetherQuartz.blockID;
		default: return Block.stairsCobblestone.blockID;
		}
	}

	/**
	 * Returns the slab block metadata associated with the given metadata value; bit8 is ignored
	 */
	public static int getSlabTypeFromMeta(int meta) {
		return slabs[meta % 8];
	}
}

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

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.block.tileentity.TileEntityDungeonBlock;
import zeldaswordskills.client.render.block.RenderTileDungeonBlock;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ItemDungeonBlock;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A simple block that renders as nearly any texture and can only be destroyed by explosions
 * 
 * Metadata bit 0x8 flags whether the block is completely indestructible or not
 *
 */
public class BlockDungeonStone extends BlockContainer implements IDungeonBlock, IExplodable, ISmashable
{
	public BlockDungeonStone(Material material) {
		super(material);
		setBlockUnbreakable();
		setResistance(6.0F);
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDungeonBlock();
	}

	@Override
	public int getRenderType() {
		return RenderTileDungeonBlock.renderId;
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta, int side) {
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
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		Block block = null;
		int blockMeta = 0;
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonBlock) {
			block = ((TileEntityDungeonBlock) te).getRenderBlock();
			blockMeta = ((TileEntityDungeonBlock) te).getRenderMetadata();
		}
		if (block != null) {
			drops.add(new ItemStack(block, 1, blockMeta));
		}
		return drops;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemPickaxe) {
			if (Config.showSecretMessage) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.block.secret");
			}
			world.playSoundAtEntity(player, Sounds.ITEM_BREAK, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
	}

	// this may not even be necessary, since these blocks will only ever be placed by a player
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonBlock) {
			TileEntityDungeonBlock stone = (TileEntityDungeonBlock) te;
			if (stone.getRenderBlock() == null) {
				stone.setRenderBlock(BlockSecretStone.getBlockFromMeta(world.getBlockMetadata(x, y, z)), 0);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof TileEntityDungeonBlock && stack != null && stack.getItem() instanceof ItemDungeonBlock) {
			Block block = ((ItemDungeonBlock) stack.getItem()).getBlockFromStack(stack);
			if (block == ZSSBlocks.dungeonStone) {
				block = (stack.getItemDamage() == 0 ? Blocks.stone : Blocks.obsidian);
			} else if (block == ZSSBlocks.dungeonCore) {
				block = (stack.getItemDamage() == 0 ? Blocks.mossy_cobblestone : Blocks.stonebrick);
			}
			int meta = ((ItemDungeonBlock) stack.getItem()).getMetaFromStack(stack);
			((TileEntityDungeonBlock) te).setRenderBlock(block, meta);
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
	public boolean isSameVariant(World world, int x, int y, int z, int expected) {
		return true; // doesn't matter as this block is never used as a door anyway
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0x0));
		list.add(new ItemStack(item, 1, 0x8));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon("stone");
	}
}

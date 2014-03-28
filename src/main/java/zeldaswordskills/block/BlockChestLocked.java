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

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.block.tileentity.TileEntityChestLocked;
import zeldaswordskills.client.render.block.RenderChestLocked;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * A chest which cannot be broken nor its inventory interacted with until it
 * is unlocked with a 'small key.'
 * 
 * While in Creative Mode, right-click to open up a normal GUI for easy
 * manipulation of the locked chest contents.
 * 
 * Normal chests use 0x2-0x5 for orientation, so these do as well
 *
 */
public class BlockChestLocked extends BlockContainer
{
	/** Prevents inventory from dropping when block is changed to a normal chest */
	private static boolean keepInventory;

	public BlockChestLocked(int id) {
		super(id, Material.wood);
		setBlockUnbreakable();
		setResistance(BlockWeight.IMPOSSIBLE.weight);
		setStepSound(soundWoodFootstep);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
	}

	@Override
	public boolean isOpaqueCube() { return false; }

	@Override
	public boolean renderAsNormalBlock() { return false; }
	
	@Override
	public int getMobilityFlag() { return 2; }

	@Override
	public int getRenderType() { return RenderChestLocked.renderId; }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new TileEntityChestLocked();
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) { return false; }

	@Override
	public void breakBlock(World world, int x, int y, int z, int id, int meta) {
		if (!keepInventory) {
			WorldUtils.dropContainerBlockInventory(world, x, y, z);
		}
		super.breakBlock(world, x, y, z, id, meta);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
		if (world.isRemote) {
			return true;
		}
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (!(te instanceof IInventory)) {
			return false;
		}
		if (player.capabilities.isCreativeMode && !player.isSneaking()) {
			player.displayGUIChest((IInventory) te);
			return true;
		} else if (canUnlock(player)) {
			IInventory inv = (IInventory) te;
			int meta = world.getBlockMetadata(x, y, z);
			keepInventory = true;
			world.setBlock(x, y, z, Block.chest.blockID);
			keepInventory = false;
			world.setBlockMetadataWithNotify(x, y, z, meta, 3);
			world.playSoundAtEntity(player, ModInfo.SOUND_LOCK_CHEST, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));

			// copy the old inventory to the new chest
			TileEntity chest = world.getBlockTileEntity(x, y, z);
			if (chest instanceof TileEntityChest) {
				IInventory inv2 = (IInventory) chest;
				for (int i = 0; i < inv.getSizeInventory() && i < inv2.getSizeInventory(); ++i) {
					inv2.setInventorySlotContents(i, inv.getStackInSlot(i));
				}
			}
			return true;
		} else {
			world.playSoundAtEntity(player, ModInfo.SOUND_LOCK_RATTLE, 0.25F, 1.0F / (world.rand.nextFloat() * 0.4F + 0.5F));
		}
		return false;
	}
	
	private boolean canUnlock(EntityPlayer player) {
		return (player.getHeldItem() != null && (player.getHeldItem().getItem() == ZSSItems.keySkeleton ||
				(player.getHeldItem().getItem() == ZSSItems.keySmall && player.inventory.consumeInventoryItem(ZSSItems.keySmall.itemID))));
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		int l = world.getBlockId(x, y, z - 1);
		int i1 = world.getBlockId(x, y, z + 1);
		int j1 = world.getBlockId(x - 1, y, z);
		int k1 = world.getBlockId(x + 1, y, z);
		int meta = 3;

		if (Block.opaqueCubeLookup[l] && !Block.opaqueCubeLookup[i1]) {
			meta = 3;
		}
		if (Block.opaqueCubeLookup[i1] && !Block.opaqueCubeLookup[l]) {
			meta = 2;
		}
		if (Block.opaqueCubeLookup[j1] && !Block.opaqueCubeLookup[k1]) {
			meta = 5;
		}
		if (Block.opaqueCubeLookup[k1] && !Block.opaqueCubeLookup[j1]) {
			meta = 4;
		}
		
		world.setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	/**
	 * For use during Creative Mode
	 */
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		int face = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		byte meta = (byte)(face == 0 ? 2 : face == 1 ? 5 : face == 2 ? 3 : 4);
		world.setBlockMetadataWithNotify(x, y, z, meta, 3);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister register) {
		blockIcon = register.registerIcon("planks_oak");
	}
}

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

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.client.render.block.RenderCeramicJar;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityCeramicJar;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.SideHit;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.DungeonLootLists;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class BlockCeramicJar extends BlockContainer implements IExplodable, IHookable, ISmashable, IWhipBlock
{
	/** Prevents inventory from dropping when block is picked up */
	private static boolean keepInventory;

	public BlockCeramicJar() {
		super(Material.clay);
		disableStats();
		setBlockUnbreakable();
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.285F, 0.0F, 0.285F, 0.715F, 0.665F, 0.715F);
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.ALLOW;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, int x, int y, int z, int side) {
		return Result.DENY;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, int x, int y, int z) {
		return blockMaterial;
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, int meta) {
		return BlockWeight.VERY_LIGHT;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, int x, int y, int z, int side) {
		WorldUtils.playSoundAt(world, x, y, z, Sounds.BREAK_JAR, 0.4F, 0.5F);
		world.func_147480_a(x, y, z, false);
		return Result.ALLOW;
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, int x, int y, int z, int side) {
		return (side != SideHit.BOTTOM && side != SideHit.TOP);
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, int x, int y, int z, int ticksInGround) {
		if (ticksInGround > 30) {
			EntityLivingBase thrower = whip.getThrower();
			EntityCeramicJar jar = new EntityCeramicJar(world, whip.posX, whip.posY + 1, whip.posZ);
			double dx = thrower.posX - jar.posX;
			double dy = thrower.posY - jar.posY;
			double dz = thrower.posZ - jar.posZ;
			TargetUtils.setEntityHeading(jar, dx, dy, dz, 1.0F, 1.0F, true);
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof IInventory) {
				ItemStack stack = ((IInventory) te).getStackInSlot(0);
				if (stack != null) {
					jar.setStack(stack);
				}
			}
			if (!world.isRemote) {
				world.spawnEntityInWorld(jar);
			}
			keepInventory = true; // don't drop items from breakBlock
			world.setBlockToAir(x, y, z);
			keepInventory = false;
			whip.setDead();
		}
		return Result.DENY;
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int fortune) {
		return null;
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
	public int getRenderType() {
		return RenderCeramicJar.renderId;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCeramicJar();
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta) {
		return false;
	}

	@Override
	public boolean canBlockStay(World world, int x, int y, int z) {
		return world.getBlock(x, y - 1, z).func_149730_j();
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z) {
		return super.canPlaceBlockAt(world, x, y, z) && world.getBlock(x, y - 1, z).func_149730_j();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if (!keepInventory) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof IInventory) {
				IInventory inv = (IInventory) te;
				if (inv.getStackInSlot(0) == null && world.rand.nextFloat() < Config.getJarDropChance()) {
					inv.setInventorySlotContents(0, ChestGenHooks.getInfo(DungeonLootLists.JAR_DROPS).getOneItem(world.rand));
				}
			}
			WorldUtils.dropContainerBlockInventory(world, x, y, z);
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && player.getHeldItem() == null) {
			ItemStack jarStack = new ItemStack(this);
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof IInventory) {
				ItemStack invStack = ((IInventory) te).getStackInSlot(0);
				if (invStack != null) {
					NBTTagCompound item = new NBTTagCompound();
					invStack.writeToNBT(item);
					jarStack.setTagCompound(new NBTTagCompound());
					jarStack.getTagCompound().setTag("jarStack", item);
				}
			}
			player.setCurrentItemOrArmor(0, jarStack);
			keepInventory = true;
			world.setBlockToAir(x, y, z);
			keepInventory = false;
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
		if (!world.isRemote && player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemSword) {
			WorldUtils.playSoundAt(world, x, y, z, Sounds.BREAK_JAR, 0.4F, 0.5F);
			// func_147480_a is destroyBlock
			world.func_147480_a(x, y, z, false);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("jarStack")) {
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof IInventory) {
				ItemStack jarStack = ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("jarStack"));
				((IInventory) te).setInventorySlotContents(0, jarStack);
			}
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		WorldUtils.playSoundAt(world, x, y, z, Sounds.BREAK_JAR, 0.4F, 0.5F);
		world.func_147480_a(x, y, z, false);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (entity instanceof EntityArrow || entity instanceof EntityBoomerang || entity instanceof EntityHookShot) {
			WorldUtils.playSoundAt(world, x, y, z, Sounds.BREAK_JAR, 0.4F, 0.5F);
			// func_147480_a is destroyBlock
			world.func_147480_a(x, y, z, false);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		if (!canBlockStay(world, x, y, z)) {
			WorldUtils.playSoundAt(world, x, y, z, Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.func_147480_a(x, y, z, false);
		}
	}
}

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
import net.minecraft.block.BlockGlass;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.BlockWeight;
import zeldaswordskills.api.block.IExplodable;
import zeldaswordskills.api.block.IHookable;
import zeldaswordskills.api.block.IQuakeBlock;
import zeldaswordskills.api.block.ISmashable;
import zeldaswordskills.api.block.IWhipBlock;
import zeldaswordskills.block.tileentity.TileEntityCeramicJar;
import zeldaswordskills.client.render.block.RenderTileEntityCeramicJar;
import zeldaswordskills.creativetab.ZSSCreativeTabs;
import zeldaswordskills.entity.projectile.EntityBoomerang;
import zeldaswordskills.entity.projectile.EntityCeramicJar;
import zeldaswordskills.entity.projectile.EntityHookShot;
import zeldaswordskills.entity.projectile.EntityWhip;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TargetUtils;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.DungeonLootLists;

public class BlockCeramicJar extends Block implements IExplodable, IHookable, IQuakeBlock, ISmashable, ISpecialRenderer, ITileEntityProvider, IWhipBlock
{
	/** Prevents inventory from dropping when block is picked up */
	private static boolean keepInventory;

	public BlockCeramicJar() {
		super(ZSSBlockMaterials.adventureClay);
		disableStats();
		setBlockUnbreakable();
		setStepSound(soundTypeStone);
		setCreativeTab(ZSSCreativeTabs.tabBlocks);
		setBlockBounds(0.25F, 0.0F, 0.25F, 0.6875F, 0.6875F, 0.6875F);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCeramicJar();
	}

	@Override
	public Result canDestroyBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Result.ALLOW;
	}

	@Override
	public Result canGrabBlock(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Result.DENY;
	}

	@Override
	public Material getHookableMaterial(HookshotType type, World world, BlockPos pos, EnumFacing face) {
		return Material.clay;
	}

	@Override
	public void handleQuakeEffect(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (!world.isRemote) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
	}

	@Override
	public BlockWeight getSmashWeight(EntityPlayer player, ItemStack stack, IBlockState state, EnumFacing face) {
		return BlockWeight.VERY_LIGHT;
	}

	@Override
	public Result onSmashed(World world, EntityPlayer player, ItemStack stack, BlockPos pos, IBlockState state, EnumFacing face) {
		if (!world.isRemote) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
		return Result.ALLOW;
	}

	@Override
	public boolean canBreakBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canGrabBlock(WhipType whip, EntityLivingBase thrower, World world, BlockPos pos, EnumFacing face) {
		return true;
	}

	@Override
	public Result shouldSwing(EntityWhip whip, World world, BlockPos pos, int ticksInGround) {
		if (ticksInGround > 30) {
			EntityLivingBase thrower = whip.getThrower();
			EntityCeramicJar jar = new EntityCeramicJar(world, whip.posX, whip.posY + 1, whip.posZ);
			double dx = thrower.posX - jar.posX;
			double dy = (thrower.posY + thrower.getEyeHeight()) - (jar.posY - 1);
			double dz = thrower.posZ - jar.posZ;
			TargetUtils.setEntityHeading(jar, dx, dy + 0.5D, dz, 1.0F, 0.0F, true);
			TileEntity te = world.getTileEntity(pos);
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
			world.setBlockToAir(pos);
			keepInventory = false;
			whip.setDead();
		}
		return Result.DENY;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public boolean canHarvestBlock(IBlockAccess World, BlockPos pos, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos) && (world.isSideSolid(pos.down(), EnumFacing.UP) || world.getBlockState(pos.down()).getBlock() instanceof BlockGlass);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!keepInventory) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory) {
				IInventory inv = (IInventory) te;
				if (inv.getStackInSlot(0) == null && world.rand.nextFloat() < Config.getJarDropChance()) {
					inv.setInventorySlotContents(0, ChestGenHooks.getInfo(DungeonLootLists.JAR_DROPS).getOneItem(world.rand));
				}
			}
			WorldUtils.dropContainerBlockInventory(world, pos);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && player.getHeldItem() == null) {
			ItemStack jarStack = new ItemStack(this);
			TileEntity te = world.getTileEntity(pos);
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
			world.setBlockToAir(pos);
			keepInventory = false;
		}
		return true;
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote && PlayerUtils.isHoldingWeapon(player)) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("jarStack")) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IInventory) {
				ItemStack jarStack = ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("jarStack"));
				((IInventory) te).setInventorySlotContents(0, jarStack);
			}
		}
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion) {
		if (!world.isRemote) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
		this.onEntityCollidedWithBlock(world, pos, world.getBlockState(pos), entity);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (!world.isRemote && (entity instanceof EntityArrow || entity instanceof EntityBoomerang || entity instanceof EntityHookShot)) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighbor) {
		if (!world.isRemote && !world.isSideSolid(pos.down(), EnumFacing.UP)) {
			WorldUtils.playSoundAt(world, pos.getX(), pos.getY(), pos.getZ(), Sounds.BREAK_JAR, 0.4F, 0.5F);
			world.destroyBlock(pos, false);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSpecialRenderer() {
		if (Config.doJarsUpdate()) { // only need to do special rendering if jars can pick up dropped items
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCeramicJar.class, new RenderTileEntityCeramicJar());
		}
	}
}

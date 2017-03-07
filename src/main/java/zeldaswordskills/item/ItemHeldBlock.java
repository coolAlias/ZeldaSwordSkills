/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.item;

import java.util.Collection;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.item.IDynamicItemBlock;
import zeldaswordskills.api.item.IHandleToss;
import zeldaswordskills.api.item.IUnenchantable;
import zeldaswordskills.client.ISwapModel;
import zeldaswordskills.client.render.item.ModelDynamicItemBlock;

/**
 * 
 * The goal of this class is to represent a single block, picked up and held
 * in the player's hands as though it were carried for real, using NBT to store
 * the block id and metadata.
 * 
 * If at any time the player switches to a different item, it should be placed
 * immediately into the world.
 *
 */
public class ItemHeldBlock extends BaseModItem implements IDynamicItemBlock, IHandleToss, ISwapModel, IUnenchantable
{
	public ItemHeldBlock() {
		super();
		setMaxDamage(0);
		setMaxStackSize(1);
	}

	/**
	 * Returns a new ItemStack containing the passed in block, metadata, and gauntlet stack
	 * NBT format is 'blockId' => integer id of block, 'metadata' => metadata of block, 'gauntlets' => stored gauntlet itemstack
	 */
	public static ItemStack getBlockStack(IBlockState state, ItemStack gauntlets) {
		ItemStack stack = new ItemStack(ZSSItems.heldBlock);
		Block block = state.getBlock();
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("blockId", Block.getIdFromBlock(block));
		stack.getTagCompound().setInteger("metadata", state.getBlock().damageDropped(state));
		if (gauntlets != null) {
			// Hack to allow server to contain the client-side block color information (see HeldBlockColorPacket)
			if (gauntlets.hasTagCompound() && gauntlets.getTagCompound().hasKey("blockColor")) {
				stack.getTagCompound().setInteger("blockColor", gauntlets.getTagCompound().getInteger("blockColor"));
				gauntlets.getTagCompound().removeTag("blockColor");
			}
			stack.getTagCompound().setTag("gauntlets", gauntlets.writeToNBT(new NBTTagCompound()));
		}
		return stack;
	}

	@Override
	public IBlockState getBlockStateFromStack(ItemStack stack) {
		Block block = getBlockFromStack(stack);
		return block.getStateFromMeta(getMetaFromStack(stack));
	}

	/** Returns the stored Block or stone if none available */
	public Block getBlockFromStack(ItemStack stack) {
		Block block = Blocks.stone;
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blockId", Constants.NBT.TAG_INT)) {
			block = Block.getBlockById(stack.getTagCompound().getInteger("blockId"));
		}
		return (block == null ? Blocks.stone : block);
	}

	/** Returns the metadata value associated with the stored block */
	public int getMetaFromStack(ItemStack stack) {
		return (stack.hasTagCompound() ? stack.getTagCompound().getInteger("metadata") : 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int renderPass) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blockColor")) {
			return stack.getTagCompound().getInteger("blockColor");
		}
		return super.getColorFromItemStack(stack, renderPass);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	/**
	 * Drops the held block as close to the entity's position as possible, without
	 * dropping it on the entity
	 * @return true if the dropped block was able to be placed
	 */
	public boolean dropHeldBlock(ItemStack stack, World world, EntityPlayer player) {
		Block block = getBlockFromStack(stack);
		if (block != null) {
			int x = MathHelper.floor_double(player.posX);
			int y = MathHelper.floor_double(player.getEntityBoundingBox().minY);
			int z = MathHelper.floor_double(player.posZ);
			int meta = getMetaFromStack(stack);
			IBlockState state = block.getStateFromMeta(meta);
			Vec3 vec3 = player.getLookVec();
			int dx = Math.abs(vec3.xCoord) < 0.25D ? 0 : (vec3.xCoord < 0 ? -1 : 1);
			int dz = Math.abs(vec3.zCoord) < 0.25D ? 0 : (vec3.zCoord < 0 ? -1 : 1);
			boolean flag = tryDropBlock(stack, world, player, x + dx, y + 1, z + dz, dx, dz, state, meta, 4);
			if (!flag) {
				flag = tryDropBlock(stack, world, player, x, y + 1, z, -dx, -dz, state, meta, 5);
			}
			if (!flag && !block.getMaterial().isSolid()) {
				flag = placeBlockAt(stack, player, world, new BlockPos(x, y, z), EnumFacing.UP, state);
			}
			if (flag) {
				world.playSoundEffect((x + 0.5D), (y + 0.5D), (z + 0.5D), block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
			}
			return flag;
		}
		return false;
	}

	/**
	 * Tries to drop the block at the closest block to x/y/z along the vector dx/dz in a semi-circular pattern
	 * @param n the number of block lengths to check
	 * @return true if the block was placed
	 */
	private boolean tryDropBlock(ItemStack stack, World world, EntityPlayer player, int x, int y, int z, int dx, int dz, IBlockState state, int meta, int n) {
		boolean flag = false;
		for (int i = 0; i < n && !flag; ++i) {
			for (int j = 0; j < (n - i) && !flag; ++ j) {
				for (int k = 0; k < 4 && !flag; ++k) {
					flag = tryPlaceBlock(stack, world, player, new BlockPos(x + (dz * j), y - k, z + (dx * j)), state, meta);
					if (!flag) {
						flag = tryPlaceBlock(stack, world, player, new BlockPos(x - (dz * j), y - k, z - (dx * j)), state, meta);
					}
				}
			}
			x += dx;
			z += dz;
		}
		return flag;
	}

	/**
	 * Returns true if the block was placed at the given position; checks for entity collision and other blocks
	 */
	private boolean tryPlaceBlock(ItemStack stack, World world, EntityPlayer player, BlockPos pos, IBlockState state, int meta) {
		Block block = state.getBlock();
		Block b = world.getBlockState(pos).getBlock();
		if (world.getBlockState(pos.down()).getBlock().isFullBlock() && b.isReplaceable(world, pos)) {
			if (world.canBlockBePlaced(block, pos, false, EnumFacing.UP, null, stack)) {
				state = block.onBlockPlaced(world, pos, EnumFacing.UP, (pos.getX() + 0.5F), (pos.getY() + 0.5F), (pos.getZ() + 0.5F), meta, player);
				if (placeBlockAt(stack, player, world, pos, EnumFacing.UP, state)) {
					world.playSoundEffect((pos.getX() + 0.5D), (pos.getY() + 0.5D), (pos.getZ() + 0.5D), block.stepSound.getBreakSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
		if (isHeld) {
			if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isFlying) {
				entity.motionX *= 0.25D;
				entity.motionZ *= 0.25D;
			}
		} else {
			if (!world.isRemote && entity instanceof EntityPlayer && dropHeldBlock(stack, world, (EntityPlayer) entity)) {
				ItemStack gauntlets = (stack.hasTagCompound() && stack.getTagCompound().hasKey("gauntlets") ?
						ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("gauntlets")) : null);
				((EntityPlayer) entity).inventory.setInventorySlotContents(slot, gauntlets);
			}
		}
	}

	@Override
	public boolean onEntitySwing(EntityLivingBase entity, ItemStack stack) {
		return true;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		IBlockState worldState = world.getBlockState(pos);
		Block worldBlock = worldState.getBlock();
		Block blockToPlace = getBlockFromStack(stack);
		if (worldBlock == Blocks.snow_layer && ((Integer) worldState.getValue(BlockSnow.LAYERS)).intValue() < 1) {
			side = EnumFacing.UP;
		} else if (!worldBlock.isReplaceable(world, pos)) {
			pos = pos.offset(side);
		}
		if (stack.stackSize == 0) {
			return false;
		} else if (!player.canPlayerEdit(pos, side, stack)) {
			return false;
		} else if (pos.getY() == 255 && blockToPlace.getMaterial().isSolid()) {
			return false;
		} else if (world.canBlockBePlaced(blockToPlace, pos, false, side, null, stack)) {
			int meta = getMetaFromStack(stack);
			IBlockState state = blockToPlace.onBlockPlaced(world, pos, side, hitX, hitY, hitZ, meta, player);
			if (!world.isRemote && placeBlockAt(stack, player, world, pos, side, state)) {
				world.playSoundEffect((pos.getX() + 0.5D), (pos.getY() + 0.5D), (pos.getZ() + 0.5D), blockToPlace.stepSound.getBreakSound(), (blockToPlace.stepSound.getVolume() + 1.0F) / 2.0F, blockToPlace.stepSound.getFrequency() * 0.8F);
				ItemStack gauntlets = (stack.hasTagCompound() && stack.getTagCompound().hasKey("gauntlets") ?
						ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("gauntlets")) : null);
				player.setCurrentItemOrArmor(0, gauntlets);
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Copied from ItemBlock (removed hit parameters); places the block after all other checks have been made
	 */
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, IBlockState state) {
		if (!world.setBlockState(pos, state, 3)) {
			return false;
		}
		Block block = world.getBlockState(pos).getBlock();
		if (block == state.getBlock()) {
			ItemBlock.setTileEntityNBT(world, player, pos, stack);
			block.onBlockPlacedBy(world, pos, state, player, stack);
			if (block instanceof ILiftable) {
				((ILiftable) block).onHeldBlockPlaced(world, stack, pos, state);
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		ItemStack block = new ItemStack(getBlockFromStack(stack));
		return (block != null ? block.getUnlocalizedName() : Blocks.stone.getUnlocalizedName());
	}

	@Override
	public void onItemTossed(EntityItem item, EntityPlayer player) {
		ItemStack stack = item.getEntityItem();
		if (dropHeldBlock(stack, player.getEntityWorld(), player)) {
			ItemStack gauntlets = (stack.hasTagCompound() && stack.getTagCompound().hasKey("gauntlets") ?
					ItemStack.loadItemStackFromNBT(stack.getTagCompound().getCompoundTag("gauntlets")) : null);
			player.setCurrentItemOrArmor(0, gauntlets);
			item.setDead();
		}
	}

	/**
	 * Required or smart model will not work
	 */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerResources() {
		ModelLoader.registerItemVariants(this, ModelDynamicItemBlock.resource);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Collection<ModelResourceLocation> getDefaultResources() {
		return Lists.newArrayList(ModelDynamicItemBlock.resource);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<? extends IBakedModel> getNewModel() {
		return ModelDynamicItemBlock.class;
	}
}

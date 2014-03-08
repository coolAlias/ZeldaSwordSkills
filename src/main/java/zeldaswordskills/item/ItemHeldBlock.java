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

package zeldaswordskills.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import zeldaswordskills.api.block.ILiftable;
import zeldaswordskills.api.item.IHandleToss;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
public class ItemHeldBlock extends Item implements IHandleToss {

	public ItemHeldBlock(int id) {
		super(id);
		setMaxDamage(0);
		setMaxStackSize(1);
		setTextureName("stone");
	}

	/**
	 * Returns a new ItemStack containing the passed in block, metadata, and gauntlet stack
	 */
	public static ItemStack getBlockStack(Block block, int metadata, ItemStack gauntlets) {
		ItemStack stack = new ItemStack(ZSSItems.heldBlock);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("blockID", block.blockID);
		stack.getTagCompound().setInteger("metadata", metadata);
		stack.getTagCompound().setTag("gauntlets", gauntlets.writeToNBT(new NBTTagCompound()));
		return stack;
	}

	/**
	 * Returns the stored Block or null if none available
	 */
	public Block getBlockFromStack(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("blockID")) {
			return Block.blocksList[stack.getTagCompound().getInteger("blockID")];
		}
		return null;
	}

	/**
	 * Returns the metadata value associated with the stored block
	 */
	public int getMetaFromStack(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("metadata")) {
			return stack.getTagCompound().getInteger("metadata");
		} else {
			return 0;
		}
	}
	
	/**
	 * Drops the held block as close to the entity's position as possible, without
	 * dropping it on the entity
	 * @return true if the dropped block was able to be placed
	 */
	public boolean dropHeldBlock(ItemStack stack, World world, EntityPlayer player) {
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.boundingBox.minY);
		int z = MathHelper.floor_double(player.posZ);
		int meta = getMetaFromStack(stack);
		Block block = getBlockFromStack(stack);
		if (block != null) {
			Vec3 vec3 = player.getLookVec();
			int dx = Math.abs(vec3.xCoord) < 0.25D ? 0 : (vec3.xCoord < 0 ? -1 : 1);
			int dz = Math.abs(vec3.zCoord) < 0.25D ? 0 : (vec3.zCoord < 0 ? -1 : 1);
			boolean flag = tryDropBlock(stack, world, x + dx, y + 1, z + dz, dx, dz, block, meta, 4);
			if (!flag) {
				flag = tryDropBlock(stack, world, x, y + 1, z, -dx, -dz, block, meta, 5);
			}
			if (!flag && !world.getBlockMaterial(x, y, z).isSolid()) {
				flag = placeBlockAt(stack, player ,world, x, y, z, 1, (float) player.posX, (float) player.posY, (float) player.posZ, block, meta);
			}
			if (flag) {
				world.playSoundEffect((double)(x + 0.5D), (double)(y + 0.5D), (double)(z + 0.5D), block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
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
	private boolean tryDropBlock(ItemStack stack, World world, int x, int y, int z, int dx, int dz, Block block, int meta, int n) {
		boolean flag = false;
		for (int i = 0; i < n && !flag; ++i) {
			for (int j = 0; j < (n - i) && !flag; ++ j) {
				for (int k = 0; k < 4 && !flag; ++k) {
					flag = tryPlaceBlock(stack, world, x + (dz * j), y - k, z + (dx * j), block, meta);
					if (!flag) {
						flag = tryPlaceBlock(stack, world, x - (dz * j), y - k, z - (dx * j), block, meta);
					}
				}
			}
			x += dx;
			z += dz;
		}
		return flag;
	}
	
	/**
	 * Returns true if the block was placed at x/y/z; checks for entity collision and other blocks
	 */
	private boolean tryPlaceBlock(ItemStack stack, World world, int x, int y, int z, Block block, int meta) {
		int i1 = world.getBlockId(x, y, z);
		if (world.isBlockOpaqueCube(x, y - 1, z) && (Block.blocksList[i1] == null || Block.blocksList[i1].isBlockReplaceable(world, x, y, z))) {
			if (world.canPlaceEntityOnSide(block.blockID, x, y, z, false, 1, null, stack)) {
				int placedMeta = block.onBlockPlaced(world, x, y, z, 1, (float)(x + 0.5F), (float)(y + 0.5F), (float)(z + 0.5F), meta);
				if (world.setBlock(x, y, z, block.blockID, placedMeta, 3)) {
					if (world.getBlockId(x, y, z) == block.blockID) {
						block.onPostBlockPlaced(world, x, y, z, placedMeta);
						if (block instanceof ILiftable) {
							((ILiftable) block).onHeldBlockPlaced(world, stack, x, y, z, placedMeta);
						}
					}
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
			if (entity instanceof EntityPlayer && dropHeldBlock(stack, world, (EntityPlayer) entity)) {
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
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
		int i1 = world.getBlockId(x, y, z);
		if (i1 == Block.snow.blockID && (world.getBlockMetadata(x, y, z) & 7) < 1) {
			side = 1;
		} else if (i1 != Block.vine.blockID && i1 != Block.tallGrass.blockID && i1 != Block.deadBush.blockID
				&& (Block.blocksList[i1] == null || !Block.blocksList[i1].isBlockReplaceable(world, x, y, z)))
		{
			switch(side) {
			case 0: --y; break;
			case 1: ++y; break;
			case 2: --z; break;
			case 3: ++z; break;
			case 4: --x; break;
			case 5: ++x; break;
			default:
			}
		}

		Block block = getBlockFromStack(stack);
		if (block == null || stack.stackSize == 0) {
			return false;
		} else if (!player.canPlayerEdit(x, y, z, side, stack) && !(block instanceof ILiftable)) {
			return false;
		} else if (y == 255 && block.blockMaterial.isSolid()) {
			return false;
		} else if (world.canPlaceEntityOnSide(block.blockID, x, y, z, false, side, null, stack)) {
			int meta = getMetaFromStack(stack);
			int placedMeta = block.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);
			if (placeBlockAt(stack, player, world, x, y, z, side, hitX, hitY, hitZ, block, placedMeta)) {
				world.playSoundEffect((double)(x + 0.5D), (double)(y + 0.5D), (double)(z + 0.5D), block.stepSound.getPlaceSound(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
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
	 * Copied from ItemBlock with added Block parameter; places the block after
	 * all other checks have been made
	 */
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, Block block, int meta) {
		if (!world.setBlock(x, y, z, block.blockID, meta, 3)) {
			return false;
		}
		if (world.getBlockId(x, y, z) == block.blockID) {
			block.onBlockPlacedBy(world, x, y, z, player, stack);
			block.onPostBlockPlaced(world, x, y, z, meta);
			if (block instanceof ILiftable) {
				((ILiftable) block).onHeldBlockPlaced(world, stack, x, y, z, meta);
			}
		}
		return true;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		Block block = getBlockFromStack(stack);
		return (block != null ? block.getUnlocalizedName() : Block.blocksList[Block.stone.blockID].getUnlocalizedName());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(ItemStack stack, int pass) {
		Block block = getBlockFromStack(stack);
		return (block != null ? block.getIcon(1, getMetaFromStack(stack)) : Block.blocksList[Block.stone.blockID].getIcon(1, 0));
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
}

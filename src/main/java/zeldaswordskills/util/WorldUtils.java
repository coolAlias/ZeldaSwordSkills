/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.passive.EntityFairy;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;

/**
 * 
 * Collection of methods related to placing objects or entities into the world
 *
 */
public class WorldUtils
{
	/** Maximum explosionSize within which blocks can be affected, regardless of explosion size */
	public static final int MAX_RADIUS = 16;

	/**
	 * Activates a button or toggles a lever at the given position and notifies neighbors
	 * @param state Must contain either a BlockButton or BlockLever
	 */
	public static void activateButton(World world, IBlockState state, BlockPos pos) {
		Block block = state.getBlock();
		if (!(block instanceof BlockButton) && !(block instanceof BlockLever)) {
			return;
		}
		IProperty powered = (block instanceof BlockButton) ? BlockButton.POWERED : BlockLever.POWERED;
		boolean setPowered = block instanceof BlockButton || !((Boolean) state.getValue(powered)).booleanValue();
		IProperty facing = (block instanceof BlockButton) ? BlockButton.FACING : BlockLever.FACING;
		world.setBlockState(pos, state.withProperty(powered, Boolean.valueOf(setPowered)), 3);
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.playSoundEffect((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, "random.click", 0.3F, 0.6F);
		world.notifyNeighborsOfStateChange(pos, state.getBlock());
		world.notifyNeighborsOfStateChange(pos.offset(((BlockLever.EnumOrientation) state.getValue(facing)).getFacing().getOpposite()), state.getBlock());
		world.scheduleUpdate(pos, state.getBlock(), state.getBlock().tickRate(world));
		world.playSoundEffect((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, Sounds.CLICK, 0.3F, 0.6F);
	}

	/**
	 * Whether the block at the given position can be melted by any of the various fire effects
	 */
	public static boolean canMeltBlock(World world, Block block, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		int meta = state.getBlock().getMetaFromState(state);
		boolean flag = block instanceof BlockSecretStone && (Config.enableFireArrowMelt() ? (meta & ~8) == 5 : meta == 5);
		return (flag || block.getMaterial() == Material.ice || block.getMaterial() == Material.packedIce ||
				block.getMaterial() == Material.snow || block.getMaterial() == Material.craftedSnow);
	}

	/**
	 * Drops the entity's currently held item into the world
	 */
	public static void dropHeldItem(EntityLivingBase entity) {
		if (!entity.worldObj.isRemote && entity.getHeldItem() != null) {
			EntityItem drop = new EntityItem(entity.worldObj, entity.posX,
					entity.posY - 0.30000001192092896D + (double) entity.getEyeHeight(),
					entity.posZ, entity.getHeldItem().copy());
			float f = 0.3F;
			float f1 = entity.worldObj.rand.nextFloat() * (float) Math.PI * 2.0F;
			drop.motionX = (double)(-MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f);
			drop.motionZ = (double)(MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI) * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f);
			drop.motionY = (double)(-MathHelper.sin(entity.rotationPitch / 180.0F * (float) Math.PI) * f + 0.1F);
			f = 0.02F * entity.worldObj.rand.nextFloat();
			drop.motionX += Math.cos((double) f1) * (double) f;
			drop.motionY += (double)((entity.worldObj.rand.nextFloat() - entity.worldObj.rand.nextFloat()) * 0.1F);
			drop.motionZ += Math.sin((double) f1) * (double) f;
			drop.setPickupDelay(40);
			entity.worldObj.spawnEntityInWorld(drop);
			entity.setCurrentItemOrArmor(0, (ItemStack) null);
		}
	}

	/**
	 * Call when a container block is broken to drop the entire inv into the world
	 */
	public static void dropContainerBlockInventory(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof IInventory)) { return; }
		IInventory inv = (IInventory) tileEntity;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			spawnItemWithRandom(world, inv.removeStackFromSlot(i), pos.getX(), pos.getY(), pos.getZ());
		}
	}

	/**
	 * Undoes whatever acceleration has been applied by materials such as flowing water (see {@link World#handleMaterialAcceleration})
	 */
	public static boolean reverseMaterialAcceleration(World world, AxisAlignedBB aabb, Material material, Entity entity) {
		int i = MathHelper.floor_double(aabb.minX);
		int j = MathHelper.floor_double(aabb.maxX + 1.0D);
		int k = MathHelper.floor_double(aabb.minY);
		int l = MathHelper.floor_double(aabb.maxY + 1.0D);
		int i1 = MathHelper.floor_double(aabb.minZ);
		int j1 = MathHelper.floor_double(aabb.maxZ + 1.0D);
		if (!WorldUtils.isAreaLoaded(world, i, k, i1, j, l, j1, true)) {
			return false;
		} else {
			boolean flag = false;
			Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
			for (int k1 = i; k1 < j; ++k1) {
				for (int l1 = k; l1 < l; ++l1) {
					for (int i2 = i1; i2 < j1; ++i2) {
						BlockPos pos = new BlockPos(k1, l1, i2);
						IBlockState state = world.getBlockState(pos);
						Block block = state.getBlock();
						if (block.getMaterial() == material) {
							double liquidLevel = (double)((float)(l1 + 1) - BlockLiquid.getLiquidHeightPercent(((Integer) state.getValue(BlockLiquid.LEVEL)).intValue()));
							if ((double)l >= liquidLevel) {
								flag = true;
								vec3 = block.modifyAcceleration(world, pos, entity, vec3);
							}
						}
					}
				}
			}
			if (vec3.lengthVector() > 0.0D && entity.isPushedByWater()) {
				vec3 = vec3.normalize();
				double d1 = 0.014D;
				entity.motionX -= vec3.xCoord * d1;
				entity.motionY -= vec3.yCoord * d1;
				entity.motionZ -= vec3.zCoord * d1;
				entity.motionX *= 0.85D;
				entity.motionY *= 0.85D;
				entity.motionZ *= 0.85D;
			}
			return flag;
		}
	}

	/**
	 * Copied from {@link World#isAreaLoaded}
	 */
	public static boolean isAreaLoaded(World world, int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
		if (yEnd >= 0 && yStart < 256) {
			xStart >>= 4;
		zStart >>= 4;
		xEnd >>= 4;
		zEnd >>= 4;
		for (int k1 = xStart; k1 <= xEnd; ++k1) {
			for (int l1 = zStart; l1 <= zEnd; ++l1) {
				if (!WorldUtils.isChunkLoaded(world, k1, l1, allowEmpty)) {
					return false;
				}
			}
		}
		return true;
		}
		return false;
	}

	/**
	 * Copied from {@link World#isChunkLoaded}
	 */
	public static boolean isChunkLoaded(World world, int x, int z, boolean allowEmpty) {
		return world.getChunkProvider().chunkExists(x, z) && (allowEmpty || !world.getChunkProvider().provideChunk(x, z).isEmpty());
	}

	/**
	 * Generates a number of random chest contents, placing them in the chest either completely
	 * at random or only in empty slots, as designated by the parameters
	 */
	public static void generateRandomChestContents(Random rand, List<WeightedRandomChestContent> weightedContents, IInventory chest, int numItems, boolean atRandom) {
		for (int i = 0; i < numItems; ++i) {
			WeightedRandomChestContent weightedChest = (WeightedRandomChestContent) WeightedRandom.getRandomItem(rand, weightedContents);
			ItemStack[] stacks = ChestGenHooks.generateStacks(rand, weightedChest.theItemId, weightedChest.minStackSize, weightedChest.maxStackSize);
			for (ItemStack item : stacks) {
				if (atRandom) {
					chest.setInventorySlotContents(rand.nextInt(chest.getSizeInventory()), item);
				} else {
					addItemToInventoryAtRandom(rand, item, chest, 3);
				}
			}
		}
	}

	/**
	 * Attempts to add the itemstack to a random slot in the inventory; failing that,
	 * it will add to the first available slot
	 * @param numAttempts number of times to attempt random placement
	 * @return the number of items remaining in the stack, zero if all were added
	 */
	public static int addItemToInventoryAtRandom(Random rand, ItemStack stack, IInventory inv, int numAttempts) {
		for (int i = 0; i < numAttempts; ++i) {
			int slot = rand.nextInt(inv.getSizeInventory());
			if (inv.getStackInSlot(slot) == null) {
				inv.setInventorySlotContents(slot, stack);
				return 0;
			}
		}	
		return addItemToInventory(stack, inv);
	}

	/**
	 * Adds an ItemStack to the first available slot in the provided IInventory, continuing
	 * to distribute the stack as necessary until the stacksize reaches 0, if possible
	 * @return the number of items remaining in the stack, zero if all were added
	 */
	public static int addItemToInventory(ItemStack stack, IInventory inv) {
		int remaining = stack.stackSize;
		for (int i = 0; i < inv.getSizeInventory() && remaining > 0; ++i) {
			ItemStack slotstack = inv.getStackInSlot(i);
			if (slotstack == null && inv.isItemValidForSlot(i, stack)) {
				remaining -= inv.getInventoryStackLimit();
				stack.stackSize = (remaining > 0 ? inv.getInventoryStackLimit() : stack.stackSize);
				inv.setInventorySlotContents(i, stack);
				inv.markDirty();
			} else if (slotstack != null && stack.isStackable() && inv.isItemValidForSlot(i, stack)) {
				if (slotstack.getItem() == stack.getItem()  && (!stack.getHasSubtypes() || 
						stack.getItemDamage() == slotstack.getItemDamage()) && ItemStack.areItemStackTagsEqual(stack, slotstack))
				{
					int l = slotstack.stackSize + remaining;
					if (l <= stack.getMaxStackSize() && l <= inv.getInventoryStackLimit()) {
						remaining = 0;
						slotstack.stackSize = l;
						inv.markDirty();
					} else if (slotstack.stackSize < stack.getMaxStackSize() && stack.getMaxStackSize() <= inv.getInventoryStackLimit()) {
						remaining -= stack.getMaxStackSize() - slotstack.stackSize;
						slotstack.stackSize = stack.getMaxStackSize();
						inv.markDirty();
					}
				}
			}
		}

		return remaining;
	}

	/**
	 * Attempts to add all items in the list to the IInventory at the given
	 * position using {@link #addItemToInventoryAtRandom}. Items are added
	 * beginning at the first available slot.
	 * @return true if all items were successfully added
	 */
	public static boolean addInventoryContents(World world, BlockPos pos, List<ItemStack> items) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IInventory) {
			IInventory inv = (IInventory) te;
			for (ItemStack stack : items) {
				if (addItemToInventory(stack, inv) > 0) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Attempts to add all items in the list to the IInventory at the given
	 * position using {@link #addItemToInventoryAtRandom}
	 * @return true if all items were successfully added
	 */
	public static boolean addInventoryContentsRandomly(World world, BlockPos pos, List<ItemStack> items) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IInventory) {
			IInventory inv = (IInventory) te;
			boolean flag = true;
			for (ItemStack stack : items) {
				if (addItemToInventoryAtRandom(world.rand, stack, inv, 3) > 0) {
					flag = false;
				}
			}
			return flag;
		}
		return false;
	}

	/**
	 * Populates a list with blocks that can be affected within the given radius
	 * @param targetBlock the block to target, or null if all blocks may be targeted
	 */
	public static HashSet<BlockPos> getAffectedBlocksList(World world, Random rand, float radius, double posX, double posY, double posZ, Block targetBlock) {
		HashSet<BlockPos> hashset = Sets.newHashSet();
		for (int i = 0; i < MAX_RADIUS; ++i) {
			for (int j = 0; j < MAX_RADIUS; ++j) {
				for (int k = 0; k < MAX_RADIUS; ++k) {
					if (i == 0 || i == MAX_RADIUS - 1 || j == 0 || j == MAX_RADIUS - 1 || k == 0 || k == MAX_RADIUS - 1)
					{
						double d3 = (double)((float)i / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d4 = (double)((float)j / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d5 = (double)((float)k / ((float) MAX_RADIUS - 1.0F) * 2.0F - 1.0F);
						double d6 = Math.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
						d3 /= d6;
						d4 /= d6;
						d5 /= d6;
						float f1 = radius * (0.7F + rand.nextFloat() * 0.6F);
						double d0 = posX;
						double d1 = posY;
						double d2 = posZ;

						for (float f2 = 0.3F; f1 > 0.0F; f1 -= 0.22500001F)
						{
							int l = MathHelper.floor_double(d0);
							int i1 = MathHelper.floor_double(d1);
							int j1 = MathHelper.floor_double(d2);
							BlockPos blockpos = new BlockPos(l, i1, j1);
							Block block = world.getBlockState(blockpos).getBlock();
							if (block.getMaterial() != Material.air) {
								f1 -= 1.3F * f2;
							}
							if (f1 > 0.0F && (targetBlock == null || block == targetBlock)) {
								hashset.add(blockpos);
							}

							d0 += d3 * (double)f2;
							d1 += d4 * (double)f2;
							d2 += d5 * (double)f2;
						}
					}
				}
			}
		}

		return hashset;
	}

	/**
	 * Returns a list of all Tile Entities matching the class given within the bounding box
	 */
	public static <T extends TileEntity> List<T> getTileEntitiesWithinAABB(World world, Class<T> clazz, AxisAlignedBB aabb) {
		List<T> list = new ArrayList<T>();
		int minX = MathHelper.floor_double(aabb.minX - World.MAX_ENTITY_RADIUS);
		int maxX = MathHelper.floor_double(aabb.maxX + World.MAX_ENTITY_RADIUS);
		int minY = MathHelper.floor_double(aabb.minY - World.MAX_ENTITY_RADIUS);
		int maxY = MathHelper.floor_double(aabb.maxY + World.MAX_ENTITY_RADIUS);
		int minZ = MathHelper.floor_double(aabb.minZ - World.MAX_ENTITY_RADIUS);
		int maxZ = MathHelper.floor_double(aabb.maxZ + World.MAX_ENTITY_RADIUS);
		if (!world.isAreaLoaded(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))) {
			return list;
		}
		for (int i = minX; i <= maxX; ++i) {
			for (int j = minY; j <= maxY; ++j) {
				for (int k = minZ; k <= maxZ; ++k) {
					TileEntity te = world.getTileEntity(new BlockPos(i, j, k));
					if (te != null && clazz.isAssignableFrom(te.getClass())) {
						list.add((T) te);
					}
				}
			}
		}

		return list;
	}

	/**
	 * Returns an entity by UUID, possibly traversing every single loaded entity
	 */
	public static Entity getEntityByUUID(World world, UUID uuid) {
		for (Object o : world.loadedEntityList) {
			if (((Entity) o).getUniqueID().compareTo(uuid) == 0) {
				return (Entity) o;
			}
		}
		return null;
	}

	/**
	 * Returns the nearest fairy spawner dungeon core to the position, or null if none exists
	 * @param requiresFairy if true, only returns a spawner with at least one fairy nearby
	 */
	public static TileEntityDungeonCore getNearbyFairySpawner(World world, double x, double y, double z, boolean requiresFairy) {
		List<TileEntityDungeonCore> list = WorldUtils.getTileEntitiesWithinAABB(world, TileEntityDungeonCore.class, new AxisAlignedBB(x - 3.0D, y - 2.0D, z - 3.0D, x + 3.0D, y + 2.0D, z + 3.0D));
		for (TileEntityDungeonCore core : list) {
			BlockPos pos = core.getPos();
			if (core.isSpawner() && (!requiresFairy || world.getEntitiesWithinAABB(EntityFairy.class, new AxisAlignedBB((double) pos.getX() - 5D, (double) pos.getY() - 1, (double) pos.getZ() - 5D, (double) pos.getX() + 5D, (double) pos.getY() + 5.0D, (double) pos.getZ() + 5D)).size() > 0)) {
				return core;
			}
		}
		return null;
	}

	/**
	 * Plays a sound on the server with randomized volume and pitch; no effect if called on client
	 * @param f		Volume: nextFloat() * f + add
	 * @param add	Pitch: 1.0F / (nextFloat() * f + add)
	 */
	public static void playSoundAtEntity(Entity entity, String sound, float f, float add) {
		playSoundAt(entity.worldObj, entity.posX, entity.posY, entity.posZ, sound, f, add);
	}

	/**
	 * Plays a sound on the server with randomized volume and pitch; no effect if called on client
	 * @param f		Volume: nextFloat() * f + add
	 * @param add	Pitch: 1.0F / (nextFloat() * f + add)
	 */
	public static void playSoundAt(World world, double x, double y, double z, String sound, float f, float add) {
		float volume = world.rand.nextFloat() * f + add;
		float pitch = 1.0F / (world.rand.nextFloat() * f + add);
		world.playSoundEffect(x, y, z, sound, volume, pitch);
	}

	/**
	 * Spawns the provided ItemStack as an EntityItem with randomized position and motion,
	 * with default motion factor used by blocks to scatter items when the block is broken
	 */
	public static void spawnItemWithRandom(World world, ItemStack stack, double x, double y, double z) {
		spawnItemWithRandom(world, stack, x, y, z, 0.05F);
	}

	/**
	 * Spawns the provided ItemStack as an EntityItem with randomized position and motion
	 * @param motionFactor	EntityItem's motion is multiplied by this amount; clamped between 0.0F and 1.0F
	 */
	public static void spawnItemWithRandom(World world, ItemStack stack, double x, double y, double z, float motionFactor) {
		if (!world.isRemote && stack != null) {
			double spawnX = x + world.rand.nextFloat();
			double spawnY = y + world.rand.nextFloat();
			double spawnZ = z + world.rand.nextFloat();
			float f = MathHelper.clamp_float(motionFactor, 0.0F, 1.0F);
			EntityItem entityitem = new EntityItem(world, spawnX, spawnY, spawnZ, stack);
			entityitem.motionX = (-0.5F + world.rand.nextGaussian()) * f;
			entityitem.motionY = (4 + world.rand.nextGaussian()) * f;
			entityitem.motionZ = (-0.5F + world.rand.nextGaussian()) * f;
			entityitem.setDefaultPickupDelay();
			world.spawnEntityInWorld(entityitem);
		}
	}

	/**
	 * Adds previously instantiated particle effect to the effect renderer depending on minecraft game settings
	 */
	@SideOnly(Side.CLIENT)
	public static void spawnWorldParticles(World world, EntityFX particle) {
		Minecraft mc = Minecraft.getMinecraft();
		if (particle != null && mc != null && mc.getRenderViewEntity() != null && mc.effectRenderer != null) {
			int particleSetting = mc.gameSettings.particleSetting;
			if (particleSetting == 2 || (particleSetting == 1 && world.rand.nextInt(3) == 0)) {
				return;
			}
			double dx = mc.getRenderViewEntity().posX - particle.posX;
			double dy = mc.getRenderViewEntity().posY - particle.posY;
			double dz = mc.getRenderViewEntity().posZ - particle.posZ;
			if (dx * dx + dy * dy + dz * dz < 256) {
				Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			}
		}
	}

	/**
	 * Spawns XP Orbs for the amount given with randomized position and motion
	 */
	public static void spawnXPOrbsWithRandom(World world, Random rand, int x, int y, int z, int xpAmount) {
		if (!world.isRemote) {
			while (xpAmount > 0) {
				int xp = (xpAmount > 50 ? 50 : EntityXPOrb.getXPSplit(xpAmount));
				xpAmount -= xp;
				float spawnX = x + rand.nextFloat();
				float spawnY = y + rand.nextFloat();
				float spawnZ = z + rand.nextFloat();
				EntityXPOrb xpOrb = new EntityXPOrb(world, spawnX, spawnY, spawnZ, xp);
				xpOrb.motionY += (4 + rand.nextGaussian()) * 0.05F;
				world.spawnEntityInWorld(xpOrb);
			}
		}
	}

	/**
	 * Sets an entity's location near x/y/z so that it doesn't spawn inside of walls.
	 * @return false if no suitable location found
	 */
	public static boolean setEntityInStructure(World world, Entity entity, BlockPos pos) {
		if (entity == null) { return false; }
		int i = 0;
		entity.setLocationAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
		while (entity.isEntityInsideOpaqueBlock() && i < 8) {
			if (i == 4) { entity.setPosition(pos.getX() + 1, pos.getY(), pos.getZ() + 1); }
			switch(i % 4) {
			case 0: entity.setPosition(entity.posX + 0.5D, entity.posY, entity.posZ + 0.5D); break;
			case 1: entity.setPosition(entity.posX, entity.posY, entity.posZ - 1.0D); break;
			case 2: entity.setPosition(entity.posX - 1.0D, entity.posY, entity.posZ); break;
			case 3: entity.setPosition(entity.posX, entity.posY, entity.posZ + 1.0D); break;
			}
			++i;
		}
		if (entity.isEntityInsideOpaqueBlock()) {
			entity.setPosition(entity.posX + 0.5D, entity.posY, entity.posZ + 0.5D);
			return false;
		}
		return true;
	}
}

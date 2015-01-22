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

package zeldaswordskills.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.EntityFairy;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
	 * Switches active state of button or lever at x/y/z to and notifies neighbors
	 */
	public static void activateButton(World world, Block block, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		world.setBlockMetadataWithNotify(x, y, z, (meta < 8 ? meta | 8 : meta & ~8), 3);
		// func_147479_m is markBlockForRenderUpdate
		world.func_147479_m(x, y, z);
		world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, Sounds.CLICK, 0.3F, 0.6F);
		world.scheduleBlockUpdate(x, y, z, block, block.tickRate(world));
		world.notifyBlocksOfNeighborChange(x, y, z, block);
		switch(meta & 7) {
		case 0: // 0 and 7 are ceiling levers
		case 7: world.notifyBlocksOfNeighborChange(x, y + 1, z, block); break;
		case 1: world.notifyBlocksOfNeighborChange(x - 1, y, z, block); break;
		case 2: world.notifyBlocksOfNeighborChange(x + 1, y, z, block); break;
		case 3: world.notifyBlocksOfNeighborChange(x, y, z - 1, block); break;
		case 4: world.notifyBlocksOfNeighborChange(x, y, z + 1, block); break;
		default: world.notifyBlocksOfNeighborChange(x, y - 1, z, block);
		}
	}

	/**
	 * Whether the block at x/y/z can be melted by any of the various fire effects
	 */
	public static boolean canMeltBlock(World world, Block block, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		boolean flag = Config.enableFireArrowMelt() ? (meta & ~8) == 5 : meta == 5;
		return (block.getMaterial() == Material.ice || block.getMaterial() == Material.snow ||
				(block == ZSSBlocks.secretStone && flag));
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
			drop.delayBeforeCanPickup = 40;
			entity.worldObj.spawnEntityInWorld(drop);
			entity.setCurrentItemOrArmor(0, (ItemStack) null);
		}
	}

	/**
	 * Call when a container block is broken to drop the entire inv into the world
	 */
	public static void dropContainerBlockInventory(World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof IInventory)) { return; }
		IInventory inv = (IInventory) tileEntity;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			spawnItemWithRandom(world, inv.getStackInSlotOnClosing(i), x, y, z);
		}
	}

	/**
	 * Generates a number of random chest contents, placing them in the chest either completely
	 * at random or only in empty slots, as designated by the parameters
	 */
	public static void generateRandomChestContents(Random rand, WeightedRandomChestContent[] weightedContents, IInventory chest, int numItems, boolean atRandom) {
		for (int i = 0; i < numItems; ++i) {
			WeightedRandomChestContent weightedChest = (WeightedRandomChestContent) WeightedRandom.getRandomItem(rand, weightedContents);
			ItemStack[] stacks = ChestGenHooks.generateStacks(rand, weightedChest.theItemId, weightedChest.theMinimumChanceToGenerateItem, weightedChest.theMaximumChanceToGenerateItem);
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
	 * Attempts to add all items in the list to container at x/y/z, provided that the tile
	 * entity at x/y/z is an IInventory. Items are added from the first available slot.
	 * @return true if all items were successfully added
	 */
	public static boolean addInventoryContents(World world, int x, int y, int z, List<ItemStack> items) {
		TileEntity te = world.getTileEntity(x, y, z);
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
	 * Attempts to add all items in the list to container at x/y/z, provided that the tile
	 * entity at x/y/z is an IInventory using {@link #addItemToInventoryAtRandom}
	 * @return true if all items were successfully added
	 */
	public static boolean addInventoryContentsRandomly(World world, int x, int y, int z, List<ItemStack> items) {
		TileEntity te = world.getTileEntity(x, y, z);
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
	public static HashSet<ChunkPosition> getAffectedBlocksList(World world, Random rand, float radius, double posX, double posY, double posZ, Block targetBlock) {
		HashSet<ChunkPosition> hashset = new HashSet<ChunkPosition>();
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

						for (float f2 = 0.3F; f1 > 0.0F; f1 -= f2 * 0.75F)
						{
							int l = MathHelper.floor_double(d0);
							int i1 = MathHelper.floor_double(d1);
							int j1 = MathHelper.floor_double(d2);
							Block block = world.getBlock(l, i1, j1);
							if (block.getMaterial() != Material.air) {
								f1 -= 1.3F * f2;
							}
							if (f1 > 0.0F && (targetBlock == null || block == targetBlock)) {
								hashset.add(new ChunkPosition(l, i1, j1));
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
		if (!world.checkChunksExist(minX, minY, minZ, maxX, maxY, maxZ)) {
			return list;
		}
		for (int i = minX; i <= maxX; ++i) {
			for (int j = minY; j <= maxY; ++j) {
				for (int k = minZ; k <= maxZ; ++k) {
					TileEntity te = world.getTileEntity(i, j, k);
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
		List<TileEntityDungeonCore> list = WorldUtils.getTileEntitiesWithinAABB(world, TileEntityDungeonCore.class, AxisAlignedBB.getBoundingBox(x - 3.0D, y - 2.0D, z - 3.0D, x + 3.0D, y + 2.0D, z + 3.0D));
		for (TileEntityDungeonCore core : list) {
			if (core.isSpawner() && (!requiresFairy || world.getEntitiesWithinAABB(EntityFairy.class, AxisAlignedBB.getBoundingBox((double) core.xCoord - 5D, (double) core.yCoord - 1, (double) core.zCoord - 5D, (double) core.xCoord + 5D, (double) core.yCoord + 5.0D, (double) core.zCoord + 5D)).size() > 0)) {
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
	 * Spawns the provided ItemStack as an EntityItem with randomized position and motion
	 * Used by blocks to scatter items when broken
	 */
	public static void spawnItemWithRandom(World world, ItemStack stack, double x, double y, double z) {
		if (!world.isRemote && stack != null) {
			double spawnX = x + world.rand.nextFloat();
			double spawnY = y + world.rand.nextFloat();
			double spawnZ = z + world.rand.nextFloat();
			float f3 = 0.05F;
			EntityItem entityitem = new EntityItem(world, spawnX, spawnY, spawnZ, stack);
			entityitem.motionX = (-0.5F + world.rand.nextGaussian()) * f3;
			entityitem.motionY = (4 + world.rand.nextGaussian()) * f3;
			entityitem.motionZ = (-0.5F + world.rand.nextGaussian()) * f3;
			entityitem.delayBeforeCanPickup = 10;
			world.spawnEntityInWorld(entityitem);
		}
	}

	/**
	 * Adds previously instantiated particle effect to the effect renderer depending on minecraft game settings
	 */
	@SideOnly(Side.CLIENT)
	public static void spawnWorldParticles(World world, EntityFX particle) {
		Minecraft mc = Minecraft.getMinecraft();
		if (particle != null && mc != null && mc.renderViewEntity != null && mc.effectRenderer != null) {
			int particleSetting = mc.gameSettings.particleSetting;
			if (particleSetting == 2 || (particleSetting == 1 && world.rand.nextInt(3) == 0)) {
				return;
			}
			double dx = mc.renderViewEntity.posX - particle.posX;
			double dy = mc.renderViewEntity.posY - particle.posY;
			double dz = mc.renderViewEntity.posZ - particle.posZ;
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
	public static boolean setEntityInStructure(World world, Entity entity, int x, int y, int z) {
		if (entity == null) { return false; }
		int i = 0;
		entity.setLocationAndAngles(x, y, z, 0.0F, 0.0F);
		while (entity.isEntityInsideOpaqueBlock() && i < 8) {
			if (i == 4) { entity.setPosition(x + 1, y, z + 1); }
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

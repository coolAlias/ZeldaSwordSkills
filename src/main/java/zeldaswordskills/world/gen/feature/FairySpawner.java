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

package zeldaswordskills.world.gen.feature;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.EntityFairy;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.WorldUtils;

public class FairySpawner
{
	/** The dungeon core to which the spawner belongs */
	protected final TileEntityDungeonCore core;

	/** The fairy pool's bounding box */
	protected final StructureBoundingBox box;

	/** Max fairies this spawner can spawn before it must refresh */
	protected int maxFairies = 0;

	/** Number of fairies already spawned; this gets reset at a specific time each night */
	protected int fairiesSpawned = 0;

	/** Minimum date before next spawn reset */
	protected long nextResetDate = 0;

	/** Scheduled update timer used for processing nearby dropped items */
	protected int itemUpdate = -1;

	/** Player who initiated the next scheduled item update */
	protected String playerName = "";

	/** Number of rupees (emeralds) donated to the fairy */
	protected int rupees = 0;

	public FairySpawner(TileEntityDungeonCore core) {
		this.core = core;
		this.box = core.getDungeonBoundingBox();
		if (box == null) {
			throw new IllegalArgumentException("Dungeon Core bounding box can not be null!");
		}
	}

	/**
	 * Sets the max number of fairies that can be spawned before needing to replenish
	 */
	public FairySpawner setMaxFairies(int maxFairies) {
		this.maxFairies = maxFairies;
		return this;
	}

	/**
	 * Returns true if the amount of rupees was present and consumed
	 */
	public boolean consumeRupees(int amount) {
		if (amount > rupees) {
			return false;
		} else {
			rupees -= amount;
			return true;
		}
	}

	/**
	 * Schedules this spawner to check for dropped items in the near future
	 */
	public void scheduleItemUpdate(EntityPlayer player) {
		if (itemUpdate < 0) {
			itemUpdate = 2;
		}
		if (playerName.equals("")) {
			playerName = player.getCommandSenderName();
		}
	}

	/**
	 * Call when the supporting tile entity is removed to release fairy pool contents
	 */
	public void onBlockBroken() {
		onBlockBroken(core.getWorldObj(), core.xCoord, core.yCoord, core.zCoord);
	}

	/**
	 * Arguments provide cleaner-looking version of onBlockBroken()
	 */
	private void onBlockBroken(World world, int x, int y, int z) {
		while (rupees > 0) {
			int k = (rupees > 64 ? 64 : rupees);
			rupees -= k;
			world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, "random.orb", 1.0F, 1.0F);
			WorldUtils.spawnItemWithRandom(world, new ItemStack(Items.emerald, k), x, y, z);
		}
	}

	/**
	 * Call every tick to allow the fairy spawner to update
	 */
	public void onUpdate() {
		updateSpawner(core.getWorldObj(), core.xCoord, core.yCoord, core.zCoord);
		updateItems(core.getWorldObj(), core.xCoord, core.yCoord, core.zCoord);
	}

	/**
	 * Updates fairy spawning and checks if spawn count should be reset
	 * Args: world and x, y, z coordinates of tile entity
	 */
	private void updateSpawner(World world, int x, int y, int z) {
		if (fairiesSpawned < maxFairies && world.rand.nextFloat() < (world.isDaytime() ? 0.01F : 0.2F)) {
			int nearby = world.getEntitiesWithinAABB(EntityFairy.class, AxisAlignedBB.
					getBoundingBox(x, box.getCenterY(), z, x + 1, box.getCenterY() + 1, z + 1).
					expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2)).size();
			if (nearby < 4) {
				EntityFairy fairy = new EntityFairy(world);
				fairy.setFairyHome(x, y + 2, z);
				world.spawnEntityInWorld(fairy);
				if (++fairiesSpawned == maxFairies) {
					nextResetDate = world.getWorldTime() + 24000 * (world.rand.nextInt(Config.getDaysToRespawn()) + 1);
				}
			}
		}

		if (fairiesSpawned == maxFairies && !world.isDaytime() && world.getWorldTime() > nextResetDate) {
			fairiesSpawned = 0;
		}
	}

	/**
	 * Updates item timer and checks for nearby items if scheduled
	 * Args: world and x, y, z coordinates of tile entity
	 */
	private void updateItems(World world, int x, int y, int z) {
		if (itemUpdate > 0) {
			--itemUpdate;
		} else if (itemUpdate == 0) {
			EntityPlayer player = world.getPlayerEntityByName(playerName);
			if (player != null) {
				List<EntityItem> list = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.
						getBoundingBox(x, box.getCenterY(), z, x + 1, box.getCenterY() + 1, z + 1).
						expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2));
				for (EntityItem item : list) {
					ItemStack stack = item.getEntityItem();
					if (stack.getItem() == Items.emerald) {
						player.triggerAchievement(ZSSAchievements.fairyEmerald);
						world.playSoundEffect(x + 0.5D, y + 1, z + 0.5D, "random.orb", 1.0F, 1.0F);
						rupees += stack.stackSize;
						item.setDead();
					} else if (stack.getItem() instanceof IFairyUpgrade && ((IFairyUpgrade) stack.getItem()).hasFairyUpgrade(stack)) {
						((IFairyUpgrade) stack.getItem()).handleFairyUpgrade(item, player, core);
					}
				}
			}
			itemUpdate = -1;
			playerName = "";
		}
	}

	/**
	 * Writes fairy spawner data to NBT
	 */
	public void writeToNBT(NBTTagCompound compound) {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("maxFairies", maxFairies);
		data.setInteger("spawned", fairiesSpawned);
		data.setLong("nextResetDate", nextResetDate);
		data.setInteger("itemUpdate", itemUpdate);
		data.setInteger("rupees", rupees);
		data.setString("playerName", playerName);
		compound.setTag("FairySpawner", data);
	}

	/**
	 * Reads fairy data data from NBT
	 */
	public void readFromNBT(NBTTagCompound compound) {
		NBTTagCompound data = compound.getCompoundTag("FairySpawner");
		maxFairies = data.getInteger("maxFairies");
		fairiesSpawned = data.getInteger("spawned");
		// fixes class cast exception from changing types:
		nextResetDate = (data.getTag("nextResetDate").getId() == Constants.NBT.TAG_LONG ? data.getLong("nextResetDate") : 0);
		itemUpdate = data.getInteger("itemUpdate");
		rupees = data.getInteger("rupees");
		playerName = data.getString("playerName");
	}
}

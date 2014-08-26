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

package zeldaswordskills.block.tileentity;

import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.IDungeonBlock;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.entity.ZSSPlayerInfo.Stats;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.world.crisis.BossBattle;
import zeldaswordskills.world.gen.feature.FairySpawner;
import zeldaswordskills.world.gen.structure.RoomBoss;

/**
 * 
 * This is the core of each secret dungeon room. It is responsible for verifying the
 * integrity of the structure and, when the integrity is broken, playing the secret
 * medley as well as setting the unbreakable blocks to normal stone.
 * 
 * If set to spawn fairies, each core will have its own maximum spawn count that is
 * occasionally reset, limiting the number of fairies that can spawn on any given day.
 *
 */
public class TileEntityDungeonCore extends TileEntityDungeonBlock
{
	/** The bounding box of the associated structure*/
	protected StructureBoundingBox box;

	/** The Fairy Spawner object, if any */
	private FairySpawner fairySpawner = null;

	/** Whether this room has a boss type and can trigger events */
	private boolean isBossRoom = false;

	/** Type of dungeon which this is, based on biome; only used for locked dungeons */
	private BossType dungeonType = null;

	/** The boss battle event currently in progress */
	private BossBattle bossBattle = null;

	/** Set to true when the door is unlocked; allows for triggering events */
	private boolean isOpened;

	/** The door's block type, if any */
	private Block door = null;

	/** Side in which the door is located; always located at centerX or centerZ of that side */
	private int doorSide;

	/** Set to true when structure is broken; prevents second call of verifyStructure */
	private boolean alreadyVerified = false;

	public TileEntityDungeonCore() {}

	@Override
	public boolean canUpdate() {
		return true;
	}

	/** Call after setting the block to set the dungeon's structure bounding box */
	public void setDungeonBoundingBox(StructureBoundingBox box) {
		this.box = box;
	}

	/** Returns the bounding box for this dungeon; may be null */
	public StructureBoundingBox getDungeonBoundingBox() {
		return box;
	}

	/** Sets the boss type for this boss room */
	public void setBossType(BossType type) {
		dungeonType = type;
		isBossRoom = true;
	}

	/** The BossType of this dungeon */
	public BossType getBossType() {
		return dungeonType;
	}

	/**
	 * Sets the "door" block and side for this room
	 * @param block the type of block that is acting as a door to this room
	 */
	public void setDoor(Block block, int side) {
		door = block;
		doorSide = side;
	}

	/** Returns true if the tile entity's fairy spawner is not null */
	public boolean isSpawner() {
		return fairySpawner != null;
	}

	/**
	 * Sets this tile entity to act as a fairy spawner
	 */
	public void setSpawner() {
		if (box == null) {
			alreadyVerified = true;
			setDungeonBoundingBox(new StructureBoundingBox(xCoord - 3, yCoord, zCoord - 3, xCoord + 3, yCoord + 4, zCoord + 3));
		}
		fairySpawner = new FairySpawner(this).setMaxFairies(worldObj.rand.nextInt(5) + 2);
	}

	/**
	 * Helper method schedules fairy spawner to check for dropped items in the near future
	 */
	public void scheduleItemUpdate(EntityPlayer player) {
		if (isSpawner()) {
			fairySpawner.scheduleItemUpdate(player);
		}
	}

	/**
	 * Helper method returns true if the amount of rupees was present and consumed
	 */
	public boolean consumeRupees(int amount) {
		return isSpawner() && fairySpawner.consumeRupees(amount);
	}

	/**
	 * Returns true every 30 ticks if there is a nearby player and no event is active
	 */
	private boolean shouldUpdate() {
		return (bossBattle == null && worldObj.getWorldTime() % 20 == 0 &&
				worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 16.0D) != null);
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
			return;
		}
		if (isBossRoom && bossBattle == null) {
			if (box == null) {
				LogHelper.log(Level.WARNING, String.format("Boss room at %d/%d/%d missing structure bounding box - dungeon is being disabled", xCoord, yCoord, zCoord));
				verifyStructure(true);
				removeCoreBlock();
			} else {
				EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, (double)(box.getXSize() - 2) / 2.0D);
				if (closestPlayer != null && !isOpened) { // player got in somehow other than the door:
					closestPlayer.addChatMessage("Ganon: Thought you could sneak in, eh? Mwa ha ha ha!");
					verifyStructure(true);
					alreadyVerified = true;
				}
				bossBattle = dungeonType.getBossBattle(this);
				if (bossBattle != null) {
					bossBattle.beginCrisis(worldObj);
				}
			}
		}
		if (bossBattle != null) {
			bossBattle.onUpdate(worldObj);
			if (bossBattle.isFinished()) {
				bossBattle = null;
				removeCoreBlock();
			}
		} else if (shouldUpdate()) {
			LogHelper.log(Level.FINER, String.format("Verifying structure during update at %d/%d/%d", xCoord, yCoord, zCoord));
			if (!alreadyVerified && box != null && !verifyStructure(false)) {
				LogHelper.log(Level.FINER, "Failed verification; setting blocks to stone");
				verifyStructure(true);
				alreadyVerified = true;
				if (isBossRoom) {
					isOpened = true;
				} else {
					LogHelper.log(Level.FINER, "Calling removeCoreBlock after all blocks set to stone");
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
					removeCoreBlock();
				}
			} else if (isSpawner()) {
				fairySpawner.onUpdate();
			}
		}
	}

	/**
	 * Removes the core block; if it's a spawner, the tile entity is kept intact
	 * Called only when validation fails during an update, not when block broken
	 */
	protected void removeCoreBlock() {
		LogHelper.log(Level.FINE, String.format("Removing core block from update at %d/%d/%d", xCoord, yCoord, zCoord));
		EntityPlayer player = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 2.5D, zCoord + 0.5D, 16.0D);
		if (player != null) {
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			if (dungeonType != null) {
				info.addStat(Stats.STAT_BOSS_ROOMS, 1 << dungeonType.ordinal());
				player.triggerAchievement(ZSSAchievements.bossBattle);
				// TODO == 127 (or 255 if the End boss room ever gets made)
				if (info.getStat(Stats.STAT_BOSS_ROOMS) == 127) {
					player.triggerAchievement(ZSSAchievements.bossComplete);
				}
				if (dungeonType == BossType.DESERT || dungeonType == BossType.OCEAN || dungeonType == BossType.HELL) {
					player.triggerAchievement(ZSSAchievements.swordPendant);
				}
			} else {
				info.addStat(Stats.STAT_SECRET_ROOMS, 1);
				LogHelper.log(Level.FINE, "Added stat for secret rooms; current total: " + info.getStat(Stats.STAT_SECRET_ROOMS));
				player.triggerAchievement(ZSSAchievements.bombsAway);
				if (info.getStat(Stats.STAT_SECRET_ROOMS) > 49) {
					player.triggerAchievement(ZSSAchievements.bombJunkie);
				}
			}
		}

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (isSpawner()) {
			// dungeon core block without bit8 can be broken normally
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & ~0x8), 2);
		} else {
			worldObj.setBlock(xCoord, yCoord, zCoord, BlockSecretStone.getIdFromMeta(meta), 0, 2);
		}
	}

	/**
	 * Removes the block placed to prevent early plundering of loot
	 */
	public void removeHinderBlock() {
		int x = box.getCenterX() + (doorSide == RoomBoss.EAST ? 1 : (doorSide == RoomBoss.WEST ? -1 : 0));
		int z = box.getCenterZ() + (doorSide == RoomBoss.SOUTH ? 1 : (doorSide == RoomBoss.NORTH ? -1 : 0));
		int y = (worldObj.getBlockId(x, box.minY + 2, z) == ZSSBlocks.secretStone.blockID ? box.minY + 2 : box.minY + 3);
		worldObj.setBlockToAir(x, y, z);
	}

	/**
	 * Returns false if the structure is locked and the door blocks have been removed.
	 * This is only called when the door and bounding box fields are not null.
	 */
	private boolean verifyDoor() {
		int x = box.getCenterX();
		int z = box.getCenterZ();
		switch(doorSide) {
		case RoomBoss.SOUTH: z = box.maxZ; break;
		case RoomBoss.NORTH: z = box.minZ; break;
		case RoomBoss.EAST: x = box.maxX; break;
		case RoomBoss.WEST: x = box.minX; break;
		default: LogHelper.log(Level.WARNING, "Verifying door in Dungeon Core with invalid door side");
		}
		for (int y = box.minY; y < box.maxY; ++y) {
			if (worldObj.getBlockId(x, y, z) == door.blockID) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Called when core block is broken; re-verifies structure and/or sets all blocks to stone
	 */
	public void onBlockBroken() {
		LogHelper.log(Level.FINER, String.format("Verifying structure after core block broken at %d/%d/%d", xCoord, yCoord, zCoord));
		if (!alreadyVerified) {
			LogHelper.log(Level.FINER, "Wasn't already verified: removing structure after core block broken");
			worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
			verifyStructure(true);
		}
		if (isSpawner()) {
			fairySpawner.onBlockBroken();
		}
	}

	/**
	 * Returns true if the structure is missing fewer than one block
	 * @param replace if true, all remaining boundary blocks will be replaced with normal stone
	 */
	protected boolean verifyStructure(boolean replace) {
		if (box != null && hasWorldObj()) {
			if (!replace && door != null) {
				return verifyDoor();
			}
			int invalid = 0;
			for (int i = box.minX; i <= box.maxX; ++i) {
				for (int j = box.minY; j <= box.maxY; ++j) {
					for (int k = box.minZ; k <= box.maxZ; ++k) {
						if (i == box.minX || i == box.maxX || j == box.minY || j == box.maxY || k == box.minZ || k == box.maxZ) {
							int id = worldObj.getBlockId(i, j, k);
							if (replace) {
								if (id == ZSSBlocks.secretStone.blockID) {
									LogHelper.log(Level.FINEST, String.format("Replacing secret stone block at %d/%d/%d", i, j, k));
									int meta = worldObj.getBlockMetadata(i, j, k);
									worldObj.setBlock(i, j, k, BlockSecretStone.getIdFromMeta(meta), 0, 2);
								}
							} else {
								Block block = (id > 0 ? Block.blocksList[id] : null);
								if (!(block instanceof IDungeonBlock)) {
									LogHelper.log(Level.FINER, String.format("Block %s with id %d at %d/%d/%d with is invalid for this structure", block, id, i, j, k));
									if (++invalid > 2) {
										LogHelper.log(Level.FINER, "Too many invalid blocks during verification; returning false");
										return false;
									}
								}
							}
						}
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
		readFromNBT(packet.data);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("hasBB", box != null);
		if (box != null) {
			tag.setTag("boxBounds", box.func_143047_a("boxBounds"));
		}
		tag.setBoolean("verified", alreadyVerified);
		tag.setBoolean("isBossRoom", isBossRoom);
		if (isBossRoom) {
			tag.setString("dungeonName", dungeonType.getUnlocalizedName());
			tag.setBoolean("isOpened", isOpened);
			tag.setBoolean("hasBossBattle", bossBattle != null);
			if (bossBattle != null) {
				bossBattle.writeToNBT(tag);
			}
		}
		tag.setBoolean("hasDoor", door != null);
		if (door != null) {
			tag.setInteger("doorBlockId", door.blockID);
			tag.setInteger("doorSide", doorSide);
		}
		if (isSpawner()) {
			fairySpawner.writeToNBT(tag);
		}
		compound.setTag("ZSSDungeonCore", tag);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		NBTTagCompound tag = compound.getCompoundTag("ZSSDungeonCore");
		if (tag.hasKey("boxBounds")) {
			this.box = new StructureBoundingBox(tag.getIntArray("boxBounds"));
		}
		alreadyVerified = tag.hasKey("verified") ? tag.getBoolean("verified") : false;
		// TODO change to just getBoolean("isBossRoom") after everyone updates
		isBossRoom = tag.hasKey("isBossRoom") ? tag.getBoolean("isBossRoom") : tag.getBoolean("isLocked");
		if (isBossRoom) {
			if (tag.hasKey("dungeonName")) {
				dungeonType = BossType.getBossType(tag.getString("dungeonName"));
			} else {
				// TODO remove after all previous worlds update to String storage:
				LogHelper.log(Level.WARNING, String.format("Detected old boss dungeon save format at %d/%d/%d - if you still see this message after saving and reloading, please contact the mod author", xCoord, yCoord, zCoord));
				dungeonType = BossType.values()[tag.getInteger("dungeonType") % BossType.values().length];
			}
			isOpened = tag.getBoolean("isOpened");
			if (tag.getBoolean("hasBossBattle")) {
				bossBattle = dungeonType.getBossBattle(this);
				if (bossBattle != null) {
					bossBattle.readFromNBT(tag);
				} else {
					LogHelper.log(Level.WARNING, String.format("Error retrieving Boss Battle while loading Dungeon Core from NBT at %d/%d/%d", xCoord, yCoord, zCoord));
				}
			}
		}
		if (tag.getBoolean("hasDoor")) {
			int id = tag.getInteger("doorBlockId");
			door = id > 0 ? Block.blocksList[id] : null;
			doorSide = tag.getInteger("doorSide");
		}
		// TODO workaround for backwards compatibility:
		if (tag.getBoolean("isSpawner")) {
			LogHelper.log(Level.WARNING, String.format("Detected old fairy spawner save format at %d/%d/%d - if you still see this message after saving and reloading, please contact the mod author", xCoord, yCoord, zCoord));
			NBTTagCompound spawnerData = new NBTTagCompound();
			spawnerData.setInteger("maxFairies", tag.getInteger("maxFairies"));
			spawnerData.setInteger("spawned", tag.getInteger("spawned"));
			spawnerData.setInteger("nextResetDate", tag.getInteger("nextResetDate"));
			spawnerData.setInteger("itemUpdate", tag.getInteger("itemUpdate"));
			spawnerData.setInteger("rupees", tag.getInteger("rupees"));
			spawnerData.setString("playerName", tag.getString("playerName"));
			tag.setTag("FairySpawner", spawnerData);
		}
		if (tag.hasKey("FairySpawner")) {
			fairySpawner = new FairySpawner(this);
			fairySpawner.readFromNBT(tag);
		}
	}
}

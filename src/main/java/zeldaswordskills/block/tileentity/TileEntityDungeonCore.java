/**
    Copyright (C) <2018> <coolAlias>

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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.block.IDungeonBlock;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.mobs.EntityChu;
import zeldaswordskills.entity.mobs.EntityChuBlue;
import zeldaswordskills.entity.mobs.EntityChuGreen;
import zeldaswordskills.entity.mobs.EntityChuRed;
import zeldaswordskills.entity.mobs.EntityChuYellow;
import zeldaswordskills.entity.mobs.EntityDarknut;
import zeldaswordskills.entity.mobs.EntityKeese;
import zeldaswordskills.entity.mobs.EntityKeeseCursed;
import zeldaswordskills.entity.mobs.EntityKeeseFire;
import zeldaswordskills.entity.mobs.EntityKeeseIce;
import zeldaswordskills.entity.mobs.EntityKeeseThunder;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntityOctorokPink;
import zeldaswordskills.entity.mobs.EntityWizzrobe;
import zeldaswordskills.entity.mobs.EntityWizzrobeFire;
import zeldaswordskills.entity.mobs.EntityWizzrobeGale;
import zeldaswordskills.entity.mobs.EntityWizzrobeIce;
import zeldaswordskills.entity.mobs.EntityWizzrobeThunder;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.entity.player.ZSSPlayerInfo.Stats;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.PlayerUtils;
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
	private BossType dungeonType;

	/** The boss battle event currently in progress */
	private BossBattle bossBattle = null;

	/** Set to true when the door is unlocked; allows for triggering events */
	private boolean isOpened;

	/** The door's block type, if any */
	private Block door = null;

	/** The door block's metadata value used to verify state for IDungeonBlocks */
	private int doorMeta = 0;

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
	public void setDoor(Block block, int meta, int side) {
		door = block;
		doorMeta = meta;
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
		return (bossBattle == null && worldObj.getTotalWorldTime() % 20 == 0 &&
				worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, 16.0D) != null);
	}

	@Override
	public void updateEntity() {
		if (worldObj.isRemote) {
			return;
		}
		if (isBossRoom && bossBattle == null) {
			if (box == null) {
				ZSSMain.logger.warn(String.format("Boss room at %d/%d/%d missing structure bounding box - dungeon is being disabled", xCoord, yCoord, zCoord));
				verifyStructure(true);
				removeCoreBlock();
			} else {
				EntityPlayer closestPlayer = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, (double)(box.getXSize() - 2) / 2.0D);
				if (closestPlayer != null && box.isVecInside(MathHelper.floor_double(closestPlayer.posX), MathHelper.floor_double(closestPlayer.posY), MathHelper.floor_double(closestPlayer.posZ))) {
					if (!isOpened) { // player got in somehow other than the door
						PlayerUtils.sendTranslatedChat(closestPlayer, "chat.zss.dungeon.sneak_in");
						verifyStructure(true);
						alreadyVerified = true;
					}
					bossBattle = dungeonType.getBossBattle(this);
					if (bossBattle != null) {
						bossBattle.beginCrisis(worldObj);
					}
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
			if (!alreadyVerified && box != null && !verifyStructure(false)) {
				verifyStructure(true);
				alreadyVerified = true;
				if (isBossRoom) {
					isOpened = true;
				} else {
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
		EntityPlayer player = worldObj.getClosestPlayer(xCoord + 0.5D, yCoord + 2.5D, zCoord + 0.5D, 16.0D);
		if (player != null) {
			ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
			if (dungeonType != null) {
				clearDungeon();
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
				player.triggerAchievement(ZSSAchievements.bombsAway);
				if (info.getStat(Stats.STAT_SECRET_ROOMS) > 49) {
					player.triggerAchievement(ZSSAchievements.bombJunkie);
				}
				if (worldObj.rand.nextFloat() < Config.getRoomSpawnMobChance()) {
					spawnRandomMob();
				}
			}
		}

		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (isSpawner()) {
			// dungeon core block without bit8 can be broken normally
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & ~0x8), 2);
		} else {
			worldObj.setBlock(xCoord, yCoord, zCoord, BlockSecretStone.getBlockFromMeta(meta), 0, 2);
		}
	}

	/**
	 * Removes the block placed to prevent early plundering of loot
	 */
	public void removeHinderBlock() {
		int x = box.getCenterX() + (doorSide == RoomBoss.EAST ? 1 : (doorSide == RoomBoss.WEST ? -1 : 0));
		int z = box.getCenterZ() + (doorSide == RoomBoss.SOUTH ? 1 : (doorSide == RoomBoss.NORTH ? -1 : 0));
		int y = (worldObj.getBlock(x, box.minY + 2, z) == ZSSBlocks.secretStone ? box.minY + 2 : box.minY + 3);
		worldObj.setBlockToAir(x, y, z);
	}

	/**
	 * Spawns a mob inside of non-boss secret rooms (call when breached)
	 * Only allows spawning for rooms size 5+ (at least a 3x3x3 space inside)
	 */
	private void spawnRandomMob() {
		if (worldObj.isRemote || box == null || box.getXSize() < 5) {
			return;
		}
		// get the block above the tile to check for liquids
		Block block = worldObj.getBlock(xCoord, yCoord + 1, zCoord);
		EntityLiving mob = null;
		int rarity = worldObj.rand.nextInt(64) - (worldObj.difficultySetting.getDifficultyId() * 2);
		if (block.getMaterial() == Material.water) {
			if (rarity < 8) {
				mob = new EntityOctorokPink(worldObj);
			} else {
				mob = new EntityOctorok(worldObj);
			}
		} else if (block.getMaterial() == Material.lava) {
			if (rarity > 7) {
				mob = new EntityKeeseFire(this.worldObj).setSpawnSwarm(false);
			} else {
				mob = new EntityKeeseCursed(this.worldObj).setSpawnSwarm(false);
			}
			// START rarity 'switch', starting with most likely cases
		} else if (rarity > 50) {
			mob = new EntityZombie(worldObj);
		} else if (rarity > 40) {
			mob = new EntitySkeleton(worldObj);
		} else if (rarity > 30) {
			mob = (worldObj.rand.nextInt(8) > 1 ? new EntitySpider(worldObj) : new EntityCaveSpider(worldObj));
		} else if (rarity > 20) {
			mob = new EntityCreeper(worldObj);
		} else if (rarity > 10) {
			// Try for a biome-appropriate Keese
			mob = EntityKeese.getRandomKeeseForLocation(this.worldObj, 0.5F, this.xCoord, this.yCoord, this.zCoord);
			// Otherwise just generate a random one
			if (mob == null) {
				switch (worldObj.rand.nextInt(10)) {
				case 0: mob = new EntityKeeseCursed(worldObj); break;
				case 1: mob = new EntityKeeseThunder(worldObj); break;
				case 2: // fall-through
				case 3: mob = new EntityKeeseIce(worldObj); break;
				case 4: // fall-through
				case 5: mob = new EntityKeeseFire(worldObj); break;
				default: mob = new EntityKeese(worldObj); break;
				}
			}
			if (mob instanceof EntityKeese) {
				((EntityKeese) mob).setSpawnSwarm(false);
			}
		} else if (rarity > 4) {
			// Try for a biome-appropriate Chu
			mob = EntityChu.getRandomChuForLocation(this.worldObj, 0.5F, this.xCoord, this.yCoord, this.zCoord);
			// Otherwise just generate a random one
			if (mob == null) {
				switch (worldObj.rand.nextInt(8)) {
				case 0: mob = new EntityChuBlue(worldObj); break;
				case 1: mob = new EntityChuYellow(worldObj); break;
				case 2: // fall-through
				case 3: mob = new EntityChuGreen(worldObj); break;
				default: mob = new EntityChuRed(worldObj); break;
				}
			}
		} else if (rarity > -2) {
			// Try for a biome-appropriate Wizzrobe
			mob = EntityWizzrobe.getRandomWizzrobeForLocation(this.worldObj, 0.5F, this.xCoord, this.yCoord, this.zCoord);
			// Otherwise just generate a random one
			if (mob == null) {
				switch (worldObj.rand.nextInt(4)) {
				case 0: mob = new EntityWizzrobeFire(worldObj); break;
				case 1: mob = new EntityWizzrobeGale(worldObj); break;
				case 2: mob = new EntityWizzrobeIce(worldObj); break;
				default: mob = new EntityWizzrobeThunder(worldObj); break;
				}
			}
			//((EntityWizzrobe) mob).getTeleportAI().setTeleBounds(box);
		} else {
			mob = new EntityDarknut(worldObj);
		}
		if (mob != null) {
			mob.setPosition(xCoord + 0.5D, yCoord + 1.5D, zCoord + 0.5D);
			mob.onSpawnWithEgg(null);
			worldObj.spawnEntityInWorld(mob);
			mob.playLivingSound();
		}
	}

	/**
	 * Opens dungeon door after the boss battle has finished; places Warp Stone if available
	 */
	private void clearDungeon() {
		int x = box.getCenterX();
		int z = box.getCenterZ();
		switch(doorSide) {
		case RoomBoss.SOUTH: z = box.maxZ - 1; break;
		case RoomBoss.NORTH: z = box.minZ + 1; break;
		case RoomBoss.EAST: x = box.maxX - 1; break;
		case RoomBoss.WEST: x = box.minX + 1; break;
		}
		// remove webs blocking door for forest temple
		if (worldObj.getBlock(x, box.minY + 1, z) == Blocks.web) {
			worldObj.setBlockToAir(x, box.minY + 1, z);
		}
		if (worldObj.getBlock(x, box.minY + 2, z) == Blocks.web) {
			worldObj.setBlockToAir(x, box.minY + 2, z);
		}
		placeOpenDoor((worldObj.getBlock(x, box.minY + 1, z).getMaterial().isLiquid() ? 2 : 1));
		// Place warp stone
		if (dungeonType.warpSong != null) {
			Integer meta = BlockWarpStone.reverseLookup.get(dungeonType.warpSong);
			if (meta != null) {
				worldObj.setBlock(x, box.minY, z, ZSSBlocks.warpStone, meta, 2);
			}
		}
	}

	/**
	 * Sets 2 air blocks where door used to be, after dungeon defeated
	 * @param dy	Amount to adjust y position above box.minY; usually either 1 or 2
	 */
	private void placeOpenDoor(int dy) {
		int x = box.getCenterX();
		int z = box.getCenterZ();
		switch(doorSide) {
		case RoomBoss.SOUTH: z = box.maxZ; break;
		case RoomBoss.NORTH: z = box.minZ; break;
		case RoomBoss.EAST: x = box.maxX; break;
		case RoomBoss.WEST: x = box.minX; break;
		}
		worldObj.setBlockToAir(x, box.minY + dy, z);
		worldObj.setBlockToAir(x, box.minY + dy + 1, z);
	}

	/**
	 * Returns false if the structure is locked and the door blocks have been removed.
	 * This is only called when the door and bounding box fields are not null.
	 */
	private boolean verifyDoor() {
		int x = box.getCenterX();
		int z = box.getCenterZ();
		switch (doorSide) {
		case RoomBoss.SOUTH: z = box.maxZ; break;
		case RoomBoss.NORTH: z = box.minZ; break;
		case RoomBoss.EAST: x = box.maxX; break;
		case RoomBoss.WEST: x = box.minX; break;
		default: ZSSMain.logger.warn(String.format("Verifying door in Dungeon Core with invalid door side at %d/%d/%d", xCoord, yCoord, zCoord));
		}
		for (int y = box.minY; y < box.maxY; ++y) {
			if (worldObj.getBlock(x, y, z) == door) {
				return (door instanceof IDungeonBlock) ? ((IDungeonBlock) door).isSameVariant(worldObj, x, y, z, doorMeta) : true;
			}
		}
		return false;
	}

	/**
	 * Called when core block is broken; re-verifies structure and/or sets all blocks to stone
	 */
	public void onBlockBroken() {
		if (!alreadyVerified) {
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
							Block block = worldObj.getBlock(i, j, k);
							if (replace) {
								if (block == ZSSBlocks.secretStone) {
									int meta = worldObj.getBlockMetadata(i, j, k);
									worldObj.setBlock(i, j, k, BlockSecretStone.getBlockFromMeta(meta), 0, 2);
								}
							} else if (!(block instanceof IDungeonBlock)) {
								if (++invalid > 2) {
									return false;
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
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setBoolean("hasBB", box != null);
		if (box != null) {
			tag.setTag("boxBounds", box.func_151535_h());
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
			tag.setInteger("doorBlockId", Block.getIdFromBlock(door));
			tag.setInteger("doorMeta", doorMeta);
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
				ZSSMain.logger.warn(String.format("Detected old boss dungeon save format at %d/%d/%d - if you still see this message after saving and reloading near this location, please contact the mod author", xCoord, yCoord, zCoord));
				dungeonType = BossType.values()[tag.getInteger("dungeonType") % BossType.values().length];
			}
			isOpened = tag.getBoolean("isOpened");
			if (dungeonType == null) {
				ZSSMain.logger.error(String.format("Error retrieving Boss Type from string %s while loading Dungeon Core from NBT at %d/%d/%d", tag.getString("dungeonName"), xCoord, yCoord, zCoord));
			} else if (tag.getBoolean("hasBossBattle")) {
				bossBattle = dungeonType.getBossBattle(this);
				if (bossBattle != null) {
					bossBattle.readFromNBT(tag);
				} else {
					ZSSMain.logger.warn(String.format("Error retrieving Boss Battle while loading Dungeon Core from NBT at %d/%d/%d - returned NULL", xCoord, yCoord, zCoord));
				}
			}
		}
		if (tag.getBoolean("hasDoor")) {
			int id = tag.getInteger("doorBlockId");
			door = id > 0 ? Block.getBlockById(id) : null;
			doorMeta = tag.getInteger("doorMeta");
			doorSide = tag.getInteger("doorSide");
		}
		// TODO workaround for backwards compatibility:
		if (tag.getBoolean("isSpawner")) {
			ZSSMain.logger.warn(String.format("Detected old fairy spawner save format at %d/%d/%d - if you still see this message after saving and reloading near this location, please contact the mod author", xCoord, yCoord, zCoord));
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

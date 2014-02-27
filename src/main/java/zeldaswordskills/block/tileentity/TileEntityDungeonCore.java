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

import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.api.item.IFairyUpgrade;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.entity.EntityFairy;
import zeldaswordskills.entity.EntityOctorok;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.util.WorldUtils;
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
public class TileEntityDungeonCore extends TileEntity
{
	/** Various timed event types; Generic type is merely a counter until all enemies defeated */
	private static enum TimedEvent {GENERIC,DESERT_BATTLE,OCEAN_BATTLE,FOREST_BATTLE,MOUNTAIN_BATTLE,NETHER_BATTLE};
	
	/** The bounding box of the associated structure*/
	protected StructureBoundingBox box;
	
	/** Whether room should check for locked door blocks when validating */
	private boolean isLocked = false;
	
	/** Side in which the door is located; always located at centerX or centerZ of that side */
	private int doorSide;
	
	/** Type of dungeon which this is, based on biome; only used for locked dungeons */
	private BossType dungeonType;
	
	/** Set to true when the door is unlocked; allows for triggering events */
	private boolean isOpened;
	
	/** Whether this tile entity is also a fairy spawner */
	protected boolean isSpawner = false;
	
	/** Max fairies this spawner can spawn before it must refresh */
	private int maxFairies = 0;
	
	/** Number of fairies already spawned; this gets reset at a specific time each night */
	private int fairiesSpawned = 0;
	
	/** Minimum date before next spawn reset */
	private int nextResetDate = 0;
	
	/** Scheduled update timer used for processing nearby dropped items */
	private int itemUpdate = -1;
	
	/** Player who initiated the next scheduled item update */
	private String playerName = "";
	
	/** Number of rupees (emeralds) donated to the fairy */
	private int rupees = 0;
	
	/** Event timer; when it reaches zero, the entity will remove itself */
	private int eventTimer = 0;
	
	/** Type of event that is occurring */
	private TimedEvent event = null;
	
	/** Minecraft game settings difficulty level; set when event triggered */
	private int difficulty = -1;
	
	/** Set to true when structure is broken; prevents second call of verifyStructure */
	private boolean alreadyVerified = false;
	
	public TileEntityDungeonCore() {}
	
	/** Call after setting the block to set the dungeon's structure bounding box */
	public void setDungeonBoundingBox(StructureBoundingBox box) { this.box = box; }
	
	/** Returns the bounding box for this dungeon; may be null */
	public StructureBoundingBox getDungeonBoundingBox() { return box; }
	
	/** Sets the side in which the door is located, the dungeon's type, and sets isLocked to true */
	public void setDoorSideAndType(int side, BossType type) {
		doorSide = side;
		dungeonType = type;
		isLocked = true;
	}
	
	/** Returns whether this tile is a spawner */
	public boolean isSpawner() { return isSpawner; }
	
	/**
	 * Sets this tile entity to act as a fairy spawner
	 */
	public void setSpawner() {
		isSpawner = true;
		maxFairies = worldObj.rand.nextInt(5) + 2;
		if (box == null) {
			alreadyVerified = true;
			setDungeonBoundingBox(new StructureBoundingBox(xCoord - 3, yCoord, zCoord - 3, xCoord + 3, yCoord + 4, zCoord + 3));
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
	 * Returns true every 30 ticks if there is a nearby player and no event is active
	 */
	private boolean shouldUpdate() {
		return (eventTimer == 0 && worldObj.getWorldTime() % 20 == 0 && worldObj.getClosestPlayer((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D, 16.0D) != null);
	}
	
	@Override
	public void updateEntity() {
		if (worldObj.isRemote) { return; }
		
		if (eventTimer == 0 && isOpened && worldObj.getClosestPlayer((double) xCoord + 0.5D, (double) yCoord + 0.5D, (double) zCoord + 0.5D, (double)(box.getXSize() - 2) / 2.0D) != null) {
			if (triggerEvent()) {
				//LogHelper.log(Level.INFO, "Removing core block after event triggered");
				removeCoreBlock();
			}
			return;
		}
		
		if (eventTimer > 0) {
			updateEvent();
		} else if (shouldUpdate()) {
			//LogHelper.log(Level.INFO, "Verifying structure during update at " + xCoord + "/" + yCoord + "/" + zCoord);
			if (!alreadyVerified && box != null && !verifyStructure(false)) {
				//LogHelper.log(Level.INFO, "Failed verification; setting blocks to stone");
				verifyStructure(true);
				alreadyVerified = true;
				if (isLocked) {
					isOpened = true;
				} else {
					//LogHelper.log(Level.INFO, "Calling removeCoreBlock after all blocks set to stone");
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_SECRET_MEDLEY, 1.0F, 1.0F);
					removeCoreBlock();
				}
			} else if (isSpawner) {
				updateSpawner();
			}
		}
	}
	
	protected void updateEvent() {
		--eventTimer;
		if (eventTimer % 20 == 0 && areAllEnemiesDead()) {
			eventTimer = 0;
		} else if (eventTimer < 40){
			eventTimer += 40;
		}
		if (eventTimer == 0 || event == null) {
			//LogHelper.log(Level.INFO, "Event timer has reached zero - removing core");
			StructureGenUtils.replaceMaterialWith(worldObj, box.minX, box.maxX, box.minY, box.maxY, box.minZ, box.maxZ, Material.lava, 0, 0);
			WorldUtils.spawnXPOrbsWithRandom(worldObj, worldObj.rand, xCoord, box.getCenterY() + 1, zCoord, 1000 * difficulty);
			// TODO play victory music
			worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_SECRET_MEDLEY, 1.0F, 1.0F);
			ZSSMain.atlasHelper.placeCustomTile(worldObj, ModInfo.ATLAS_DUNGEON_ID + dungeonType.ordinal() + "_fin", xCoord, yCoord, zCoord);
			removeCoreBlock();
		} else {
			handleEventTick();
		}
	}
	
	protected void updateSpawner() {
		if (fairiesSpawned < maxFairies && worldObj.rand.nextFloat() < (worldObj.isDaytime() ? 0.01F : 0.2F)) {
			int nearby = worldObj.getEntitiesWithinAABB(EntityFairy.class, AxisAlignedBB.getAABBPool().
					getAABB(xCoord, box.getCenterY(), zCoord, (xCoord + 1), (box.getCenterY() + 1), (zCoord + 1)).
					expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2)).size();
			if (nearby < 4) {
				EntityFairy fairy = new EntityFairy(worldObj);
				fairy.setFairyHome(xCoord, yCoord + 2, zCoord);
				worldObj.spawnEntityInWorld(fairy);
				if (++fairiesSpawned == maxFairies) {
					nextResetDate = Calendar.DATE + worldObj.rand.nextInt(Config.getDaysToRespawn()) + 1;
					//System.out.println("Max fairy spawn count reached; current date is: " + Calendar.DATE + "; next reset date is: " + nextResetDate);
				}
			}
		}
		
		if (fairiesSpawned == maxFairies && worldObj.getWorldTime() > 12000 && worldObj.getWorldTime() < 12250 && Calendar.DATE >= nextResetDate) {
			fairiesSpawned = 0;
		}
		
		if (itemUpdate > 0) {
			--itemUpdate;
		} else if (itemUpdate == 0) {
			EntityPlayer player = worldObj.getPlayerEntityByName(playerName);
			List<EntityItem> list = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getAABBPool().
					getAABB(xCoord, box.getCenterY(), zCoord, (xCoord + 1), (box.getCenterY() + 1), (zCoord + 1)).
					expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2));
			for (EntityItem item : list) {
				ItemStack stack = item.getEntityItem();
				if (stack.getItem() == Item.emerald) {
					worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, "random.orb", 1.0F, 1.0F);
					rupees += stack.stackSize;
					item.setDead();
				} else if (stack.getItem() instanceof IFairyUpgrade && ((IFairyUpgrade) stack.getItem()).hasFairyUpgrade(stack)) {
					((IFairyUpgrade) stack.getItem()).handleFairyUpgrade(item, player, this);
				}
			}
			
			itemUpdate = -1;
			playerName = "";
		}
	}
	
	/**
	 * Removes the core block; if it's a spawner, the tile entity is kept intact
	 * Called only when validation fails during an update, not when block broken
	 */
	protected void removeCoreBlock() {
		//LogHelper.log(Level.INFO, "Removing core block from update at " + xCoord + "/" + yCoord + "/" + zCoord);
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (isSpawner) {
			// dungeon core block without bit8 can be broken normally
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & ~0x8), 2);
		} else {
			worldObj.setBlock(xCoord, yCoord, zCoord, BlockSecretStone.getIdFromMeta(meta), 0, 2);
		}
	}
	
	/**
	 * Called after the time opened counts down to zero;
	 * triggers different events for different dungeon types
	 * @return true if the tile entity should remove itself immediately
	 */
	protected boolean triggerEvent() {
		difficulty = worldObj.difficultySetting;
		//TODO worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_BOSS_BATTLE, 1.0F, 1.0F);
		fillDoor();
		removeHinderBlock();
		generateBossMobs(4);
		switch(dungeonType) {
		case HELL:
			event = TimedEvent.NETHER_BATTLE;
			eventTimer = 3600;
			break;
		case DESERT:
			if (difficulty > 1) {
				event = TimedEvent.DESERT_BATTLE;
				eventTimer = 3200;
				handleDispensers(false);
				if (difficulty == 3) {
					setSoulSandFloor();
				}
			}
			break;
		case OCEAN:
			event = TimedEvent.OCEAN_BATTLE;
			eventTimer = 4800; // three minutes to defeat boss + one minute falling sand
			break;
		case MOUNTAIN:
			if (difficulty > 1) {
				event = TimedEvent.MOUNTAIN_BATTLE;
				eventTimer = 3600;
			}
			break;
		case FOREST:
			if (difficulty > 1) {
				event = TimedEvent.FOREST_BATTLE;
				eventTimer = 3600;
			}
			StructureGenUtils.fillWithoutReplace(worldObj, box.minX + 1, box.minX + 2, box.minY + 1, box.minY + 4, box.minZ + 1, box.maxZ, Block.web.blockID, 0, 3);
			StructureGenUtils.fillWithoutReplace(worldObj, box.maxX - 1, box.maxX, box.minY + 1, box.minY + 4, box.minZ + 1, box.maxZ, Block.web.blockID, 0, 3);
			StructureGenUtils.fillWithoutReplace(worldObj, box.minX + 2, box.maxX - 1, box.minY + 1, box.minY + 4, box.minZ + 1, box.minZ + 2, Block.web.blockID, 0, 3);
			StructureGenUtils.fillWithoutReplace(worldObj, box.minX + 2, box.maxX - 1, box.minY + 1, box.minY + 4, box.maxZ - 1, box.maxZ, Block.web.blockID, 0, 3);
			break;
		default:
		}
		if (event == null) {
			event = TimedEvent.GENERIC;
			eventTimer = 200;
		}

		return eventTimer < 1;
	}
	
	/**
	 * Performs one of several timed effects, depending on the type
	 */
	protected void handleEventTick() {
		if (difficulty == -1) {
			difficulty = worldObj.difficultySetting;
		}
		switch(event) {
		case DESERT_BATTLE:
			if (eventTimer % (250 - (difficulty * 50)) == 0) {
				handleDispensers(true);
			}
			break;
		case OCEAN_BATTLE:
			if (eventTimer < 1201 && eventTimer % (140 - (difficulty * 20)) == 0) {
				worldObj.playSoundEffect(xCoord + 0.5D, box.getCenterY(), zCoord + 0.5D, ModInfo.SOUND_ROCK_FALL, 1.0F, 1.0F);
				StructureGenUtils.fillWithoutReplace(worldObj, box.minX + 1, box.maxX, box.maxY-1, box.maxY, box.minZ + 1, box.maxZ, Block.sand.blockID, 0, 3);
			}
			break;
		case FOREST_BATTLE:
			if (eventTimer % 400 == 0) {
				for (int i = 0; i < (difficulty + 2); ++i) {
					setRandomBlockTo(Block.web.blockID, 0, ModInfo.SOUND_WEB_SPLAT);
				}
			}
			if (eventTimer % 600 == 0) {
				destroyRandomPillar(difficulty == 3);
			}
			break;
		case MOUNTAIN_BATTLE:
			if (eventTimer % (600 - (difficulty * 100)) == 0) {
				int x = box.minX + worldObj.rand.nextInt(box.getXSize() - 1) + 1;
				int y = box.maxY - 1;
				int z = box.minZ + worldObj.rand.nextInt(box.getZSize() - 1) + 1;
				if (Math.abs(box.getCenterX() - x) > 1 && Math.abs(box.getCenterZ() - z) > 1) {
					float radius = 1.5F + (float)(difficulty * 0.5F);
					EntityBomb bomb = new EntityBomb(worldObj).setRadius(radius).addTime((3 - difficulty) * 16);
					bomb.setPosition(x, y, z);
					if (!worldObj.isRemote) {
						worldObj.playSoundEffect(x, y, z, ModInfo.SOUND_BOMB_WHISTLE, 1.0F, 1.0F);
						worldObj.spawnEntityInWorld(bomb);
					}
				}
			}
			break;
		case NETHER_BATTLE:
			if (eventTimer % (550 - (difficulty * 100)) == 0) {
				setRandomBlockTo(Block.lavaStill.blockID, 0, "");
			}
			if (eventTimer % (800 - (difficulty * 50)) == 0) {
				spawnMobInCorner(new EntitySkeleton(worldObj), worldObj.rand.nextInt(4), true);
			}
			if (eventTimer % 500 == 0) {
				destroyRandomPillar(true);
			}
			break;
		default:
		}
	}
	
	/**
	 * Returns true if all boss enemies have been defeated and timed event should end
	 */
	protected boolean areAllEnemiesDead() {
		// TODO instead, add all enemies by id to a List and check if still alive in world
		return (worldObj.getEntitiesWithinAABB(IMob.class, AxisAlignedBB.getAABBPool().getAABB(xCoord, box.getCenterY(), zCoord, (xCoord + 1), (box.getCenterY() + 1), (zCoord + 1)).expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2)).isEmpty());
	}
	
	/**
	 * Fills in the doorway and other holes in the structure with blocks
	 */
	protected void fillDoor() {
		int blockId = BlockSecretStone.getIdFromMeta(worldObj.getBlockMetadata(xCoord, yCoord, zCoord));
		for (int i = box.minX; i <= box.maxX; ++i) {
			for (int j = box.minY; j <= box.maxY; ++j) {
				for (int k = box.minZ; k <= box.maxZ; ++k) {
					if (i == box.minX || i == box.maxX || j == box.minY || j == box.maxY || k == box.minZ || k == box.maxZ) {
						if (!worldObj.isBlockOpaqueCube(i, j, k)) {
							worldObj.setBlock(i, j, k, blockId, 0, 2);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Removes the block placed to prevent early plundering of loot
	 */
	protected void removeHinderBlock() {
		int x = box.getCenterX() + (doorSide == RoomBoss.EAST ? 1 : (doorSide == RoomBoss.WEST ? -1 : 0));
		int z = box.getCenterZ() + (doorSide == RoomBoss.SOUTH ? 1 : (doorSide == RoomBoss.NORTH ? -1 : 0));
		int y = (worldObj.getBlockId(x, box.minY + 2, z) == ZSSBlocks.secretStone.blockID ? box.minY + 2 : box.minY + 3);
		worldObj.setBlockToAir(x, y, z);
	}
	
	/**
	 * Sets a random block in the structure to the blockId given if that block is air
	 * @param sound the sound to play, if any
	 */
	protected void setRandomBlockTo(int blockId, int meta, String sound) {
		int x = box.minX + worldObj.rand.nextInt(box.getXSize() - 1) + 1;
		int y = box.minY + worldObj.rand.nextInt(4) + 3;
		int z = box.minZ + worldObj.rand.nextInt(box.getZSize() - 1) + 1;
		if (worldObj.isAirBlock(x, y, z)) {
			worldObj.setBlock(x, y, z, blockId, meta, 3);
			if (sound.length() > 0) {
				worldObj.playSoundEffect(x, y, z, sound, 1.0F, 1.0F);
			}
		}
	}
	
	/**
	 * Sets dungeon floor to soulsand
	 */
	protected void setSoulSandFloor() {
		for (int i = box.minX + 1; i < box.maxX; ++i) {
			for (int j = box.minZ + 1; j < box.maxZ; ++j) {
				if (worldObj.getBlockId(i, box.minY, j) == Block.sandStone.blockID) {
					worldObj.setBlock(i, box.minY, j, Block.slowSand.blockID);
				}
			}
		}
	}
	
	/**
	 * Places dispensers in the centers of all four walls and fills them with arrows
	 * @param activate whether the dispensers are being activated (shot) or placed
	 */
	protected void handleDispensers(boolean activate) {
		int j = box.minY + 2;
		for (int side = 0; side < 4; ++side) {
			int minX = (side == RoomBoss.EAST ? box.maxX : side == RoomBoss.WEST ? box.minX : (box.minX + 3));
			int maxX = (side == RoomBoss.SOUTH || side == RoomBoss.NORTH ? box.maxX - 2 : minX + 1);
			int minZ = (side == RoomBoss.SOUTH ? box.maxZ : side == RoomBoss.NORTH ? box.minZ : (box.minZ + 3));
			int maxZ = (side == RoomBoss.EAST || side == RoomBoss.WEST ? box.maxZ - 2 : minZ + 1);
			
			for (int i = minX; i < maxX; ++i) {
				for (int k = minZ; k < maxZ; ++k) {
					if (activate) {
						if (worldObj.getBlockId(i, j, k) == Block.dispenser.blockID) { 
							Block.blocksList[Block.dispenser.blockID].updateTick(worldObj, i, j, k, worldObj.rand);
						}
					} else {
						worldObj.setBlock(i, j, k, Block.dispenser.blockID);
						worldObj.setBlockMetadataWithNotify(i, j, k, RoomBoss.facingToOrientation[(side + 2) % 4], 2);
						TileEntity te = worldObj.getBlockTileEntity(i, j, k);
						if (te instanceof IInventory) {
							WorldUtils.addItemToInventory(new ItemStack(Item.arrow,(difficulty == 3 ? 24 : 16)), (IInventory) te);
						}
					}
				}
			}
		}
	}

	/**
	 * Spawns the dungeon's boss or mini-boss
	 * @param number the number of boss entities to spawn
	 */
	protected void generateBossMobs(int number) {
		for (int i = 0; i < number; ++i) {
			Entity mob = BossType.getNewMob(dungeonType, worldObj);
			if (mob != null) {
				spawnMobInCorner(mob, i, true);
			}
		}
	}
	
	/**
	 * Sets the entity's position near the given corner (0-3)
	 * @param corner 0 NW, 1 NE, 2 SW, 3 SE
	 * @param equip whether to equip the entity and boost health as a boss mob
	 */
	protected void spawnMobInCorner(Entity mob, int corner, boolean equip) {
		int x = (corner < 2 ? box.minX + 2 : box.maxX - 2);
		int z = (corner % 2 == 0 ? box.minZ + 2 : box.maxZ - 2);
		int y = (worldObj.doesBlockHaveSolidTopSurface(x, box.minY + 1, z) ? box.minY + 1 : box.minY + 3);
		WorldUtils.setEntityInStructure(worldObj, mob, x, y, z);
		if (equip && mob instanceof EntityLivingBase) {
			equipEntity((EntityLivingBase) mob);
		}
		if (!worldObj.isRemote) {
			// TODO boss spawn sound 'roar!' or whatever
			worldObj.spawnEntityInWorld(mob);
		}
	}
	
	/**
	 * Equips entity with diamond armor and appropriate weapon; multiplies health based on difficulty setting
	 */
	protected void equipEntity(EntityLivingBase entity) {
		double d =  (2 * difficulty * Config.getBossHealthFactor());
		entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue() * d);
		entity.setHealth(entity.getMaxHealth());

		ItemStack melee = null;
		ItemStack ranged = new ItemStack(Item.bow);
		switch(difficulty) {
		case 1:
			entity.setCurrentItemOrArmor(1, new ItemStack(Item.bootsChain));
			entity.setCurrentItemOrArmor(2, new ItemStack(Item.legsChain));
			entity.setCurrentItemOrArmor(3, new ItemStack(Item.plateChain));
			entity.setCurrentItemOrArmor(4, new ItemStack(Item.helmetChain));
			melee = new ItemStack(Item.swordIron);
			ranged.addEnchantment(Enchantment.power, 1);
			break;
		case 2:
			entity.setCurrentItemOrArmor(1, new ItemStack(Item.bootsIron));
			entity.setCurrentItemOrArmor(2, new ItemStack(Item.legsIron));
			entity.setCurrentItemOrArmor(3, new ItemStack(Item.plateIron));
			entity.setCurrentItemOrArmor(4, new ItemStack(Item.helmetIron));
			melee = new ItemStack(Item.swordIron);
			melee.addEnchantment(Enchantment.sharpness, 2);
			ranged.addEnchantment(Enchantment.punch, 1);
			ranged.addEnchantment(Enchantment.power, 3);
			break;
		case 3:
			entity.setCurrentItemOrArmor(1, new ItemStack(Item.bootsDiamond));
			entity.setCurrentItemOrArmor(2, new ItemStack(Item.legsDiamond));
			entity.setCurrentItemOrArmor(3, new ItemStack(Item.plateDiamond));
			entity.setCurrentItemOrArmor(4, new ItemStack(Item.helmetDiamond));
			melee = new ItemStack(Item.swordDiamond);
			melee.addEnchantment(Enchantment.sharpness, 4);
			melee.addEnchantment(Enchantment.fireAspect, 1);
			ranged.addEnchantment(Enchantment.flame, 1);
			ranged.addEnchantment(Enchantment.punch, 2);
			ranged.addEnchantment(Enchantment.power, 5);
			break;
		}
		if (entity instanceof EntityZombie) {
			((EntityZombie) entity).setCurrentItemOrArmor(0, melee);
		} else if (entity instanceof EntitySkeleton) {
			EntitySkeleton skeleton = (EntitySkeleton) entity;
			if (dungeonType == BossType.HELL) {
				skeleton.setSkeletonType(1);
				skeleton.setCurrentItemOrArmor(0, melee);
			} else {
				skeleton.setCurrentItemOrArmor(0, ranged);
			}
		} else {
			if (entity instanceof EntityOctorok) {
				((EntityOctorok) entity).setOctorokType((byte) 1);
			}
			AttributeInstance attribute = entity.getEntityAttribute(SharedMonsterAttributes.attackDamage);
			AttributeModifier modifier = (new AttributeModifier(UUID.randomUUID(), "Boss Attack Bonus", difficulty * 2.0D, 0)).setSaved(true);
			attribute.applyModifier(modifier);
		}
	}
	
	/**
	 * Destroys part of a random pillar during boss event
	 * @param explode whether a difficulty-scaled explosion should be created as well
	 */
	protected void destroyRandomPillar(boolean explode) {
		int corner = worldObj.rand.nextInt(4);
		int offset = (box.getXSize() < 11 ? 2 : 3);
		int x = (corner < 2 ? ((box.getXSize() < 11 ? box.getCenterX() : box.minX) + offset)
							: ((box.getXSize() < 11 ? box.getCenterX() : box.maxX) - offset));
		int y = box.minY + (worldObj.rand.nextInt(3) + 1);
		int z = (corner % 2 == 0 ? ((box.getZSize() < 11 ? box.getCenterZ() : box.minZ) + offset)
								 : ((box.getZSize() < 11 ? box.getCenterZ() : box.maxZ) - offset));
		if (!worldObj.isAirBlock(x, y, z)) {
			if (explode) {
				float radius = 1.5F + (float)(difficulty * 0.5F);
				CustomExplosion.createExplosion(worldObj, x, y, z, radius, BombType.BOMB_STANDARD);
			}
			worldObj.playSoundEffect(x + 0.5D, box.getCenterY(), z + 0.5D, ModInfo.SOUND_ROCK_FALL, 1.0F, 1.0F);
			StructureGenUtils.destroyBlocksAround(worldObj, x - 1, x + 2, y, box.maxY - 2, z - 1, z + 2, -1, false);
		}
	}
	
	/**
	 * Returns false if the structure is locked and the door blocks have been removed
	 */
	protected boolean verifyDoor() {
		if (isLocked && box != null && hasWorldObj()) {
			int x = box.getCenterX();
			int z = box.getCenterZ();
			switch(doorSide) {
			case RoomBoss.SOUTH: z = box.maxZ; break;
			case RoomBoss.NORTH: z = box.minZ; break;
			case RoomBoss.EAST: x = box.maxX; break;
			case RoomBoss.WEST: x = box.minX; break;
			default: LogHelper.log(Level.WARNING, "Verifying Boss door in Dungeon Core with invalid door side");
			}
			for (int y = box.minY; y < box.maxY; ++y) {
				if (worldObj.getBlockId(x, y, z) == ZSSBlocks.doorLocked.blockID) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Called when core block is broken; re-verifies structure and/or sets all blocks to stone
	 */
	public void onBlockBroken() {
		//LogHelper.log(Level.INFO, "Verifying structure after core block broken at " + xCoord + "/" + yCoord + "/" + zCoord);
		if (!alreadyVerified) {
			//LogHelper.log(Level.INFO, "Wasn't already verified: removing structure after core block broken");
			worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, ModInfo.SOUND_SECRET_MEDLEY, 1.0F, 1.0F);
			verifyStructure(true);
		}
		while (rupees > 0) {
			int k = (rupees > 64 ? 64 : rupees);
			rupees -= k;
			worldObj.playSoundEffect(xCoord + 0.5D, yCoord + 1, zCoord + 0.5D, "random.orb", 1.0F, 1.0F);
			WorldUtils.spawnItemWithRandom(worldObj, new ItemStack(Item.emerald, k), xCoord, yCoord + 2, zCoord);
		}
	}
	
	/**
	 * Returns true if the structure is missing fewer than one block
	 * @param replace if true, all remaining boundary blocks will be replaced with normal stone
	 */
	protected boolean verifyStructure(boolean replace) {
		if (box != null && hasWorldObj()) {
			if (!replace && isLocked) {
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
									//System.out.println("Replacing secret stone block at " + i + "/" + j + "/" + k);
									int meta = worldObj.getBlockMetadata(i, j, k);
									worldObj.setBlock(i, j, k, BlockSecretStone.getIdFromMeta(meta), 0, 2);
								}
							} else {
								if (id != ZSSBlocks.secretStone.blockID && id != ZSSBlocks.dungeonCore.blockID) {
									//LogHelper.log(Level.WARNING, "Block at " + i + "/" + j + "/" + k + " with id " + id + " is invalid for this structure");
									if (++invalid > 2) {
										//LogHelper.log(Level.INFO, "Too many invalid blocks during verification; returning false");
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
		tag.setBoolean("isLocked", isLocked);
		if (isLocked) {
			tag.setInteger("doorSide", doorSide);
			tag.setInteger("dungeonType", dungeonType.ordinal());
			tag.setBoolean("isOpened", isOpened);
			tag.setInteger("eventTimer", eventTimer);
			tag.setInteger("eventType", event != null ? event.ordinal() : -1);
		}
		tag.setBoolean("isSpawner", isSpawner);
		if (isSpawner) {
			tag.setInteger("maxFairies", maxFairies);
			tag.setInteger("spawned", fairiesSpawned);
			tag.setInteger("nextResetDate", nextResetDate);
			tag.setInteger("itemUpdate", itemUpdate);
			tag.setInteger("rupees", rupees);
			tag.setString("playerName", playerName);
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
		isLocked = tag.getBoolean("isLocked");
		if (isLocked) {
			doorSide = tag.getInteger("doorSide");
			dungeonType = BossType.values()[tag.getInteger("dungeonType") % BossType.values().length];
			isOpened = tag.getBoolean("isOpened");
			eventTimer = tag.getInteger("eventTimer");
			event = (tag.getInteger("eventType") == -1 ? null : TimedEvent.values()[tag.getInteger("eventType") % TimedEvent.values().length]);
		}
		isSpawner = tag.getBoolean("isSpawner");
		if (isSpawner) {
			maxFairies = tag.getInteger("maxFairies");
			fairiesSpawned = tag.getInteger("spawned");
			nextResetDate = tag.getInteger("nextResetDate");
			itemUpdate = tag.getInteger("itemUpdate");
			rupees = tag.getInteger("rupees");
			playerName = tag.getString("playerName");
		}
	}
}

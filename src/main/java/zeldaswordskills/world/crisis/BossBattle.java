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

package zeldaswordskills.world.crisis;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeInstance;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.CustomExplosion;
import zeldaswordskills.block.BlockSecretStone;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.EntityOctorok;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.lib.Sounds;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * Base class for boss dungeon battle events can also be used for generic battles.
 * Extend to add more specific behaviors.
 *
 */
public class BossBattle extends AbstractCrisis
{
	/** The dungeon core in which the battle is occurring */
	protected final TileEntityDungeonCore core;
	/** The bounding box of the associated boss dungeon*/
	protected final StructureBoundingBox box;
	/** The difficulty setting in effect when the battle was begun */
	protected int difficulty = -1;

	/**
	 * Sets event timer to default value of 6000 (5 minutes).
	 * When the core loads from NBT, the core's world object is null, so expect that.
	 * @param core must have both a valid BossType and a valid StructureBoundingBox
	 */
	public BossBattle(TileEntityDungeonCore core) {
		this.core = core;
		this.box = core.getDungeonBoundingBox();
		this.eventTimer = 6000;
		if (core.getBossType() == null) {
			throw new IllegalArgumentException("Dungeon Core must have a valid BossType!");
		}
		if (box == null) {
			throw new IllegalArgumentException("Dungeon Core bounding box can not be null!");
		}
	}

	/**
	 * Commences the epic battle: sets difficulty, fills in the dungeon door, spawns boss mobs, etc.
	 * Be sure to call super and schedule the first update tick when overriding.
	 */
	@Override
	public void beginCrisis(World world) {
		// TODO play boss battle music
		difficulty = world.difficultySetting;
		fillAllGaps(world);
		generateBossMobs(world, 4);
		core.removeHinderBlock();
	}

	/**
	 * Handles everything that happens at the end of battle;
	 * default spawns xp, plays victory sound, and marks the dungeon as complete on the Atlas Map
	 */
	@Override
	protected void endCrisis(World world) {
		// TODO play victory music instead of secret medley:
		world.playSoundEffect(box.getCenterX() + 0.5D, box.getCenterY() + 1, box.getCenterZ() + 0.5D, Sounds.SECRET_MEDLEY, 1.0F, 1.0F);
		if (world.difficultySetting > 0) {
			WorldUtils.spawnXPOrbsWithRandom(world, world.rand, box.getCenterX(), box.getCenterY(), box.getCenterZ(), 1000 * difficulty);
		}
		ZSSMain.atlasHelper.placeCustomTile(world, ModInfo.ATLAS_DUNGEON_ID + core.getBossType().ordinal() + "_fin", core.xCoord, core.yCoord, core.zCoord);
	}

	@Override
	protected boolean canCrisisConclude(World world) {
		return areAllEnemiesDead(world);
	}

	/**
	 * Nothing happens in generic boss battle update tick; no need to call super.
	 */
	@Override
	protected void onUpdateTick(World world) {}

	/**
	 * Returns true if all boss enemies have been defeated and the crisis should end
	 */
	private boolean areAllEnemiesDead(World world) {
		// TODO instead, add all enemies by id to a List and check if still alive in world
		return (world.getEntitiesWithinAABB(IMob.class, AxisAlignedBB.getAABBPool().getAABB(
				box.getCenterX() - 0.5D, box.getCenterY(), box.getCenterZ() - 0.5D,
				box.getCenterX() + 0.5D, box.getCenterY() + 1, box.getCenterZ() + 0.5D).
				expand(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2)).isEmpty());
	}

	/**
	 * Destroys part of a random pillar during boss event
	 * @param explode whether a difficulty-scaled explosion should be created as well
	 */
	protected void destroyRandomPillar(World world, boolean explode) {
		int corner = world.rand.nextInt(4);
		int offset = (box.getXSize() < 11 ? 2 : 3);
		int x = (corner < 2 ? ((box.getXSize() < 11 ? box.getCenterX() : box.minX) + offset)
				: ((box.getXSize() < 11 ? box.getCenterX() : box.maxX) - offset));
		int y = box.minY + (world.rand.nextInt(3) + 1);
		int z = (corner % 2 == 0 ? ((box.getZSize() < 11 ? box.getCenterZ() : box.minZ) + offset)
				: ((box.getZSize() < 11 ? box.getCenterZ() : box.maxZ) - offset));
		if (!world.isAirBlock(x, y, z)) {
			if (explode) {
				float radius = 1.5F + (float)(difficulty * 0.5F);
				CustomExplosion.createExplosion(world, x, y, z, radius, BombType.BOMB_STANDARD);
			}
			world.playSoundEffect(x + 0.5D, box.getCenterY(), z + 0.5D, Sounds.ROCK_FALL, 1.0F, 1.0F);
			StructureGenUtils.destroyBlocksAround(world, x - 1, x + 2, y, box.maxY - 2, z - 1, z + 2, -1, false);
		}
	}

	/**
	 * Fills in the doorway and all other holes in the structure with appropriate blocks
	 */
	protected void fillAllGaps(World world) {
		int blockId = BlockSecretStone.getIdFromMeta(world.getBlockMetadata(core.xCoord, core.yCoord, core.zCoord));
		for (int i = box.minX; i <= box.maxX; ++i) {
			for (int j = box.minY; j <= box.maxY; ++j) {
				for (int k = box.minZ; k <= box.maxZ; ++k) {
					if (i == box.minX || i == box.maxX || j == box.minY || j == box.maxY || k == box.minZ || k == box.maxZ) {
						if (!world.isBlockOpaqueCube(i, j, k)) {
							world.setBlock(i, j, k, blockId, 0, 2);
						}
					}
				}
			}
		}
	}

	/**
	 * Sets dungeon floor to block and meta given
	 * @param toReplace if null, checks for standard dungeon blocks instead
	 */
	protected void setDungeonFloorTo(World world, Block block, int meta, Block toReplace) {
		int replace = (toReplace != null ? toReplace.blockID : BlockSecretStone.getIdFromMeta(world.getBlockMetadata(core.xCoord, core.yCoord, core.zCoord)));
		for (int i = box.minX + 1; i < box.maxX; ++i) {
			for (int j = box.minZ + 1; j < box.maxZ; ++j) {
				if (world.getBlockId(i, box.minY, j) == replace) {
					world.setBlock(i, box.minY, j, block.blockID, meta, 2);
				}
			}
		}
	}

	/**
	 * Sets a random block in the structure to the block given if the current block is air
	 * @param sound the sound to play, if any
	 */
	protected void setRandomBlockTo(World world, Block block, int meta, String sound) {
		int x = box.minX + world.rand.nextInt(box.getXSize() - 1) + 1;
		int y = box.minY + world.rand.nextInt(4) + 3;
		int z = box.minZ + world.rand.nextInt(box.getZSize() - 1) + 1;
		if (world.isAirBlock(x, y, z)) {
			world.setBlock(x, y, z, block.blockID, meta, 3);
			if (sound.length() > 0) {
				world.playSoundEffect(x, y, z, sound, 1.0F, 1.0F);
			}
		}
	}

	/**
	 * Spawns the dungeon's boss or mini-boss
	 * @param number the number of boss entities to spawn
	 */
	public final void generateBossMobs(World world, int number) {
		for (int i = 0; i < number; ++i) {
			Entity mob = core.getBossType().getNewMob(world);
			if (mob != null) {
				spawnMobInCorner(world, mob, i, true, true);
			}
		}
	}

	/**
	 * Sets the entity's position near the given corner (0-3)
	 * @param corner	0 NW, 1 NE, 2 SW, 3 SE
	 * @param equip		whether to equip the entity with boss-level gear
	 * @param health	whether to grant the entity boss-level health
	 */
	protected void spawnMobInCorner(World world, Entity mob, int corner, boolean equip, boolean health) {
		int x = (corner < 2 ? box.minX + 2 : box.maxX - 2);
		int z = (corner % 2 == 0 ? box.minZ + 2 : box.maxZ - 2);
		int y = (world.doesBlockHaveSolidTopSurface(x, box.minY + 1, z) ? box.minY + 1 : box.minY + 3);
		WorldUtils.setEntityInStructure(world, mob, x, y, z);
		if (mob instanceof EntityLivingBase) {
			if (health) {
				boostHealth(world, (EntityLivingBase) mob);
			}
			if (equip) {
				equipEntity(world, (EntityLivingBase) mob);
			}
		}
		if (!world.isRemote) {
			// TODO boss spawn sound 'roar!' or whatever
			world.spawnEntityInWorld(mob);
		}
	}

	/**
	 * Multiplies the entity's health based on difficulty and config settings
	 */
	protected void boostHealth(World world, EntityLivingBase entity) {
		double d =  (2 * difficulty * Config.getBossHealthFactor());
		entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue() * d);
		entity.setHealth(entity.getMaxHealth());
	}

	/**
	 * Equips entity with appropriate weapon and armor
	 */
	protected void equipEntity(World world, EntityLivingBase entity) {
		ItemStack melee = null;
		ItemStack ranged = new ItemStack(Item.bow);
		Item[] armorSet = null;
		switch(difficulty) {
		case 1:
			armorSet = new Item[]{Item.bootsChain, Item.legsChain, Item.plateChain, Item.helmetChain};
			melee = new ItemStack(Item.swordIron);
			ranged.addEnchantment(Enchantment.power, 1);
			break;
		case 2:
			armorSet = new Item[]{Item.bootsIron, Item.legsIron, Item.plateIron, Item.helmetIron};
			melee = new ItemStack(Item.swordIron);
			melee.addEnchantment(Enchantment.sharpness, 2);
			ranged.addEnchantment(Enchantment.punch, 1);
			ranged.addEnchantment(Enchantment.power, 3);
			break;
		case 3:
			armorSet = new Item[]{Item.bootsDiamond, Item.legsDiamond, Item.plateDiamond, Item.helmetDiamond};
			melee = new ItemStack(Item.swordDiamond);
			melee.addEnchantment(Enchantment.sharpness, 4);
			melee.addEnchantment(Enchantment.fireAspect, 1);
			ranged.addEnchantment(Enchantment.flame, 1);
			ranged.addEnchantment(Enchantment.punch, 2);
			ranged.addEnchantment(Enchantment.power, 5);
			break;
		}
		if (armorSet != null) {
			for (int i = 0; i < armorSet.length; ++i) {
				ItemStack armor = new ItemStack(armorSet[i]);
				EnchantmentHelper.addRandomEnchantment(world.rand, armor, difficulty + world.rand.nextInt(difficulty * 5));
				entity.setCurrentItemOrArmor(i + 1, armor);
			}
		}
		if (entity instanceof EntityZombie) {
			((EntityZombie) entity).setCurrentItemOrArmor(0, melee);
		} else if (entity instanceof EntitySkeleton) {
			EntitySkeleton skeleton = (EntitySkeleton) entity;
			if (core.getBossType() == BossType.HELL) {
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

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("difficulty", difficulty);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		difficulty = compound.getInteger("difficulty");
	}
}

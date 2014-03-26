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

package zeldaswordskills.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraftforge.common.ChestGenHooks;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.item.HookshotType;
import zeldaswordskills.item.ItemHookShotUpgrade.AddonType;
import zeldaswordskills.item.ItemKeyBig;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.LibPotionID;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.WorldUtils;
import zeldaswordskills.world.gen.structure.RoomBase;
import zeldaswordskills.world.gen.structure.RoomBoss;

/**
 * 
 * Thanks to FatherToast for his tips on registering chests with Forge.
 *
 */
public class DungeonLootLists
{
	/**
	 * Adds all weighted contents to the specified category, setting minimum and
	 * maximum number of items that can generate in this chest category
	 */
	public static void setCategoryStats(ChestGenHooks category, int min, int max, WeightedRandomChestContent[] contents) {
		category.setMin(min);
		category.setMax(max);
		for (WeightedRandomChestContent item : contents) {
			category.addItem(item);
		}
	}

	/** Item version: Creates a weighted chest content for a generic item with the min, max, and weight */
	public static WeightedRandomChestContent getLoot(Item item, int min, int max, int weight) {
		return new WeightedRandomChestContent(new ItemStack(item), min, max, weight);
	}

	/** Item with damage version: Creates a weighted chest content with the min, max, and weight */
	public static WeightedRandomChestContent getLoot(Item item, int damage, int min, int max, int weight) {
		return new WeightedRandomChestContent(new ItemStack(item, 1, damage), min, max, weight);
	}

	public static String
	BASIC_LOOT = "zss.basic_chest_loot",
	BOSS_LOOT = "zss.boss_chest_loot",
	JAR_DROPS = "zss.jar_drops",
	LOCKED_LOOT = "zss.locked_chest_loot",
	NETHER_LOOT = "zss.nether_chest_loot";

	/** List of droppable skill orb items as weighted chest contents, not including the bonus heart */
	private static final List<WeightedRandomChestContent> skillOrbLootList = new ArrayList<WeightedRandomChestContent>();

	/**
	 * Initializes all of the loot tables for dungeon generation
	 */
	public static void initLootLists() {
		initBasicConsumables();
		initBossLoot();
		initJarDrops();
		initLockedLoot();
		initNetherLoot();
		initSkillOrbLoot();
	}

	/** Returns the list of droppable skill orbs */
	public static WeightedRandomChestContent[] getSkillOrbList() {
		List<WeightedRandomChestContent> lootTable = new ArrayList<WeightedRandomChestContent>(skillOrbLootList);
		return lootTable.toArray(new WeightedRandomChestContent[lootTable.size()]);
	}

	/**
	 * Generates the chest contents for non-boss secret rooms, adding appropriate location-based items and locked chest loot
	 */
	public static void generateChestContents(World world, Random rand, IInventory chest, RoomBase room, boolean isLockedChest) {
		ChestGenHooks info = ChestGenHooks.getInfo(BASIC_LOOT);
		WeightedRandomChestContent.generateChestContents(rand, info.getItems(rand), chest, info.getCount(rand));
		int n = rand.nextInt(3);
		if (n > 0 && (isLockedChest || rand.nextInt(4) == 0)) {
			if (room.inLava) {
				WeightedRandomChestContent.generateChestContents(rand, lavaLoot, chest, n);
			} else if (room.inOcean) {
				WeightedRandomChestContent.generateChestContents(rand, oceanLoot, chest, n);
			} else if (room.inMountain) {
				WeightedRandomChestContent.generateChestContents(rand, mountainLoot, chest, n);
			} else {
				WeightedRandomChestContent.generateChestContents(rand, regularLoot, chest, n);
			}
		}
		if (room.inNether) {
			info = ChestGenHooks.getInfo(NETHER_LOOT);
			WorldUtils.generateRandomChestContents(rand, info.getItems(rand), chest, info.getCount(rand), true);
		}
		if (isLockedChest) {
			info = ChestGenHooks.getInfo(LOCKED_LOOT);
			WeightedRandomChestContent.generateChestContents(rand, info.getItems(rand), chest, info.getCount(rand));
			if (!(room instanceof RoomBoss)) {
				if (rand.nextFloat() < Config.getRandomBossItemChance()) {
					WorldUtils.addItemToInventoryAtRandom(rand, ChestGenHooks.getInfo(BOSS_LOOT).getOneItem(rand), chest, 3);
				}
				if (rand.nextInt(20) < Config.getBigKeyWeight()) {
					ItemStack key = ItemKeyBig.getKeyForBiome(world, room.getBoundingBox().getCenterX(), room.getBoundingBox().getCenterZ());
					if (key != null) {
						WorldUtils.addItemToInventoryAtRandom(rand, key, chest, 3);
					}
				}
			}
		}
	}

	/**
	 * Generates a random number of items plus special boss chest loot
	 */
	public static void generateBossChestContents(World world, Random rand, IInventory chest, RoomBoss room) {
		generateChestContents(world, rand, chest, room, true);
		ChestGenHooks info = ChestGenHooks.getInfo(BOSS_LOOT);
		WeightedRandomChestContent.generateChestContents(rand, info.getItems(rand), chest, info.getCount(rand));
		WorldUtils.addItemToInventoryAtRandom(rand, new ItemStack(ZSSItems.skillOrb,1,SkillBase.bonusHeart.getId()), chest, 3);
		// special items that always generate, i.e. the Pendants of Virtue
		ItemStack stack = room.getBossType().getSpecialItem();
		if (stack != null) {
			WorldUtils.addItemToInventoryAtRandom(rand, stack, chest, 3);
		}
		// possibly select a random special item from the boss type's list
		stack = room.getBossType().getRandomSpecialItem(rand);
		if (stack != null && rand.nextFloat() < 0.2F) {
			WorldUtils.addItemToInventoryAtRandom(rand, stack, chest, 3);
		} else {
			WorldUtils.generateRandomChestContents(rand, getSkillOrbList(), chest, 1, false);
		}
	}

	private static void initBasicConsumables() {
		setCategoryStats(ChestGenHooks.getInfo(BASIC_LOOT), Config.getMinNumItems(), Config.getMinNumItems() + 4, new WeightedRandomChestContent[] {
			getLoot(Item.appleRed, 1, 2, 5),
			getLoot(Item.bread, 1, 2, 5),
			getLoot(Item.compass, 1, 2, 4),
			getLoot(Item.emptyMap, 1, 3, 4),
			getLoot(Item.expBottle, 1, 5, 5),
			getLoot(Item.nameTag, 1, 2, 5),
			getLoot(Item.diamond, 1, 2, 2),
			getLoot(Item.ingotGold, 1, 2, 3),
			getLoot(Item.ingotIron, 1, 2, 4),
			getLoot(Item.glassBottle, 1, 3, 5),
			getLoot(Item.emerald, 2, 5, 7),
			getLoot(Item.arrow, 3, 7, 7),
			getLoot(Item.melonSeeds, 1, 2, 3),
			getLoot(ZSSItems.bombBag, 1, 1, Config.getBombBagWeight() / 2),
			getLoot(ZSSItems.keySmall, 1, 1, Config.getSmallKeyWeight()),
			getLoot(ZSSItems.potionRed, 1, 1, 3),
			getLoot(ZSSItems.potionGreen, 1, 1, 1),
			getLoot(ZSSItems.shieldDeku, 1, 1, 1),
			getLoot(ZSSItems.swordBroken, ZSSItems.swordKokiri.itemID, 1, 1, 1)
		});
	}

	private static void initBossLoot() {
		setCategoryStats(ChestGenHooks.getInfo(BOSS_LOOT), 1, 2, new WeightedRandomChestContent[] {
			getLoot(ZSSItems.arrowFire, 7, 15, 1),
			getLoot(ZSSItems.arrowIce, 7, 15, 1),
			getLoot(ZSSItems.arrowLight, 3, 7, 1),
			getLoot(ZSSItems.bombBag, 1, 1, 1),
			getLoot(ZSSItems.boomerang, 1, 1, 1),
			getLoot(ZSSItems.bootsHeavy, 1, 1, 1),
			getLoot(ZSSItems.bootsHover, 1, 1, 1),
			getLoot(ZSSItems.bootsPegasus, 1, 1, 1),
			getLoot(ZSSItems.bootsRubber, 1, 1, 1),
			getLoot(ZSSItems.crystalSpirit, 1, 1, 1),
			getLoot(ZSSItems.dekuLeaf, 1, 1, 1),
			getLoot(ZSSItems.hammer, 1, 1, 1),
			getLoot(ZSSItems.heroBow, 1, 1, 1),
			getLoot(ZSSItems.hookshot, HookshotType.WOOD_SHOT.ordinal(), 1, 1, 1),
			getLoot(ZSSItems.hookshotAddon, AddonType.EXTENSION.ordinal(), 1, 1, 1),
			getLoot(ZSSItems.hookshotAddon, AddonType.STONECLAW.ordinal(), 1, 1, 1),
			getLoot(ZSSItems.hookshotAddon, AddonType.MULTI.ordinal(), 1, 1, 1),
			getLoot(ZSSItems.keySkeleton, 1, 1, 1),
			getLoot(ZSSItems.magicMirror, 1, 1, 1),
			getLoot(ZSSItems.masterOre, 1, 1, 1),
			getLoot(ZSSItems.potionBlue, 1, 1, 1),
			getLoot(ZSSItems.rocsFeather, 1, 1, 1),
			getLoot(ZSSItems.shieldHylian, 1, 1, 1),
			getLoot(ZSSItems.slingshot, 1, 1, 1),
			getLoot(ZSSItems.treasure, Treasures.ZELDAS_LETTER.ordinal(), 1, 1, 1),
		});
	}

	private static void initJarDrops() {
		setCategoryStats(ChestGenHooks.getInfo(JAR_DROPS), 1, 1, new WeightedRandomChestContent[] {
			getLoot(ZSSItems.bomb, BombType.BOMB_STANDARD.ordinal(), 1, 1, 1),
			getLoot(ZSSItems.potionRed, 1, 1, 3),
			getLoot(ZSSItems.potionGreen, 1, 1, 1),
			getLoot(ZSSItems.smallHeart, 1, 1, 5),
			getLoot(ZSSItems.dekuNut, 1, 1, 4),
			getLoot(Item.arrow, 1, 1, 5),
			getLoot(Item.emerald, 1, 1, 10)
		});
	}
	
	private static final WeightedRandomChestContent[] lavaLoot = {
		getLoot(Item.netherStalkSeeds, 1, 2, 1),
		getLoot(Item.potion, LibPotionID.FIRERESIST.id, 1, 1, 2),
		getLoot(Item.fireballCharge, 1, 2, 3),
		getLoot(ZSSItems.arrowBombFire, 2, 5, 3),
		getLoot(ZSSItems.bomb, BombType.BOMB_FIRE.ordinal(), 1, 2, Config.getBombWeight() * 2),
		getLoot(ZSSItems.tunicGoronHelm, 1, 1, 1),
		getLoot(ZSSItems.tunicGoronChest, 1, 1, 1),
		getLoot(ZSSItems.tunicGoronLegs, 1, 1, 1)
	};

	private static void initLockedLoot() {
		setCategoryStats(ChestGenHooks.getInfo(LOCKED_LOOT), 1, 3, new WeightedRandomChestContent[] {
			getLoot(ZSSItems.arrowFire, 2, 5, Config.getLockedLootWeight()),
			getLoot(ZSSItems.arrowIce, 2, 5, Config.getLockedLootWeight()),
			getLoot(ZSSItems.arrowLight, 1, 3, 1),
			getLoot(Item.appleGold, 1, 2, Math.max(Config.getLockedLootWeight() / 3, 1)),
			getLoot(Item.potion, LibPotionID.HEALING_II.id, 1, 1, Math.max(Config.getLockedLootWeight() / 2, 2)),
			getLoot(Item.potion, LibPotionID.HEALING_SPLASH.id, 1, 1, Math.max(Config.getLockedLootWeight() / 2, 2)),
			getLoot(ZSSItems.bombBag, 1, 1, Config.getBombBagWeight()),
			getLoot(ZSSItems.magicMirror, 1, 1, Config.getLockedLootWeight()),
			getLoot(ZSSItems.potionBlue, 1, 1, 1),
			getLoot(ZSSItems.swordOrdon, 1, 1, Config.getLockedLootWeight()),
			getLoot(ZSSItems.tunicHeroBoots, 1, 1, 1),
			getLoot(ZSSItems.tunicHeroLegs, 1, 1, 1),
			getLoot(ZSSItems.tunicHeroChest, 1, 1, 1),
			getLoot(ZSSItems.tunicHeroHelm, 1, 1, 1)
		});
	}
	
	private static final WeightedRandomChestContent[] mountainLoot = {
		getLoot(Item.potion, LibPotionID.STRENGTH_II.id, 1, 1, 1),
		getLoot(Item.potion, LibPotionID.STRENGTH.id, 1, 1, 3),
		getLoot(Item.diamond, 1, 3, 3),
		getLoot(ZSSItems.arrowBomb, 2, 5, 3),
		getLoot(ZSSItems.bomb,BombType.BOMB_STANDARD.ordinal(), 1, 2, Config.getBombWeight() * 2),
		getLoot(ZSSItems.rocsFeather, 1, 1, 1),
		getLoot(ZSSItems.swordBroken, ZSSItems.swordOrdon.itemID, 1, 1, 1)
	};

	private static void initNetherLoot() {
		setCategoryStats(ChestGenHooks.getInfo(NETHER_LOOT), 1, 2, new WeightedRandomChestContent[] {
			getLoot(Item.ghastTear, 1, 2, 1),
			getLoot(Item.netherStalkSeeds, 1, 2, 1),
			getLoot(Item.potion, LibPotionID.FIRERESIST.id, 1, 1, 2),
			getLoot(Item.blazeRod, 1, 3, 2),
			getLoot(Item.fireballCharge, 1, 3, 3),
			getLoot(Item.magmaCream, 1, 3, 3),
			getLoot(ZSSItems.arrowBombFire, 2, 5, 3),
			getLoot(ZSSItems.bomb, BombType.BOMB_FIRE.ordinal(), 1, 2, Config.getBombWeight() * 2)
		});
	}
	
	private static final WeightedRandomChestContent[] oceanLoot = {
		getLoot(Item.fishingRod, 1, 1, 2),
		getLoot(Item.fishRaw, 1, 2, 4),
		getLoot(ZSSItems.arrowBombWater, 2, 5, 3),
		getLoot(ZSSItems.bomb,BombType.BOMB_WATER.ordinal(), 1, 2, Config.getBombWeight() * 2),
		getLoot(ZSSItems.tunicZoraHelm, 1, 1, 1),
		getLoot(ZSSItems.tunicZoraChest, 1, 1, 1),
		getLoot(ZSSItems.tunicZoraLegs, 1, 1, 1)
	};
	
	private static final WeightedRandomChestContent[] regularLoot = {
		getLoot(Item.horseArmorDiamond, 1, 1, 1),
		getLoot(Item.horseArmorGold, 1, 1, 1),
		getLoot(Item.horseArmorIron, 1, 1, 2),
		getLoot(Item.saddle, 1, 1, 3),
		getLoot(ZSSItems.arrowBomb, 2, 5, 3),
		getLoot(ZSSItems.bomb,BombType.BOMB_STANDARD.ordinal(), 1, 2, Config.getBombWeight() * 2),
		getLoot(ZSSItems.swordBroken, ZSSItems.swordOrdon.itemID, 1, 1, 1),
		getLoot(ZSSItems.swordKokiri, 1, 1, 1)
	};

	private static void initSkillOrbLoot() {
		for (SkillBase skill : SkillBase.getSkills()) {
			if (skill.isLoot()) {
				skillOrbLootList.add(getLoot(ZSSItems.skillOrb, skill.getId(), 1, 1, Config.getLockedLootWeight()));
			}
		}
	}
}

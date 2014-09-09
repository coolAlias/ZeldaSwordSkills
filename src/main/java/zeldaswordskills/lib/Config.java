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

package zeldaswordskills.lib;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.skills.BonusHeart;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.BossType;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class Config
{
	public static Configuration config;
	/*================== MOD INTER-COMPATIBILITY =====================*/
	/** [BattleGear2] Allow Master Swords to be held in the off-hand */
	private static boolean enableOffhandMaster;
	/*================== GENERAL =====================*/
	/** Whether players can be stunned; if false, item use is still interrupted */
	private static boolean enableStunPlayer;
	/** Whether the swing speed timer prevents all left-clicks, or only items that use swing speeds */
	private static boolean enableSwingSpeed;
	/** Default swing speed (anti-left-click-spam): Sets base number of ticks between each left-click (0 to disable)[0-20] */
	private static int baseSwingSpeed;
	/** Hardcore Zelda Fan: Start with only 3 hearts (applies a -14 max health modifier, so it can be enabled or disabled at any time) */
	private static boolean enableHardcoreZeldaFanMode;
	/** Whether vanilla blocks can be picked up using appropriate items (e.g. gauntlets) */
	private static boolean enableVanillaLift;
	/** Whether vanilla blocks can be smashed using appropriate items (e.g. hammers) */
	private static boolean enableVanillaSmash;
	/** Always pick up small hearts regardless of health */
	private static boolean alwaysPickupHearts;
	/** [Boss] Boss health multiplier, as a percent increase per difficulty level (will not apply to real bosses) [100-500] */
	private static int bossHealthFactor;
	/** [Boss] Number of boss mobs to spawn in Boss Dungeons (will not apply to real bosses) [1-8] */
	private static int bossNumber;
	/** [Ceramic Jars] Allow ceramic jars to generate in water */
	private static boolean allowJarsInWater;
	/** [Ceramic Jars][Surface] Chance of generating a jar cluster in a given chunk */
	private static int jarGenChance;
	/** [Ceramic Jars][Surface] Max number of jars per cluster */
	private static int jarsPerCluster;
	/** [Ceramic Jars][Underground] Chance for each jar cluster to generate */
	private static int jarGenChanceSub;
	/** [Ceramic Jars][Underground] Max number of jars per cluster */
	private static int jarsPerClusterSub;
	/** [Ceramic Jars][Underground] Max number of jar clusters per chunk */
	private static int jarClustersPerChunkSub;
	/** [Ceramic Jars][Nether] Chance for each jar cluster to generate */
	private static int jarGenChanceNether;
	/** [Ceramic Jars][Nether] Max number of jars per cluster */
	private static int jarsPerClusterNether;
	/** [Ceramic Jars][Nether] Max number of jar clusters per chunk */
	private static int jarClustersPerChunkNether;
	/** [Mobs][Keese] Chance of Keese spawning in a swarm */
	private static int keeseSwarmChance;
	/** [Mobs][Keese] Maximum number of Keese that can spawn in a swarm */
	private static int keeseSwarmSize;
	/** [Sacred Flames] Number of days before flame rekindles itself (0 to disable) [0-30] */
	private static int sacredRefreshRate;
	/** Whether to show a chat message when striking secret blocks */
	private static boolean showSecretMessage;
	/** [Mob Buff] Disable all buffs (resistances and weaknesses) for vanilla mobs */
	private static boolean disableVanillaBuffs;
	/*================== Buff Bar HUD =====================*/
	/** [Buff HUD] Whether the buff bar should be displayed by default */
	private static boolean isBuffBarEnabled;
	/** [Buff HUD] Whether the buff bar should be displayed horizontally */
	private static boolean isBuffBarHorizontal;
	/** [Buff HUD] Whether the buff bar should be displayed on the left side of the screen */
	private static boolean isBuffBarLeft;
	/*================== ITEMS =====================*/
	/** [Arrows] Whether transforming arrows with the Sacred Flames has a chance to consume the flame */
	private static boolean arrowsConsumeFlame;
	/** [Bombs] Minimum fuse time; set to 0 to disable held bomb ticks */
	private static int bombFuseTime;
	/** [Bombs] Whether bombs are non-griefing, i.e. can only destroy secret stone */
	private static boolean onlyBombSecretStone;
	/** [Bombs] Whether bombs can destroy regular blocks in Adventure Mode */
	private static boolean bombsGriefAdventure;
	/** [Deku Leaf] Allow Deku Leaf whirlwind to destroy leaves */
	private static boolean enableDekuDenude;
	/** [Din's Fire] Whether Din's Fire can set blocks on fire */
	private static boolean enableDinIgnite;
	/** [Din's Fire] Whether Din's Fire can melt unbreakable ice blocks */
	private static boolean enableDinMelt;
	/** [Enchantments] Disable the vanilla behavior allowing unenchantable items to be enchanted using the anvil */
	private static boolean disableAllUnenchantables;
	/** [Hero's Bow] Cost (in emeralds) to upgrade, per level */
	private static int heroBowUpgradeCost;
	/** [Hero's Bow] Whether the fire arrow can ignite affected blocks */
	private static boolean enableFireArrowIgnite;
	/** [Hero's Bow] Whether the fire arrow can destroy unbreakable ice blocks */
	private static boolean enableFireArrowMelt;
	/** [Hero's Bow] Whether the light arrow can penetrate blocks */
	private static boolean enableLightArrowNoClip;
	/** [Hero's Bow] Whether to automate bomb arrow firing when sneaking */
	private static boolean enableAutoBombArrows;
	/** [Hookshot] Max range of non-extended hookshots */
	private static int hookshotRange;
	/** [Hookshot] Whether hookshots are allowed to destroy certain blocks such as glass */
	private static boolean enableHookshotBreakBlocks;
	/** [Hookshot] Whether to play the 'itembreak' sound when the hookshot misses */
	private static boolean enableHookshotSound;
	/** [Magic Rods] Cost (in emeralds) to upgrade (note that the Tornado Rod costs 3/4 this value) [128-1280] */
	private static int rodUpgradeCost;
	/** [Master Sword] Number of mobs that need to be killed to upgrade the Tempered Sword */
	private static int temperedRequiredKills;
	/** [Slingshot] Cost (in emeralds) for first upgrade */
	private static int slingshotUpgradeOne;
	/** [Slingshot] Cost (in emeralds) for second upgrade */
	private static int slingshotUpgradeTwo;
	/*================== STARTING GEAR =====================*/
	/** Whether starting gear will be granted */
	public static boolean enableStartingGear;
	/** Whether starting gear is automatically equipped when granted */
	public static boolean enableAutoEquip;
	/** Begin the game with Link's House - place it anywhere you like! */
	public static boolean enableLinksHouse;
	/** Grants a single Basic Sword skill orb */
	public static boolean enableOrb;
	/** Grants the full set of Kokiri clothing: hat, tunic, trousers, boots */
	public static boolean enableFullSet;
	/** Grants only the Kokiri Tunic */
	public static boolean enableTunic;
	/** Grants the Kokiri sword (a named wooden sword) */
	public static boolean enableSword;
	/*================== SKILLS =====================*/
	/** Whether to use default movement controls to activate skills such as Dodge */
	private static boolean allowVanillaControls;
	/** Whether Dodge and Parry require double-tap or not (double-tap always required for vanilla movement keys) */
	private static boolean doubleTap;
	/** Max number of bonus 1/2 hearts (capped at 80) */
	private static int maxBonusHearts;
	/** Whether auto-targeting is enabled or not */
	private static boolean autoTarget;
	/** Whether players can be targeted */
	private static boolean enablePlayerTarget;
	/** Number of combo hits to display */
	private static int hitsToDisplay;
	/** [Parry] Bonus to disarm based on timing: tenths of a percent added per tick remaining on the timer [0-50] */
	private static int disarmTimingBonus;
	/** [Parry] Penalty to disarm chance: percent per Parry level of the opponent, default negates defender's skill bonus so disarm is based entirely on timing [0-20] */
	private static int disarmPenalty;
	/** [Super Spin Attack | Sword Beam] True to require a completely full health bar to use, or false to allow a small amount to be missing per level */
	private static boolean requireFullHealth;
	/*================== DUNGEON GEN =====================*/
	/** Whether to prevent ZSS structures from generating if any non-vanilla blocks are detected */
	private static boolean avoidModBlocks;
	/** Whether boss dungeons are allowed to have windows or not */
	private static boolean enableWindows;
	/** Enable Boss Dungeon generation */
	private static boolean enableBossDungeons;
	/** Minimum number of chunks between Boss Dungeons in the Overworld */
	private static int minBossDistance;
	/** Minimum number of chunks between Boss Dungeons in the Nether */
	private static int minBossDistanceNether;
	/** Minimum number of blocks between land-based secret rooms */
	private static int minLandDistance;
	/** Minimum number of blocks between nether-based secret rooms */
	private static int minDistanceNether;
	/** Minimum number of blocks between ocean-based secret rooms */
	private static int minOceanDistance;
	/** Max number of overworld secret room generation attempts per chunk (capped at 10) */
	private static int genAttemptsPerChunk;
	/** Chance of each iteration to attempt to generate a secret room in the overworld */
	private static int secretRoomChance;
	/** Difficulty level for finding overworld dungeons (1 very easy, 3 very hard) */
	private static int mainDungeonDifficulty;
	/** Max number of Nether secret room generation attempts per chunk (capped at 10) */
	private static int genAttemptsPerChunkNether;
	/** Chance of each iteration to attempt to generate a secret room in the Nether */
	private static int secretRoomChanceNether;
	/** Difficulty level for finding nether dungeons (1 very easy, 3 very hard) */
	private static int netherDungeonDifficulty;
	/** Chance (as a percent) for certain dungeons to have fairy spawners */
	private static int fairySpawnerChance;
	/** Maximum number of days required for fairies to replenish */
	private static int resetSpawnerTime;
	/*================== LOOT =====================*/
	/** Chance (as a percent) a chest will be locked */
	private static int lockedChestChance;
	/** Chance that a secret room may have two chests */
	private static int doubleChestChance;
	/** Chance that a secret room's entrance will be barred by some obstacle */
	private static int barredRoomChance;
	/** Chance of a heart piece always appearing in secret room chests */
	private static int heartPieceChance;
	/** Chance of a random boss-level item being added to locked chest loot table */
	private static int randomBossItemChance;
	/** Minimum number of random chest contents for first chest */
	private static int minNumChestItems;
	/** Random loot generation weights for each individual item */
	private static int  bombWeight,
	bombBagWeight,
	heartPieceWeight,
	bigKeyWeight,
	smallKeyWeight;
	/** Loot weight for items in locked chests */
	private static int lockedLootWeight;
	/*================== DROPS =====================*/
	/** Chance of grass dropping loot (set to zero to disable) */
	private static int grassDropChance;
	/** Chance of empty jars dropping loot (set to zero to disable) */
	private static int jarDropChance;
	/** Creeper bomb drop chance */
	private static int creeperDrop;
	/** [Skill Orbs] Enable skill orbs to drop as loot from mobs */
	private static boolean enableOrbDrops;
	/** [Skill Orbs] Chance of dropping random orb */
	private static int randomDropChance;
	/** [Skill Orbs] Chance for unmapped mob to drop an orb */
	private static int genericMobDropChance;
	/** [Skill Orbs] Individual drop chances for skill orbs and heart pieces */
	private static Map<Byte, Integer> orbDropChance;
	/** [Piece of Power] Approximate number of enemies you need to kill before a piece of power drops */
	private static int powerDropRate;
	/*================== TRADES =====================*/
	/** [Bomb Bag] Enable random villager trades for bomb bags */
	private static boolean enableTradeBombBag;
	/** [Bomb Bag] Minimum price (in emeralds) for a bomb bag */
	private static int minBombBagPrice;
	/** [Bombs] Enable random villager trades for bombs */
	private static boolean enableTradeBomb;
	/** [Hero's Bow] Whether magic arrows (fire, ice, light) can be purchased */
	private static boolean enableArrowTrades;
	/** [Masks] Chance that a villager will be interested in purchasing a random mask */
	private static int maskBuyChance;
	/** Number of trades required before a villager offers other services */
	private static int friendTradesRequired;

	public static void init(FMLPreInitializationEvent event) {
		config = new Configuration(new File(event.getModConfigurationDirectory().getAbsolutePath() + ModInfo.CONFIG_PATH));
		config.load();
		ZSSEntities.init(config);
		ZSSItems.init(config);

		/*================== MOD INTER-COMPATIBILITY =====================*/
		enableOffhandMaster = config.get("Mod Support", "[BattleGear2] Allow Master Swords to be held in the off-hand", false).getBoolean(false);
		/*================== GENERAL =====================*/
		enableStunPlayer = config.get("General", "Whether players can be stunned; if false, item use is still interrupted", false).getBoolean(false);
		enableSwingSpeed = config.get("General", "Whether the swing speed timer prevents all left-clicks, or only items that use swing speeds", true).getBoolean(true);
		baseSwingSpeed = config.get("General", "Default swing speed (anti-left-click-spam): Sets base number of ticks between each left-click (0 to disable)[0-20]", 0).getInt();
		enableVanillaLift = config.get("General", "Whether vanilla blocks can be picked up using appropriate items (e.g. gauntlets)", true).getBoolean(true);
		enableVanillaSmash = config.get("General", "Whether vanilla blocks can be smashed using appropriate items (e.g. hammers)", true).getBoolean(true);
		alwaysPickupHearts = config.get("General", "Always pick up small hearts regardless of health", false).getBoolean(false);
		enableHardcoreZeldaFanMode = config.get("General", "Hardcore Zelda Fan: Start with only 3 hearts (applies a -14 max health modifier, so it can be enabled or disabled at any time)", false).getBoolean(false);
		bossHealthFactor = config.get("General", "[Boss] Boss health multiplier, as a percent increase per difficulty level (will not apply to real bosses) [100-500]", 250).getInt();
		bossNumber = config.get("General", "[Boss] Number of boss mobs to spawn in Boss Dungeons (will not apply to real bosses) [1-8]", 4).getInt();
		allowJarsInWater = config.get("General", "[Ceramic Jars][Surface] Allow ceramic jars to generate in water", true).getBoolean(true);
		jarGenChance = config.get("General", "[Ceramic Jars][Surface] Chance of generating a jar cluster in a given chunk [0-100]", 50).getInt();
		jarsPerCluster = config.get("General", "[Ceramic Jars][Surface] Max number of jars per jar cluster [2-20]", 8).getInt();
		jarGenChanceSub = config.get("General", "[Ceramic Jars][Underground] Chance for each jar cluster to generate [0-100]", 65).getInt();
		jarsPerClusterSub = config.get("General", "[Ceramic Jars][Underground] Max number of jars per cluster [2-20]", 8).getInt();
		jarClustersPerChunkSub = config.get("General", "[Ceramic Jars][Underground] Max number of jar clusters per chunk [1-20]", 10).getInt();
		jarGenChanceNether = config.get("General", "[Ceramic Jars][Nether] Chance for each jar cluster to generate [0-100]", 50).getInt();
		jarsPerClusterNether = config.get("General", "[Ceramic Jars][Nether] Max number of jars per cluster [2-20]", 8).getInt();
		jarClustersPerChunkNether = config.get("General", "[Ceramic Jars][Nether] Max number of jar clusters per chunk [1-20]", 8).getInt();
		keeseSwarmChance = config.get("General", "[Mobs][Keese] Chance of Keese spawning in a swarm (0 to disable)[0-100]", 25).getInt();
		keeseSwarmSize = config.get("General", "[Mobs][Keese] Maximum number of Keese that can spawn in a swarm [4-16]", 6).getInt();
		sacredRefreshRate = config.get("General", "[Sacred Flames] Number of days before flame rekindles itself (0 to disable) [0-30]", 7).getInt();
		showSecretMessage = config.get("General", "Whether to show a chat message when striking secret blocks", false).getBoolean(false);
		disableVanillaBuffs = config.get("General", "[Mob Buff] Disable all buffs (resistances and weaknesses) for vanilla mobs", false).getBoolean(false);
		/*================== Buff Bar HUD =====================*/
		isBuffBarEnabled = config.get("General", "[Buff HUD] Whether the buff bar should be displayed at all times", true).getBoolean(true);
		isBuffBarHorizontal = config.get("General", "[Buff HUD] Whether the buff bar should be displayed horizontally", true).getBoolean(true);
		isBuffBarLeft = config.get("General", "[Buff HUD] Whether the buff bar should be displayed on the left side of the screen", false).getBoolean(false);
		/*================== ITEMS =====================*/
		arrowsConsumeFlame = config.get("Item", "[Arrows] Whether transforming arrows with the Sacred Flames has a chance to consume the flame", true).getBoolean(true);
		bombFuseTime = config.get("Item", "[Bombs] Minimum fuse time; set to 0 to disable held bomb ticks [0-128]", 56).getInt();
		onlyBombSecretStone = config.get("Item", "[Bombs] Whether bombs are non-griefing, i.e. can only destroy secret stone", false).getBoolean(false);
		// TODO bombsGriefAdventure = config.get("Item", "[Bombs] Whether bombs can destroy regular blocks in Adventure Mode", false).getBoolean(false);
		enableDekuDenude = config.get("Item", "[Deku Leaf] Allow Deku Leaf whirlwind to destroy leaves", true).getBoolean(true);
		enableDinIgnite = config.get("Item", "[Din's Fire] Whether Din's Fire can set blocks on fire", false).getBoolean(false);
		enableDinMelt = config.get("Item", "[Din's Fire] Whether Din's Fire can melt unbreakable ice blocks", true).getBoolean(true);
		disableAllUnenchantables = config.get("Item", "[Enchantments] Disable the vanilla behavior allowing unenchantable items to be enchanted using the anvil", false).getBoolean(false);
		heroBowUpgradeCost = config.get("Item", "[Hero's Bow] Cost (in emeralds) to upgrade, per level [128 - 640]", 192).getInt();
		enableFireArrowIgnite = config.get("Item", "[Hero's Bow] Whether the fire arrow can ignite affected blocks", true).getBoolean(true);
		enableFireArrowMelt = config.get("Item", "[Hero's Bow] Whether the fire arrow can melt unbreakable ice blocks", true).getBoolean(true);
		enableLightArrowNoClip = config.get("Item", "[Hero's Bow] Whether the light arrow can penetrate blocks", true).getBoolean(true);
		enableAutoBombArrows = config.get("Item", "[Hero's Bow] Whether to automate bomb arrow firing when sneaking", true).getBoolean(true);
		hookshotRange = config.get("Item","[Hookshot] Max range of non-extended hookshots [4-16]", 8).getInt();
		enableHookshotBreakBlocks = config.get("Item", "[Hookshot] Whether hookshots are allowed to destroy certain blocks such as glass", true).getBoolean(true);
		enableHookshotSound = config.get("Item", "[Hookshot] Whether to play the 'itembreak' sound when the hookshot misses", true).getBoolean(true);
		rodUpgradeCost = config.get("Item", "[Magic Rods] Cost (in emeralds) to upgrade (note that the Tornado Rod costs 3/4 this value) [128-1280]", 768).getInt();
		temperedRequiredKills = config.get("Item", "[Master Sword] Number of mobs that need to be killed to upgrade the Tempered Sword [100-1000]", 300).getInt();
		slingshotUpgradeOne = config.get("Item", "[Slingshot] Cost (in emeralds) for first upgrade [64- 320]", 128).getInt();
		slingshotUpgradeTwo = config.get("Item", "[Slingshot] Cost (in emeralds) for second upgrade [128 - 640]", 320).getInt();
		/*================== STARTING GEAR =====================*/
		enableStartingGear = config.get("Bonus Gear", "Enable bonus starting equipment", false).getBoolean(false);
		enableAutoEquip = config.get("Bonus Gear", "Automatically equip starting equipment", true).getBoolean(true);
		enableLinksHouse = config.get("Bonus Gear", "Begin the game with Link's House - place it anywhere you like!", true).getBoolean(true);
		enableOrb = config.get("Bonus Gear", "Grants a single Basic Sword skill orb", true).getBoolean(true);
		enableFullSet = config.get("Bonus Gear", "Grants a full set of Kokiri clothing: hat, tunic, trousers, boots", true).getBoolean(true);
		enableTunic = config.get("Bonus Gear", "Grants only a Kokiri Tunic (if full set is disabled)", true).getBoolean(true);
		enableSword = config.get("Bonus Gear", "Grants a Kokiri sword", true).getBoolean(true);
		/*================== SKILLS =====================*/
		allowVanillaControls = config.get("Skills", "Allow vanilla controls to activate skills", true).getBoolean(true);
		autoTarget = config.get("Skills", "Enable auto-targeting of next opponent", true).getBoolean(true);
		enablePlayerTarget = config.get("Skills", "Enable targeting of players by default (can be toggled in game)", true).getBoolean(true);
		doubleTap = config.get("Skills", "Require double tap activation (double-tap always required for vanilla movement keys)", true).getBoolean(true);
		maxBonusHearts = config.get("Skills", "Max Bonus Hearts [0-50]", 20).getInt();
		hitsToDisplay = config.get("Skills", "Max hits to display in Combo HUD [0-12]", 3).getInt();
		disarmTimingBonus = config.get("Skills", "[Parry] Bonus to disarm based on timing: tenths of a percent added per tick remaining on the timer [0-50]", 25).getInt();
		disarmPenalty = config.get("Skills", "[Parry] Penalty to disarm chance: percent per Parry level of the opponent, default negates defender's skill bonus so disarm is based entirely on timing [0-20]", 10).getInt();
		requireFullHealth = config.get("Skills", "[Super Spin Attack | Sword Beam] True to require a completely full health bar to use, or false to allow a small amount to be missing per level", false).getBoolean(false);
		/*================== DUNGEON GEN =====================*/
		avoidModBlocks = config.get("Dungeon Generation", "Whether to prevent ZSS structures from generating if any non-vanilla blocks are detected", true).getBoolean(true);
		enableWindows = config.get("Dungeon Generation", "Whether boss dungeons are allowed to have windows or not", true).getBoolean(true);
		enableBossDungeons = config.get("Dungeon Generation", "Enable Boss Dungeon generation", true).getBoolean(true);
		mainDungeonDifficulty = config.get("Dungeon Generation", "[Overworld] Adjust secret rooms so they are more hidden [1 = less, 3 = most]", 2).getInt();
		secretRoomChance = config.get("Dungeon Generation", "[Overworld] Chance (as a percent) per iteration of secret room generating [1-100]", 80).getInt();
		minLandDistance = config.get("Dungeon Generation", "[Overworld] Minimum number of blocks between land-based secret rooms [2-16]", 6).getInt();
		minOceanDistance = config.get("Dungeon Generation", "[Overworld] Minimum number of blocks between ocean-based secret rooms [2-32]", 6).getInt();
		minBossDistance = config.get("Dungeon Generation", "[Overworld] Minimum number of chunks between Boss Dungeons [8-128]", 24).getInt();
		genAttemptsPerChunk = config.get("Dungeon Generation", "[Overworld] Secret room generation attempts per chunk (0 to disable) [0-20]", 12).getInt();
		netherDungeonDifficulty = config.get("Dungeon Generation", "[Nether] Adjust secret rooms so they are more hidden [1 = less, 3 = most]", 2).getInt();
		secretRoomChanceNether = config.get("Dungeon Generation", "[Nether] Chance (as a percent) per iteration of secret room generating [1-100]", 80).getInt();
		minDistanceNether = config.get("Dungeon Generation", "[Nether] Minimum number of blocks between land-based secret rooms [2-16]", 6).getInt();
		minBossDistanceNether = config.get("Dungeon Generation", "[Nether] Minimum number of chunks between Boss Dungeons [8-64]", 12).getInt();
		genAttemptsPerChunkNether = config.get("Dungeon Generation", "[Nether] Secret room generation attempts per chunk (0 to disable) [0-20]", 12).getInt();
		fairySpawnerChance = config.get("Dungeon Generation", "Chance (as a percent) for certain dungeons to have fairy spawners [0-100]", 10).getInt();
		resetSpawnerTime = config.get("Dungeon Generation", "Maximum number of days required for fairies to replenish [2-10]", 7).getInt();
		/*================== LOOT =====================*/
		lockedChestChance = config.get("Loot", "Chance (as a percent) a chest will be locked [10-50]", 33).getInt();
		doubleChestChance = config.get("Loot", "Chance (as a percent) a secret room may have two chests [0-25]", 10).getInt();
		barredRoomChance = config.get("Loot", "Chance that a secret room's entrance will be barred by some obstacle [1-50]", 25).getInt();
		heartPieceChance = config.get("Loot", "Chance (as a percent) of a heart piece generating in secret room chests [0-100]", 60).getInt();
		randomBossItemChance = config.get("Loot", "Chance (as a percent) of a random boss-level item being added to locked chest loot table [0-50]", 25).getInt();
		minNumChestItems = config.get("Loot", "Minimum number of random chest contents for first chest [1-10]", 4).getInt();
		bombWeight = config.get("Loot", "Weight: Bomb [1-10]", 5).getInt();
		bombBagWeight = config.get("Loot", "Weight: Bomb Bag (locked chest weight only) [1-10]", 3).getInt();
		heartPieceWeight = config.get("Loot", "Weight: Heart Piece (vanilla chests only) [1-10]", 1).getInt();
		bigKeyWeight = config.get("Loot", "Weight: Key, Big [1-10]", 3).getInt();
		smallKeyWeight = config.get("Loot", "Weight: Key, Small [1-10]", 4).getInt();
		lockedLootWeight = config.get("Loot", "Weight: Locked Chest Content [1-10]", 3).getInt();
		/*================== DROPS =====================*/
		grassDropChance = config.get("Drops", "Chance (as a percent) of loot dropping from grass [0-100]", 15).getInt();
		jarDropChance = config.get("Drops", "Chance (as a percent) of loot dropping from empty jars when broken [0-100]", 20).getInt();
		creeperDrop = config.get("Drops", "Chance (as a percent) for creepers to drop bombs [0-100]", 10).getInt();
		enableOrbDrops = config.get("Drops", "[Skill Orbs] Enable skill orbs to drop as loot from mobs", true).getBoolean(true);
		randomDropChance = config.get("Drops", "[Skill Orbs] Chance (as a percent) for specified mobs to drop a random orb [0-100]", 10).getInt();
		genericMobDropChance = config.get("Drops", "[Skill Orbs] Chance (as a percent) for random mobs to drop a random orb [0-100]", 1).getInt();
		orbDropChance = new HashMap<Byte, Integer>(SkillBase.getNumSkills());
		for (SkillBase skill : SkillBase.getSkills()) {
			if (skill.canDrop()) {
				orbDropChance.put(skill.getId(), config.get("Drops", "[Skill Orbs] Chance (in tenths of a percent) for " + skill.getDisplayName() + " [0-10]", 5).getInt());
			}
		}
		powerDropRate = config.get("Drops", "[Piece of Power] Approximate number of enemies you need to kill before a piece of power drops [minimum 20]", 50).getInt();
		/*================== TRADES =====================*/
		friendTradesRequired = config.get("Trade", "Number of unlocked trades required before a villager considers you 'friend' [3+]", 6).getInt();
		enableTradeBombBag = config.get("Trade", "[Bomb Bag] Enable random villager trades for bomb bags", true).getBoolean(true);
		minBombBagPrice = config.get("Trade", "[Bomb Bag] Minimum price (in emeralds) [32-64]", 64).getInt();
		enableTradeBomb = config.get("Trade", "[Bombs] Enable random villager trades for bombs", true).getBoolean(true);
		enableArrowTrades = config.get("Trade", "[Hero's Bow] Whether magic arrows (fire, ice, light) can be purchased", true).getBoolean(true);
		maskBuyChance = config.get("Trade", "[Masks] Chance that a villager will be interested in purchasing a random mask [1-15]", 5).getInt();
	}

	public static void postInit() {
		for (BossType type : BossType.values()) {
			BossType.addBiomes(type, config.get("Dungeon Generation", String.format("[Boss Dungeon] List of biomes in which %ss can generate", type.getDisplayName()), type.getDefaultBiomes()).getStringList());
		}
		if (config.hasChanged()) {
			config.save();
		}
	}

	/*================== MOD INTER-COMPATIBILITY =====================*/
	public static boolean allowOffhandMaster() { return enableOffhandMaster; }
	/*================== GENERAL =====================*/
	public static boolean canPlayersBeStunned() { return enableStunPlayer; }
	public static boolean affectAllSwings() { return enableSwingSpeed; }
	public static int getBaseSwingSpeed() { return MathHelper.clamp_int(baseSwingSpeed, 0, 20); }
	public static boolean canLiftVanilla() { return enableVanillaLift; }
	public static boolean canSmashVanilla() { return enableVanillaSmash; }
	public static boolean alwaysPickupHearts() { return alwaysPickupHearts; }
	public static boolean isHardcoreZeldaFan() { return enableHardcoreZeldaFanMode; }
	public static float getBossHealthFactor() { return MathHelper.clamp_float(bossHealthFactor * 0.01F, 1F, 5F); }
	public static int getNumBosses() { return MathHelper.clamp_int(bossNumber, 1, 8); }
	public static boolean genJarsInWater() { return allowJarsInWater; }
	public static float getJarGenChance() { return MathHelper.clamp_float(jarGenChance * 0.01F, 0F, 1F); }
	public static int getJarsPerCluster() { return MathHelper.clamp_int(jarsPerCluster, 2, 20); }
	public static float getJarGenChanceSub() { return MathHelper.clamp_float(jarGenChanceSub * 0.01F, 0F, 1F); }
	public static int getJarClustersPerChunkSub() { return MathHelper.clamp_int(jarClustersPerChunkSub, 1, 20); }
	public static int getJarsPerClusterSub() { return MathHelper.clamp_int(jarsPerClusterSub, 2, 20); }
	public static float getJarGenChanceNether() { return MathHelper.clamp_float(jarGenChanceNether * 0.01F, 0F, 1F); }
	public static int getJarClustersPerChunkNether() { return MathHelper.clamp_int(jarClustersPerChunkNether, 1, 20); }
	public static int getJarsPerClusterNether() { return MathHelper.clamp_int(jarsPerClusterNether, 2, 20); }
	public static int getSacredFlameRefreshRate() { return MathHelper.clamp_int(sacredRefreshRate, 0, 30); }
	public static boolean showSecretMessage() { return showSecretMessage; }
	public static boolean areVanillaBuffsDisabled() { return disableVanillaBuffs; }
	/*================== MOBS =====================*/
	public static float getKeeseSwarmChance() { return (float) MathHelper.clamp_int(keeseSwarmChance, 0, 100) * 0.01F; }
	public static int getKeeseSwarmSize() { return MathHelper.clamp_int(keeseSwarmSize, 4, 16); }
	/*================== BUFF BAR HUD =====================*/
	public static boolean isBuffBarEnabled() { return isBuffBarEnabled; }
	public static boolean isBuffBarHorizontal() { return isBuffBarHorizontal; }
	public static boolean isBuffBarLeft() { return isBuffBarLeft; }
	/*================== ITEMS =====================*/
	public static boolean getArrowsConsumeFlame() { return arrowsConsumeFlame; }
	public static boolean onlyBombSecretStone() { return onlyBombSecretStone; }
	public static boolean canGriefAdventure() { return bombsGriefAdventure; }
	public static int getBombFuseTime() { return MathHelper.clamp_int(bombFuseTime, 0, 128); }
	public static boolean canDekuDenude() { return enableDekuDenude; }
	public static boolean isDinIgniteEnabled() { return enableDinIgnite; }
	public static boolean isDinMeltEnabled() { return enableDinMelt; }
	public static boolean allUnenchantablesAreDisabled() { return disableAllUnenchantables; }
	public static int getHeroBowUpgradeCost() { return MathHelper.clamp_int(heroBowUpgradeCost, 128, 640); }
	public static boolean enableFireArrowIgnite() { return enableFireArrowIgnite; }
	public static boolean enableFireArrowMelt() { return enableFireArrowMelt; }
	public static boolean enableLightArrowNoClip() { return enableLightArrowNoClip; }
	public static boolean enableAutoBombArrows() { return enableAutoBombArrows; }
	public static int getHookshotRange() { return MathHelper.clamp_int(hookshotRange, 4, 16); }
	public static boolean canHookshotBreakBlocks() { return enableHookshotBreakBlocks; }
	public static boolean enableHookshotMissSound() { return enableHookshotSound; }
	public static int getRodUpgradeCost() { return MathHelper.clamp_int(rodUpgradeCost, 128, 1280); }
	public static int getRequiredKills() { return MathHelper.clamp_int(temperedRequiredKills, 100, 1000) - 1; }
	public static int getSlingshotCostOne() { return MathHelper.clamp_int(slingshotUpgradeOne, 64, 320); }
	public static int getSlingshotCostTwo() { return MathHelper.clamp_int(slingshotUpgradeTwo, 128, 640); }
	/*================== SKILLS =====================*/
	public static boolean allowVanillaControls() { return allowVanillaControls; }
	public static boolean requiresDoubleTap() { return doubleTap; }
	public static byte getMaxBonusHearts() { return (byte) MathHelper.clamp_int(maxBonusHearts, 0, BonusHeart.MAX_BONUS_HEARTS); }
	public static boolean autoTargetEnabled() { return autoTarget; }
	public static boolean toggleAutoTarget() { autoTarget = !autoTarget; return autoTarget; }
	public static boolean canTargetPlayers() { return enablePlayerTarget; }
	public static boolean toggleTargetPlayers() { enablePlayerTarget = !enablePlayerTarget; return enablePlayerTarget; }
	public static int getHitsToDisplay() { return Math.max(hitsToDisplay, 0); }
	public static float getDisarmPenalty() { return 0.01F * ((float) MathHelper.clamp_int(disarmPenalty, 0, 20)); }
	public static float getDisarmTimingBonus() { return 0.001F * ((float) MathHelper.clamp_int(disarmTimingBonus, 0, 50)); }
	/** Returns amount of health that may be missing and still be able to activate certain skills (e.g. Sword Beam) */
	public static float getHealthAllowance(int level) {
		return (requireFullHealth ? 0.0F : (0.6F * level));
	}
	/*================== DUNGEON GEN =====================*/
	public static boolean avoidModBlocks() { return avoidModBlocks; }
	public static boolean areWindowsEnabled() { return enableWindows; }
	public static boolean areBossDungeonsEnabled() { return enableBossDungeons; }
	public static int getMinBossDistance() { return MathHelper.clamp_int(minBossDistance, 8, 128); }
	public static int getMinLandDistance() { return MathHelper.clamp_int(minLandDistance, 2, 16); }
	public static int getMinOceanDistance() { return MathHelper.clamp_int(minOceanDistance, 2, 32); }
	public static int getAttemptsPerChunk() { return Math.min(genAttemptsPerChunk, 20); }
	public static float getSecretRoomChance() { return MathHelper.clamp_float(secretRoomChance * 0.01F, 0F, 1F); }
	public static int getMainDungeonDifficulty() { return MathHelper.clamp_int(mainDungeonDifficulty, 1, 3); }
	public static int getNetherMinBossDistance() { return MathHelper.clamp_int(minBossDistanceNether, 8, 64); }
	public static int getNetherMinDistance() { return MathHelper.clamp_int(minDistanceNether, 2, 16); }
	public static int getNetherAttemptsPerChunk() { return Math.min(genAttemptsPerChunkNether, 20); }
	public static float getNetherSecretRoomChance() { return MathHelper.clamp_float(secretRoomChanceNether * 0.01F, 0F, 1F); }
	public static int getNetherDungeonDifficulty() { return MathHelper.clamp_int(netherDungeonDifficulty, 1, 3); }
	public static float getFairySpawnerChance() { return MathHelper.clamp_float(fairySpawnerChance * 0.01F, 0F, 1.0F); }
	public static int getDaysToRespawn() { return MathHelper.clamp_int(resetSpawnerTime, 2, 10); }
	/*================== LOOT =====================*/
	public static float getLockedChestChance() { return MathHelper.clamp_float(lockedChestChance * 0.01F, 0.1F, 0.5F); }
	public static float getDoubleChestChance() { return MathHelper.clamp_float(doubleChestChance * 0.01F, 0F, 0.25F); }
	public static float getBarredRoomChance() { return MathHelper.clamp_float(barredRoomChance * 0.01F, 0.01F, 0.5F); }
	public static float getHeartPieceChance() { return MathHelper.clamp_float(heartPieceChance * 0.01F, 0F, 1F); }
	public static float getRandomBossItemChance() { return MathHelper.clamp_float(randomBossItemChance * 0.01F, 0F, 0.5F); }
	public static int getMinNumItems() { return MathHelper.clamp_int(minNumChestItems, 1, 10); }
	public static int getBombWeight() { return MathHelper.clamp_int(bombWeight, 1, 10); }
	public static int getBombBagWeight() { return MathHelper.clamp_int(bombBagWeight, 1, 10); }
	public static int getHeartWeight() { return MathHelper.clamp_int(heartPieceWeight, 1, 10); }
	public static int getBigKeyWeight() { return MathHelper.clamp_int(bigKeyWeight, 1, 10); }
	public static int getSmallKeyWeight() { return MathHelper.clamp_int(smallKeyWeight, 1, 10); }
	public static int getLockedLootWeight() { return MathHelper.clamp_int(lockedLootWeight, 1, 10); }
	/*================== DROPS =====================*/
	public static float getGrassDropChance() { return MathHelper.clamp_float(grassDropChance * 0.01F, 0F, 1.0F); }
	public static float getJarDropChance() { return MathHelper.clamp_float(jarDropChance * 0.01F, 0F, 1.0F); }
	public static float getCreeperDropChance() { return MathHelper.clamp_float(creeperDrop * 0.01F, 0F, 1.0F); }
	public static boolean areOrbDropsEnabled() { return enableOrbDrops; }
	public static float getChanceForRandomDrop() { return MathHelper.clamp_float(randomDropChance * 0.01F, 0F, 1.0F); }
	public static float getRandomMobDropChance() { return MathHelper.clamp_float(genericMobDropChance * 0.0F, 0F, 1.0F); }
	public static float getDropChance(int orbID) {
		int i = (orbDropChance.containsKey((byte) orbID) ? orbDropChance.get((byte) orbID) : 0);
		return MathHelper.clamp_float(i * 0.001F, 0.0F, 0.01F);
	}
	public static int getPowerDropRate() { return Math.max(powerDropRate, 20); }
	/*================== TRADES =====================*/
	public static boolean enableTradeBomb() { return enableTradeBomb; }
	public static boolean enableTradeBombBag() { return enableTradeBombBag; }
	public static int getMinBombBagPrice() { return Math.max(minBombBagPrice, 32); }
	public static boolean areArrowTradesEnabled() { return enableArrowTrades; }
	public static float getMaskBuyChance() { return MathHelper.clamp_float(maskBuyChance * 0.01F, 0.01F, 0.15F); }
	public static int getFriendTradesRequired() { return Math.max(friendTradesRequired, 3); }

}

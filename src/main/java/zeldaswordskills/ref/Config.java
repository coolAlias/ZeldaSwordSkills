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

package zeldaswordskills.ref;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.client.gui.IGuiOverlay.HALIGN;
import zeldaswordskills.client.gui.IGuiOverlay.VALIGN;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.client.SyncConfigPacket;
import zeldaswordskills.skills.BonusHeart;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.BiomeType;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.WarpPoint;

/**
 * 
 * Fields denoted with [SYNC] need to be sent to each client as they log in
 * to the server. In some cases this is because the field is mainly used
 * on the client (e.g. attack speed), in others because the field is used on
 * both sides and may result in inconsistencies if not synced (e.g. whip length).
 *
 */
public class Config
{
	public static Configuration config;
	/*================== CATEGORIES =====================*/
	public static final String GENERAL = "general",
			CLIENT = "client",
			BUFF_BAR = CLIENT + ".buff bar hud",
			COMBO = CLIENT + ".combo hud",
			ENDING_BLOW = CLIENT + ".ending blow hud",
			ITEM_MODE = CLIENT + ".item mode hud",
			MAGIC_METER = CLIENT + ".magic meter",
			MOD_SUPPORT = "mod support",
			WEAPON_REGISTRY = "weapon registry",
			ITEMS = "item",
			BONUS_GEAR = "bonus gear",
			SKILLS = "skills",
			DUNGEON_GEN = "dungeon generation",
			WORLD_GEN = "world generation",
			LOOT = "loot",
			DROPS = "drops",
			TRADES = "trade",
			MOB_SPAWNS = "mob spawns",
			RECIPES = "recipes";
	/*================== GENERAL =====================*/
	/** [SYNC] Whether players can be stunned; if false, item use is still interrupted */
	private static boolean enableStunPlayer;
	/** [SYNC] Whether the swing speed timer prevents all left-clicks, or only items that use swing speeds */
	private static boolean enableSwingSpeed;
	/** [SYNC] Default swing speed (anti-left-click-spam): Sets base number of ticks between each left-click (0 to disable)[0-20] */
	private static int baseSwingSpeed;
	/** Hardcore Zelda Fan: Start with only 3 hearts (applies a -14 max health modifier, so it can be enabled or disabled at any time) */
	private static boolean enableHardcoreZeldaFanMode;
	/** [SYNC] Whether regular (i.e. breakable) secret stone blocks can be picked up using appropriate items (e.g. gauntlets) */
	private static boolean enableSecretStoneLift;
	/** [SYNC] Whether vanilla blocks can be picked up using appropriate items (e.g. gauntlets) */
	private static boolean enableVanillaLift;
	/** [SYNC] Whether vanilla blocks can be smashed using appropriate items (e.g. hammers) */
	private static boolean enableVanillaSmash;
	/** Always pick up small hearts regardless of health */
	private static boolean alwaysPickupHearts;
	/** [Boss] Boss health multiplier, as a percent increase per difficulty level (will not apply to real bosses) [100-500] */
	private static float bossHealthFactor;
	/** [Boss] Number of boss mobs to spawn in Boss Dungeons (will not apply to real bosses) [1-8] */
	private static int bossNumber;
	/** [Ceramic Jars] Whether ceramic jar tile entities update each tick, allowing them to store dropped items */
	private static boolean enableJarUpdates;
	/** [Mobs][Keese] Chance of Keese spawning in a swarm */
	private static float keeseSwarmChance;
	/** [Mobs][Keese] Maximum number of Keese that can spawn in a swarm */
	private static int keeseSwarmSize;
	/** [Sacred Flames] Number of days before flame rekindles itself (0 to disable) [0-30] */
	private static int sacredRefreshRate;
	/** [Skulltula Tokens] Number of days between each recurring reward for completing the quest (0 to disable recurring reward) [0-30] */
	private static int skulltulaRewardRate;
	/** [Mob Buff] Disable all buffs (resistances and weaknesses) for vanilla mobs */
	private static boolean disableVanillaBuffs;
	/** [NPC] Sets whether Zelda NPCs are invulnerable or not */
	private static boolean npcsAreInvulnerable;
	/** [NPC][Navi] Range at which Navi can sense secret rooms, in blocks (0 to disable) [0-10] */
	private static int naviRange;
	/** [NPC][Navi] Frequency with which Navi checks the proximity for secret rooms, in ticks [20-200] */
	private static int naviFrequency;
	/*=================== BUFF BAR HUD ===================*/
	/** [Buff HUD] Maximum number of icons to display per row or column [1-10] */
	public static int buffBarMaxIcons;
	/** [Buff HUD] Whether the buff bar should be displayed by default */
	public static boolean isBuffBarEnabled;
	/** [Buff HUD] Whether the buff bar should be displayed horizontally */
	public static boolean isBuffBarHorizontal;
	/** [Buff HUD][Alignment: Horizontal] Alignment on the X axis [left|center|right] */
	public static HALIGN buffBarHAlign;
	/** [Buff HUD][Alignment: Vertical] Alignment on the Y axis [top|center|bottom] */
	public static VALIGN buffBarVAlign;
	/** [Buff HUD][Offset: X] Moves the HUD element left (-) or right (+) this number of pixels */
	public static int buffBarOffsetX;
	/** [Buff HUD][Offset: Y] Moves the HUD element up (-) or down (+) this number of pixels */
	public static int buffBarOffsetY;
	/*=================== COMBO HUD ===================*/
	public static boolean isComboHudEnabled;
	/** [Combo HUD] Number of combo hits to display */
	public static int hitsToDisplay;
	/** [Combo HUD][Alignment: Horizontal] Alignment on the X axis [left|center|right] */
	public static HALIGN comboHudHAlign;
	/** [Combo HUD][Alignment: Vertical] Alignment on the Y axis [top|center|bottom] */
	public static VALIGN comboHudVAlign;
	/** [Combo HUD][Offset: X] Moves the HUD element left (-) or right (+) this number of pixels */
	public static int comboHudOffsetX;
	/** [Combo HUD][Offset: Y] Moves the HUD element up (-) or down (+) this number of pixels */
	public static int comboHudOffsetY;
	/*=================== ENDING BLOW HUD ===================*/
	/** [Ending Blow HUD] Enable Ending Blow HUD display (if disabled, there is not any indication that the skill is ready to use) */
	public static boolean isEndingBlowHudEnabled;
	/** [Ending Blow HUD][Alignment: Horizontal] Alignment on the X axis [left|center|right] */
	public static HALIGN endingBlowHudHAlign;
	/** [Ending Blow HUD][Alignment: Vertical] Alignment on the Y axis [top|center|bottom] */
	public static VALIGN endingBlowHudVAlign;
	/** [Ending Blow HUD][Offset: X] Moves the HUD element left (-) or right (+) this number of pixels */
	public static int endingBlowHudOffsetX;
	/** [Ending Blow HUD][Offset: Y] Moves the HUD element up (-) or down (+) this number of pixels */
	public static int endingBlowHudOffsetY;
	/*=================== ITEM MODE HUD ===================*/
	/** [Item Mode HUD] Enable item mode HUD display (if disabled, mode may still be viewed in the item's tooltip) */
	public static boolean isItemModeEnabled;
	/** [Item Mode HUD][Alignment: Horizontal] Alignment on the X axis [left|center|right] */
	public static HALIGN itemModeHAlign;
	/** [Item Mode HUD][Alignment: Vertical] Alignment on the Y axis [top|center|bottom] */
	public static VALIGN itemModeVAlign;
	/** [Item Mode HUD][Offset: X] Moves the HUD element left (-) or right (+) this number of pixels */
	public static int itemModeOffsetX;
	/** [Item Mode HUD][Offset: Y] Moves the HUD element up (-) or down (+) this number of pixels */
	public static int itemModeOffsetY;
	/*================== CLIENT SIDE SETTINGS  =====================*/
	/** [Chat] Whether to show a chat message when striking secret blocks */
	public static boolean showSecretMessage;
	/** [Combo HUD] Whether the combo hit counter will display by default (may be toggled in game) */
	/** [Controls] Whether to use vanilla movement keys to activate skills such as Dodge and Parry */
	public static boolean allowVanillaControls;
	/** [Controls] Whether Dodge and Parry require double-tap or not (double-tap always required with vanilla control scheme) */
	public static boolean requireDoubleTap;	
	/** [Song GUI] Number of ticks allowed between notes before played notes are cleared [5-100] */
	private static int resetNotesInterval;
	/** [Sound] Whether to play the 'itembreak' sound when the hookshot misses */
	public static boolean enableHookshotSound;
	/** [Targeting] Prioritize mobs over other entity types when targeting */
	private static boolean targetMobs;
	/** [Targeting] Whether auto-targeting is enabled or not (toggle in game by pressing '.') */
	public static boolean enableAutoTarget;
	/** [Targeting] Whether players can be targeted (toggle in game by pressing '.' while sneaking) */
	public static boolean canTargetPlayers;
	/*================== MAGIC METER (CLIENT SIDE) =====================*/
	/** [Alignment: Horizontal] Alignment on the X axis [left|center|right] */
	public static HALIGN magicMeterHAlign;
	/** [Alignment: Vertical] Alignment on the Y axis [top|center|bottom] */
	public static VALIGN magicMeterVAlign;
	/** Enable text display of current Magic Points */
	public static boolean isMagicMeterTextEnabled;
	/** Enable the Magic Meter HUD display */
	public static boolean isMagicMeterEnabled;
	/** [Offset: X] Moves the Meter left (-) or right (+) this number of pixels */
	public static int magicMeterOffsetX;
	/** [Offset: Y] Moves the Meter up (-) or down (+) this number of pixels */
	public static int magicMeterOffsetY;
	/** [Orientation] True for a horizontal magic meter, or false for a vertical one */
	public static boolean isMagicMeterHorizontal;
	/** [Orientation: Mana] True to drain mana from right-to-left or top-to-bottom depending on orientation; false for the opposite */
	public static boolean isMagicBarLeft;
	/** [Width: Max] Maximum width of the magic meter [25-100] */
	public static int magicMeterWidth;
	/** [Width: Increment] Number of increments required to max out the magic meter, where each increment is 50 magic points [1-10] */
	public static int magicMeterIncrements;
	/*================== MOD INTER-COMPATIBILITY =====================*/
	/** [SYNC] [BattleGear2] Allow Master Swords to be held in the off-hand */
	private static boolean enableOffhandMaster;
	/*================== WEAPON REGISTRY =====================*/
	/** Items that are considered Swords for all intents and purposes */
	private static String[] swords = new String[0];
	/** Items that are considered Melee Weapons for all intents and purposes */
	private static String[] weapons = new String[0];
	/** Items that are forbidden from being considered as Swords */
	private static String[] forbidden_swords = new String[0];
	/** Items that are forbidden from being considered as Melee Weapons */
	private static String[] forbidden_weapons = new String[0];
	/*================== ITEMS =====================*/
	/** [Arrows] Whether transforming arrows with the Sacred Flames has a chance to consume the flame */
	private static boolean arrowsConsumeFlame;
	/** [SYNC] [Bombs] Minimum fuse time; set to 0 to disable held bomb ticks */
	private static int bombFuseTime;
	/** [Bombs] Whether bombs are non-griefing, i.e. can only destroy secret stone */
	private static boolean onlyBombSecretStone;
	/** [Bombs] Whether bombs can destroy regular blocks in Adventure Mode */
	private static boolean bombsGriefAdventure;
	/** [Boomerang] Allow Boomerang to destroy grass and similar blocks */
	private static boolean enableBoomerangDenude;
	/** [Deku Leaf] Allow Deku Leaf whirlwind to destroy leaves */
	private static boolean enableDekuDenude;
	/** [Din's Fire] Whether Din's Fire can set blocks on fire */
	private static boolean enableDinIgnite;
	/** [Din's Fire] Whether Din's Fire can melt unbreakable ice blocks */
	private static boolean enableDinMelt;
	/** [SYNC] [Enchantments] Disable the vanilla behavior allowing unenchantable items to be enchanted using the anvil */
	private static boolean disableAllUnenchantables;
	/** [Hammer] True to allow the Megaton Hammer to break Quake Stone (also requires player to have Golden Gauntlets in inventory) */
	private static boolean enableMegaSmashQuake;
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
	/** [SYNC] [Hookshot] Max range of non-extended hookshots */
	private static int hookshotRange;
	/** [SYNC] [Hookshot] Whether hookshots are allowed to interact ONLY with IHookable blocks - great for adventure maps! */
	private static boolean enableHookableOnly;
	/** [Hookshot] Whether hookshots are allowed to destroy certain blocks such as glass */
	private static boolean enableHookshotBreakBlocks;
	/** [Magic Medallions] True if Ether and Quake medallions can affect players */
	private static boolean medallionsAffectPlayers;
	/** [Magic Rods] Cost (in emeralds) to upgrade (note that the Tornado Rod costs 3/4 this value) [128-1280] */
	private static int rodUpgradeCost;
	/** [Magic Rods] Enable fire rod to set blocks on fire */
	private static boolean rodFireGriefing;
	/** [Master Sword] Number of mobs that need to be killed to upgrade the Tempered Sword */
	private static int temperedRequiredKills;
	/** [SYNC] [Master Sword] Whether ALL master swords provide power when placed in a Sword Pedestal */
	private static boolean allMasterSwordsProvidePower;
	/** [Skeleton Key] Number of locked chests which can be opened before key breaks (0 for no limit) [0-500] */
	private static int numSkelKeyUses;
	/** [Slingshot] Cost (in emeralds) for first upgrade */
	private static int slingshotUpgradeOne;
	/** [Slingshot] Cost (in emeralds) for second upgrade */
	private static int slingshotUpgradeTwo;
	/** [SYNC] [Whip] Range, in blocks, of the standard whip [4-12] */
	private static int whipRange;
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
	/** Start the game with Navi in a bottle (you can always acquire her later if false) */
	public static boolean enableNavi;
	/*================== SKILLS =====================*/
	/** Max number of bonus 1/2 hearts (capped at 80) */
	private static int maxBonusHearts;
	/** [Back Slice] Allow Back Slice to potentially knock off player armor */
	private static boolean allowDisarmorPlayer;
	/** [Parry] Bonus to disarm based on timing: tenths of a percent added per tick remaining on the timer [0-50] */
	private static float disarmTimingBonus;
	/** [Parry] Penalty to disarm chance: percent per Parry level of the opponent, default negates defender's skill bonus so disarm is based entirely on timing [0-20] */
	private static float disarmPenalty;
	/** [Magic] Allow Nayru's Love to be activated even when magic bar is unlimited (such as after drinking a Chateau Romani) */
	private static boolean allowUnlimitedNayru;
	/** [Magic] Maximum magic points attainable [50-1000] */
	private static int maxMagicPoints;
	/** [SYNC] [Super Spin Attack | Sword Beam] True to require a completely full health bar to use, or false to allow a small amount to be missing per level */
	private static boolean requireFullHealth;
	/*================== SONGS =====================*/
	/** [Song of Storms] Time required between each use of the song (by anybody) [0-24000] */
	private static int minSongIntervalStorm;
	/** [Sun's Song] Time required between each use of the song (by anybody) [0-24000] */
	private static int minSongIntervalSun;
	/*================== DUNGEON GEN =====================*/
	/** Whether to prevent ZSS structures from generating if any non-vanilla blocks are detected */
	private static boolean avoidModBlocks;
	/** [Boss Dungeon] Whether boss dungeons are allowed to have windows or not */
	private static boolean enableWindows;
	/** [Boss Dungeon] Enable Boss Dungeon generation */
	private static boolean enableBossDungeons;
	/** [Boss Dungeon] Ignore biome settings and randomize boss dungeon / boss key locations */
	private static boolean randomizeBossDungeons;
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
	private static float secretRoomChance;
	/** Difficulty level for finding overworld dungeons (1 very easy, 3 very hard) */
	private static int mainDungeonDifficulty;
	/** Max number of Nether secret room generation attempts per chunk (capped at 10) */
	private static int genAttemptsPerChunkNether;
	/** Chance of each iteration to attempt to generate a secret room in the Nether */
	private static float secretRoomChanceNether;
	/** Difficulty level for finding nether dungeons (1 very easy, 3 very hard) */
	private static int netherDungeonDifficulty;
	/** Chance (as a percent) for certain dungeons to have fairy spawners */
	private static float fairySpawnerChance;
	/** Maximum number of days required for fairies to replenish */
	private static int resetSpawnerTime;
	/** [No-Gen] Disable structure and feature generation entirely within a specified zone */
	private static boolean disableStructureGen;
	/** [No-Gen] Starting chunk coordinate X for the structure free zone [max is +/- 1875000] */
	private static int noGenX;
	/** [No-Gen] Starting chunk coordinate Z for the structure free zone [max is +/- 1875000] */
	private static int noGenZ;
	/*================== WORLD GEN =====================*/
	/** [Ancient Tablet] Chance that a random tablet will spawn when a boss dungeon is defeated */
	private static float ancientTabletGenChance;
	/** [Ancient Tablet] Maximum number of chunks from boss dungeon a tablet may generate [0-8] */
	private static int ancientTabletGenDistance;
	/** [Bomb Flowers] Enable bomb flower generation */
	private static boolean enableBombFlowerGen;
	/** [Ceramic Jars] Allow ceramic jars to generate in water */
	private static boolean allowJarsInWater;
	/** [Ceramic Jars][Surface] Chance of generating a jar cluster in a given chunk */
	private static float jarGenChance;
	/** [Ceramic Jars][Surface] Max number of jars per cluster */
	private static int jarsPerCluster;
	/** [Ceramic Jars][Underground] Chance for each jar cluster to generate */
	private static float jarGenChanceSub;
	/** [Ceramic Jars][Underground] Max number of jars per cluster */
	private static int jarsPerClusterSub;
	/** [Ceramic Jars][Underground] Max number of jar clusters per chunk */
	private static int jarClustersPerChunkSub;
	/** [Ceramic Jars][Nether] Chance for each jar cluster to generate */
	private static float jarGenChanceNether;
	/** [Ceramic Jars][Nether] Max number of jars per cluster */
	private static int jarsPerClusterNether;
	/** [Ceramic Jars][Nether] Max number of jar clusters per chunk */
	private static int jarClustersPerChunkNether;
	/** [Gossip Stones] Chance (1 = 0.1% chance) per chunk of a Gossip Stone generating [0-100] */
	private static float gossipStoneRate;
	/** [Song Pillars] Enable song and broken pillar generation */
	private static boolean enablePillarGen;
	/** [Song Pillars] Maximum search range; reduce if new chunks are loading too slowly [16-64] */
	private static int maxPillarRange;
	/** [Song Pillars] Minimum number of chunks between broken pillars [4-64] */
	private static int minBrokenPillarDistance;
	/** [Song Pillars] Minimum number of chunks between song pillars [8-64] */
	private static int minSongPillarDistance;
	/*================== LOOT =====================*/
	/** Chance (as a percent) of a Forest Temple containing a Master Sword [1-100] */
	private static float masterSwordChance;
	/** Chance (as a percent) a chest will be locked */
	private static float lockedChestChance;
	/** Chance that a secret room may have two chests */
	private static float doubleChestChance;
	/** Chance that a secret room's entrance will be barred by some obstacle */
	private static float barredRoomChance;
	/** Chance of a heart piece always appearing in secret room chests */
	private static float heartPieceChance;
	/** Chance of a random boss-level item being added to locked chest loot table */
	private static float randomBossItemChance;
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
	/** [Skill Orbs] Whether this skill may appear as random loot, such as in Boss chests */
	private static Set<Byte> lootableOrbs = new HashSet<Byte>();
	/*================== DROPS =====================*/
	/** Chance of grass dropping loot (set to zero to disable) */
	private static float grassDropChance;
	/** Chance of empty jars dropping loot (set to zero to disable) */
	private static float jarDropChance;
	/** Creeper bomb drop chance */
	private static float creeperDrop;
	/** Frequency of small heart and magic jar drops from mobs [zero to disable; 1 = rare, 10 = very common] */
	private static int mobConsumableFrequency;
	/** [Skill Orbs] Enable skill orbs to drop as loot from mobs */
	private static boolean enableOrbDrops;
	/** [Skill Orbs] Chance of dropping random orb */
	private static float randomDropChance;
	/** [Skill Orbs] Chance for unmapped mob to drop an orb */
	private static float genericMobDropChance;
	/** [Skill Orbs] Individual drop chances for skill orbs and heart pieces */
	private static Map<Byte, Float> orbDropChance;
	/** [Piece of Power] Approximate number of enemies you need to kill before a piece of power drops */
	private static int powerDropRate;
	/** [Whip] Chance that loot may be snatched from various vanilla mobs, using a whip (0 to disable)[0-100] */
	private static float vanillaWhipLootChance;
	/** [Whip] All whip-stealing chances are multiplied by this value, as a percentage, including any added by other mods (0 disables ALL whip stealing!)[0-500] */
	private static float globalWhipLootChance;
	/** [Whip] Whether to inflict damage to entities when stealing an item (IEntityLootable entities determine this separately) */
	private static boolean hurtOnSteal;
	/*================== TRADES =====================*/
	/** [Bomb Bag] Allow Barnes to sell bomb bags (checked each time Barnes is shown a bomb) */
	private static boolean enableTradeBombBag;
	/** [Bomb Bag] Cost of a bomb bag at Barnes' shop (only applied to new trades) */
	private static int bombBagPrice;
	/** [Bombs] Enable random villager trades for bombs */
	private static boolean enableTradeBomb;
	/** [Hero's Bow] Whether magic arrows (fire, ice, light) can be purchased */
	private static boolean enableArrowTrades;
	/** [Masks] Chance that a villager will be interested in purchasing a random mask */
	private static float maskBuyChance;
	/*================== MOB SPAWNING =====================*/
	/** Chance that a random mob will spawn inside of secret rooms (0 to disable) [0-100] */
	private static float roomSpawnMobChance;
	/** Minimum number of days required to pass before Darknuts may spawn [0-30] */
	private static int minDaysToSpawnDarknut;
	/** Minimum number of days required to pass before Wizzrobes may spawn [0-30] */
	private static int minDaysToSpawnWizzrobe;
	/*================== MAP MAKING =====================*/
	private static final String WARP_LOCATIONS_KEY = "Default Warp Locations: one per line with format 'song_name:[dimension_id,x,y,z]'";
	/** [Warp Stone] Default warp locations */
	private static Map<AbstractZeldaSong, WarpPoint> warp_defaults = new HashMap<AbstractZeldaSong, WarpPoint>();

	public static void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(new File(event.getModConfigurationDirectory().getAbsolutePath() + ModInfo.CONFIG_PATH));
		config.load();
		init();
	}

	public static void init(){
		ZSSItems.initConfig(config);

		/*================== GENERAL =====================*/
		config.setCategoryLanguageKey(GENERAL, "config.zss.general");

		enableStunPlayer = config.getBoolean("Players Can Be Stunned", GENERAL, false, "Whether players can be stunned; if false, item use is still interrupted", "config.zss.general.enable_stun_player");
		enableSwingSpeed = config.getBoolean("Enable Swing Speed Timer", GENERAL, true, "Whether the swing speed timer prevents all left-clicks, or only items that use swing speeds", "config.zss.general.enable_swing_speed");
		baseSwingSpeed = config.getInt("Default Swing Speed", GENERAL, 0, 0, 20, "Default swing speed (anti-left-click-spam): Sets base number of ticks between each left-click (0 to disable)", "config.zss.general.base_swing_speed");
		enableSecretStoneLift = config.getBoolean("Can Pick Up Secret Blocks", GENERAL, false, "Whether regular (i.e. breakable) secret stone blocks can be picked up using appropriate items (e.g. gauntlets)", "config.zss.general.enable_secret_stone_lift");
		enableVanillaLift = config.getBoolean("Can Pick Up Vanilla Blocks", GENERAL, true, "Whether vanilla blocks can be picked up using appropriate items (e.g. gauntlets)", "config.zss.general.enable_vanilla_lift");
		enableVanillaSmash = config.getBoolean("Can Smash Vanilla Blocks", GENERAL, true, "Whether vanilla blocks can be smashed using appropriate items (e.g. hammers)", "config.zss.general.enable_vanilla_smash");
		alwaysPickupHearts = config.getBoolean("Always Pick Up Hearts", GENERAL, false, "Always pick up small hearts regardless of health", "config.zss.general.always_pickup_hearts");
		enableHardcoreZeldaFanMode = config.getBoolean("Start With Three Hearts", GENERAL, false, "Hardcore Zelda Fan: Start with only 3 hearts (applies a -14 max health modifier, so it can be enabled or disabled at any time)", "config.zss.general.enable_hardcore_zelda_fan_mode");
		bossHealthFactor = 0.01F * config.getInt("[Boss] Boss Health Multiplier", GENERAL, 250, 100, 500, "Boss health multiplier, as a percent increase per difficulty level (does not apply to real bosses)", "config.zss.general.boss_health_factor");
		bossNumber = config.getInt("[Boss] Number of Mobs Spawned in Boss Dungeons", GENERAL, 4, 1, 8, "Number of boss mobs to spawn in Boss Dungeons (does not apply to real bosses)", "config.zss.general.boss_number");
		enableJarUpdates = config.getBoolean("[Ceramic Jars] Jars Can Store Dropped Items", GENERAL, true, "Whether ceramic jar tile entities update each tick, allowing them to store dropped items", "config.zss.general.enable_jar_updates");
		keeseSwarmChance = 0.01F * config.getInt("[Mobs][Keese] Chance of Keese to Spawn in a Swarm", GENERAL, 25, 0, 100, "Chance of Keese spawning in a swarm (0 to disable)", "config.zss.general.keese_swarm_chance");
		keeseSwarmSize = config.getInt("[Mobs][Keese] Number of Keese to Spawn in a Swarm", GENERAL, 6, 4, 16, "Maximum number of Keese that can spawn in a swarm", "config.zss.general.keese_swarm_size");
		sacredRefreshRate = config.getInt("[Sacred Flames] Frequency of Rekindling Sacred Flame", GENERAL, 7, 0, 30, "Number of days before flame rekindles itself (0 to disable)", "config.zss.general.sacred_refresh_rate");
		skulltulaRewardRate = config.getInt("[Skulltula Tokens] Days Between Recurring Quest Reward", GENERAL, 7, 0, 30, "Number of days between each recurring reward for completing the quest (0 to disable recurring reward)", "config.zss.general.skulltula_reward_rate");
		disableVanillaBuffs = config.getBoolean("[Mob Buff] Disable Vanilla Mob Buffs", GENERAL, false, "Disable all buffs (resistances and weaknesses) for vanilla mobs", "config.zss.general.disable_vanilla_buffs");
		npcsAreInvulnerable = config.getBoolean("[NPC] Zelda NPCs are Invulnerable", GENERAL, true, "Sets whether Zelda NPCs are invulnerable or not", "config.zss.general.npcs_are_invulnerable");
		naviRange = config.getInt("[NPC][Navi] Navi Sense Range", GENERAL, 4, 0, 10, "Range at which Navi can sense secret rooms, in blocks (0 to disable)", "config.zss.general.navi_range");
		naviFrequency = config.getInt("[NPC][Navi] Navi Sense Frequency", GENERAL, 50, 20, 200, "Frequency with which Navi checks the proximity for secret rooms, in ticks", "config.zss.general.navi_frequency");

		/*================== CLIENT SIDE SETTINGS  =====================*/
		config.setCategoryLanguageKey(CLIENT, "config.zss.client");

		showSecretMessage = config.getBoolean("[Chat] Alert When Striking Secret Blocks", CLIENT, false, "Whether to show a chat message when striking secret blocks", "config.zss.client.show_secret_message");
		allowVanillaControls = config.getBoolean("[Controls] Use Vanilla Movement Keys for Skills", CLIENT, true, "Whether to use vanilla movement keys to activate skills such as Dodge and Parry", "config.zss.client.allow_vanilla_controls");
		requireDoubleTap = config.getBoolean("[Controls] Double-tap for Movement Skills", CLIENT, true, "Whether Dodge and Parry require double-tap or not (double-tap always required with vanilla control scheme)", "config.zss.client.require_double_tap");
		resetNotesInterval = config.getInt("[Song GUI] Ticks Between Notes Before Clear", CLIENT, 30, 5, 100, "Number of ticks allowed between notes before played notes are cleared", "config.zss.client.reset_notes_interval");
		enableHookshotSound = config.getBoolean("[Sound] Hookshot Miss Plays Item-Break Sound", CLIENT, true, "Whether to play the 'itembreak' sound when the hookshot misses", "config.zss.client.enable_hookshot_sound");
		targetMobs = config.getBoolean("[Targeting] Prioritize mobs", CLIENT, true, "Prioritize mobs over other entity types when targeting", "config.zss.client.target_mobs");
		enableAutoTarget = config.getBoolean("[Targeting] Enable Auto-Targeting", CLIENT, true, "Whether auto-targeting is enabled or not (toggle in game: '.')", "config.zss.client.enable_auto_target");
		canTargetPlayers = config.getBoolean("[Targeting] Can Target Players", CLIENT, true, "Whether players can be targeted (toggle in game: '.' while sneaking)", "config.zss.client.can_target_players");

		/*=================== BUFF BAR HUD ===================*/
		config.setCategoryLanguageKey(BUFF_BAR, "config.zss.buff_bar");
		
		buffBarMaxIcons = config.getInt("Number of Buffs per Row/Column", BUFF_BAR, 5, 1, 10, "Maximum number of icons to display per row or column", "config.zss.buff_bar.buff_bar_max_icons");
		isBuffBarEnabled = config.getBoolean("Buff Bar Displays at All Times", BUFF_BAR, true, "Whether the buff bar should be displayed at all times");
		isBuffBarHorizontal = config.getBoolean("Display Buff Bar Horizontally", BUFF_BAR, true, "Whether the buff bar should be displayed horizontally", "config.zss.buff_bar.is_buff_bar_horizontal");
		buffBarHAlign = HALIGN.fromString(config.getString("Buff HUD X Alignment", BUFF_BAR, "right", "Alignment on the X axis [left|center|right]", new String[]{"left", "center", "right"}, "config.zss.buff_bar.buff_bar_halign"));
		buffBarVAlign = VALIGN.fromString(config.getString("Buff HUD Y Alignment", BUFF_BAR, "top", "Alignment on the Y axis [top|center|bottom]", new String[]{"top", "center", "bottom"}, "config.zss.buff_bar.buff_bar_valign"));
		buffBarOffsetX = config.get(BUFF_BAR, "Buff HUD X Offset", 0, "Moves the HUD element left (-) or right (+) this number of pixels").setLanguageKey("config.zss.buff_bar.buff_bar_offset_x").getInt();
		buffBarOffsetY = config.get(BUFF_BAR, "Buff HUD Y Offset", 0,"Moves the HUD element up (-) or down (+) this number of pixels").setLanguageKey("config.zss.buff_bar.buff_bar_offset_y").getInt();
		
		/*=================== COMBO HUD ===================*/
		config.setCategoryLanguageKey(COMBO, "config.zss.combo_hud");
		
		isComboHudEnabled = config.getBoolean("Display Combo Counter", COMBO, true, "Whether the combo hit counter will display by default (toggle in game: 'v')");
		hitsToDisplay = config.getInt("Hits to Display in Combo HUD", COMBO, 3, 0, 12, "Max hits to display in Combo HUD", "config.zss.combo_hud.hits_to_display");
		comboHudHAlign = HALIGN.fromString(config.getString("Combo HUD X Alignment", COMBO, "left", "Alignment on the X axis [left|center|right]", new String[]{"left", "center", "right"}, "config.zss.combo_hud.combo_hud_halign"));
		comboHudVAlign = VALIGN.fromString(config.getString("Combo HUD Y Alignment", COMBO, "top", "Alignment on the Y axis [top|center|bottom]", new String[]{"top", "center", "bottom"}, "config.zss.combo_hud.combo_hud_valign"));
		comboHudOffsetX = config.get(COMBO, "Combo HUD X Offset", 0, "Moves the HUD element left (-) or right (+) this number of pixels").setLanguageKey("config.zss.combo_hud.combo_hud_offset_x").getInt();
		comboHudOffsetY = config.get(COMBO, "Combo HUD Y Offset", 0, "Moves the HUD element up (-) or down (+) this number of pixels").setLanguageKey("config.zss.combo_hud.combo_hud_offset_y").getInt();

		/*================== ENDING BLOW HUD =====================*/
		config.setCategoryLanguageKey(ENDING_BLOW, "config.zss.ending_blow");

		isEndingBlowHudEnabled = config.getBoolean("Display Ending Blow HUD", ENDING_BLOW, true, "Enable Ending Blow HUD display (if disabled, there is not any indication that the skill is ready to use)");
		endingBlowHudHAlign = HALIGN.fromString(config.getString("Ending Blow HUD X Alignment", ENDING_BLOW, "center", "Alignment on the X axis [left|center|right]", new String[]{"left", "center", "right"}, "config.zss.ending_blow.ending_blow_hud_halign"));
		endingBlowHudVAlign = VALIGN.fromString(config.getString("Ending Blow HUD Y Alignment", ENDING_BLOW, "top", "Alignment on the Y axis [top|center|bottom]", new String[]{"top", "center", "bottom"}, "config.zss.ending_blow.ending_blow_hud_valign"));
		endingBlowHudOffsetX = config.get(ENDING_BLOW, "Ending Blow HUD X Offset", 0, "Moves the HUD element left (-) or right (+) this number of pixels").setLanguageKey("config.zss.ending_blow.ending_blow_hud_offset_x").getInt();
		endingBlowHudOffsetY = config.get(ENDING_BLOW, "Ending Blow HUD Y Offset", 30, "Moves the HUD element up (-) or down (+) this number of pixels").setLanguageKey("config.zss.ending_blow.ending_blow_hud_offset_y").getInt();

		/*================== ITEM MODE HUD =====================*/
		config.setCategoryLanguageKey(ITEM_MODE, "config.zss.item_mode");

		isItemModeEnabled = config.getBoolean("Display Item Mode HUD", ITEM_MODE, true, "Enable item mode HUD display (if disabled, mode may still be viewed in the item's tooltip)");
		itemModeHAlign = HALIGN.fromString(config.getString("Item Mode HUD X Alignment", ITEM_MODE, "left", "Alignment on the X axis [left|center|right]", new String[]{"left", "center", "right"}, "config.zss.item_mode.item_mode_halign"));
		itemModeVAlign = VALIGN.fromString(config.getString("Item Mode HUD Y Alignment", ITEM_MODE, "top", "Alignment on the Y axis [top|center|bottom]", new String[]{"top", "center", "bottom"}, "config.zss.item_mode.item_mode_valign"));
		itemModeOffsetX = config.get(ITEM_MODE, "Item Mode HUD X Offset", 0, "Moves the HUD element left (-) or right (+) this number of pixels").setLanguageKey("config.zss.item_mode.item_mode_offset_x").getInt();
		itemModeOffsetY = config.get(ITEM_MODE, "Item Mode HUD Y Offset", 0, "Moves the HUD element up (-) or down (+) this number of pixels").setLanguageKey("config.zss.item_mode.item_mode_offset_y").getInt();

		/*================== MAGIC METER (CLIENT SIDE) =====================*/
		config.setCategoryLanguageKey(MAGIC_METER, "config.zss.magic_meter");
		
		magicMeterHAlign = HALIGN.fromString(config.getString("Magic Meter X Alignment", MAGIC_METER, "center", "Alignment on the X axis [left|center|right]", new String[]{"left", "center", "right"}, "config.zss.magic_meter.magic_meter_halign"));
		magicMeterVAlign = VALIGN.fromString(config.getString("Magic Meter Y Alignment", MAGIC_METER, "bottom", "Alignment on the Y axis [top|center|bottom]", new String[]{"top", "center", "bottom"}, "config.zss.magic_meter.magic_meter_valign"));
		isMagicMeterTextEnabled = config.getBoolean("Display Current Magic Points", MAGIC_METER, false, "Enable text display of current Magic Points");
		isMagicMeterEnabled = config.getBoolean("Display Magic Meter", MAGIC_METER, true, "Enable the Magic Meter HUD display");
		magicMeterOffsetX = config.get(MAGIC_METER, "Magic Meter X Offset", 47, "Moves the Meter left (-) or right (+) this number of pixels").setLanguageKey("config.zss.magic_meter.magic_meter_offset_x").getInt();
		magicMeterOffsetY = config.get(MAGIC_METER, "Magic Meter Y Offset", -40, "Moves the Meter up (-) or down (+) this number of pixels").setLanguageKey("config.zss.magic_meter.magic_meter_offset_y").getInt();
		isMagicMeterHorizontal = config.getBoolean("Magic Meter Displays Horizontally", MAGIC_METER, true,"True for a horizontal magic meter, or false for a vertical one", "config.zss.magic_meter.is_magic_meter_horizontal");
		isMagicBarLeft = config.getBoolean("Drain Magic Bar To the Bottom/Left", MAGIC_METER, true, "True to drain mana from right-to-left or top-to-bottom depending on orientation; false for the opposite", "config.zss.magic_meter.is_magic_bar_left");
		magicMeterWidth = config.getInt("Magic Meter Width", MAGIC_METER, 75, 25, 100, "Maximum width of the magic meter", "config.zss.magic_meter.magic_meter_width");
		magicMeterIncrements = config.getInt("Number of Meter Increments", MAGIC_METER, 2, 1, 10, "Number of increments required to max out the magic meter, where each increment is 50 magic points", "config.zss.magic_meter.magic_meter_increments");
		
		/*================== MOD INTER-COMPATIBILITY =====================*/
		config.setCategoryLanguageKey(MOD_SUPPORT, "config.zss.mod_support");

		enableOffhandMaster = config.getBoolean("[BattleGear2] Can Offhand Master Swords", MOD_SUPPORT, false, "Allow Master Swords to be held in the off-hand", "config.mod_support.enable_offhand_master");

		/*================== WEAPON REGISTRY =====================*/
		// TODO add comment for clarity
		config.setCategoryLanguageKey(WEAPON_REGISTRY, "config.zss.weapon_registry");

		swords = config.get(WEAPON_REGISTRY, "Allowed Swords", new String[0], "Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'. Register an item so that it is considered a SWORD by ZSS, i.e. it be used with skills that\nrequire swords, as well as other interactions that require swords, such as cutting grass. All swords are also considered WEAPONS.").setRequiresMcRestart(true).setLanguageKey("config.zss.weapon_registry.allowed_swords").getStringList();
		Arrays.sort(swords);
		weapons = config.get(WEAPON_REGISTRY, "Allowed Weapons", new String[0], "Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'. Register an item as a generic melee WEAPON. This means it can be used for all\nskills except those that specifically require a sword, as well as some other things.").setRequiresMcRestart(true).setLanguageKey("config.zss.weapon_registry.allowed_weapons").getStringList();
		Arrays.sort(weapons);
		// Battlegear2 weapons ALL extend ItemSword, but are not really swords
		String[] forbidden = new String[]{
				"battlegear2:dagger.diamond","battlegear2:dagger.gold","battlegear2:dagger.iron","battlegear2:dagger.stone","battlegear2:dagger.wood",
				"battlegear2:mace.diamond","battlegear2:mace.gold","battlegear2:mace.iron","battlegear2:mace.stone","battlegear2:mace.wood",
				"battlegear2:spear.diamond","battlegear2:spear.gold","battlegear2:spear.iron","battlegear2:spear.stone","battlegear2:spear.wood",
				"battlegear2:waraxe.diamond","battlegear2:waraxe.gold","battlegear2:waraxe.iron","battlegear2:waraxe.stone","battlegear2:waraxe.wood"
		};
		forbidden_swords = config.get(WEAPON_REGISTRY, "Forbidden Swords", forbidden, "Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'. Forbid one or more items from acting as SWORDs, e.g. if a mod item extends ItemSword but is not really a sword.").setRequiresMcRestart(true).setLanguageKey("config.zss.weapon_registry.forbidden_swords").getStringList();
		Arrays.sort(forbidden_swords);
		forbidden_weapons = config.get(WEAPON_REGISTRY, "Forbidden Weapons", new String[0], "Enter items as modid:registered_item_name, each on a separate line between the '<' and '>'. Forbid one or more items from acting as WEAPONs, e.g. if an item is added by IMC and you don't want it to be usable with skills.\nNote that this will also prevent the item from behaving as a SWORD.").setRequiresMcRestart(true).setLanguageKey("config.zss.weapon_registry.forbidden_weapons").getStringList();
		Arrays.sort(forbidden_weapons);

		/*================== ITEMS =====================*/
		config.setCategoryLanguageKey(ITEMS, "config.zss.item");

		arrowsConsumeFlame = config.getBoolean("[Arrows] Can Arrows Consume Sacred Flame", ITEMS, true , "Whether transforming arrows with the Sacred Flames has a chance to consume the flame", "config.zss.item.arrows_consume_flame");
		bombFuseTime = config.getInt("[Bombs] Minimum Bomb Fuse Time", ITEMS, 56, 0, 128, "Minimum fuse time; set to 0 to disable held bomb ticks", "config.zss.item.bomb_fuse_time");
		onlyBombSecretStone = config.getBoolean("[Bombs] Can Bombs Grief", ITEMS, false, "Whether bombs are non-griefing, i.e. can only destroy secret stone", "config.zss.item.only_bomb_secret_room");
		bombsGriefAdventure = config.getBoolean("[Bombs] Can Bombs Grief in Adventure Mode", ITEMS, false, "Whether bombs can destroy regular blocks in Adventure Mode", "config.zss.item.bombs_grief_adventure");
		enableBoomerangDenude = config.getBoolean("[Boomerang] Can Boomerang destroy grass", ITEMS, true, "Allow Boomerang to destroy grass and similar blocks", "config.zss.item.enable_boomerang_denude");
		enableDekuDenude = config.getBoolean("[Deku Leaf] Can Whirlwind Strip Trees", ITEMS, true, "Allow Deku Leaf whirlwind to destroy leaves", "config.zss.item.enable_deku_denude");
		enableDinIgnite = config.getBoolean("[Din's Fire] Can Din's Fire Ignite Blocks", ITEMS, false, "Whether Din's Fire can set blocks on fire", "config.zss.item.enable_din_ignite");
		enableDinMelt = config.getBoolean("[Din's Fire] Can Din's Fire Melt Ice", ITEMS, true, "Whether Din's Fire can melt unbreakable ice blocks", "config.zss.item.enable_din_melt");
		disableAllUnenchantables = config.getBoolean("[Enchantments] Can Enchant on the Vanilla Anvil", ITEMS, false, "Disable the vanilla behavior allowing unenchantable items to be enchanted using the anvil", "config.zss.item.disable_all_unenchantables");
		enableMegaSmashQuake = config.getBoolean("[Hammer] Can Megaton Hammer Break Quake Stone", ITEMS, true, "True to allow the Megaton Hammer to break Quake Stone (also requires player to have Golden Gauntlets in inventory)", "config.zss.item.enable_mega_smash_quake");
		heroBowUpgradeCost = config.getInt("[Hero's Bow] Hero's Bow Upgrade Cost", ITEMS, 192, 128, 640, "Cost (in emeralds) to upgrade, per level", "config.zss.item.hero_bow_upgrade_cost");
		enableFireArrowIgnite = config.getBoolean("[Hero's Bow] Can Fire Arrows Ignite Blocks", ITEMS, true, "Whether the fire arrow can ignite affected blocks", "config.zss.item.enable_fire_arrow_ignite");
		enableFireArrowMelt = config.getBoolean("[Hero's Bow] Can Fire Arrow Melt Ice", ITEMS, false, "Whether the fire arrow can melt unbreakable ice blocks", "config.zss.item.enable_fire_arrow_melt");
		enableLightArrowNoClip = config.getBoolean("[Hero's Bow] Can Light Arrow Penetrate Blocks", ITEMS, true, "Whether the light arrow can penetrate blocks", "config.zss.item.enable_light_arrow_no_clip");
		enableAutoBombArrows = config.getBoolean("[Hero's Bow] Automate Bomb Arrow Firing While Sneaking", ITEMS, true, "Whether to automate bomb arrow firing when sneaking", "config.zss.item.enable_auto_bomb_arrows");
		hookshotRange = config.getInt("[Hookshot] Range of Non-Extended Hookshots", ITEMS, 8, 4, 16, "Max range of non-extended hookshots", "config.zss.item.hookshot_range");
		enableHookableOnly = config.getBoolean("[Hookshot] Hookshots Only Grab Hookable Blocks", ITEMS, false, "Whether hookshots are allowed to interact ONLY with IHookable blocks - great for adventure maps!", "config.zss.item.enable_hookable_only");
		enableHookshotBreakBlocks = config.getBoolean("[Hookshot] Can Hookshots Destroy Blocks", ITEMS, true, "Whether hookshots are allowed to destroy certain blocks such as glass", "config.zss.item.enable_hookshot_break_blocks");
		rodUpgradeCost = config.getInt("[Hookshot] Magic Rod Upgrade Cost", ITEMS, 768, 128, 1280, "Cost (in emeralds) to upgrade (note that the Tornado Rod costs 3/4 this value)", "config.zss.item.rod_upgrade_cost");
		rodFireGriefing = config.getBoolean("[Hookshot] Can Fire Rod Ignite Blocks", ITEMS, true, "Enable fire rod to set blocks on fire", "config.zss.item.rod_fire_griefing");
		medallionsAffectPlayers = config.getBoolean("[Magic Medallions] Can Medallions Affect Players", ITEMS, true, "True if Ether and Quake medallions can affect players", "config.zss.item.medallions_affect_players");
		temperedRequiredKills = config.getInt("[Master Sword] Mobs Killed to Upgrade Temprered Sword", ITEMS, 300, 100, 1000, "Number of mobs that need to be killed to upgrade the Tempered Sword", "config.zss.item.tempered_required_kills");
		allMasterSwordsProvidePower = config.getBoolean("[Master Sword] Any Master Sword Provides Power", ITEMS, true, "Whether ALL master swords provide power when placed in a Sword Pedestal", "config.zss.item.all_master_swords_provide_power");
		numSkelKeyUses = config.getInt("[Skeleton Key] Chest Key Unlock Limit", ITEMS, 50, 0, 500, "Number of locked chests which can be opened before key breaks (0 for no limit)", "config.zss.item.num_skel_key_uses");
		slingshotUpgradeOne = config.getInt("[Slingshot] Slingshot First Upgrade Cost", ITEMS, 128, 64, 320, "Cost (in emeralds) for first upgrade", "config.zss.item.slingshot_upgrade_one");
		slingshotUpgradeTwo = config.getInt("[Slingshot] Slingshot Second Upgrade Cost", ITEMS, 320, 128, 640, "Cost (in emeralds) for second upgrade", "config.zss.item.slingshot_upgrade_two");
		whipRange = config.getInt("[Whip] Whip Range", ITEMS, 6, 4, 12, "Range, in blocks, of the standard whip", "config.zss.item.whip_range");

		/*================== STARTING GEAR =====================*/
		config.setCategoryLanguageKey(BONUS_GEAR, "config.zss.bonus_gear");

		enableStartingGear = config.getBoolean("Enable Start Equipment", BONUS_GEAR, true, "Enable bonus starting equipment", "config.zss.bonus_gear.enable_starting_gear");
		enableAutoEquip = config.getBoolean("Is Starting Gear Equipped", BONUS_GEAR, true, "Automatically equip starting equipment", "config.zss.bonus_gear.enable_auto_equip");
		enableLinksHouse = config.getBoolean("Start With Link's House", BONUS_GEAR, true, "Begin the game with Link's House - place it anywhere you like!", "config.zss.bonus_gear.enable_links_house");
		enableOrb = config.getBoolean("Start With a Skill Orb", BONUS_GEAR, true, "Grants a single Basic Sword skill orb", "config.zss.bonus_gear.enable_orb");
		enableFullSet = config.getBoolean("Start With a Set of Kokiri Clothing", BONUS_GEAR, true, "Grants a full set of Kokiri clothing: hat, tunic, trousers, boots", "config.zss.bonus_gear.enable_full_set");
		enableTunic = config.getBoolean("Start With a Kokiri Tunic", BONUS_GEAR, true, "Grants only a Kokiri Tunic (if full set is disabled)", "config.zss.bonus_gear.enable_tunic");
		enableSword = config.getBoolean("Start With a Kokiri Sword", BONUS_GEAR, true, "Grants a Kokiri sword", "config.zss.bonus_gear.enable_sword");
		enableNavi = config.getBoolean("Start With Navi", BONUS_GEAR, false, "Start the game with Navi in a bottle (you can always acquire her later if false)", "config.zss.bonus_gear.enable_navi");

		/*================== SKILLS =====================*/
		config.setCategoryLanguageKey(SKILLS, "config.zss.skills");

		maxBonusHearts = config.getInt("Max Bonus Hearts", SKILLS, 20, 0, BonusHeart.MAX_BONUS_HEARTS, "The maximum number of hearts a player can have", "config.zss.skills.max_bonus_hearts");
		allowDisarmorPlayer = config.getBoolean("[Back Slice] Back Slice Can Disarm Players", SKILLS, true, "Allow Back Slice to potentially knock off player armor", "config.zss.skills.allow_disarm_player");
		disarmTimingBonus = 0.001F * config.getInt("[Parry] Disarm Timing Bonus", SKILLS, 25, 0, 50, "Bonus to disarm based on timing: tenths of a percent added per tick remaining on the timer", "config.zss.skills.disarm_timing_bonus");
		disarmPenalty = 0.01F * config.getInt("[Parry] Disarm Penalty Chance", SKILLS, 10, 0, 20, "Penalty to disarm chance: percent per Parry level of the opponent, default negates defender's skill bonus so disarm is based entirely on timing", "config.zss.skills.disarm_penalty");
		maxMagicPoints = config.getInt("[Magic] Maximum Attainable Magic Points", SKILLS, 250, 50, 1000, "Maximum magic points attainable", "config.zss.skills.max_maigc_points");
		allowUnlimitedNayru = config.getBoolean("[Magic] Can Always Activate Naryu's Love", SKILLS, false, "Allow Nayru's Love to be activated even when magic bar is unlimited (such as after drinking a Chateau Romani)", "config.zss.skills.allow_unlimited_nayru");
		requireFullHealth = config.getBoolean("[Super Spin Attack | Sword Beam] Sword Skills Require Full Health", SKILLS, false, "True to require a completely full health bar to use, or false to allow a small amount to be missing per level", "config.zss.skills.require_full_health");

		/*================== DUNGEON GEN =====================*/
		config.setCategoryLanguageKey(DUNGEON_GEN, "config.zss.dun_gen");

		avoidModBlocks = config.getBoolean("Avoid Mod Blocks when Generating", DUNGEON_GEN, true, "Whether to prevent ZSS structures from generating if any non-vanilla blocks are detected", "config.zss.dun_gen.avoid_mod_blocks");
		enableWindows = config.getBoolean("[Boss Dungeon] Boss Dungeons Can Have Windows", DUNGEON_GEN, true, "Whether boss dungeons are allowed to have windows or not", "config.zss.dun_gen.enable_windows");
		enableBossDungeons = config.getBoolean("[Boss Dungeon] Boss Dungeons Generate", DUNGEON_GEN, true, "Enable Boss Dungeon generation", "config.zss.dun_gen.enable_boss_dungeons");
		randomizeBossDungeons = config.getBoolean("[Boss Dungeon] Dungeons Can Spawn Wherever", DUNGEON_GEN, false, "Ignore biome settings and randomize boss dungeon / boss key locations", "config.zss.dun_gen.randomize_boss_dungeons");
		mainDungeonDifficulty = config.getInt("[Overworld] Secret Room Hiding Difficulty", DUNGEON_GEN, 2, 1, 3, "Adjust secret rooms so they are more hidden (difficulty in increasing number)", "config.zss.dun_gen.main_dungeon_difficulty");
		secretRoomChance = 0.01F * config.getInt("[Overworld] Secret Room Gen Chance", DUNGEON_GEN, 80, 1, 100, "Chance (as a percent) per iteration of secret room generating", "config.zss.dun_gen.secret_room_chance");
		minLandDistance = config.getInt("[Overworld] Minimum Secret Room Land Distance", DUNGEON_GEN, 6, 2, 12, "Minimum number of blocks between land-based secret rooms", "config.zss.dun_gen.min_land_distance");
		minOceanDistance = config.getInt("[Overworld] Minimum Secret Room Ocean Distance", DUNGEON_GEN, 6, 2, 32, "Minimum number of blocks between ocean-based secret rooms", "config.zss.dun_gen.min_ocean_distance");
		minBossDistance = config.getInt("[Overworld] Minimum Chunks Between Dungeons", DUNGEON_GEN, 24, 8, 128, "Minimum number of chunks between Boss Dungeons", "config.zss.dun_gen.min_boss_distance");
		genAttemptsPerChunk = config.getInt("[Overworld] Secret Room Chunk Gen Attempts", DUNGEON_GEN, 12, 0, 20, "Secret room generation attempts per chunk (0 to disable)", "config.zss.dun_gen.gen_attempts_per_chunk");
		netherDungeonDifficulty = config.getInt("[Nether] Secret Room Hiding Difficulty", DUNGEON_GEN, 2, 1, 3, "Adjust secret rooms so they are more hidden (difficulty in increasing number)", "config.zss.dun_gen.nether_dungeon_difficulty");
		secretRoomChanceNether = 0.01F * config.getInt("[Nether] Secret Room Gen Chance", DUNGEON_GEN, 80, 1, 100, "Chance (as a percent) per iteration of secret room generating", "config.zss.dun_gen.secret_room_chance_nether");
		minDistanceNether = config.getInt("[Nether] Minimum Secret Room Distance", DUNGEON_GEN, 6, 2, 16, "Minimum number of blocks between land-based secret rooms", "config.zss.dun_gen.min_distance_nether");
		minBossDistanceNether = config.getInt("[Nether] Chunks Between Dungeons", DUNGEON_GEN, 12, 8, 64, "Minimum number of chunks between Boss Dungeons", "config.zss.dun_gen.min_boss_distance_nether");
		genAttemptsPerChunkNether = config.getInt("[Nether] Secret Room Chunk Gen Attempts", DUNGEON_GEN, 12, 0, 20, "Secret room generation attempts per chunk (0 to disable)", "config.zss.dun_gen.gen_attempts_per_chunk_nether");
		fairySpawnerChance = 0.01F * config.getInt("Fairy Spawner Gen Chance", DUNGEON_GEN, 10, 0, 100, "Chance (as a percent) for certain dungeons to have fairy spawners", "config.zss.dun_gen.fairy_spawner_chance");
		resetSpawnerTime = config.getInt("Fairy Replenish Time", DUNGEON_GEN, 7, 2, 10, "Maximum number of days required for fairies to replenish", "config.zss.dun_gen.reset_spawner_time");
		disableStructureGen = config.getBoolean("[No-Gen] Disable Dungeon Generation", DUNGEON_GEN, false, "Disable structure and feature generation entirely within a specified zone", "config.zss.dun_gen.disable_structure_gen");
		noGenX = config.getInt("[No-Gen] Structure Gen X Limit", DUNGEON_GEN, 0, -1875000, 1875000, "Starting chunk coordinate X for the structure free zone", "config.zss.dun_gen.no_gen_x");
		noGenZ = config.getInt("[No-Gen] Structure Gen Z Limit", DUNGEON_GEN, 0, -1875000, 1875000, "Starting chunk coordinate Z for the structure free zone", "config.zss.dun_gen.no_gen_z");

		/*================== WORLD GEN =====================*/
		config.setCategoryLanguageKey(WORLD_GEN, "config.zss.world_gen");

		ancientTabletGenChance = 0.01F * config.getInt("[Ancient Tablet] Ancient Tablet Spawn Chance", WORLD_GEN, 20, 0, 100, "Chance that a random tablet will spawn when a boss dungeon is defeated", "config.zss.world_gen.ancient_tablet_gen_chance");
		// TODO ancientTabletGenDistance = config.getInt("Ancient Tablet Spawn Distance", WORLD_GEN, 0, 0, 8, "Maximum number of chunks from boss dungeon a tablet may generate", "config.zss.world_gen.ancient_tablet_gen_distance");//TODO need a lang key when this is implemented
		enableBombFlowerGen = config.getBoolean("[Bomb Flowers] Flower Bombs Generate", WORLD_GEN, true, "Enable bomb flower generation", "config.zss.world_gen.enable_bomb_flower_gen");
		allowJarsInWater = config.getBoolean("[Ceramic Jars][Surface] Jars Generate in Water", WORLD_GEN, true, "Allow ceramic jars to generate in water", "config.zss.world_gen.allowed_jars_in_water");
		jarGenChance = 0.01F * config.getInt("[Ceramic Jars][Surface] Jar Cluster Spawn Chance", WORLD_GEN, 50, 0, 100, "Chance of generating a jar cluster in a given chunk", "config.zss.world_gen.jars_gen_chance");
		jarsPerCluster = config.getInt("[Ceramic Jars][Surface] Max Jars in a Cluster", WORLD_GEN, 8, 2, 20, "Max number of jars per jar cluster", "config.zss.world_gen.jars_per_cluster");
		jarGenChanceSub = 0.01F * config.getInt("[Ceramic Jars][Underground] Cluster Spawn Chance", WORLD_GEN, 65, 0, 100, "Chance for each jar cluster to generate", "config.zss.world_gen.jar_gen_chance_sub");
		jarsPerClusterSub = config.getInt("[Ceramic Jars][Underground] Max Jars in a Cluster", WORLD_GEN, 8, 2, 20, "Max number of jars per cluster", "config.zss.world_gen.jars_per_cluster_sub");
		jarClustersPerChunkSub = config.getInt("[Ceramic Jars][Underground] Max Clusters per Chunk", WORLD_GEN, 10, 1, 20, "Max number of jar clusters per chunk", "config.zss.world_gen.jar_clusters_per_chunk_sub");
		jarGenChanceNether = 0.01F * config.getInt("[Ceramic Jars][Nether] Jar Cluster Spawn Chance", WORLD_GEN, 50, 0, 100, "Chance for each jar cluster to generate", "config.zss.world_gen.jar_gen_chance_nether");
		jarsPerClusterNether = config.getInt("[Ceramic Jars][Nether] Max Jars in a Cluster", WORLD_GEN, 8, 2, 20, "Max number of jars per cluster", "config.zss.world_gen.jars_per_cluster_nether");
		jarClustersPerChunkNether = config.getInt("[Ceramic Jars][Nether] Max Clusters per Chunk", WORLD_GEN, 8, 1, 20, "Max number of jar clusters per chunk", "config.zss.world_gen.jar_clusters_per_chunk_nether");
		gossipStoneRate = 0.0001F * config.getInt("[Gossip Stones] Gossip Stone Spawn Chance", WORLD_GEN, 50, 0, 500, "Chance per chunk of a Gossip Stone generating (100 = 1% chance)", "config.zss.world_gen.gossip_stone_rate");
		enablePillarGen = config.getBoolean("[Song Pillars] Can Pillars Generate", WORLD_GEN, true, "Enable song and broken pillar generation", "config.zss.world_gen.enable_pillar_gen");
		maxPillarRange = config.getInt("[Song Pillars] Maximum Pillar Range", WORLD_GEN, 64, 16, 64, "Maximum search range; reduce if new chunks are loading too slowly", "config.zss.world_gen.max_pillar_range");
		minBrokenPillarDistance = config.getInt("[Song Pillars] Broken Pillar Sparceness", WORLD_GEN, 32, 4, 128, "Minimum number of chunks between broken pillars", "config.zss.world_gen.min_broken_pillar_distance");
		minSongPillarDistance = config.getInt("[Song Pillars] Song Pillar Sparceness", WORLD_GEN, 64, 8, 128, "Minimum number of chunks between song pillars", "config.zss.world_gen.min_song_pillar_distance");

		/*================== LOOT =====================*/
		config.setCategoryLanguageKey(LOOT, "config.zss.loot");

		masterSwordChance = 0.01F * config.getInt("Chance a Temple Contains a Master Sword", LOOT, 33, 1, 100, "Chance (as a percent) of a Forest Temple containing a Master Sword", "config.zss.loot.master_sword_chance");
		lockedChestChance = 0.01F * config.getInt("Locked Chest Chance", LOOT, 33, 10, 50, "Chance (as a percent) a chest will be locked", "config.zss.loot.locked_chest_chance");
		doubleChestChance = 0.01F * config.getInt("Secret Room Double Chest Chance", LOOT, 10, 0, 25, "Chance (as a percent) a secret room may have two chests", "config.zss.loot.double_chest_chance");
		barredRoomChance = 0.01F * config.getInt("Secret Room Barred Chance", LOOT, 25, 1, 50, "Chance that a secret room's entrance will be barred by some obstacle", "config.zss.loot.barred_room_chance");
		heartPieceChance = 0.01F * config.getInt("Secret Room Heart Piece Chance", LOOT, 15, 0, 100, "Chance (as a percent) of a heart piece generating in secret room chests", "config.zss.loot.heart_piece_chance");
		randomBossItemChance = 0.01F * config.getInt("Boss Item Chest Chance", LOOT, 25, 0, 50, "Chance (as a percent) of a random boss-level item being added to locked chest loot table", "config.zss.loot.random_boss_item_chance");
		minNumChestItems = config.getInt("Minimum Number of Items in First Chest", LOOT, 4, 1, 10, "Minimum number of random chest contents for first chest", "config.zss.loot.min_num_chest_items");
		bombWeight = config.getInt("[Loot Weight] Bomb Loot Weight", LOOT, 5, 1, 10, "The loot weight of a Bomb", "config.zss.loot.bomb_weight");
		bombBagWeight = config.getInt("[Loot Weight] Bomb Bag Loot Weight", LOOT, 3, 1, 10, "The loot weight of a Bomb Bag (locked chest only)", "config.zss.loot.bomb_bag_weight");
		heartPieceWeight = config.getInt("[Loot Weight] Heart Piece Loot Weight", LOOT, 1, 1, 10, "The loot weight of a Heart Piece (vanilla chests only)", "config.zss.loot.heart_piece_weight");
		bigKeyWeight = config.getInt("[Loot Weight] Big Key Loot Weight", LOOT, 4, 1, 10, "The loot weight of a Big Key", "config.zss.loot.big_key_weight");
		smallKeyWeight = config.getInt("[Loot Weight] Small Key Loot Weight", LOOT, 4, 1, 10, "The loot weight of a Small Key", "config.zss.loot.small_key_weight");
		lockedLootWeight = config.getInt("[Loot Weight] Locked Chest Content Weight", LOOT, 3, 1, 10, "The maximum weight of the contents in a Locked Chest", "config.zss.loot.locked_loot_weight");

		/*================== DROPS =====================*/
		//TODO add to GuiConfig in a later commit
		category = "drops";

		grassDropChance = 0.01F * config.getFloat("Grass Loot Drop Chance", category, 15, 0, 100, "Chance (as a percent) of loot dropping from grass (0 to disable)", "");
		jarDropChance = 0.01F * config.getFloat("Jar Loot Drop Chance", category, 20, 0, 100, "Chance (as a percent) of loot dropping from empty jars when broken (0 to disable)", "");
		creeperDrop = 0.01F * (float) MathHelper.clamp_int(config.get("Drops", "Chance (as a percent) for creepers to drop bombs", 10).getInt(), 0, 100);
		mobConsumableFrequency = MathHelper.clamp_int(config.get("Drops", "Frequency of small heart and magic jar drops from mobs [zero to disable; 1 = rare, 10 = very common]", 5).getInt(), 0, 10);
		enableOrbDrops = config.get("Drops", "[Skill Orbs] Enable skill orbs to drop as loot from mobs", true).getBoolean(true);
		randomDropChance = 0.01F * (float) MathHelper.clamp_int(config.get("Drops", "[Skill Orbs] Chance (as a percent) for specified mobs to drop a random orb [0-100]", 10).getInt(), 0, 100);
		genericMobDropChance = 0.01F * (float) MathHelper.clamp_int(config.get("Drops", "[Skill Orbs] Chance (as a percent) for random mobs to drop a random orb [0-100]", 1).getInt(), 0, 100);
		orbDropChance = new HashMap<Byte, Float>(SkillBase.getNumSkills());
		for (SkillBase skill : SkillBase.getSkills()) {
			if (skill.canDrop()) {
				int i = MathHelper.clamp_int(config.get("drops", "Chance (in tenths of a percent) for " + skill.getDisplayName() + " (0 to disable)", 5).getInt(), 0, 10);
				orbDropChance.put(skill.getId(), (0.001F * (float) i));
			}
			if (skill.isLoot() && config.getBoolean("[Skill Orbs] " + skill.getDisplayName() + " Orbs are Lootable", "loot", true, "Whether " + skill.getDisplayName() + " orbs may appear as random loot, such as in Boss chests", skill.getTranslationString() + ".loot")) {
				lootableOrbs.add(skill.getId());
			}
		}
		powerDropRate = Math.max(config.get("Drops", "[Piece of Power] Approximate number of enemies you need to kill before a piece of power drops [minimum 20]", 50).getInt(), 20);
		// TODO playerWhipLootChance = config.get("Drops", "[Whip] Chance that a random item may be stolen from players, using a whip (0 to disable)[0-100]", 15).getInt();
		vanillaWhipLootChance = 0.01F * (float) MathHelper.clamp_int(config.get("Drops", "[Whip] Chance that loot may be snatched from various vanilla mobs, using a whip (0 to disable)[0-100]", 15).getInt(), 0, 100);
		globalWhipLootChance = 0.01F * (float) MathHelper.clamp_int(config.get("Drops", "[Whip] All whip-stealing chances are multiplied by this value, as a percentage, including any added by other mods (0 disables ALL whip stealing!)[0-500]", 100).getInt(), 0, 500);
		hurtOnSteal = config.get("Drops", "[Whip] Whether to inflict damage to entities when stealing an item (IEntityLootable entities determine this separately)", true).getBoolean(true);

		/*================== TRADES =====================*/
		config.setCategoryLanguageKey(TRADES, "config.zss.trade");

		enableTradeBombBag = config.getBoolean("[Bomb Bag] Barnes Sells Bomb Bags", TRADES, true, "Allow Barnes to sell bomb bags (checked each time Barnes is shown a bomb)", "config.zss.trade.enable_trade_bomb_bag");
		bombBagPrice = config.getInt("[Bomb Bag] Bomb Bag Cost", TRADES, 64, 32, 64, "Cost of a bomb bag at Barnes' shop (only applied to new trades)", "config.zss.trade.bomb_bag_price");
		enableTradeBomb = config.getBoolean("[Bombs] Villagers Can Trade Bombs", TRADES, true, "Enable random villager trades for bombs", "config.zss.trade.enable_trade_bomb");
		enableArrowTrades = config.getBoolean("[Hero's Bow] Can Trade Magic Arrows", TRADES, true, "Whether magic arrows (fire, ice, light) can be purchased", "config.zss.trade.enable_arrow_trades");
		maskBuyChance = 0.01F * config.getInt("[Masks] Mask Trade Chance", TRADES, 15, 1, 50, "Chance that a villager will be interested in purchasing a random mask", "config.zss.trade.mask_buy_chance");

		/*================== MOB SPAWNING =====================*/
		config.setCategoryLanguageKey(MOB_SPAWNS, "config.zss.mob_spawns").addCustomCategoryComment(MOB_SPAWNS, "Mobs use the 'Biome Type' lists to populate their individual spawn settings the first time the game is loaded.\nChanging the type lists after this point has no effect UNLESS you also delete the mob spawn locations in the\nconfig - this will force them to re-populate the next time the game is loaded.\nAlternatively, you may add new biomes directly to the individual mob spawn entries and completely ignore biome type.");

		roomSpawnMobChance = 0.01F * config.getInt("Mob Secret Room Spawn Chance", MOB_SPAWNS, 25, 0, 100, "Chance that a random mob will spawn inside of secret rooms (0 to disable)", "config.zss.mob_spawns.room_spawn_mob_chance");
		minDaysToSpawnDarknut = 24000 * config.getInt("Darknut Grace Period", MOB_SPAWNS, 7, 0, 30, "Minimum number of days required to pass before Darknuts may spawn", "config.zss.mob_spawns.min_days_to_spawn_darknut");
		minDaysToSpawnWizzrobe = 24000 * config.getInt("Wizzrode Grace Period", MOB_SPAWNS, 7, 0, 30, "Minimum number of days required to pass before Wizzrobes may spawn", "config.zss.mob_spawns.min_days_to_spawn_wizzrobe");

		/*================== MAP MAKING =====================*/
		config.addCustomCategoryComment("map making", "Configuration settings related to map making; none of these have any impact on normal play.");

		if (config.hasChanged()) {
			config.save();
		}
	}

	public static void postInit(){
		/*
		 * This method should only be called from the postInit phase of startup. Any changes made to the following
		 * would warrant a Minecraft restart to take into effect. Also, when the GuiConfig is refreshed, these should not be called,
		 * so as to not cause any registry errors. #postPropInit() allows for property refresh from the GuiConfig without bumping into any
		 * the previously listed problems
		 */
		WeaponRegistry.INSTANCE.registerItems(swords, "Config", true);
		WeaponRegistry.INSTANCE.registerItems(weapons, "Config", false);
		WeaponRegistry.INSTANCE.forbidItems(forbidden_swords, "Config", true);
		WeaponRegistry.INSTANCE.forbidItems(forbidden_weapons, "Config", false);
		// load boss types last because they rely on blocks, mobs, etc. to already have been initialized
		// other biome-related stuff just so all biomes can be sure to have loaded
		BiomeType.postInit(config);
		BossType.postInit(config);
		ZSSEntities.postInit(config);
		postPropInit();
	}

	public static void postPropInit() {
		/*================== SONGS =====================*/
		//TODO add to GuiConfig
		minSongIntervalStorm = MathHelper.clamp_int(config.get("Songs", "[Song of Storms] Time required between each use of the song (by anybody) [0-24000]", 600).getInt(), 0, 24000);
		minSongIntervalSun = MathHelper.clamp_int(config.get("Songs", "[Sun's Song] Time required between each use of the song (by anybody) [0-24000]", 1200).getInt(), 0, 24000);
		for (AbstractZeldaSong song : ZeldaSongs.getRegisteredSongs()) {
			if (!config.get("Songs", "Whether " + song.getDisplayName() + "'s main effect is enabled (does not affect notification of Song Blocks or Entities)", true).getBoolean(true)) {
				song.setIsEnabled(false);
			}
		}
		/*================== MAP MAKING =====================*/
		String[] warp_locations = config.get("map making", Config.WARP_LOCATIONS_KEY, new String[0]).getStringList();
		for (String entry : warp_locations) {
			String[] split = entry.split(":");
			if (split.length != 2) {
				ZSSMain.logger.warn("Invalid default warp location entry: " + entry);
			} else {
				AbstractZeldaSong song = ZeldaSongs.getSongByName(split[0]);
				WarpPoint warp = WarpPoint.convertFromString(split[1]);
				if (song == null) {
					ZSSMain.logger.warn("Default warp location entry contained invalid song name: " + split[0]);
				} else if (warp == null) {
					ZSSMain.logger.warn("Default warp location entry contained invalid warp point entry: " + split[1] + "\nExpected format is [dimension_id, x, y, z]");
				} else {
					warp_defaults.put(song, warp);
				}
			}
		}
		if (config.hasChanged()) {
			config.save();
		}
	}

	/*================== CLIENT SIDE SETTINGS  =====================*/
	public static int getHitsToDisplay() { return hitsToDisplay; }
	public static boolean preferTargetingMobs() { return targetMobs; }
	public static boolean toggleAutoTarget() { enableAutoTarget = !enableAutoTarget; return enableAutoTarget; }
	public static boolean toggleTargetPlayers() { canTargetPlayers = !canTargetPlayers; return canTargetPlayers; }
	public static int getNoteResetInterval() { return resetNotesInterval; }
	/*================== MOD INTER-COMPATIBILITY =====================*/
	public static boolean allowOffhandMaster() { return enableOffhandMaster; }
	/*================== GENERAL =====================*/
	public static boolean canPlayersBeStunned() { return enableStunPlayer; }
	public static boolean affectAllSwings() { return enableSwingSpeed; }
	public static int getBaseSwingSpeed() { return baseSwingSpeed; }
	public static boolean canLiftSecretStone() { return enableSecretStoneLift; }
	public static boolean canLiftVanilla() { return enableVanillaLift; }
	public static boolean canSmashVanilla() { return enableVanillaSmash; }
	public static boolean alwaysPickupHearts() { return alwaysPickupHearts; }
	public static boolean isHardcoreZeldaFan() { return enableHardcoreZeldaFanMode; }
	public static float getBossHealthFactor() { return bossHealthFactor; }
	public static int getNumBosses() { return bossNumber; }
	public static boolean doJarsUpdate() { return enableJarUpdates; }
	public static int getSacredFlameRefreshRate() { return sacredRefreshRate; }
	public static int getSkulltulaRewardRate() { return skulltulaRewardRate; }
	public static boolean areVanillaBuffsDisabled() { return disableVanillaBuffs; }
	public static boolean areNpcsInvulnerable() { return npcsAreInvulnerable; }
	public static int getNaviRange() { return naviRange; }
	public static int getNaviFrequency() { return naviFrequency; }
	/*================== MOBS =====================*/
	public static float getKeeseSwarmChance() { return keeseSwarmChance; }
	public static int getKeeseSwarmSize() { return keeseSwarmSize; }
	/*================== ITEMS =====================*/
	public static boolean getArrowsConsumeFlame() { return arrowsConsumeFlame; }
	public static boolean onlyBombSecretStone() { return onlyBombSecretStone; }
	public static boolean canGriefAdventure() { return bombsGriefAdventure; }
	public static int getBombFuseTime() { return bombFuseTime; }
	public static boolean canBoomerangDenude() { return enableBoomerangDenude; }
	public static boolean canDekuDenude() { return enableDekuDenude; }
	public static boolean isDinIgniteEnabled() { return enableDinIgnite; }
	public static boolean isDinMeltEnabled() { return enableDinMelt; }
	public static boolean areUnenchantablesDisabled() { return disableAllUnenchantables; }
	public static boolean allowMegaSmashQuakeStone() { return enableMegaSmashQuake; }
	public static int getHeroBowUpgradeCost() { return heroBowUpgradeCost; }
	public static boolean enableFireArrowIgnite() { return enableFireArrowIgnite; }
	public static boolean enableFireArrowMelt() { return enableFireArrowMelt; }
	public static boolean enableLightArrowNoClip() { return enableLightArrowNoClip; }
	public static boolean enableAutoBombArrows() { return enableAutoBombArrows; }
	public static int getHookshotRange() { return hookshotRange; }
	public static boolean allowHookableOnly() { return enableHookableOnly; }
	public static boolean canHookshotBreakBlocks() { return enableHookshotBreakBlocks; }
	public static boolean doMedallionsDamagePlayers() { return medallionsAffectPlayers; }
	public static int getRodUpgradeCost() { return rodUpgradeCost; }
	public static boolean getRodFireGriefing() { return rodFireGriefing; }
	public static int getRequiredKills() { return temperedRequiredKills - 1; }
	public static boolean getMasterSwordsProvidePower() { return allMasterSwordsProvidePower; }
	public static int getNumSkelKeyUses() { return numSkelKeyUses; }
	public static int getSlingshotCostOne() { return slingshotUpgradeOne; }
	public static int getSlingshotCostTwo() { return slingshotUpgradeTwo; }
	public static int getWhipRange() { return whipRange; }
	/*================== SKILLS =====================*/
	public static byte getMaxBonusHearts() { return (byte) maxBonusHearts; }
	public static boolean canDisarmorPlayers() { return allowDisarmorPlayer; }
	public static float getDisarmPenalty() { return disarmPenalty; }
	public static float getDisarmTimingBonus() { return disarmTimingBonus; }
	public static boolean allowUnlimitedNayru() { return allowUnlimitedNayru; }
	public static int getMaxMagicPoints() { return maxMagicPoints; }
	/** Returns amount of health that may be missing and still be able to activate certain skills (e.g. Sword Beam) */
	public static float getHealthAllowance(int level) {
		return (requireFullHealth ? 0.0F : (0.6F * level));
	}
	/*================== SONGS =====================*/
	public static int getMinIntervalStorm() { return minSongIntervalStorm; }
	public static int getMinIntervalSun() { return minSongIntervalSun; }
	/*================== DUNGEON GEN =====================*/
	/**
	 * Returns true if structure/feature generation is enabled for the given chunk coordinates
	 */
	public static boolean isGenEnabledAt(int chunkX, int chunkZ) {
		if (disableStructureGen) {
			return chunkX < noGenX || chunkZ < noGenZ;
		}
		return true;
	}
	public static boolean avoidModBlocks() { return avoidModBlocks; }
	public static boolean areWindowsEnabled() { return enableWindows; }
	public static boolean areBossDungeonsEnabled() { return enableBossDungeons; }
	public static boolean areBossDungeonsRandom() { return randomizeBossDungeons; }
	public static int getMinBossDistance() { return minBossDistance; }
	public static int getMinLandDistance() { return minLandDistance; }
	public static int getMinOceanDistance() { return minOceanDistance; }
	public static int getAttemptsPerChunk() { return genAttemptsPerChunk; }
	public static float getSecretRoomChance() { return secretRoomChance; }
	public static int getMainDungeonDifficulty() { return mainDungeonDifficulty; }
	public static int getNetherMinBossDistance() { return minBossDistanceNether; }
	public static int getNetherMinDistance() { return minDistanceNether; }
	public static int getNetherAttemptsPerChunk() { return genAttemptsPerChunkNether; }
	public static float getNetherSecretRoomChance() { return secretRoomChanceNether; }
	public static int getNetherDungeonDifficulty() { return netherDungeonDifficulty; }
	public static float getFairySpawnerChance() { return fairySpawnerChance; }
	public static int getDaysToRespawn() { return resetSpawnerTime; }
	/*================== WORLD GEN =====================*/
	public static float getAncientTabletGenChance() { return ancientTabletGenChance; }
	public static int getAncientTabletGenDistance() { return ancientTabletGenDistance; }
	public static boolean doBombFlowerGen() { return enableBombFlowerGen; }
	public static boolean genJarsInWater() { return allowJarsInWater; }
	public static float getJarGenChance() { return jarGenChance; }
	public static int getJarsPerCluster() { return jarsPerCluster; }
	public static float getJarGenChanceSub() { return jarGenChanceSub; }
	public static int getJarClustersPerChunkSub() { return jarClustersPerChunkSub; }
	public static int getJarsPerClusterSub() { return jarsPerClusterSub; }
	public static float getJarGenChanceNether() { return jarGenChanceNether; }
	public static int getJarClustersPerChunkNether() { return jarClustersPerChunkNether; }
	public static int getJarsPerClusterNether() { return jarsPerClusterNether; }
	public static float getGossipStoneRate() { return gossipStoneRate; }
	public static boolean doPillarGen() { return enablePillarGen; }
	public static int getPillarRange() { return maxPillarRange; }
	public static int getBrokenPillarMin() { return minBrokenPillarDistance; }
	public static int getSongPillarMin() { return minSongPillarDistance; }
	/*================== LOOT =====================*/
	public static float getMasterSwordChance() { return masterSwordChance; }
	public static float getLockedChestChance() { return lockedChestChance; }
	public static float getDoubleChestChance() { return doubleChestChance; }
	public static float getBarredRoomChance() { return barredRoomChance; }
	public static float getHeartPieceChance() { return heartPieceChance; }
	public static float getRandomBossItemChance() { return randomBossItemChance; }
	public static int getMinNumItems() { return minNumChestItems; }
	public static int getBombWeight() { return bombWeight; }
	public static int getBombBagWeight() { return bombBagWeight; }
	public static int getHeartWeight() { return heartPieceWeight; }
	public static int getBigKeyWeight() { return bigKeyWeight; }
	public static int getSmallKeyWeight() { return smallKeyWeight; }
	public static int getLockedLootWeight() { return lockedLootWeight; }
	public static boolean isLootableSkill(SkillBase skill) {
		return lootableOrbs.contains(skill.getId());
	}
	/*================== DROPS =====================*/
	public static float getGrassDropChance() { return grassDropChance; }
	public static float getJarDropChance() { return jarDropChance; }
	public static float getCreeperDropChance() { return creeperDrop; }
	public static int getMobConsumableFrequency() { return mobConsumableFrequency; }
	public static boolean areOrbDropsEnabled() { return enableOrbDrops; }
	public static float getChanceForRandomDrop() { return randomDropChance; }
	public static float getRandomMobDropChance() { return genericMobDropChance; }
	public static float getDropChance(int orbID) {
		return (orbDropChance.containsKey((byte) orbID) ? orbDropChance.get((byte) orbID) : 0.0F);
	}
	public static int getPowerDropRate() { return powerDropRate; }
	public static float getVanillaWhipLootChance() { return vanillaWhipLootChance; }
	public static float getWhipLootMultiplier() { return globalWhipLootChance; }
	public static boolean getHurtOnSteal() { return hurtOnSteal; }
	/*================== TRADES =====================*/
	public static boolean enableTradeBomb() { return enableTradeBomb; }
	public static boolean enableTradeBombBag() { return enableTradeBombBag; }
	public static int getBombBagPrice() { return bombBagPrice; }
	public static boolean areArrowTradesEnabled() { return enableArrowTrades; }
	public static float getMaskBuyChance() { return maskBuyChance; }
	/*================== MOB SPAWNING =====================*/
	public static float getRoomSpawnMobChance() { return roomSpawnMobChance; }
	public static int getTimeToSpawnDarknut() { return minDaysToSpawnDarknut; }
	public static int getTimeToSpawnWizzrobe() { return minDaysToSpawnWizzrobe; }

	/**
	 * Returns the default warp point for the given song, if any
	 */
	public static WarpPoint getDefaultWarpPoint(AbstractZeldaSong song) {
		return warp_defaults.get(song);
	}

	/**
	 * Sets the default warp point for the given song, returning the previous default warp point, if any
	 */
	public static WarpPoint setDefaultWarpPoint(AbstractZeldaSong song, WarpPoint warp) {
		BlockWarpStone.EnumWarpSong warp_block = BlockWarpStone.EnumWarpSong.bySong(song);
		if (warp_block != null) { // make sure song has a Warp Stone mapping
			WarpPoint previous = warp_defaults.put(song, warp);
			if (warp.equals(previous)) {
				return null; // nothing has changed
			}
			Config.saveDefaultWarpPoints();
			return previous;
		}
		ZSSMain.logger.warn("Attempted to set default warp point for non-warp song: " + song.getDisplayName());
		return null;
	}

	/**
	 * Saves default warp point map to the config file
	 */
	private static void saveDefaultWarpPoints() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<AbstractZeldaSong, WarpPoint> entry : warp_defaults.entrySet()) {
			builder.append(entry.getKey().getUnlocalizedName()).append(":").append(entry.getValue().convertToString()).append("\n");
		}
		Property prop = config.get("map making", Config.WARP_LOCATIONS_KEY, new String[0]);
		prop.set(builder.toString().split("\n"));
		config.getCategory("map making").put(Config.WARP_LOCATIONS_KEY, prop);
		if (config.hasChanged()) {
			config.save();
		}
	}

	/**
	 * Updates client settings from server packet
	 */
	public static void syncClientSettings(SyncConfigPacket msg) {
		if (!msg.isMessageValid()) {
			ZSSMain.logger.error("Invalid SyncConfigPacket attempting to process!");
			return;
		}
		Config.enableOffhandMaster = msg.enableOffhandMaster;
		Config.enableStunPlayer = msg.enableStunPlayer;
		Config.enableSwingSpeed = msg.enableSwingSpeed;
		Config.enableSecretStoneLift = msg.enableSecretStoneLift;
		Config.enableVanillaLift = msg.enableVanillaLift;
		Config.enableVanillaSmash = msg.enableVanillaSmash;
		Config.disableAllUnenchantables = msg.disableAllUnenchantables;
		Config.enableHookableOnly = msg.enableHookableOnly;
		Config.requireFullHealth = msg.requireFullHealth;
		Config.baseSwingSpeed = msg.baseSwingSpeed;
		Config.bombFuseTime = msg.bombFuseTime;
		Config.hookshotRange = msg.hookshotRange;
		Config.whipRange = msg.whipRange;
		Config.allMasterSwordsProvidePower = msg.allMasterSwordsProvidePower;
	}
}

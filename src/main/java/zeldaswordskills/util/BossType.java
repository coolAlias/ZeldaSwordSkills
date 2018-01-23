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

package zeldaswordskills.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.block.IHookable.HookshotType;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.mobs.EntityBlackKnight;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.entity.mobs.EntityWizzrobeGrand;
import zeldaswordskills.item.ItemBrokenSword;
import zeldaswordskills.item.ItemHookShotUpgrade.UpgradeType;
import zeldaswordskills.item.ItemPendant.PendantType;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.world.crisis.BossBattle;
import zeldaswordskills.world.crisis.DesertBattle;
import zeldaswordskills.world.crisis.EarthBattle;
import zeldaswordskills.world.crisis.FireBattle;
import zeldaswordskills.world.crisis.ForestBattle;
import zeldaswordskills.world.crisis.OceanBattle;
import zeldaswordskills.world.crisis.SwampBattle;

/**
 * 
 * Defined types for Boss Rooms, Big Keys and other things
 * 
 */
public enum BossType implements IStringSerializable
{
	HELL("temple_fire", "Fire Temple", 0, FireBattle.class, EntityBlaze.class, 7, ZeldaSongs.songWarpFire, "hell"),
	DESERT("temple_desert", "Desert Temple", 1, DesertBattle.class, EntityBlaze.class, 1, ZeldaSongs.songWarpSpirit, "desert", "deserthills"),
	FOREST("temple_forest", "Forest Temple", 2, ForestBattle.class, EntityCaveSpider.class, 4, ZeldaSongs.songWarpForest, "forest", "foresthills"),
	TAIGA("temple_ice", "Ice Temple", 3, BossBattle.class, EntitySkeleton.class, 5, ZeldaSongs.songWarpLight, "coldtaiga", "coldtaigahills", "iceplains"),
	OCEAN("temple_water", "Water Temple", 4, OceanBattle.class, EntityOctorok.class, 1, ZeldaSongs.songWarpWater, "ocean", "frozenocean", "deepocean"),
	SWAMP("temple_wind", "Wind Temple", 5, SwampBattle.class, EntityWizzrobeGrand.class, 4, ZeldaSongs.songWarpShadow, "swampland"),
	MOUNTAIN("temple_earth", "Earth Temple", 6, EarthBattle.class, EntityBlackKnight.class, 3, ZeldaSongs.songWarpOrder, "extremehills", "extremehillsedge");
	//END("temple_shadow", EntityEnderman.class, 7, "sky");
	// TODO negate Enderman teleport ability when spawned as a boss?, perhaps by adding a new Debuff
	// need to set their target and aggravation state so they attack automatically

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	private final String unlocalizedName;

	private final String title;

	/** Default biomes in which this dungeon can generate */
	private final String[] defaultBiomes;

	/** The class that will be used during the dungeon's Boss Battle */
	private final Class<? extends BossBattle> bossBattle;

	/** The mob class to spawn when a player enters the boss dungeon */
	private final Class<? extends IMob> bossMob;

	/** Metadata value of the door / key pair for this temple */
	public final int doorKeyMeta;

	/** Currently stores metadata value used by SecretStone for returning appropriate Block */
	public final int metadata;

	/** Song that may be used to warp to this dungeon, if any */
	public final AbstractZeldaSong warpSong;

	/** Unlocalized name to BossType mapping */
	private static final Map<String, BossType> stringToTypeMap = new HashMap<String, BossType>();

	/** Mapping of biome names to boss types */
	private static final Map<String, BossType> bossBiomeList = new HashMap<String, BossType>();

	private BossType(String name, String title, int doorKeyMeta, Class<? extends BossBattle> bossBattle, Class<? extends IMob> bossMob, int meta, AbstractZeldaSong warpSong, String... defaultBiomes) {
		this.unlocalizedName = name;
		this.title = title;
		this.doorKeyMeta = doorKeyMeta;
		this.defaultBiomes = defaultBiomes;
		this.bossBattle = bossBattle;
		this.bossMob = bossMob;
		this.metadata = meta;
		this.warpSong = warpSong;
	}

	@Override
	public String getName() {
		return unlocalizedName;
	}

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	/** Returns the translated name */
	public String getDisplayName() {
		return StatCollector.translateToLocal(this.getLangKey());
	}

	public String getLangKey(){
		return "dungeon.zss." + unlocalizedName + ".name";
	}

	@Override
	public String toString() {
		return ("Name: " + getUnlocalizedName() + " BossMob: " + (bossMob != null ? bossMob.toString() : "NULL") + " Block: " + metadata);
	}

	/**
	 * Loads biome lists from config file during post initialization
	 */
	public static void postInit(Configuration config) {
		String category = "dungeon generation";
		for (BossType type : BossType.values()) {
			addBiomes(type, config.getStringList("[Boss Dungeon] " + type.title + " Dungeon Biomes", category, type.defaultBiomes, "[Boss Dungeon] List of biomes in which " + type.title + "s can generate", (String[]) null, type.getLangKey()));
		}
	}

	/**
	 * Adds each biome name to the mapping for this BossType
	 */
	public static void addBiomes(BossType type, String[] biomeNames) {
		for (String biome : biomeNames) {
			if (biome.length() < 1) {
				continue;
			}
			biome = biome.toLowerCase().replace(" ", "");
			if (!BiomeType.isRealBiome(biome)) {
				ZSSMain.logger.warn(String.format("%s is not a recognized biome! This entry will be ignored for BossType %s", biome, type.getDisplayName()));
			} else if (bossBiomeList.containsKey(biome)) {
				ZSSMain.logger.warn(String.format("Error while adding %s for %s: biome already mapped to %s", biome, type.getDisplayName(), bossBiomeList.get(biome).getDisplayName()));
			} else {
				bossBiomeList.put(biome, type);
			}
		}
	}

	/**
	 * Return the boss type based on door or key metadata value (NOT secret stone metadata)
	 */
	public static BossType byDoorMetadata(int meta) {
		return BossType.values()[meta % BossType.values().length];
	}

	/**
	 * Get a BossType by name; will return null if it the name doesn't match any BossType's unlocalizedName
	 */
	public static BossType getBossType(String name) {
		if (stringToTypeMap.isEmpty()) {
			for (BossType type : BossType.values()) {
				stringToTypeMap.put(type.unlocalizedName, type);
			}
		}
		return stringToTypeMap.get(name.toLowerCase());
	}

	/**
	 * Returns the BossType for the biome at the given position, or null if no BossType exists for that biome
	 */
	public static BossType getBossType(World world, BlockPos pos) {
		BiomeGenBase biome = world.getBiomeGenForCoords(pos);
		if (biome == null) {
			ZSSMain.logger.warn(String.format("Null biome at %d/%d while getting Boss Type", pos.getX(), pos.getZ()));
			return null;
		}
		if (Config.areBossDungeonsRandom()) {
			int i = world.rand.nextInt(BossType.values().length);
			return BossType.values()[i];
		}
		return bossBiomeList.get(biome.biomeName.toLowerCase().replace(" ", ""));
	}

	/**
	 * Returns the specific ItemStack that is always found in this Boss Type's chests, if any
	 */
	public ItemStack getSpecialItem() {
		switch(this) {
		case DESERT: return new ItemStack(ZSSItems.pendant, 1, PendantType.COURAGE.ordinal());
		case MOUNTAIN: return new ItemStack(ZSSItems.pendant, 1, PendantType.POWER.ordinal());
		case OCEAN: return new ItemStack(ZSSItems.pendant, 1, PendantType.WISDOM.ordinal());
		default: return null;
		}
	}

	/**
	 * Returns a random special item fitting for the boss type, or null if none are available
	 */
	public ItemStack getRandomSpecialItem(Random rand) {
		ItemStack[] items = null;
		switch(this) {
		case DESERT: items = desertItems; break;
		case FOREST: items = forestItems; break;
		case HELL: items = netherItems; break;
		case MOUNTAIN: items = mountainItems; break;
		case OCEAN: items = oceanItems; break;
		case SWAMP: items = swampItems; break;
		case TAIGA: items = taigaItems; break;
		default:
		}
		if (items != null && items.length > 0) {
			return items[rand.nextInt(items.length)];
		}
		return null;
	}

	// Possible special items that may generate in this boss type's chests
	private static final ItemStack[] desertItems = {
		new ItemStack(ZSSItems.boomerang),
		new ItemStack(ZSSItems.bootsHover),
		new ItemStack(ZSSItems.hookshotUpgrade, 1, UpgradeType.EXTENDER.ordinal()),
		new ItemStack(ZSSItems.maskGibdo),
		new ItemStack(ZSSItems.rodFire)
	};
	private static final ItemStack[] forestItems = {
		new ItemStack(ZSSItems.dekuLeaf),
		new ItemStack(ZSSItems.heroBow),
		new ItemStack(ZSSItems.hookshot, 1, HookshotType.WOOD_SHOT.ordinal()),
		new ItemStack(ZSSItems.maskHawkeye),
		new ItemStack(ZSSItems.whip, 1, WhipType.WHIP_SHORT.ordinal())
	};
	private static final ItemStack[] mountainItems = {
		new ItemStack(ZSSItems.bootsPegasus),
		new ItemStack(ZSSItems.hammer),
		new ItemStack(ZSSItems.maskBlast),
		new ItemStack(ZSSItems.hookshotUpgrade, 1, UpgradeType.CLAW.ordinal()),
		ItemBrokenSword.getBrokenSwordFor(ZSSItems.swordGiant)
	};
	private static final ItemStack[] netherItems = {
		new ItemStack(ZSSItems.keySkeleton),
		new ItemStack(ZSSItems.heroBow),
		new ItemStack(ZSSItems.hookshotUpgrade, 1, UpgradeType.MULTI.ordinal()),
		new ItemStack(ZSSItems.maskMajora),
		new ItemStack(ZSSItems.tunicGoronChest)
	};
	private static final ItemStack[] oceanItems = {
		new ItemStack(ZSSItems.bootsHeavy),
		new ItemStack(ZSSItems.maskStone),
		new ItemStack(ZSSItems.slingshot),
		new ItemStack(ZSSItems.tunicZoraChest)
	};
	private static final ItemStack[] swampItems = {
		new ItemStack(ZSSItems.bootsRubber),
		new ItemStack(ZSSItems.hammer),
		new ItemStack(ZSSItems.heroBow),
		new ItemStack(ZSSItems.maskHawkeye),
		new ItemStack(ZSSItems.rodTornado)
	};
	private static final ItemStack[] taigaItems = {
		new ItemStack(ZSSItems.boomerang),
		new ItemStack(ZSSItems.bootsHover),
		new ItemStack(ZSSItems.gauntletsSilver),
		new ItemStack(ZSSItems.maskGiants),
		new ItemStack(ZSSItems.rodIce)
	};

	/**
	 * Returns a new instance of the appropriate BossBattle crisis event
	 */
	public final BossBattle getBossBattle(TileEntityDungeonCore core) {
		if (bossBattle == null) {
			ZSSMain.logger.error("Error retrieving boss battle event for " + toString());
			return null;
		}
		BossBattle battle = null;
		try {
			battle = (BossBattle) bossBattle.getConstructor(TileEntityDungeonCore.class).newInstance(core);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return battle;
	}

	/**
	 * Returns a new instance of the appropriate mob for this type, or null
	 * Note that no position or other information has been set, the default constructor(World) is used
	 */
	public final Entity getNewMob(World world) {
		if (bossMob == null) {
			ZSSMain.logger.error("Error retrieving boss mob for " + toString());
			return null;
		}
		Entity entity = null;
		try {
			entity = (Entity) bossMob.getConstructor(World.class).newInstance(world);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return entity;
	}
}

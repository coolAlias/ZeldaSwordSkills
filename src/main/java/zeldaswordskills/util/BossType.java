/**
    Copyright (C) <2015> <coolAlias>

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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.Configuration;
import zeldaswordskills.api.block.IHookable.HookshotType;
import zeldaswordskills.api.block.IWhipBlock.WhipType;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.mobs.EntityBlackKnight;
import zeldaswordskills.entity.mobs.EntityGrandWizzrobe;
import zeldaswordskills.entity.mobs.EntityOctorok;
import zeldaswordskills.item.ItemHookShotUpgrade.AddonType;
import zeldaswordskills.item.ItemPendant.PendantType;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
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
public enum BossType
{
	HELL("temple_fire", FireBattle.class, EntityBlaze.class, 7, ZeldaSongs.songWarpFire, "hell"),
	DESERT("temple_desert", DesertBattle.class, EntityBlaze.class, 1, ZeldaSongs.songWarpSpirit, "desert", "deserthills"),
	FOREST("temple_forest", ForestBattle.class, EntityCaveSpider.class, 4, ZeldaSongs.songWarpForest, "forest", "foresthills"),
	TAIGA("temple_ice", BossBattle.class, EntitySkeleton.class, 5, ZeldaSongs.songWarpLight, "coldtaiga", "coldtaigahills", "iceplains"),
	OCEAN("temple_water", OceanBattle.class, EntityOctorok.class, 1, ZeldaSongs.songWarpWater, "ocean", "frozenocean", "deepocean"),
	SWAMP("temple_wind", SwampBattle.class, EntityGrandWizzrobe.class, 4, ZeldaSongs.songWarpShadow, "swampland"),
	MOUNTAIN("temple_earth", EarthBattle.class, EntityBlackKnight.class, 3, ZeldaSongs.songWarpOrder, "extremehills", "extremehillsedge");

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	private final String unlocalizedName;

	/** Default biomes in which this dungeon can generate */
	private final String[] defaultBiomes;

	/** The class that will be used during the dungeon's Boss Battle */
	private final Class<? extends BossBattle> bossBattle;

	/** The mob class to spawn when a player enters the boss dungeon */
	private final Class<? extends IMob> bossMob;

	/** Currently stores metadata value used by SecretStone for returning appropriate Block */
	public final int metadata;

	/** Song that may be used to warp to this dungeon, if any */
	public final AbstractZeldaSong warpSong;

	/** Unlocalized name to BossType mapping */
	private static final Map<String, BossType> stringToTypeMap = new HashMap<String, BossType>();

	/** Mapping of biome names to boss types */
	private static final Map<String, BossType> bossBiomeList = new HashMap<String, BossType>();

	private BossType(String name, Class<? extends BossBattle> bossBattle, Class<? extends IMob> bossMob, int meta, AbstractZeldaSong warpSong, String... defaultBiomes) {
		this.unlocalizedName = name;
		this.defaultBiomes = defaultBiomes;
		this.bossBattle = bossBattle;
		this.bossMob = bossMob;
		this.metadata = meta;
		this.warpSong = warpSong;
	}

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	/** Returns the translated name */
	public String getDisplayName() {
		return StatCollector.translateToLocal("dungeon.zss." + unlocalizedName + ".name");
	}

	@Override
	public String toString() {
		return ("Name: " + getUnlocalizedName() + " BossMob: " + (bossMob != null ? bossMob.toString() : "NULL") + " Block: " + metadata);
	}

	/**
	 * Loads biome lists from config file during post initialization
	 */
	public static void postInit(Configuration config) {
		for (BossType type : BossType.values()) {
			addBiomes(type, config.get("Dungeon Generation", String.format("[Boss Dungeon] List of biomes in which %ss can generate", type.getDisplayName()), type.defaultBiomes).getStringList());
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
				LogHelper.warning(String.format("%s is not a recognized biome! This entry will be ignored for BossType %s", biome, type.getDisplayName()));
			} else if (bossBiomeList.containsKey(biome)) {
				LogHelper.warning(String.format("Error while adding %s for %s: biome already mapped to %s", biome, type.getDisplayName(), bossBiomeList.get(biome).getDisplayName()));
			} else {
				bossBiomeList.put(biome, type);
			}
		}
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
	 * Returns the BossType for the biome at x/z, or null if no BossType exists for that biome
	 */
	public static BossType getBossType(World world, int x, int z) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if (biome == null) {
			LogHelper.warning("Null biome at " + x + "/" + z + " while getting Boss Type");
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
		new ItemStack(ZSSItems.hookshotAddon, 1, AddonType.EXTENSION.ordinal()),
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
		new ItemStack(ZSSItems.hookshotAddon, 1, AddonType.STONECLAW.ordinal()),
		new ItemStack(ZSSItems.swordBroken, 1, ZSSItems.swordGiant.itemID)
	};
	private static final ItemStack[] netherItems = {
		new ItemStack(ZSSItems.keySkeleton),
		new ItemStack(ZSSItems.heroBow),
		new ItemStack(ZSSItems.hookshotAddon, 1, AddonType.MULTI.ordinal()),
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
	@SuppressWarnings("finally")
	public final BossBattle getBossBattle(TileEntityDungeonCore core) {
		if (bossBattle == null) {
			LogHelper.warning("Error retrieving boss battle event for " + toString());
			return null;
		}
		BossBattle battle = null;
		try {
			try {
				battle = (BossBattle) bossBattle.getConstructor(TileEntityDungeonCore.class).newInstance(core);
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		} finally {
			return battle;
		}
	}

	/**
	 * Returns a new instance of the appropriate mob for this type, or null
	 * Note that no position or other information has been set, the default constructor(World) is used
	 */
	@SuppressWarnings("finally")
	public final Entity getNewMob(World world) {
		if (bossMob == null) {
			LogHelper.warning("Error retrieving boss mob for " + toString());
			return null;
		}
		Entity entity = null;
		try {
			try {
				entity = (Entity) bossMob.getConstructor(World.class).newInstance(world);
			} catch (InstantiationException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				return null;
			}
		} finally {
			return entity;
		}
	}
}

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

package zeldaswordskills.util;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import zeldaswordskills.api.item.HookshotType;
import zeldaswordskills.entity.EntityOctorok;
import zeldaswordskills.item.ItemHookShotUpgrade.AddonType;
import zeldaswordskills.item.ItemPendant.PendantType;
import zeldaswordskills.item.ZSSItems;

/**
 * 
 * Defined types for Boss Rooms, Big Keys and other things
 * 
 */
public enum BossType
{
	HELL("temple_fire", EntityBlaze.class, 7, "hell"),
	DESERT("temple_desert", EntityBlaze.class, 1, "desert", "deserthills"),
	FOREST("temple_forest", EntityCaveSpider.class, 4, "forest", "foresthills"),
	TAIGA("temple_ice", EntitySkeleton.class, 5, "taiga", "taigahills", "iceplains"),
	OCEAN("temple_water", EntityOctorok.class, 1, "ocean", "frozenocean"),
	SWAMP("temple_wind", EntityWitch.class, 4, "swampland"),
	MOUNTAIN("temple_earth", EntityZombie.class, 3, "extremehills", "extremehillsedge");

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	private final String unlocalizedName;

	/** Default biomes in which this dungeon can generate */
	private final String[] defaultBiomes;

	/** The mob class to spawn when a player enters the boss dungeon */
	private final Class<? extends IMob> bossMob;

	/** Currently stores metadata value used by SecretStone for returning appropriate Block */
	public final int metadata;

	/** Unlocalized name to BossType mapping */
	private static final Map<String, BossType> stringToTypeMap = new HashMap<String, BossType>();
	/** Mapping of biome names to boss types */
	private static final Map<String, BossType> bossBiomeList = new HashMap<String, BossType>();

	private BossType(String name, Class<? extends IMob> bossMob, int block, String... defaultBiomes) {
		this.unlocalizedName = name;
		this.defaultBiomes = defaultBiomes;
		this.bossMob = bossMob;
		this.metadata = block;
	}

	/** Name that can be used to retrieve the BossType from {@link #getBossType(String)} */
	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	/** Returns the translated name */
	public String getDisplayName() {
		return StatCollector.translateToLocal("dungeon.zss." + unlocalizedName + ".name");
	}

	/** Default biomes in which this dungeon can generate */
	public String[] getDefaultBiomes() {
		return defaultBiomes;
	}

	@Override
	public String toString() {
		return String.format("Name: %s BossMob: %s Block: %s", getUnlocalizedName(),
				(bossMob != null ? bossMob.toString() : "NULL"), metadata);
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
			if (bossBiomeList.containsKey(biome)) {
				LogHelper.log(Level.WARNING, String.format("Error while adding %s for %s: biome already mapped to %s",
						biome, type.getDisplayName(), bossBiomeList.get(biome).getDisplayName()));
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
			LogHelper.log(Level.WARNING, "Null biome at " + x + "/" + z + " while getting Boss Type");
			return null;
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
		new ItemStack(ZSSItems.maskGibdo)
	};
	private static final ItemStack[] forestItems = {
		new ItemStack(ZSSItems.dekuLeaf),
		new ItemStack(ZSSItems.heroBow),
		new ItemStack(ZSSItems.hookshot, 1, HookshotType.WOOD_SHOT.ordinal()),
		new ItemStack(ZSSItems.maskHawkeye)
	};
	private static final ItemStack[] mountainItems = {
		new ItemStack(ZSSItems.bootsPegasus),
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
		new ItemStack(ZSSItems.maskHawkeye)
	};
	private static final ItemStack[] taigaItems = {
		new ItemStack(ZSSItems.boomerang),
		new ItemStack(ZSSItems.bootsHover),
		new ItemStack(ZSSItems.gauntletsSilver),
		new ItemStack(ZSSItems.maskGiants)
	};

	/**
	 * Returns a new instance of the appropriate mob for this type, or null
	 * Note that no position or other information has been set, the default constructor(World) is used
	 */
	@SuppressWarnings("finally")
	public static final Entity getNewMob(BossType type, World world) {
		if (type.bossMob == null) {
			LogHelper.log(Level.WARNING, "Error retrieving boss mob for " + type.toString());
			return null;
		}
		Entity entity = null;
		try {
			try {
				entity = (Entity) type.bossMob.getConstructor(World.class).newInstance(world);
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

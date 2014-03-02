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
import java.util.EnumMap;
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
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import zeldaswordskills.entity.EntityOctorok;
import zeldaswordskills.item.ItemHookShot.ShotType;
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
	HELL(7),DESERT(1),FOREST(4),TAIGA(5),OCEAN(1),SWAMP(4),MOUNTAIN(3),END(7);
	
	public final int metadata;
	
	private BossType(int meta) {
		metadata = meta;
	}
	
	/** BossType to Mob spawned mapping */
	private static final Map<BossType, Class<? extends IMob>> mobSpawned = new EnumMap(BossType.class);
	
	@Override
	public String toString() {
		switch(this) {
		case HELL: return "Nether";
		case DESERT: return "Desert";
		case FOREST: return "Forest";
		case OCEAN: return "Ocean";
		case TAIGA: return "Taiga";
		case SWAMP: return "Swamp";
		case MOUNTAIN: return "Mountain";
		case END: return "The End";
		default: return "Unknown Boss Type";
		}
	}
	
	public static BossType getBossType(World world, int x, int z) {
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		if (biome == null) {
			LogHelper.log(Level.WARNING, "Null biome at " + x + "/" + z + " while getting Boss Type");
			return null;
		}
		String name = biome.biomeName.toLowerCase();
		if (world.provider.isHellWorld) {
			return HELL;
		} else if (name.contains("desert")) {
			return DESERT;
		} else if (name.contains("extreme")) {
			return MOUNTAIN;
		} else if (name.contains("forest")) {
			return FOREST;
		} else if (name.contains("ocean")) {
			return OCEAN;
		} else if (name.contains("swamp")) {
			return SWAMP;
		} else if (name.contains("taiga")) {
			return TAIGA;
		} else {
			return null;
		}
		/*
		else if (name.contains("sky")) {
			return END;
		}
		 */
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
		new ItemStack(ZSSItems.hookshot, 1, ShotType.WOOD_SHOT.ordinal()),
	};
	private static final ItemStack[] mountainItems = {
		new ItemStack(ZSSItems.bootsPegasus),
		new ItemStack(ZSSItems.maskBlast),
		new ItemStack(ZSSItems.hookshotAddon, 1, AddonType.STONECLAW.ordinal()),
		new ItemStack(ZSSItems.swordBroken, 1, ZSSItems.swordGiant.itemID)
	};
	private static final ItemStack[] netherItems = {
		new ItemStack(ZSSItems.gauntletsSilver),
		new ItemStack(ZSSItems.hookshotAddon, 1, AddonType.MULTI.ordinal()),
		new ItemStack(ZSSItems.tunicGoronChest)
	};
	private static final ItemStack[] oceanItems = {
		new ItemStack(ZSSItems.bootsHeavy),
		new ItemStack(ZSSItems.maskStone),
		new ItemStack(ZSSItems.slingshot),
		new ItemStack(ZSSItems.tunicZoraChest),
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
	 */
	@SuppressWarnings("finally")
	public static final Entity getNewMob(BossType type, World world) {
		if (mobSpawned.containsKey(type)) {
			Entity entity = null;
			try {
				try {
					entity = (Entity) mobSpawned.get(type).getConstructor(World.class).newInstance(world);
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
		} else {
			return null;
		}
	}
	
	static {
		mobSpawned.put(BossType.HELL, EntityBlaze.class);
		mobSpawned.put(BossType.DESERT, EntityBlaze.class);
		mobSpawned.put(BossType.MOUNTAIN, EntityZombie.class);
		mobSpawned.put(BossType.OCEAN, EntityOctorok.class);
		mobSpawned.put(BossType.FOREST, EntityCaveSpider.class);
		mobSpawned.put(BossType.SWAMP, EntityWitch.class);
		mobSpawned.put(BossType.TAIGA, EntitySkeleton.class);
		//mobSpawned.put(BossType.END, EntityGhast.class); // ghast needs large open area - gets stuck
	}
}

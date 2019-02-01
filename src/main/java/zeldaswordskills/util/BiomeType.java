/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import zeldaswordskills.ZSSMain;

/**
 * 
 * Helper enum for determining mob types on spawn, based on generic biome 'type.'
 *
 */
public enum BiomeType {
	ARID("arid", "Desert", "Desert Hills", "Savanna", "Savanna Plateau"),
	BEACH("beach", "Beach"),
	COLD("cold", "Cold Beach", "Cold Taiga", "Cold Taiga Hills", "Frozen River", "Ice Mountains", "Ice Plains"),
	FIERY("fiery", "Hell", "Mesa", "Mesa Plateau", "Mesa Plateau F"),
	FOREST("forest", "Birch Forest", "Birch Forest Hills", "Forest", "Forest Hills", "Roofed Forest"),
	JUNGLE("jungle", "Jungle", "Jungle Edge", "Jungle Hills"),
	MOUNTAIN("mountain", "Extreme Hills", "Extreme Hills+", "Extreme Hills Edge"),
	OCEAN("ocean", "Ocean", "Frozen Ocean", "Deep Ocean"),
	PLAINS("plains", "Plains"),
	RIVER("river", "River", "Swampland"),
	SHROOM("shroom", "MushroomIsland", "MushroomIslandShore"),
	TAIGA("taiga", "Taiga", "Taiga Hills", "Mega Taiga", "Mega Taiga Hills");

	private final String unlocalizedName;
	
	/** Default biomes for this type */
	public final String[] defaultBiomes;

	/** Mapping of biome names to biome types */
	private static final Map<String, BiomeType> biomeTypeList = new HashMap<String, BiomeType>();

	private BiomeType(String name, String... defaultBiomes) {
		this.unlocalizedName = name;
		this.defaultBiomes = defaultBiomes;
	}

	@Override
	public String toString() {
		return StatCollector.translateToLocal(this.getLangKey());
	}
	
	public String getLangKey(){
		return "biometype.zss." + unlocalizedName + ".name";
	}
	
	public String getCapName(){
		return String.valueOf(this.unlocalizedName.charAt(0)).toUpperCase() + this.unlocalizedName.substring(1);
	}
	
	/**
	 * Loads biome type lists from config file
	 */
	public static void postInit(Configuration config) {
		for (BiomeType type : BiomeType.values()) {
			addBiomes(type, config.getStringList("[Biome Types] " + type.getCapName() + " Biomes", "mob spawns", type.defaultBiomes, "List of " + type.name() + " type biomes - certain mobs spawn differently depending on the biome type", (String[]) null, type.getLangKey()));
		}
	}

	/**
	 * Adds each biome name to the mapping for this BiomeType
	 */
	private static void addBiomes(BiomeType type, String[] biomeNames) {
		for (String biome : biomeNames) {
			if (biome.length() < 1) {
				continue;
			}
			biome = biome.toLowerCase().replace(" ", "");
			if (!isRealBiome(biome)) {
				ZSSMain.logger.warn(String.format("%s is not a recognized biome! This entry will be ignored for BiomeType %s", biome, type.toString()));
			} else if (biomeTypeList.containsKey(biome)) {
				ZSSMain.logger.warn(String.format("Error while adding %s for %s: biome already mapped to %s", biome, type.toString(), biomeTypeList.get(biome).toString()));
			} else {
				biomeTypeList.put(biome, type);
			}
		}
	}

	/**
	 * Returns the BiomeType for the given BiomeGenBase, or null if none exists
	 * @param biome	Null is allowed for directly passing {@link World#getBiomeGenForCoords}
	 */
	public static BiomeType getBiomeTypeFor(BiomeGenBase biome) {
		if (biome != null && biome.biomeName != null && biome.biomeName.length() > 0) {
			return biomeTypeList.get(biome.biomeName.toLowerCase().replace(" ", ""));
		}
		return null;
	}

	/**
	 * Returns true if the name given is a real biome
	 */
	public static boolean isRealBiome(String name) {
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null && biome.biomeName != null && biome.biomeName.toLowerCase().replace(" ", "").equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Combines the given biome types into a single master array, less any biomes to ignore
	 * @param ignore Array of biome names (strings) to ignore; may be null 
	 */
	public static String[] getBiomeArray(String[] ignore, BiomeType... types) {
		List<String> combined = new ArrayList<String>();
		for (BiomeType biomes : types) {
			for (String biome : biomes.defaultBiomes) {
				if (ignore == null || Arrays.binarySearch(ignore, biome, String.CASE_INSENSITIVE_ORDER) < 0) {
					combined.add(biome);
				}
			}
		}
		return combined.toArray(new String[combined.size()]);
	}
}

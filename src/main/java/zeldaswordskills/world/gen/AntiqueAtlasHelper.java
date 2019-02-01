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

package zeldaswordskills.world.gen;

import hunternif.mc.atlas.api.AtlasAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BossType;

/**
 * 
 * Helper class for registering custom tiles with the Antique Atlas mod
 *
 */
public class AntiqueAtlasHelper
{
	/**
	 * Places custom tile at the given world position
	 */
	public static void placeCustomTile(World world, String tileName, int x, int y, int z) {
		placeCustomTile(world, tileName, x >> 4, z >> 4);
	}

	/**
	 * Places global custom tile at the given chunk coordinates
	 */
	public static void placeCustomTile(World world, String tileName, int chunkX, int chunkZ) {
		if (ZSSMain.isAtlasEnabled && !world.isRemote) {
			try {
				AtlasAPI.getTileAPI().putCustomGlobalTile(world, tileName, chunkX, chunkZ);
			} catch (Exception e) {
				ZSSMain.logger.error("Unable to add Atlas data: " + e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Registers all custom tile textures to the Atlas
	 */
	@SideOnly(Side.CLIENT)
	public static void registerTextures() {
		if (ZSSMain.isAtlasEnabled) {
			try {
				for (BossType type : BossType.values()) {
					String name = ModInfo.ATLAS_DUNGEON_ID + type.ordinal();
					AtlasAPI.getTileAPI().setCustomTileTexture(name, new ResourceLocation(ModInfo.ID, "textures/atlas/" + name + ".png"));
					AtlasAPI.getTileAPI().setCustomTileTexture(name + "_fin", new ResourceLocation(ModInfo.ID, "textures/atlas/" + name + "_fin.png"));
				}
			} catch (Exception e) {
				ZSSMain.logger.error(e.getLocalizedMessage());
			}
		}
	}
}

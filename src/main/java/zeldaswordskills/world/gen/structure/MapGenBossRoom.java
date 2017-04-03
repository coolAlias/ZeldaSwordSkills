/**
    Copyright (C) <2017> <coolAlias>

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

package zeldaswordskills.world.gen.structure;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.StructureGenUtils;
import zeldaswordskills.world.gen.AntiqueAtlasHelper;

/**
 * 
 * The structureMap for Boss Dungeons uses the standard chunkXZ pair for the key and stores
 * the BossType ordinal as the value.
 *
 */
public class MapGenBossRoom extends ZSSMapGenBase
{
	@Override
	public void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ) {
		this.setWorld(world);
		this.loadOrCreateData(world);
		int size = rand.nextInt(5) + 9;
		int posX = (chunkX << 4) + rand.nextInt(16 - size);
		int posZ = (chunkZ << 4) + rand.nextInt(16 - size);
		// Require minimum of half the minimum distance between dungeons as buffer around villages
		if (world.villageCollectionObj != null && world.villageCollectionObj.findNearestVillage(posX, 64, posZ, 8 * Config.getMinBossDistance()) != null) {
			return;
		}
		BossType type = BossType.getBossType(world, posX, posZ);
		if (type != null) {
			RoomBoss room = new RoomBoss(type, chunkX, chunkZ, rand, size, Blocks.stone);
			if (rand.nextFloat() < 0.2F && !areStructuresWithinRange(room, Config.getMinBossDistance())) {
				int posY = StructureGenUtils.getAverageSurfaceHeight(world, posX, posZ);
				if (room.generate(this, world, rand, posX, posY, posZ)) {
					onStructureGenerated(world, room);
				}
			}
		}
	}

	@Override
	public String getTagName() {
		return "zssBossRooms";
	}

	/**
	 * Always returns null for boss rooms; generate on a chunk-by-chunk basis only
	 */
	@Override
	protected StructureBoundingBox getStructureBBAt(int x, int y, int z) {
		return null;
	}

	/**
	 * Whether the lake generating at chunkX and chunkZ should be denied or not
	 * (prevents lakes from destroying boss rooms within one chunk)
	 */
	public boolean shouldDenyLakeAt(int chunkX, int chunkZ) {
		return (isRoomInChunk(chunkX, chunkZ) || isRoomInChunk(chunkX + 1, chunkZ + 1) ||
				isRoomInChunk(chunkX + 1, chunkZ - 1) || isRoomInChunk(chunkX - 1, chunkZ + 1) ||
				isRoomInChunk(chunkX - 1, chunkZ - 1) || isRoomInChunk(chunkX, chunkZ + 1) ||
				isRoomInChunk(chunkX, chunkZ - 1) || isRoomInChunk(chunkX + 1, chunkZ) ||
				isRoomInChunk(chunkX - 1, chunkZ));
	}

	/**
	 * Returns true if a boss room exists in the chunk provided
	 */
	protected boolean isRoomInChunk(int chunkX, int chunkZ) {
		return structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)));
	}

	/**
	 * @param range range is in chunks
	 */
	@Override
	public boolean areStructuresWithinRange(RoomBase room, int range) {
		loadOrCreateData(this.worldObj.get());
		for (int i = room.chunkX - range; i <= room.chunkX + range; ++i) {
			for (int j = room.chunkZ - range; j <= room.chunkZ + range; ++j) {
				if (structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)))) {
					BossType type = ((RoomBoss) room).getBossType();
					if (type != null && (Config.areBossDungeonsRandom() || type.ordinal() == getBossTypeFor(i, j))) {
						return true;
					} else if (((room.chunkX - i) * (room.chunkX - i) + (room.chunkZ - j) * (room.chunkZ - j)) < (range * range) / 2) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	protected void translateNbtIntoMap(NBTTagCompound compound) {
		if (compound.hasKey("chunkX") && compound.hasKey("chunkZ") && compound.hasKey("bossType")) {
			int i = compound.getInteger("chunkX");
			int j = compound.getInteger("chunkZ");
			int bossType = compound.getInteger("bossType");
			structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), bossType);
		} else {
			ZSSMain.logger.warn("Failed to translate Boss Room NBT compound into structure map");
		}
	}

	/**
	 * Returns the ordinal value of the BossType of the Boss Dungeon in chunkXZ, or -1 if
	 * no structure exists there
	 */
	protected int getBossTypeFor(int chunkX, int chunkZ) {
		if (structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)))) {
			return (Integer) structureMap.get(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)));
		} else {
			return -1;
		}
	}

	/**
	 * Updates the structure map and adds the appropriate nbt compound to the room data
	 */
	protected void onStructureGenerated(World world, RoomBoss room) {
		AntiqueAtlasHelper.placeCustomTile(world, ModInfo.ATLAS_DUNGEON_ID + room.getBossType().ordinal(), room.chunkX, room.chunkZ);
		structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(room.chunkX, room.chunkZ)), room.getBossType().ordinal());
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("bossType", room.getBossType().ordinal());
		addRoomTag(compound, room.chunkX, room.chunkZ);
	}
}

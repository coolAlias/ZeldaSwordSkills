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

package zeldaswordskills.world.gen.structure;

import java.util.Random;
import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import zeldaswordskills.lib.Config;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.StructureGenUtils;

/**
 * 
 * Generates secret rooms in each chunk according to the config specifications.
 * 
 * The structureMap for Secret Rooms uses the standard chunkXZ pair for the key and stores
 * an NBTTagList of all the rooms generated in that chunk as the value.
 *
 */
public class MapGenSecretRoom extends ZSSMapGenBase
{
	@Override
	public void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ) {
		this.worldObj = world;
		loadOrCreateData(worldObj);
		NBTTagList roomList = getStructureListFor(chunkX, chunkZ);
		int posX = chunkX << 4;
		int posZ = chunkZ << 4;
		int posY = StructureGenUtils.getAverageSurfaceHeight(world, posX, posZ);
		if (posY < 1) {
			return;
		}
		for (int i = 0; i < Config.getAttemptsPerChunk(); ++i) {
			if (rand.nextFloat() < Config.getSecretRoomChance()) {
				int x = posX + rand.nextInt(16);
				int y = rand.nextInt(posY) + (i % 2 == 0 ? rand.nextInt(16) : rand.nextInt(8));
				int z = posZ + rand.nextInt(16);
				RoomSecret room = new RoomSecret(chunkX, chunkZ, Math.min(rand.nextInt(6) + 3, 6), Block.stone.blockID);
				if (room.generate(this, world, rand, x, y, z)) {
					roomList.appendTag(room.writeToNBT());
					updateChunkStructureMap(roomList, chunkX, chunkZ);
				}
			}
		}
		
		if (roomList.tagCount() > 0) {
			//LogHelper.log(Level.INFO, "roomList for chunk " + chunkX + "/" + chunkZ + " contains " + roomList.tagCount() + " elements");
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("roomList", roomList);
			addRoomTag(compound, chunkX, chunkZ);
		}
	}
	
	@Override
	public String getTagName() {
		return "zssSecretRooms";
	}

	@Override
	protected StructureBoundingBox getStructureBBAt(int x, int y, int z) {
		NBTTagList roomList = getStructureListFor(x >> 4, z >> 4);
		for (int i = 0; i < roomList.tagCount(); ++i) {
			NBTTagCompound compound = (NBTTagCompound) roomList.tagAt(i);
			if (compound.hasKey("BB")) {
				StructureBoundingBox box = new StructureBoundingBox(compound.getIntArray("BB"));
				if (box.isVecInside(x, y, z)) {
					return box;
				}
			}
		}

		return null;
	}
	
	@Override
	protected void translateNbtIntoMap(NBTTagCompound compound) {
		if (compound.hasKey("chunkX") && compound.hasKey("chunkZ") && compound.hasKey("roomList")) {
			int i = compound.getInteger("chunkX");
			int j = compound.getInteger("chunkZ");
			NBTTagList roomList = compound.getTagList("roomList");
			structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(i, j)), roomList);
		} else {
			LogHelper.log(Level.WARNING, "Failed to translate NBT compound into structure map");
		}
	}
	
	/**
	 * Returns an NBTTagList of all the structures generated in the given chunk or a new list if none exists
	 */
	protected NBTTagList getStructureListFor(int chunkX, int chunkZ) {
		loadOrCreateData(worldObj);
		if (structureMap.containsKey(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)))) {
			return (NBTTagList) structureMap.get(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)));
		} else {
			return new NBTTagList();
		}
	}
	
	/** Keeps the structure map's room list for this chunk up-to-date during generation */
	protected void updateChunkStructureMap(NBTTagList roomList, int chunkX, int chunkZ) {
		structureMap.put(Long.valueOf(ChunkCoordIntPair.chunkXZ2Int(chunkX, chunkZ)), roomList);
	}

	@Override
	public boolean areStructuresWithinRange(RoomBase room, int range) {
		StructureBoundingBox box = room.getBoundingBox();
		// check room's containing chunk first
		if (isNearStructureInChunk(room, box, room.chunkX, room.chunkZ, range)) {
			return true;
		}
		
		for (int i = 0; i <= (range + 8) / 16; ++i) {
			// neighbors along x and z axis directly
			if (isNearStructureInChunk(room, box, room.chunkX + i + 1, room.chunkZ, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX - i - 1, room.chunkZ, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX, room.chunkZ + i + 1, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX, room.chunkZ - i - 1, range)) {
				return true;
			}

			// check neighbors diagonally
			if (isNearStructureInChunk(room, box, room.chunkX + i + 1, room.chunkZ + i + 1, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX + i + 1, room.chunkZ - i - 1, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX - i - 1, room.chunkZ + i + 1, range)) {
				return true;
			}
			if (isNearStructureInChunk(room, box, room.chunkX - i - 1, room.chunkZ - i - 1, range)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Returns true if the room's bounding box is within the specified range of any other structures in the given chunk
	 */
	protected boolean isNearStructureInChunk(RoomBase room, StructureBoundingBox box1, int chunkX, int chunkZ, int range) {
		NBTTagList roomList = getStructureListFor(chunkX, chunkZ);
		for (int i = 0; i < roomList.tagCount(); ++i) {
			NBTTagCompound compound = (NBTTagCompound) roomList.tagAt(i);
			if (compound.hasKey("BB")) {
				StructureBoundingBox box2 = new StructureBoundingBox(compound.getIntArray("BB"));
				double dx = (box1.getXSize() + box2.getXSize()) / 2;
				if (StructureGenUtils.getDistanceSqBetween(box1, box2) < ((range + dx) * (range + dx))) {
					if (room.inOcean && box1.minY > box2.maxY + (range / 4) + 2) {
						continue;
					} else {
						return true;
					}
				}
			} else {
				LogHelper.log(Level.WARNING, "Invalid tag while checking for structures in chunk " + chunkX + "/" + chunkZ);
			}
		}
		
		return false;
	}
}

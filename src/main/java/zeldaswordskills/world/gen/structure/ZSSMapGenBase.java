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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.common.util.Constants;

public abstract class ZSSMapGenBase
{
	/** Saved and loaded world data containing previously generated structures */
	private RoomGenData roomData;

	/**
	 * Used to store a list of all structures that have been recursively generated. Used so that during recursive
	 * generation, the structure generator can avoid generating structures that intersect ones that have already been
	 * placed. Each chunk coordinate pair key returns a list of bounding boxes.
	 */
	protected Map<Long, Object> structureMap = new HashMap<Long, Object>();

	/** A weak reference to the current World object; should be set during each call to {@link #generate} */
	protected WeakReference<World> worldObj;

	/** Creates a weak reference to the world object */
	protected final void setWorld(World world) {
		this.worldObj = new WeakReference<World>(world);
	}

	/** Generates all relevant structures within the chunk provided */
	public abstract void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ);

	/** The name of the NBTTagCompound stored in the world data */
	public abstract String getTagName();

	/** Returns the StructureBoundingBox located at x/y/z or null if none intersects with those coordinates */
	protected abstract StructureBoundingBox getStructureBBAt(int x, int y, int z);

	/**
	 * Returns true if any other structures exist within the given range from the room
	 * @param range the distance to check, either in blocks or chunks depending on implementation
	 */
	public abstract boolean areStructuresWithinRange(RoomBase room, int range);

	/**
	 * Reads appropriate data from NBT compound and places it in the structure map; this
	 * allows for different storage formats (NBTTagCompound, NBTTagList, etc) in each MapGen
	 */
	protected abstract void translateNbtIntoMap(NBTTagCompound compound);

	/**
	 * Wrapper method to add compound to roomData, using chunk coordinates as the tag identifier
	 */
	protected final void addRoomTag(NBTTagCompound compound, int chunkX, int chunkZ) {
		roomData.addRoomTag(compound, chunkX, chunkZ);
		roomData.markDirty();
	}

	/**
	 * If roomData is null, it is loaded from world storage if available or a new one is created
	 */
	protected final void loadOrCreateData(World world) {
		if (world == null) { return; } // just in case weak reference returned null
		if (roomData == null) {
			roomData = (RoomGenData) world.perWorldStorage.loadData(RoomGenData.class, getTagName());
			if (roomData == null) {
				roomData = new RoomGenData(getTagName());
				world.perWorldStorage.setData(getTagName(), roomData);
			} else {
				NBTTagCompound compound = roomData.getRoomData();
				// func_150296_c is getKeySet()
				Iterator<String> iterator = compound.func_150296_c().iterator();
				while (iterator.hasNext()) {
					String s = iterator.next();
					NBTBase nbtbase = compound.getTag(s);
					if (nbtbase.getId() == Constants.NBT.TAG_COMPOUND) {
						translateNbtIntoMap((NBTTagCompound) nbtbase);
					}
				}
			}
		}
	}

	/**
	 * Returns true if the structure generator has generated a structure located at the given position tuple.
	 */
	public boolean hasStructureAt(int x, int y, int z) {
		loadOrCreateData(this.worldObj.get());
		return getStructureBBAt(x, y, z) != null;
	}
}

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

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import zeldaswordskills.lib.Config;

public class MapGenSecretRoomNether extends MapGenSecretRoom
{
	@Override
	public void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ) {
		this.worldObj = world;
		loadOrCreateData(worldObj);
		NBTTagList roomList = getStructureListFor(chunkX, chunkZ);
		int posX = chunkX << 4;
		int posZ = chunkZ << 4;

		for (int i = 0; i < Config.getNetherAttemptsPerChunk(); ++i) {
			if (rand.nextFloat() < Config.getNetherSecretRoomChance()) {
				int x = posX + rand.nextInt(16);
				int y = rand.nextInt((i % 4 == 1 ? 64 : 128)) - rand.nextInt(16);
				int z = posZ + rand.nextInt(16);
				RoomSecret room = new RoomSecret(chunkX, chunkZ, rand.nextInt(6) + 3, Blocks.netherrack);
				if (room.generate(this, world, rand, x, y, z)) {
					roomList.appendTag(room.writeToNBT());
					updateChunkStructureMap(roomList, chunkX, chunkZ);
				}
			}
		}

		if (roomList.tagCount() > 0) {
			//LogHelper.log(Level.INFO, "Nether roomList for chunk " + chunkX + "/" + chunkZ + " contains " + roomList.tagCount() + " elements");
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("roomList", roomList);
			addRoomTag(compound, chunkX, chunkZ);
		} else {
			//LogHelper.log(Level.INFO, "Nether roomList for chunk " + chunkX + "/" + chunkZ + " contains ZERO elements");
		}
	}

	@Override
	public String getTagName() {
		return "zssSecretNether";
	}
}

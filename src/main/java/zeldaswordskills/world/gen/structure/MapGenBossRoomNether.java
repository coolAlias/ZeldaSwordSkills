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

package zeldaswordskills.world.gen.structure;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import zeldaswordskills.ref.Config;
import zeldaswordskills.util.BossType;

public class MapGenBossRoomNether extends MapGenBossRoom
{
	@Override
	public void generate(IChunkProvider provider, World world, Random rand, int chunkX, int chunkZ) {
		this.worldObj = world;
		loadOrCreateData(worldObj);
		int size = rand.nextInt(6) + 8;
		int posX = (chunkX << 4) + rand.nextInt(16 - size);
		int posZ = (chunkZ << 4) + rand.nextInt(16 - size);
		BossType type = BossType.getBossType(world, posX, posZ);
		if (type != null) {
			RoomBoss room = new RoomBoss(type, chunkX, chunkZ, rand, size, Blocks.netherrack);
			if (rand.nextFloat() < 0.2F && !areStructuresWithinRange(room, Config.getNetherMinBossDistance())) {
				int posY = rand.nextInt(128) - rand.nextInt(16);
				if (posY > 16 && room.generate(this, world, rand, posX, posY, posZ)) {
					//LogHelper.log(Level.INFO, "Nether Boss room of type " + type.toString() + " successfully generated at " + room.getBoundingBox().toString());
					onStructureGenerated(world, room);
				} else {
					//LogHelper.log(Level.INFO, "Nether Boss room of type " + type.toString() + " failed to generate at " + room.getBoundingBox().toString());
				}
			}
		}
	}

	@Override
	public String getTagName() {
		return "zssBossNether";
	}
}

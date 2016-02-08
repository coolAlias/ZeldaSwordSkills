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

package zeldaswordskills.world.gen;

import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zeldaswordskills.ref.Config;
import zeldaswordskills.world.gen.feature.WorldGenBombFlowers;
import zeldaswordskills.world.gen.structure.MapGenSecretRoom;
import zeldaswordskills.world.gen.structure.MapGenSecretRoomNether;
import zeldaswordskills.world.gen.structure.MapGenSongPillar;

public class ZSSWorldGenEvent
{
	private MapGenSecretRoom secretRoomGen = new MapGenSecretRoom();
	private MapGenSecretRoomNether netherRoomGen = new MapGenSecretRoomNether();
	private MapGenSongPillar pillarGen = new MapGenSongPillar();
	private WorldGenBombFlowers bombGen = new WorldGenBombFlowers();

	// TERRAIN_GEN_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPopulateChunk(PopulateChunkEvent.Populate event) {
		// Fix for 1.8.0 not posting PopulateChunkEvent.Post for HELL worlds
		if (event.type == PopulateChunkEvent.Populate.EventType.NETHER_LAVA2 && event.world.provider.getDimensionId() == -1) {
			if (Config.getNetherAttemptsPerChunk() > 0 && Config.isGenEnabledAt(event.chunkX, event.chunkZ)) {
				netherRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			}
		}
	}

	// EVENT_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void postPopulate(PopulateChunkEvent.Post event) {
		if (!Config.isGenEnabledAt(event.chunkX, event.chunkZ)) {
			return;
		}
		switch(event.world.provider.getDimensionId()) {
		case -1: // the Nether
			if (Config.getNetherAttemptsPerChunk() > 0) {
				netherRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			}
			break;
		case 0: // the Overworld
			if (Config.getAttemptsPerChunk() > 0) {
				secretRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			}
			if (Config.doPillarGen()) {
				pillarGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			}
			if (Config.doBombFlowerGen()) {
				bombGen.generate(event.world, event.rand, event.chunkX, event.chunkZ);
			}
			break;
		default: break;
		}
	}
}

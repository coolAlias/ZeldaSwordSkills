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

package zeldaswordskills.world.gen;

import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import zeldaswordskills.world.gen.structure.MapGenBossRoom;
import zeldaswordskills.world.gen.structure.MapGenBossRoomNether;
import zeldaswordskills.world.gen.structure.MapGenSecretRoom;
import zeldaswordskills.world.gen.structure.MapGenSecretRoomNether;

public class ZSSWorldGenEvent
{
	private MapGenSecretRoom secretRoomGen = new MapGenSecretRoom();
	private MapGenSecretRoomNether netherRoomGen = new MapGenSecretRoomNether();
	private MapGenBossRoom bossRoomGen = new MapGenBossRoom();
	private MapGenBossRoomNether netherBossGen = new MapGenBossRoomNether();
	
	@ForgeSubscribe
	public void onPopulateChunk(PopulateChunkEvent.Populate event) {
		switch(event.world.provider.dimensionId) {
		case -1: // the Nether
			if (event.type == EventType.GLOWSTONE) {
				netherRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
				netherBossGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			}
			break;
		case 0: // the Overworld
			if (event.type == EventType.ICE) { 
				secretRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
				bossRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			} else if (event.type == EventType.LAKE && bossRoomGen.shouldDenyLakeAt(event.chunkX, event.chunkZ)) {
				event.setResult(Result.DENY);
			}
			break;
		default: break;
		}
	}
}

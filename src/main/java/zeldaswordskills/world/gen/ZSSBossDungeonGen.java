/**
    Copyright (C) <2016> <coolAlias>

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

import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import zeldaswordskills.ref.Config;
import zeldaswordskills.world.gen.structure.MapGenBossRoom;
import zeldaswordskills.world.gen.structure.MapGenBossRoomNether;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ZSSBossDungeonGen
{
	private final MapGenBossRoom bossRoomGen = new MapGenBossRoom();
	private final MapGenBossRoom netherBossGen = new MapGenBossRoomNether();

	// TERRAIN_GEN_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPopulateChunk(PopulateChunkEvent.Populate event) {
		if (event.world.provider.isSurfaceWorld() && event.type == EventType.LAKE && bossRoomGen.shouldDenyLakeAt(event.chunkX, event.chunkZ)) {
			event.setResult(Result.DENY);
		}
	}

	// TERRAIN_GEN_BUS event
	@SubscribeEvent
	public void onDecorate(Decorate event) {
		if (event.world.provider.isSurfaceWorld() && event.type == Decorate.EventType.LAKE && bossRoomGen.shouldDenyLakeAt(event.chunkX, event.chunkZ)) {
			event.setResult(Result.DENY);
		}
	}

	// EVENT_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void postPopulate(PopulateChunkEvent.Post event) {
		if (!Config.isGenEnabledAt(event.chunkX, event.chunkZ)) {
			return;
		}
		switch (event.world.provider.dimensionId) {
		case -1: // the Nether
			netherBossGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			break;
		case 0: // the Overworld
			bossRoomGen.generate(event.chunkProvider, event.world, event.rand, event.chunkX, event.chunkZ);
			break;
		}
	}
}

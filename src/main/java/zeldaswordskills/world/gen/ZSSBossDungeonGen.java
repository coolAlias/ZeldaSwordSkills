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

package zeldaswordskills.world.gen;

import net.minecraftforge.event.terraingen.DecorateBiomeEvent.Decorate;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType;
import net.minecraftforge.event.world.WorldEvent;
import zeldaswordskills.ref.Config;
import zeldaswordskills.world.gen.structure.MapGenBossRoom;
import zeldaswordskills.world.gen.structure.MapGenBossRoomNether;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ZSSBossDungeonGen
{
	private MapGenBossRoom bossRoomGen;
	private MapGenBossRoom netherBossGen;

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		switch (event.world.provider.dimensionId) {
		case -1: // the Nether
			this.netherBossGen = new MapGenBossRoomNether();
			break;
		case 0: // the Overworld
			this.bossRoomGen = new MapGenBossRoom();
			break;
		}
	}

	// TERRAIN_GEN_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void onPopulateChunk(PopulateChunkEvent.Populate event) {
		switch (event.world.provider.dimensionId) {
		case 0: // the Overworld
			boolean flag = (event.type == EventType.LAKE || event.type == EventType.LAVA);
			if (flag && bossRoomGen.shouldDenyLakeAt(event.chunkX, event.chunkZ)) {
				event.setResult(Result.DENY);
			}
			break;
		}
	}

	// TERRAIN_GEN_BUS event
	@SubscribeEvent
	public void onDecorate(Decorate event) {
		switch (event.world.provider.dimensionId) {
		case 0: // the Overworld
			boolean flag = (event.type == Decorate.EventType.LAKE);
			if (flag && bossRoomGen.shouldDenyLakeAt(event.chunkX, event.chunkZ)) {
				event.setResult(Result.DENY);
			}
			break;
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

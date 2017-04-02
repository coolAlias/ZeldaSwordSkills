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

import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import zeldaswordskills.ref.Config;
import zeldaswordskills.world.gen.feature.WorldGenBombFlowers;
import zeldaswordskills.world.gen.feature.WorldGenJars;
import zeldaswordskills.world.gen.structure.MapGenSecretRoom;
import zeldaswordskills.world.gen.structure.MapGenSecretRoomNether;
import zeldaswordskills.world.gen.structure.MapGenSongPillar;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ZSSWorldGenEvent
{
	private MapGenSecretRoom secretRoomGen;
	private MapGenSecretRoomNether netherRoomGen;
	private MapGenSongPillar pillarGen;
	private WorldGenJars jarGen = new WorldGenJars();
	private WorldGenBombFlowers bombGen = new WorldGenBombFlowers();

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		switch(event.world.provider.dimensionId) {
		case -1: // the Nether
			this.netherRoomGen = new MapGenSecretRoomNether();
			break;
		case 0: // the Overworld
			this.secretRoomGen = new MapGenSecretRoom();
			this.pillarGen = new MapGenSongPillar();
			break;
		default:
		}
	}

	// EVENT_BUS event
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void postPopulate(PopulateChunkEvent.Post event) {
		if (!Config.isGenEnabledAt(event.chunkX, event.chunkZ)) {
			return;
		}
		switch(event.world.provider.dimensionId) {
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

	// EVENT_BUS event
	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {
		// DecorateBiomeEvent's chunkX and chunkZ are actually block coordinates, not chunk coordinates
		if (!Config.isGenEnabledAt(event.chunkX >> 4, event.chunkZ >> 4)) {
			return;
		}
		try {
			if (event.world.provider.isHellWorld) {
				for (int n = 0; n < Config.getJarClustersPerChunkNether(); ++n) {
					if (event.rand.nextFloat() < Config.getJarGenChanceNether()) {
						jarGen.doJarGen(event.world, event.rand, event.chunkX, event.chunkZ, Config.getJarsPerClusterNether(), true);
					}
				}
			} else if (event.rand.nextFloat() < Config.getJarGenChance() && event.rand.nextInt(4) == 0) {
				jarGen.doJarGen(event.world, event.rand, event.chunkX, event.chunkZ, Config.getJarsPerCluster(), false);
			}
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (e.getMessage() != null && e.getMessage().equals("Already decorating!!") ||
					(cause != null && cause.getMessage() != null && cause.getMessage().equals("Already decorating!!")))
			{
				;
			} else {
				e.printStackTrace();
			}
		}
	}

	// EVENT_BUS event
	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Post event) {
		// DecorateBiomeEvent's chunkX and chunkZ are actually block coordinates, not chunk coordinates
		if (!Config.isGenEnabledAt(event.chunkX >> 4, event.chunkZ >> 4)) {
			return;
		}
		try {
			if (event.world.provider.isSurfaceWorld()) {
				for (int n = 0; n < Config.getJarClustersPerChunkSub(); ++n) {
					if (event.rand.nextFloat() < Config.getJarGenChanceSub()) {
						int i = event.chunkX + event.rand.nextInt(16) + 8;
						int j = event.rand.nextInt(48) + event.rand.nextInt(48);
						int k = event.chunkZ + event.rand.nextInt(16) + 8;
						if (j < 60) {
							jarGen.generate2(event.world, event.rand, i, j, k, Config.getJarsPerClusterSub(), true);
						}
					}
				}
			}
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (e.getMessage() != null && e.getMessage().equals("Already decorating!!") ||
					(cause != null && cause.getMessage() != null && cause.getMessage().equals("Already decorating!!")))
			{
				;
			} else {
				e.printStackTrace();
			}
		}
	}
}

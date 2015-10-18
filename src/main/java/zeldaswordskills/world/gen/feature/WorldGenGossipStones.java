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

package zeldaswordskills.world.gen.feature;

import java.util.Random;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.ref.Config;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 
 * Self-contained world generator requires its INSTANCE to be
 * registered to the MinecraftForge.EVENT_BUS
 *
 */
public class WorldGenGossipStones
{
	public static final WorldGenGossipStones INSTANCE = new WorldGenGossipStones();

	private WorldGenGossipStones() {}

	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (world.isSideSolid(x, y - 1, z, ForgeDirection.UP) && world.getBlock(x, y, z).getMaterial().isReplaceable() && world.canBlockSeeTheSky(x, y, z)) {
			world.setBlock(x, y, z, ZSSBlocks.gossipStone, 0, 2);
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityGossipStone) {
				((TileEntityGossipStone) te).setMessage("chat.zss.block.gossip_stone.hint." + rand.nextInt(12));
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onDecorate(PopulateChunkEvent.Post event) {
		if (!event.world.provider.isSurfaceWorld()) {
			return;
		}
		if (!Config.isGenEnabledAt(event.chunkX, event.chunkZ)) {
			return;
		}
		if (event.rand.nextFloat() < Config.getGossipStoneRate()) {
			int i = (event.chunkX << 4) + event.rand.nextInt(16);
			int k = (event.chunkZ << 4) + event.rand.nextInt(16);
			int j = event.world.getHeightValue(i, k);
			generate(event.world, event.rand, i, j, k);
		}
	}
}

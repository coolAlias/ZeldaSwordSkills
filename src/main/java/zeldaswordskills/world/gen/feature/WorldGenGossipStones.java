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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.lib.Config;

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
		if (world.isBlockSolidOnSide(x, y - 1, z, ForgeDirection.UP) && world.getBlockMaterial(x, y, z).isReplaceable() && world.canBlockSeeTheSky(x, y, z)) {
			world.setBlock(x, y, z, ZSSBlocks.gossipStone.blockID, 0, 2);
			TileEntity te = world.getBlockTileEntity(x, y, z);
			if (te instanceof TileEntityGossipStone) {
				String hint = StatCollector.translateToLocal("chat.zss.block.gossip_stone.hint." + rand.nextInt(12));
				((TileEntityGossipStone) te).setMessage(hint);
			}
		}
		return false;
	}

	@ForgeSubscribe
	public void onDecorate(PopulateChunkEvent.Post event) {
		if (!event.world.provider.isSurfaceWorld()) {
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

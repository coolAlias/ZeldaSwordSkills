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
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.ref.Config;

/**
 * 
 * Self-contained world generator; needs only to be registered to
 * the MinecraftForge.EVENT_BUS using its INSTANCE
 *
 */
public class WorldGenGossipStones extends WorldGenerator
{
	public static final WorldGenGossipStones INSTANCE = new WorldGenGossipStones();

	private WorldGenGossipStones() {
		super(false);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (world.isSideSolid(pos.down(), EnumFacing.UP) && world.getBlockState(pos).getBlock().isReplaceable(world, pos) && world.canBlockSeeSky(pos)) {
			world.setBlockState(pos, ZSSBlocks.gossipStone.getDefaultState(), 2);
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityGossipStone) {
				String hint = StatCollector.translateToLocal("chat.block.gossip_stone.hint." + rand.nextInt(12));
				((TileEntityGossipStone) te).setMessage(hint);
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onPostPopulateChunk(PopulateChunkEvent.Post event) {
		if (!event.world.provider.isSurfaceWorld()) {
			return;
		}
		if (event.rand.nextFloat() < Config.getGossipStoneRate()) {
			int i = (event.chunkX << 4) + event.rand.nextInt(16);
			int k = (event.chunkZ << 4) + event.rand.nextInt(16);
			int j = event.world.getHeight(new BlockPos(i, 64, k)).getY();
			generate(event.world, event.rand, new BlockPos(i, j, k));
		}
	}
}

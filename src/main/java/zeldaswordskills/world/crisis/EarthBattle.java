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

package zeldaswordskills.world.crisis;

import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityDungeonCore;
import zeldaswordskills.entity.projectile.EntityBomb;
import zeldaswordskills.lib.Sounds;

public class EarthBattle extends BossBattle {

	public EarthBattle(TileEntityDungeonCore core) {
		super(core);
	}

	@Override
	public void beginCrisis(World world) {
		super.beginCrisis(world);
		if (difficulty > 1) {
			scheduleUpdateTick(200 + world.rand.nextInt(200));
		}
	}

	@Override
	protected void onUpdateTick(World world) {
		int nextUpdate = 10;
		int x = box.minX + world.rand.nextInt(box.getXSize() - 1) + 1;
		int y = box.maxY - 1;
		int z = box.minZ + world.rand.nextInt(box.getZSize() - 1) + 1;
		if (Math.abs(box.getCenterX() - x) > 1 && Math.abs(box.getCenterZ() - z) > 1) {
			EntityBomb bomb = new EntityBomb(world).setDestructionFactor(0.7F).addTime((3 - difficulty) * 16);
			bomb.setPosition(x, y, z);
			if (world.isAirBlock(x, box.minY, z)) {
				bomb.setNoGrief();
			}
			if (world.getCollidingBoundingBoxes(bomb, bomb.boundingBox).isEmpty()) {
				if (!world.isRemote) {
					world.playSoundEffect(x, y, z, Sounds.BOMB_WHISTLE, 1.0F, 1.0F);
					world.spawnEntityInWorld(bomb);
					nextUpdate = ((3 - difficulty) * 100) + 50 + world.rand.nextInt(400);
				}
			}
		}
		scheduleUpdateTick(nextUpdate);
	}
}

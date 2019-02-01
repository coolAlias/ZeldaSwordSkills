/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.world;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

/**
 * 
 * A Teleporter that can be used without creating a portal.
 * 
 * Be sure to set the y position afterward, either manually or using {@link #adjustPosY}
 * 
 * NOTE: Does NOT work when teleporting FROM the End (to is okay).
 *
 */
public class TeleporterNoPortal extends Teleporter
{
	/** Teleporter's WorldServer instance is private */
	protected final WorldServer worldServer;

	/**
	 * Sends the entity to the same coordinates as its current position in the new dimension, if possible.
	 * Be sure to set the y position afterward, either manually or using {@link #adjustPosY}
	 * NOTE: Does NOT work when teleporting FROM the End (to is okay)
	 */
	public TeleporterNoPortal(WorldServer worldServer) {
		super(worldServer);
		this.worldServer = worldServer;
	}

	/**
	 * Call this method after transferring dimensions to adjust the entity.posY appropriately
	 */
	public static void adjustPosY(Entity entity) {
		int x = MathHelper.floor_double(entity.posX);
		int z = MathHelper.floor_double(entity.posZ);
		BlockPos pos = entity.worldObj.getHeight(new BlockPos(x, 64, z));
		switch(entity.worldObj.provider.getDimensionId()) {
		case -1:
			pos = pos.down(10);
			boolean flag = true;
			while (pos.getY() > 30 && flag) {
				if (entity.worldObj.getBlockState(pos).getBlock().getMaterial().blocksMovement() && entity.worldObj.isAirBlock(pos.up(1)) && entity.worldObj.isAirBlock(pos.up(2))) {
					flag = false;
				} else {
					pos = pos.down(1);
				}
			}
			break;
		default:
		}
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).setPositionAndUpdate((double) x + 0.5D, pos.up(1).getY(), (double) z + 0.5D);
		} else {
			entity.setPosition((double) x + 0.5D, pos.up(1).getY(), (double) z + 0.5D);
		}
	}

	@Override
	public void placeInPortal(Entity entity, float yaw) {
		int x = MathHelper.floor_double(entity.posX);
		int y = MathHelper.floor_double(entity.posY) - 1;
		int z = MathHelper.floor_double(entity.posZ);
		if (entity instanceof EntityPlayer) {
			((EntityPlayer) entity).setPositionAndUpdate(x, y, z);
		} else {
			entity.setPosition(x, y, z);
		}
	}

	@Override
	public boolean makePortal(Entity entity) {
		return true;
	}
}

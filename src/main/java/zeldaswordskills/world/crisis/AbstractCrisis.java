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

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import zeldaswordskills.util.LogHelper;

/**
 * 
 * Abstract crisis class provides basic framework for an updating and self-terminating event.
 * 
 * After a crisis is constructed, {@link #beginCrisis(World)} must be called to initiate the
 * crisis, after which point it will run its course automatically so long as {@link #onUpdate(World)}
 * is called every tick until {@link #isFinished()} returns true.
 *
 */
public abstract class AbstractCrisis
{
	/** Event timer; when it reaches zero, the crisis is finished */
	protected int eventTimer = 0;
	/** Crisis update tick will be called when eventTimer reaches this value */
	protected int nextTick = 0;

	/**
	 * Returns true when the crisis has completed
	 */
	public final boolean isFinished() {
		return eventTimer < 0;
	}

	/**
	 * Schedules an update n ticks in the future
	 */
	protected final void scheduleUpdateTick(int n) {
		if (n < 0) {
			LogHelper.warning("Oops! Scheduling a crisis tick with n less than zero.");
		}
		nextTick = (n > 0 ? eventTimer - n : 0);
	}

	/**
	 * Call every tick while battle is in progress
	 */
	public final void onUpdate(World world) {
		if (eventTimer > 0) {
			--eventTimer;
			if (eventTimer % 20 == 0 && canCrisisConclude(world)) {
				//LogHelper.log(Level.INFO, "Crisis can conclude before time is up! Currently on " + (world.isRemote ? "client" : "server"));
				eventTimer = 0;
			} else if (eventTimer < 40) {
				eventTimer += 40;
			}
			if (eventTimer == 0) {
				//LogHelper.log(Level.INFO, "Crisis timed out: ending crisis on " + (world.isRemote ? "client" : "server"));
				endCrisis(world);
				eventTimer = -1;
			} else if (eventTimer == nextTick) {
				onUpdateTick(world);
			}
		} else {
			LogHelper.warning("Unexpected timer value: crisis terminated but not handled");
			eventTimer = -1;
		}
	}

	/**
	 * Allows sub-classes to define behavior during update ticks, but only if
	 * an update tick is scheduled using nextTick. Note that eventTimer counts
	 * down, and an update tick will be called when eventTimer equals nextTick.
	 */
	protected abstract void onUpdateTick(World world);

	/**
	 * This method should always be called after a crisis is first constructed (not
	 * when loaded from NBT), allowing crisis to set up before it begins updating.
	 * If an update tick needs to be scheduled, the first one should be scheduled here.
	 */
	public abstract void beginCrisis(World world);

	/**
	 * Handles everything that happens at the end of a crisis, such as spawning xp,
	 * playing victory music, etc.
	 */
	protected abstract void endCrisis(World world);

	/**
	 * Called every 20 ticks to check if the crisis should conclude before the eventTimer expires;
	 * for example, if the crisis is about defeating enemies and they are all dead, it should conclude
	 */
	protected abstract boolean canCrisisConclude(World world);

	/**
	 * Writes the crisis data to NBT
	 */
	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("eventTimer", eventTimer);
		compound.setInteger("nextTick", nextTick);
	}

	/**
	 * Reads the crisis data from NBT
	 */
	public void readFromNBT(NBTTagCompound compound) {
		eventTimer = compound.getInteger("eventTimer");
		nextTick = compound.getInteger("nextTick");
	}
}

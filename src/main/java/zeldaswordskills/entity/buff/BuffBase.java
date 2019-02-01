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

package zeldaswordskills.entity.buff;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.UpdateBuffPacket;

/**
 * 
 * An actual instance of a Buff, with duration and amplitude
 *
 */
public class BuffBase
{
	private final Buff buff;
	private int duration;
	private int amplifier;

	/**
	 * @param buff		The type of buff
	 * @param duration	Number of ticks; a duration equal to Integer.MAX_VALUE is 'permanent'
	 * @param amplifier	How powerful the effect is: see individual {@link Buff buffs} for valid values
	 */
	public BuffBase(Buff buff, int duration, int amplifier) {
		this.buff = buff;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public BuffBase(BuffBase b) {
		this.buff = b.buff;
		this.duration = b.duration;
		this.amplifier = b.amplifier;
	}

	/** The type of Buff that this effect is */
	public Buff getBuff() { return buff; }
	/** The duration remaining for this buff's effect, in ticks */
	public int getDuration() { return duration; }
	/** Returns the amplitude of this buff's effect */
	public int getAmplifier() { return amplifier; }
	/** Shortcut to get Buff's icon index */
	public int getIconIndex() { return buff.iconIndex; }
	/** Shortcut to Buff method; true if this buff is a negative effect */
	public boolean isDebuff() { return buff.isDebuff; }
	/** Shortcut to Buff method; true if this buff displays an arrow icon overlay */
	public boolean displayArrow() { return buff.displayArrow; }
	/** Whether this buff is applied permanently, used for mobs with permanent resistances */
	public boolean isPermanent() { return duration == Integer.MAX_VALUE; }

	@Override
	public int hashCode() {
		return 31 * (31 + buff.ordinal()) + amplifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		BuffBase base = (BuffBase) obj;
		return this.buff == base.buff && this.duration == base.duration && this.amplifier == base.amplifier;
	}

	@Override
	public String toString() {
		return buff.getName(); // TODO add more information
	}

	/**
	 * Combines the new buff's duration and amplifier with the old buff
	 */
	public void combine(BuffBase newBuff) {
		if (newBuff.buff != this.buff) {
			ZSSMain.logger.warn("Combining two buffs of different types should be impossible!");
		} else if (this.isPermanent()) {
			// can't combine permanent effects
		} else if (newBuff.amplifier > this.amplifier) {
			this.amplifier = newBuff.amplifier;
			this.duration = newBuff.duration;
		} else if (newBuff.amplifier == this.amplifier && newBuff.duration > this.duration) {
			this.duration = newBuff.duration;
		}
	}

	/**
	 * Adds any effects the buff may have to the entity when first applied 
	 */
	public void onAdded(EntityLivingBase entity) {
		if (!entity.worldObj.isRemote) {
			buff.onAdded(entity, amplifier);
			if (entity instanceof EntityPlayerMP) {
				PacketDispatcher.sendTo(new UpdateBuffPacket(this, false), (EntityPlayerMP) entity);
			} else if (buff.syncNonPlayerEntity && entity.worldObj instanceof WorldServer) {
				PacketDispatcher.sendToPlayers(new UpdateBuffPacket(this, entity, false), ((WorldServer) entity.worldObj).getEntityTracker().getTrackingPlayers(entity));
			}
		}
	}

	/**
	 * Removes any effects that may have been applied when the buff is removed
	 */
	public void onRemoved(EntityLivingBase entity) {
		onRemoved(entity, true);
	}

	/**
	 * Removes any effects that may have been applied when the buff is removed,
	 * updating the client player if needsUpdate is true
	 */
	public void onRemoved(EntityLivingBase entity, boolean needsUpdate) {
		if (!entity.worldObj.isRemote) {
			buff.onRemoved(entity, amplifier);
			if (needsUpdate) {
				if (entity instanceof EntityPlayerMP) {
					PacketDispatcher.sendTo(new UpdateBuffPacket(this, true), (EntityPlayerMP) entity);
				} else if (buff.syncNonPlayerEntity && entity.worldObj instanceof WorldServer) {
					PacketDispatcher.sendToPlayers(new UpdateBuffPacket(this, entity, true), ((WorldServer) entity.worldObj).getEntityTracker().getTrackingPlayers(entity));
				}
			}
		}
	}

	/**
	 * Called when a buff has changed, removing the old effects and applying the new
	 */
	public void onChanged(EntityLivingBase entity) {
		if (!entity.worldObj.isRemote) {
			onRemoved(entity, false);
			onAdded(entity);
		}
	}

	/**
	 * Updates this buff's duration and effects, if any
	 * @return true if this buff needs to be removed (duration remaining is zero)
	 */
	public boolean onUpdate(EntityLivingBase entity) {
		buff.onUpdate(entity, duration, amplifier);
		if (duration > 0 && !isPermanent()) {
			--duration;
		}
		return duration == 0;
	}

	/**
	 * Reads and returns a new BuffBase from the tag compound
	 */
	public static BuffBase readFromNBT(NBTTagCompound compound) {
		Buff b = Buff.values()[compound.getByte("buffId")];
		byte amp = compound.getByte("amplifier");
		int dur = compound.getInteger("duration");
		return new BuffBase(b, dur, (int) amp);
	}

	/**
	 * Writes this buff effect into the new tag compound, returning it for ease of use
	 */
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setByte("buffId", (byte) buff.ordinal());
		compound.setByte("amplifier", (byte) amplifier);
		compound.setInteger("duration", duration);
		return compound;
	}
}

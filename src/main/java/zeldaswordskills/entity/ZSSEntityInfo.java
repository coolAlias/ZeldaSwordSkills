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

package zeldaswordskills.entity;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.buff.BuffBase;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncEntityInfoPacket;
import zeldaswordskills.ref.Config;

/**
 * 
 * For classes that extend EntityLivingBase
 *
 */
public class ZSSEntityInfo implements IExtendedEntityProperties
{
	private static final String EXT_PROP_NAME = "ZSSEntityInfo";

	/** The entity to which these properties belong */
	private final EntityLivingBase entity;

	/** Map of active buffs */
	private Map<Buff, BuffBase> activeBuffs = new EnumMap<Buff, BuffBase>(Buff.class);

	/** Time this entity will remain immune to further stun effects */
	private int stunResistTime;

	public ZSSEntityInfo(EntityLivingBase entity) {
		this.entity = entity;
	}

	@Override
	public void init(Entity entity, World world) {}

	/** Whether a Buff is currently active */
	public boolean isBuffActive(Buff buff) {
		return activeBuffs.containsKey(buff);
	}

	/** Returns a currently active buff or null if that buff isn't active */
	public BuffBase getActiveBuff(Buff buff) {
		return activeBuffs.get(buff);
	}

	/** Returns the amplifier of the Buff, or 0 if not active */
	public int getBuffAmplifier(Buff buff) {
		return (isBuffActive(buff) ? getActiveBuff(buff).getAmplifier() : 0);
	}

	/** Returns true if a buff is both active and permanent */
	public boolean isBuffPermanent(Buff buff) {
		return isBuffActive(buff) && getActiveBuff(buff).isPermanent();
	}

	/** Returns active buffs map */
	public Map<Buff, BuffBase> getActiveBuffsMap() {
		return activeBuffs;
	}

	/**
	 * Shortcut method for applying a new buff
	 * @param buff		The type of buff
	 * @param duration	Number of ticks; a duration equal to Integer.MAX_VALUE is 'permanent'
	 * @param amplifier	How powerful the effect is: see individual {@link Buff buffs} for valid values
	 */
	public void applyBuff(Buff buff, int duration, int amplifier) {
		applyBuff(new BuffBase(buff, duration, amplifier));
	}

	/**
	 * Applies a new Buff to the active buffs map
	 */
	public void applyBuff(BuffBase newBuff) {
		if (isBuffActive(newBuff.getBuff())) {
			getActiveBuff(newBuff.getBuff()).combine(newBuff);
			getActiveBuff(newBuff.getBuff()).onChanged(this.entity);
		} else {
			activeBuffs.put(newBuff.getBuff(), newBuff);
			newBuff.onAdded(this.entity);
		}
	}

	/**
	 * Removes all buffs from this entity
	 */
	public void removeAllBuffs() {
		removeAllBuffs(true, false);
	}

	/**
	 * Removes all temporary buffs from this entity
	 * @param removeAll		If true, permanent buffs will also be removed
	 * @param sendUpdate	True will send a packet for each buff removed
	 */
	public void removeAllBuffs(boolean removeAll, boolean sendUpdate) {
		if (!entity.worldObj.isRemote) {
			Iterator<Buff> iterator = activeBuffs.keySet().iterator();
			while (iterator.hasNext()) {
				Buff buff = iterator.next();
				if (removeAll || !activeBuffs.get(buff).isPermanent()) {
					activeBuffs.get(buff).onRemoved(entity, sendUpdate);
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Removes a buff from the entity
	 */
	public void removeBuff(Buff buff) {
		BuffBase buffBase = activeBuffs.remove(buff);
		if (buffBase != null) {
			buffBase.onRemoved(this.entity);
		}
	}

	/**
	 * Updates all active buffs, removing any whose duration reaches zero
	 */
	protected void updateBuffs() {
		for (BuffBase buff : activeBuffs.values()) {
			if (buff.onUpdate(entity)) {
				removeBuff(buff.getBuff());
			}
		}
	}

	/** Stuns this entity for the time given (not additive with previous stuns) */
	public void stun(int time) {
		stun(time, false);
	}

	/**
	 * Stuns this entity for the time given with optional override for players
	 * @param canStunPlayer whether the stun time should ignore config settings for players
	 */
	public void stun(int time, boolean alwaysStuns) {
		int stunTime = (stunResistTime > 0 || (!alwaysStuns && isImmuneToStun()) ? 0 : time);
		if (stunTime > 0) {
			stunTime *= 1.0F + (getBuffAmplifier(Buff.RESIST_STUN) * 0.01F);
			stunTime *= 1.0F - (getBuffAmplifier(Buff.RESIST_STUN) * 0.01F);
			if (stunTime > 0) {
				stunResistTime = 40;
				applyBuff(new BuffBase(Buff.STUN, stunTime, 0));
			}
		}
	}

	/**
	 * Returns true if this property's entity is immune to stun effects
	 */
	public boolean isImmuneToStun() {
		// TODO make a public list that other mods can add entities to
		return ((entity instanceof EntityPlayer && !Config.canPlayersBeStunned()) || entity instanceof IBossDisplayData);
	}

	/**
	 * This method should be called every update tick; currently called from LivingUpdateEvent
	 */
	public void onUpdate() {
		updateBuffs();
		// Use a number higher than 100 otherwise it is nearly instantaneous even at low resists
		if (entity.isBurning() && entity.worldObj.rand.nextInt(500) < getBuffAmplifier(Buff.RESIST_FIRE)) {
			entity.extinguish();
		}
		if (stunResistTime > 0 && !isBuffActive(Buff.STUN)) {
			--stunResistTime;
		}
	}

	/**
	 * Used to register these extended properties for the entity during EntityConstructing event
	 */
	public static final void register(EntityLivingBase entity) {
		entity.registerExtendedProperties(EXT_PROP_NAME, new ZSSEntityInfo(entity));
	}

	/**
	 * Returns ExtendedPlayer properties for entity
	 */
	public static final ZSSEntityInfo get(EntityLivingBase entity) {
		return (ZSSEntityInfo) entity.getExtendedProperties(EXT_PROP_NAME);
	}

	/**
	 * Call each time the player joins the world to sync data to the client
	 */
	public void onJoinWorld() {
		if (entity instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncEntityInfoPacket(this), (EntityPlayerMP) entity);
		}
	}

	/**
	 * Copies given data to this one when a player is cloned
	 * If the client also needs the data, the packet must be sent from
	 * EntityJoinWorldEvent to ensure it is sent to the new client player
	 */
	public void copy(ZSSEntityInfo info) {
		NBTTagCompound compound = new NBTTagCompound();
		info.saveNBTData(compound);
		this.loadNBTData(compound);
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		if (!activeBuffs.isEmpty()) {
			NBTTagList list = new NBTTagList();
			for (BuffBase buff : activeBuffs.values()) {
				list.appendTag(buff.writeToNBT(new NBTTagCompound()));
			}
			compound.setTag("ActiveBuffs", list);
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		if (compound.hasKey("ActiveBuffs")) {
			NBTTagList list = compound.getTagList("ActiveBuffs", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				BuffBase buff = BuffBase.readFromNBT(tag);
				activeBuffs.put(buff.getBuff(), buff);
			}
		}
	}
}

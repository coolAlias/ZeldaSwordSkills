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

package zeldaswordskills.skills;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.client.UpdateComboPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Each instance of this class is a self-synchronizing and mostly self-contained module containing
 * all the data necessary to keep track of a player's current attack combo.
 * 
 * When a combo ends, the player will gain the specified type of experience in an damage equal
 * to the combo size minus one, plus an additional damage proportional to the total damage dealt.
 * 
 * Specifications:
 * A new instance should be used for each new attack combo.
 * Each instance should be updated every tick from within its containing class' update method 
 * Determining when to add damage or end the combo prematurely must be handled extraneously.
 * Only self-synchronizing when UpdateComboPacket class is kept up-to-date 
 *
 */
public class Combo
{
	/** Used only to get correct Skill class from player during update */
	private final byte skillId;

	/** Max combo size attainable by this instance of Combo */
	private final int maxComboSize;

	/** Upon landing a blow, the combo timer is set to this damage */
	private final int timeLimit;

	/** Current combo timer; combo ends when timer reaches zero. */
	private int comboTimer = 0;

	/** Set to true when endCombo method is called */
	private boolean isFinished = false;

	/** List stores each hit's damage; combo size is inherent in the list */
	private final List<Float> damageList = new ArrayList<Float>();

	/** Running total of damage inflicted during a combo */
	private float comboDamage = 0.0F;

	/** Internal flag for correcting split damage such as IArmorBreak */
	private boolean addToLast = false;

	/** Internal value for last damage */
	private float lastDamage = 0.0F;

	/** Last entity hit; used to check if consecutive hit counter should increase or reset */
	private Entity lastEntityHit = null;

	/** EntityID of last entity hit, used client side to get the entity */
	private int entityId;

	/** Total number of consecutive hits on the same target entity */
	private int consecutiveHits = 0;

	/**
	 * Constructs a new Combo with specified max combo size and time limit and sends an update
	 * packet to the player with the new Combo instance.
	 * @param maxComboSize size at which the combo self-terminates
	 * @param timeLimit damage of time allowed between strikes before combo self-terminates
	 * @param xpType the Attribute that receives xp gained from combo
	 */
	public Combo(EntityPlayer player, SkillBase skill, int maxComboSize, int timeLimit) {
		this.skillId = skill.getId();
		this.maxComboSize = maxComboSize;
		this.timeLimit = timeLimit;
		if (!player.worldObj.isRemote) {
			PacketDispatcher.sendTo(new UpdateComboPacket(this), (EntityPlayerMP) player);
		}
	}

	/**
	 * Constructs a new Combo with specified max combo size and time limit
	 * @param maxComboSize size at which the combo self-terminates
	 * @param timeLimit damage of time allowed between strikes before combo self-terminates
	 * @param xpType the Attribute that receives xp gained from combo
	 */
	private Combo(byte skillId, int maxComboSize, int timeLimit) {
		this.skillId = skillId;
		this.maxComboSize = maxComboSize;
		this.timeLimit = timeLimit;
	}

	/** Returns the skill id associated with this Combo */
	public byte getSkill() { return skillId; }

	/** Returns current combo size */
	public int getSize() { return damageList.size(); }

	/** Returns current combo's maximum size */
	public int getMaxSize() { return maxComboSize; }

	/** Returns current damage total for this combo */
	public float getDamage() { return comboDamage; }

	/** Returns a copy of the current damage list */
	public List<Float> getDamageList() { return new ArrayList(damageList); }

	/** Returns the last entity directly hit during the combo */
	public Entity getLastEntityHit() { return lastEntityHit; }

	/** Returns the number of consecutive hits on the same target entity */
	public int getConsecutiveHits() { return consecutiveHits; }

	/** Returns true if this combo is finished, i.e. no longer active */
	public boolean isFinished() { return isFinished; }

	/** Returns translated current description of combo; e.g. "Great" */
	public String getLabel() {
		return StatCollector.translateToLocal("combo.label." + getSize());
	}

	/**
	 * Updates combo timer and triggers combo ending if timer reaches zero
	 */
	public void onUpdate(EntityPlayer player) {
		if (comboTimer > 0) {
			--comboTimer;
			if (comboTimer == 0) {
				endCombo(player);
			}
		}
	}

	/**
	 * Increases the combo size by one and adds the damage to the running total, as well as
	 * ending the combo if the max size is reached. This is only called server side.
	 * @param target used to track consecutive hits on a single target
	 */
	public void add(EntityPlayer player, Entity target, float damage) {
		if (getSize() < maxComboSize && (comboTimer > 0 || getSize() == 0)) {
			if (target != null && target == lastEntityHit) {
				++consecutiveHits;
			} else {
				lastEntityHit = target;
				consecutiveHits = (target != null ? 1 : 0);
			}
			if (addToLast) {
				damageList.add(damage + lastDamage);
				addToLast = false;
				lastDamage = 0.0F;
			} else {
				damageList.add(damage);
			}
			switch(damageList.size()) {
			case 3: player.triggerAchievement(ZSSAchievements.comboBasic); break;
			case 8: player.triggerAchievement(ZSSAchievements.comboPerfect); break;
			case 12: player.triggerAchievement(ZSSAchievements.comboLegend); break;
			}
			comboDamage += damage;
			if (!player.worldObj.isRemote) {
				PacketDispatcher.sendTo(new UpdateComboPacket(this), (EntityPlayerMP) player);
			}
			if (getSize() == maxComboSize) {
				endCombo(player);
			} else {
				comboTimer = timeLimit;
			}
		} else {
			endCombo(player);
		}
	}

	/**
	 * Adds damage damage to combo's total, without incrementing the combo size.
	 * @param flag whether the damage should be added to the previous strike's total, for IArmorBreak
	 */
	public void addDamageOnly(EntityPlayer player, float damage, boolean flag) {
		if (!isFinished()) {
			comboDamage += damage;
			addToLast = flag;
			if (addToLast) {
				lastDamage = damage;
			}
			if (getSize() == 0) {
				comboTimer = timeLimit;
			}
			if (!player.worldObj.isRemote) {
				PacketDispatcher.sendTo(new UpdateComboPacket(this), (EntityPlayerMP) player);
			}
		}
	}

	/**
	 * Ends a combo and grants Xp
	 */
	public void endCombo(EntityPlayer player) {
		if (!isFinished) {
			isFinished = true;
			lastEntityHit = null;
			consecutiveHits = 0;
			if (!player.worldObj.isRemote) {
				PacketDispatcher.sendTo(new UpdateComboPacket(this), (EntityPlayerMP) player);
			}
		}
	}

	/**
	 * Attempts to set the last entity hit after loading from NBT; use from update packet
	 */
	@SideOnly(Side.CLIENT)
	public void getEntityFromWorld(World world) {
		lastEntityHit = world.getEntityByID(entityId);
	}

	/**
	 * Writes this combo to NBT and returns the tag compound
	 */
	public final NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("SkillID", skillId);
		compound.setInteger("MaxSize", maxComboSize);
		compound.setInteger("TimeLimit", timeLimit);
		compound.setInteger("CurrentSize", getSize());
		for (int i = 0; i < getSize(); ++i) {
			compound.setFloat("Dmg" + i, damageList.get(i));
		}
		compound.setFloat("TotalDamage", comboDamage);
		compound.setInteger("EntityId", (lastEntityHit != null ? lastEntityHit.getEntityId() : 0));
		compound.setInteger("ConsecutiveHits", consecutiveHits);
		compound.setBoolean("Finished", isFinished);
		return compound;
	}

	/**
	 * Creates a new combo from the nbt tag data
	 */
	public static final Combo readFromNBT(NBTTagCompound compound) {
		Combo combo = new Combo(compound.getByte("SkillID"), compound.getInteger("MaxSize"), compound.getInteger("TimeLimit"));
		int size = compound.getInteger("CurrentSize");
		for (int i = 0; i < size; ++i) {
			combo.damageList.add(compound.getFloat("Dmg" + i));
		}
		combo.comboDamage = compound.getFloat("TotalDamage");
		combo.entityId = compound.getInteger("EntityId");
		combo.consecutiveHits = compound.getInteger("ConsecutiveHits");
		combo.isFinished = compound.getBoolean("Finished");
		return combo;
	}
}

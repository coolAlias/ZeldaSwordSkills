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

package zeldaswordskills.entity.buff;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StatCollector;

/**
 * 
 * Enumeration of all possible buffs, with various helper methods to handle their effects
 * 
 * Note that damage resistances are cumulative, so if a DamageSource has multiple
 * damage descriptors, it's possible for the amount to be reduced several times.
 * 
 * For example, if the damage was both fire and magic, an entity with resistances
 * to both would reduce the damage by both resistance amounts.
 *
 */
public enum Buff
{
	/** Multiplies Attack damage by a factor of (1.0F + (amplifier * 0.01F)) */
	ATTACK_UP("buff.zss.attack_up", false, 8),
	/** Multiplies Attack damage by a factor of (1.0F - (amplifier * 0.01F)) */
	ATTACK_DOWN("buff.zss.attack_down", true, 8),
	/** Defense bonus: All damage received multiplied by (1.0F - (amplifier * 0.01F)) */
	DEFENSE_UP("buff.zss.defense_up", false, 10),
	/** Defense penalty: All damage received multiplied by (1.0F + (amplifier * 0.01F)) */
	DEFENSE_DOWN("buff.zss.defense_down", true, 10),
	/** Increases chance to evade attacks by (amplifier * 0.01F) */
	EVADE_UP("buff.zss.evade_up", false, 9),
	/** Reduces chance to evade attacks by (amplifier * 0.01F) */
	EVADE_DOWN("buff.zss.evade_down", true, 9),
	/** Multiplies COLD damage by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_COLD("buff.zss.resist_cold", false, 2),
	/** Multiplies any type of fire damage by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_FIRE("buff.zss.resist_fire", false, 3),
	/** Multiplies any type of HOLY damage by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_HOLY("buff.zss.resist_holy", false, 7),
	/** Multiplies any type of magic damage by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_MAGIC("buff.zss.resist_magic", false, 4),
	/** Multiplies SHOCK damage by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_SHOCK("buff.zss.resist_shock", false, 5),
	/** Multiplies stun time by a factor of (1.0F - (amplifier * 0.01F)) */
	RESIST_STUN("buff.zss.resist_stun", false, 6),
	/** Prevents affected entity from acting for the duration */
	STUN("buff.zss.stun", true, false, 6),
	/** Increases the amount of damage received by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_COLD("buff.zss.weakness_cold", true, 2),
	/** Increases the amount of damage received by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_FIRE("buff.zss.weakness_fire", true, 3),
	/** Increases the amount of damage received by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_HOLY("buff.zss.weakness_holy", true, 7),
	/** Increases the amount of damage received by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_MAGIC("buff.zss.weakness_magic", true, 4),
	/** Increases the amount of damage received by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_SHOCK("buff.zss.weakness_shock", true, 5),
	/** Increases stun time by a factor of (1.0F + (amplifier * 0.01F)) */
	WEAKNESS_STUN("buff.zss.weakness_stun", true, 6);

	public final String unlocalizedName;
	/** Whether this Buff is a negative effect */
	public final boolean isDebuff;
	/** Whether this Buff displays an arrow icon overlay */
	public final boolean displayArrow;
	/** Icon's texture sheet index; 0 and 1 are the buff and debuff icons */
	public final int iconIndex;

	private Buff(String name, boolean isDebuff, int index) {
		this(name, isDebuff, true, index);
	}

	private Buff(String name, boolean isDebuff, boolean displayArrow, int index) {
		this.unlocalizedName = name;
		this.isDebuff = isDebuff;
		this.displayArrow = displayArrow;
		this.iconIndex = index;
	}

	/** Returns this buff's localized name */
	public String getName() {
		return StatCollector.translateToLocal(unlocalizedName + ".name");
	}

	/**
	 * Adds any effects the buff may have to the entity when first applied 
	 */
	public void onAdded(EntityLivingBase entity, int amplifier) {
		switch(this) {
		case STUN:
			if (entity instanceof EntityPlayer) {
				((EntityPlayer) entity).clearItemInUse();
			}
			break;
		default:
		}
	}

	/**
	 * Removes any effects that may have been applied when the buff is removed
	 */
	public void onRemoved(EntityLivingBase entity, int amplifier) {}

	/**
	 * Updates this buff's effects, if any
	 */
	public void onUpdate(EntityLivingBase entity, int remainingDuration, int amplifier) {
		switch(this) {
		case STUN:
			entity.posX = entity.prevPosX - entity.motionX;
			entity.posY = entity.prevPosY - entity.motionY;
			entity.posZ = entity.prevPosZ - entity.motionZ;
			entity.motionX = entity.motionY = entity.motionZ = 0.0D;
			entity.attackTime = 20;
			if (entity instanceof EntityCreature) {
				((EntityCreature) entity).setTarget(null);
			}
			break;
		default:
		}
	}
}

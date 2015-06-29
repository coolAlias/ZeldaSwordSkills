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

package zeldaswordskills.api.damage;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;

/**
 * 
 * Provides additional Damage Types for DamageSource beyond those in vanilla
 * 
 * Note that damage resistances (and weaknesses) are cumulative, so if a DamageSource
 * has multiple damage descriptors, it's possible for the amount to be reduced or
 * augmented once for each type.
 * 
 * For example, if the damage was both fire and magic, an entity with resistances
 * to both would reduce the damage by both resistance amounts.
 *
 */
public enum EnumDamageType {
	/** Cold damage inflicts a slow effect on the target, modified by total damage */
	COLD,
	/** Fire damage has no special effect beyond what vanilla does, but is useful for flagging AoE damage */
	FIRE,
	/** Holy damage is especially potent against undead creatures */
	HOLY,
	/** Shock damage may be resisted with the RESIST_SHOCK buff */
	SHOCK,
	/** Stun damage temporarily stuns affected entities */
	STUN;
	
	/**
	 * Handles secondary effects of this damage type upon damaging a living entity
	 */
	public void handleSecondaryEffects(IPostDamageEffect source, EntityLivingBase entity, float damage) {
		switch(this) {
		case COLD:
			entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, (int)(damage * source.getDuration(this)), source.getAmplifier(this)));
			break;
		case STUN:
			if (source instanceof IDamageSourceStun) {
				IDamageSourceStun stunSource = (IDamageSourceStun) source;
				int stunTime = Math.max(source.getDuration(this), 2);
				int modifier = Math.max(source.getAmplifier(this), 1);
				stunTime += entity.worldObj.rand.nextInt((int)(Math.max(damage, 1.0F) * modifier)) - entity.worldObj.rand.nextInt(stunTime / 2);
				if (!(entity instanceof EntityPlayer) || stunSource.canStunPlayers()) {
					ZSSEntityInfo.get(entity).stun(stunTime, stunSource.alwaysStuns());
				}
			}
			break;
		default:
		}
	}
	
	/** Map of damage types to resistance types */
	public static final Map<EnumDamageType, Buff> damageResistMap = new EnumMap<EnumDamageType, Buff>(EnumDamageType.class);
	/** Map of damage types to weakness types */
	public static final Map<EnumDamageType, Buff> damageWeaknessMap = new EnumMap<EnumDamageType, Buff>(EnumDamageType.class);
	
	static {
		// do not include FIRE, since it is handled by isFireDamage()
		damageResistMap.put(COLD, Buff.RESIST_COLD);
		damageResistMap.put(HOLY, Buff.RESIST_HOLY);
		damageResistMap.put(SHOCK, Buff.RESIST_SHOCK);
		
		damageWeaknessMap.put(COLD, Buff.WEAKNESS_COLD);
		damageWeaknessMap.put(HOLY, Buff.WEAKNESS_HOLY);
		damageWeaknessMap.put(SHOCK, Buff.WEAKNESS_SHOCK);
	}
}

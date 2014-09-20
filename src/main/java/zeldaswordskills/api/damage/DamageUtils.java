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

package zeldaswordskills.api.damage;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;

/**
 * 
 * A collection of methods and classes related to damage, such as new DamageSource types
 *
 */
public class DamageUtils
{
	public static final String
	/** Standard armor break damage string */
	ARMOR_BREAK = "armorBreak",
	/** Armor break damage that is added on to the last combo hit, rather than as a normal strike */
	IARMOR_BREAK = "iArmorBreak",
	/** Used for damage from non-sword skills such as Slam */
	NON_SWORD = "nonSword",
	/** Indirect damage caused by sword skills such as Leaping Blow */
	INDIRECT_SWORD = "indirectSword";

	/**
	 * Returns an armor-bypassing physical DamageSource
	 */
	public static DamageSource causeArmorBreakDamage(Entity entity) {
		return new DamageSourceArmorBreak(ARMOR_BREAK, entity);
	}

	/**
	 * Returns IArmorBreak damage, distinguished from regular armor break damage
	 * because the damage is added on to the last hit for combos
	 */
	public static DamageSource causeIArmorBreakDamage(Entity entity) {
		return new DamageSourceArmorBreak(IARMOR_BREAK, entity);
	}

	public static class DamageSourceArmorBreak extends EntityDamageSource {
		/** Creates an armor-bypassing physical DamageSource */
		public DamageSourceArmorBreak(String name, Entity entity) {
			super(name, entity);
			setDamageBypassesArmor();
		}
	}

	/**
	 * Returns a direct non-sword-based DamageSource for skills such as "Slam"
	 * @param entity - entity directly responsible for causing the damage
	 */
	public static DamageSource causeNonSwordDamage(Entity entity) {
		return new EntityDamageSource(NON_SWORD, entity);
	}

	/**
	 * Returns an indirect sword-based DamageSource
	 * @param direct - entity directly responsible for causing the damage
	 * @param indirect - entity indirectly responsible, typically the player
	 */
	public static DamageSource causeIndirectSwordDamage(Entity direct, Entity indirect) {
		return new EntityDamageSourceIndirect(INDIRECT_SWORD, direct, indirect);
	}

	public static class DamageSourceShock extends EntityDamageSource implements IDamageType, IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Amount of hunger to drain */
		private final float hunger;

		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/**
		 * Creates a direct SHOCK damage source, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 */
		public DamageSourceShock(String name, Entity entity, int duration, float hunger) {
			super(name, entity);
			this.duration = duration;
			this.hunger = hunger;
			setDamageBypassesArmor();
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.SHOCK);
			enumDamageTypes.add(EnumDamageType.STUN);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return 5;
		}

		@Override
		public boolean canStunPlayers() {
			return true;
		}

		@Override
		public boolean alwaysStuns() {
			return true;
		}

		@Override
		public float getHungerDamage() {
			return hunger;
		}
	}

	public static class DamageSourceShockIndirect extends EntityDamageSourceIndirect implements IDamageType, IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Amount of hunger to drain */
		private final float hunger;

		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/**
		 * Creates indirect source of SHOCK damage, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 */
		public DamageSourceShockIndirect(String name, Entity direct, Entity indirect, int duration, float hunger) {
			super(name, direct, indirect);
			this.duration = duration;
			this.hunger = hunger;
			setDamageBypassesArmor();
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.SHOCK);
			enumDamageTypes.add(EnumDamageType.STUN);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return 5;
		}

		@Override
		public boolean canStunPlayers() {
			return true;
		}

		@Override
		public boolean alwaysStuns() {
			return true;
		}

		@Override
		public float getHungerDamage() {
			return hunger;
		}
	}

	public static class DamageSourceStun extends EntityDamageSource implements IDamageType,IDamageSourceStun
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private final int amplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers = false;

		/**
		 * Creates a direct stun damage source
		 * @param duration base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier amount, multiplied by damage received, that may be added to the duration
		 */
		public DamageSourceStun(String name, Entity entity, int duration, int amplifier) {
			super(name, entity);
			this.duration = duration;
			this.amplifier = amplifier;
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.STUN);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}

		/** Allows this damage source to stun players if allowed in the config */
		public DamageSourceStun setCanStunPlayers() {
			canStunPlayers = true;
			return this;
		}

		@Override
		public boolean canStunPlayers() {
			return canStunPlayers;
		}

		@Override
		public boolean alwaysStuns() {
			return false;
		}
	}

	public static class DamageSourceStunIndirect extends EntityDamageSourceIndirect implements IDamageType,IDamageSourceStun
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private final int amplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers = false;

		/**
		 * Creates an indirect stun damage source
		 * @param duration base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier amount, multiplied by damage received, that may be added to the duration
		 */
		public DamageSourceStunIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier) {
			super(name, direct, indirect);
			this.duration = duration;
			this.amplifier = amplifier;
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.STUN);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}

		/** Allows this damage source to stun players if allowed in the config */
		public DamageSourceStunIndirect setCanStunPlayers() {
			canStunPlayers = true;
			return this;
		}

		@Override
		public boolean canStunPlayers() {
			return canStunPlayers; }

		@Override
		public boolean alwaysStuns() {
			return false;

		}
	}

	public static class DamageSourceHoly extends EntityDamageSource implements IDamageType
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/** Creates a HOLY type EntityDamageSource */
		public DamageSourceHoly(String name, Entity entity) {
			super(name, entity);
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.HOLY);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}
	}

	public static class DamageSourceHolyIndirect extends EntityDamageSourceIndirect implements IDamageType
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/** Creates a HOLY type indirect entity DamageSource */
		public DamageSourceHolyIndirect(String name, Entity direct, Entity indirect) {
			super(name, direct, indirect);
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.HOLY);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}
	}

	public static class DamageSourceIce extends EntityDamageSource implements IDamageType, IPostDamageEffect
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;
		/** Slow effect duration and amplifier */
		private final int duration, amplifier;

		/** Creates a ice-based EntityDamageSource */
		public DamageSourceIce(String name, Entity entity, int duration, int amplifier) {
			super(name, entity);
			this.duration = duration;
			this.amplifier = amplifier;
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.COLD);
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}
	}

	public static class DamageSourceIceIndirect extends EntityDamageSourceIndirect implements IDamageType, IPostDamageEffect
	{
		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;
		/** Slow effect duration and amplifier */
		private final int duration, amplifier;

		/** Creates a ice-based indirect entity DamageSource */
		public DamageSourceIceIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier) {
			super(name, direct, indirect);
			this.duration = duration;
			this.amplifier = amplifier;
			enumDamageTypes = new HashSet<EnumDamageType>();
			enumDamageTypes.add(EnumDamageType.COLD);
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return enumDamageTypes;
		}
	}
}

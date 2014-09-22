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

import java.util.Collections;
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

	public static class DamageSourceBaseDirect extends EntityDamageSource implements IDamageAoE, IDamageType
	{
		/** Whether this particular damage source will result in AoE damage */
		protected final boolean isAoE;

		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		/**
		 * Creates a direct damage source with the given AoE flag
		 */
		public DamageSourceBaseDirect(String name, Entity entity, boolean isAoE) {
			super(name, entity);
			this.isAoE = isAoE;
			enumDamageTypes = new HashSet<EnumDamageType>();
		}

		@Override
		public final boolean isAoEDamage() {
			return isAoE;
		}

		protected void addDamageType(EnumDamageType type) {
			enumDamageTypes.add(type);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return Collections.unmodifiableSet(enumDamageTypes);
		}
	}

	public static class DamageSourceBaseIndirect extends EntityDamageSourceIndirect implements IDamageAoE, IDamageType
	{
		/** Whether this particular damage source will result in AoE damage */
		protected final boolean isAoE;

		/** EnumDamageTypes associated with this DamageSource */
		private Set<EnumDamageType> enumDamageTypes;

		public DamageSourceBaseIndirect(String name, Entity direct, Entity indirect, boolean isAoE) {
			super(name, direct, indirect);
			this.isAoE = isAoE;
			enumDamageTypes = new HashSet<EnumDamageType>();
		}

		@Override
		public final boolean isAoEDamage() {
			return isAoE;
		}

		protected void addDamageType(EnumDamageType type) {
			enumDamageTypes.add(type);
		}

		@Override
		public Set<EnumDamageType> getEnumDamageTypes() {
			return Collections.unmodifiableSet(enumDamageTypes);
		}
	}

	public static class DamageSourceShock extends DamageSourceBaseDirect implements IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Amount of hunger to drain */
		private final float hunger;

		/**
		 * Creates a non-AoE direct SHOCK damage source, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 */
		public DamageSourceShock(String name, Entity entity, int duration, float hunger) {
			this(name, entity, duration, hunger, false);
		}

		/**
		 * Creates a direct SHOCK damage source, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 * @param isAoE		True if this damage is an AoE attack
		 */
		public DamageSourceShock(String name, Entity entity, int duration, float hunger, boolean isAoE) {
			super(name, entity, isAoE);
			this.duration = duration;
			this.hunger = hunger;
			setDamageBypassesArmor();
			addDamageType(EnumDamageType.SHOCK);
			addDamageType(EnumDamageType.STUN);
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

	public static class DamageSourceShockIndirect extends DamageSourceBaseIndirect implements IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Amount of hunger to drain */
		private final float hunger;

		/**
		 * Creates non-AoE indirect source of SHOCK damage, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 */
		public DamageSourceShockIndirect(String name, Entity direct, Entity indirect, int duration, float hunger) {
			this(name, direct, indirect, duration, hunger, false);
		}

		/**
		 * Creates indirect source of SHOCK damage, causing stun and damaging hunger
		 * @param duration	Maximum stun time; will also be modified by total damage inflicted
		 * @param hunger	Amount of hunger to drain
		 * @param isAoE		True if this damage is an AoE attack
		 */
		public DamageSourceShockIndirect(String name, Entity direct, Entity indirect, int duration, float hunger, boolean isAoE) {
			super(name, direct, indirect, isAoE);
			this.duration = duration;
			this.hunger = hunger;
			setDamageBypassesArmor(); // TODO set on a case-by-case basis
			addDamageType(EnumDamageType.SHOCK);
			addDamageType(EnumDamageType.STUN);
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

	public static class DamageSourceStun extends DamageSourceBaseDirect implements IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private final int amplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers = false;

		/**
		 * Creates a non-AoE direct stun damage source
		 * @param duration	base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier	amount, multiplied by damage received, that may be added to the duration
		 */
		public DamageSourceStun(String name, Entity entity, int duration, int amplifier) {
			this(name, entity, duration, amplifier, false);
		}

		/**
		 * Creates a direct stun damage source
		 * @param duration	base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier	amount, multiplied by damage received, that may be added to the duration
		 * @param isAoE		True if this damage is an AoE attack
		 */
		public DamageSourceStun(String name, Entity entity, int duration, int amplifier, boolean isAoE) {
			super(name, entity, isAoE);
			this.duration = duration;
			this.amplifier = amplifier;
			addDamageType(EnumDamageType.STUN);
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

	public static class DamageSourceStunIndirect extends DamageSourceBaseIndirect implements IDamageSourceStun
	{
		/** Maximum stun time; will also be modified by total damage inflicted */
		private final int duration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private final int amplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers = false;

		/**
		 * Creates a non-AoE indirect stun damage source
		 * @param duration	base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier	amount, multiplied by damage received, that may be added to the duration
		 */
		public DamageSourceStunIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier) {
			this(name, direct, indirect, duration, amplifier, false);
		}

		/**
		 * Creates an indirect stun damage source
		 * @param duration	base stun duration, modified by amplifier and -rand.nextInt(duration / 2)
		 * @param amplifier	amount, multiplied by damage received, that may be added to the duration
		 * @param isAoE		True if this damage is an AoE attack
		 */
		public DamageSourceStunIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier, boolean isAoE) {
			super(name, direct, indirect, isAoE);
			this.duration = duration;
			this.amplifier = amplifier;
			addDamageType(EnumDamageType.STUN);
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

	public static class DamageSourceFire extends DamageSourceBaseDirect
	{
		/** Creates a non-AoE fire-based EntityDamageSource */
		public DamageSourceFire(String name, Entity entity) {
			this(name, entity, false);
		}

		/**
		 * Creates a fire-based EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceFire(String name, Entity entity, boolean isAoE) {
			super(name, entity, isAoE);
			addDamageType(EnumDamageType.FIRE);
			setFireDamage();
		}
	}

	public static class DamageSourceFireIndirect extends DamageSourceBaseIndirect
	{
		/** Creates a non-AoE fire-based indirect EntityDamageSource */
		public DamageSourceFireIndirect(String name, Entity direct, Entity indirect) {
			this(name, direct, indirect, false);
		}

		/**
		 * Creates a fire-based indirect EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceFireIndirect(String name, Entity direct, Entity indirect, boolean isAoE) {
			super(name, direct, indirect, isAoE);
			addDamageType(EnumDamageType.FIRE);
			setFireDamage();
		}
	}

	public static class DamageSourceHoly extends DamageSourceBaseDirect
	{
		/** Creates a non-AoE HOLY type EntityDamageSource */
		public DamageSourceHoly(String name, Entity entity) {
			this(name, entity, false);
		}

		/**
		 * Creates a HOLY type EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceHoly(String name, Entity entity, boolean isAoE) {
			super(name, entity, isAoE);
			addDamageType(EnumDamageType.HOLY);
		}
	}

	public static class DamageSourceHolyIndirect extends DamageSourceBaseIndirect
	{
		/** Creates a non-AoE HOLY type indirect entity DamageSource */
		public DamageSourceHolyIndirect(String name, Entity direct, Entity indirect) {
			this(name, direct, indirect, false);
		}

		/**
		 * Creates a HOLY type indirect entity DamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceHolyIndirect(String name, Entity direct, Entity indirect, boolean isAoE) {
			super(name, direct, indirect, isAoE);
			addDamageType(EnumDamageType.HOLY);
		}
	}

	public static class DamageSourceIce extends DamageSourceBaseDirect implements IPostDamageEffect
	{
		/** Slow effect duration and amplifier */
		private final int duration, amplifier;

		/** Creates a non-AoE ice-based EntityDamageSource */
		public DamageSourceIce(String name, Entity entity, int duration, int amplifier) {
			this(name, entity, duration, amplifier, false);
		}

		/**
		 * Creates a ice-based EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceIce(String name, Entity entity, int duration, int amplifier, boolean isAoE) {
			super(name, entity, isAoE);
			this.duration = duration;
			this.amplifier = amplifier;
			addDamageType(EnumDamageType.COLD);
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}
	}

	public static class DamageSourceIceIndirect extends DamageSourceBaseIndirect implements IPostDamageEffect
	{
		/** Slow effect duration and amplifier */
		private final int duration, amplifier;

		/** Creates a non-AoE ice-based indirect EntityDamageSource */
		public DamageSourceIceIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier) {
			this(name, direct, indirect, duration, amplifier, false);
		}

		/**
		 * Creates a ice-based indirect EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceIceIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier, boolean isAoE) {
			super(name, direct, indirect, isAoE);
			this.duration = duration;
			this.amplifier = amplifier;
			addDamageType(EnumDamageType.COLD);
		}

		@Override
		public int getDuration() {
			return duration;
		}

		@Override
		public int getAmplifier() {
			return amplifier;
		}
	}
}

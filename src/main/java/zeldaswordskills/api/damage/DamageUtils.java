/**
    Copyright (C) <2018> <coolAlias>

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import zeldaswordskills.api.damage.IComboDamage.IComboDamageFull;

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
	/** Used for damage from non-sword skills such as Slam */
	NON_SWORD = "nonSword",
	/** Indirect sword damage caused by sword skills such as Leaping Blow */
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
		return new DamageSourceArmorBreak(ARMOR_BREAK, entity, false);
	}

	public static class DamageSourceArmorBreak extends EntityDamageSource implements IComboDamageFull
	{
		private final boolean addHit;

		/** Creates an armor-bypassing physical DamageSource */
		public DamageSourceArmorBreak(String name, Entity direct) {
			this(name, direct, true);
		}

		/**
		 * Creates an armor-bypassing physical DamageSource
		 * @param addHit True to add the damage as its own hit, false to merge into previous combo damage
		 */
		public DamageSourceArmorBreak(String name, Entity direct, boolean addHit) {
			super(name, direct);
			this.addHit = addHit;
			setDamageBypassesArmor();
		}

		@Override
		public boolean isComboDamage(EntityPlayer player) {
			return true;
		}

		@Override
		public boolean increaseComboCount(EntityPlayer player) {
			return addHit;
		}

		@Override
		public boolean applyDamageToPrevious(EntityPlayer player) {
			return true; // already didn't add as its own hit, so merge with previous damage
		}

		@Override
		public boolean playDefaultSound(EntityPlayer player) {
			return true;
		}

		@Override
		public String getHitSound(EntityPlayer player) {
			return null;
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
	 * Returns an indirect sword-based DamageSource that does NOT add to the combo hit counter
	 * @param direct - entity directly responsible for causing the damage
	 * @param indirect - entity indirectly responsible, typically the player
	 */
	public static DamageSource causeIndirectSwordDamage(Entity direct, Entity indirect) {
		return new DamageSourceComboIndirect(INDIRECT_SWORD, direct, indirect, false);
	}

	/**
	 * Returns an indirect sword-based DamageSource that will count as a hit for combos
	 * @param direct - entity directly responsible for causing the damage
	 * @param indirect - entity indirectly responsible, typically the player
	 */
	public static DamageSource causeIndirectComboDamage(Entity direct, Entity indirect) {
		return new DamageSourceComboIndirect(INDIRECT_SWORD, direct, indirect, true);
	}

	public static class DamageSourceComboIndirect extends EntityDamageSourceIndirect implements IComboDamageFull
	{
		private final boolean increaseCount;
		private final boolean mergeDamage;

		public DamageSourceComboIndirect(String name, Entity direct, Entity indirect) {
			this(name, direct, indirect, true);
		}

		public DamageSourceComboIndirect(String name, Entity direct, Entity indirect, boolean increaseCount) {
			this(name, direct, indirect, increaseCount, false);
		}

		public DamageSourceComboIndirect(String name, Entity direct, Entity indirect, boolean increaseCount, boolean mergeDamage) {
			super(name, direct, indirect);
			this.increaseCount = increaseCount;
			this.mergeDamage = mergeDamage;
		}

		@Override
		public boolean isComboDamage(EntityPlayer player) {
			return true;
		}

		@Override
		public boolean increaseComboCount(EntityPlayer player) {
			return increaseCount;
		}

		@Override
		public boolean applyDamageToPrevious(EntityPlayer player) {
			return mergeDamage;
		}

		@Override
		public boolean playDefaultSound(EntityPlayer player) {
			return true;
		}

		@Override
		public String getHitSound(EntityPlayer player) {
			return null;
		}
	}

	public static class DamageSourceBaseDirect extends EntityDamageSource implements IDamageAoE, IDamageType, IDamageSourceStun
	{
		/** Whether this particular damage source will result in AoE damage */
		protected final boolean isAoE;

		/** EnumDamageTypes associated with this DamageSource */
		private final Set<EnumDamageType> enumDamageTypes = new HashSet<EnumDamageType>();

		/** Maximum base stun time; will also be modified by total damage inflicted */
		private int stunDuration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private int stunAmplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers;

		/**
		 * Creates a non-AoE direct damage source
		 */
		public DamageSourceBaseDirect(String name, Entity entity) {
			this(name, entity, false);
		}

		/**
		 * Creates a direct damage source with the given AoE flag
		 */
		public DamageSourceBaseDirect(String name, Entity entity, boolean isAoE) {
			super(name, entity);
			this.isAoE = isAoE;
		}

		/**
		 * Creates a non-AoE direct damage source with the specified damage type
		 * @param type	Only damage types with no special effects should be added this way
		 */
		public DamageSourceBaseDirect(String name, Entity entity, EnumDamageType type) {
			this(name, entity, false, type);
		}

		/**
		 * Creates a direct damage source with the given AoE flag and specified damage type
		 * @param type	Only damage types with no special effects should be added this way
		 */
		public DamageSourceBaseDirect(String name, Entity entity, boolean isAoE, EnumDamageType type) {
			this(name, entity, isAoE);
			addDamageType(type);
		}

		@Override
		public DamageSourceBaseDirect setFireDamage() {
			super.setFireDamage();
			this.addDamageType(EnumDamageType.FIRE);
			return this;
		}

		@Override
		public DamageSourceBaseDirect setMagicDamage() {
			super.setMagicDamage();
			this.addDamageType(EnumDamageType.MAGIC);
			return this;
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

		/**
		 * Adds stun effect to this damage source
		 * @param duration			Maximum base stun time; will also be modified by total damage inflicted
		 * @param amplifier			Factor by which stun time will be modified, multiplied by total damage inflicted
		 * @param canStunPlayers	If true, this damage source is capable of stunning players unless disabled in the config
		 */
		public DamageSourceBaseDirect setStunDamage(int duration, int amplifier, boolean canStunPlayers) {
			addDamageType(EnumDamageType.STUN);
			this.stunDuration = duration;
			this.stunAmplifier = amplifier;
			this.canStunPlayers = canStunPlayers;
			return this;
		}

		@Override
		public int getDuration(EnumDamageType type) {
			return stunDuration;
		}

		@Override
		public int getAmplifier(EnumDamageType type) {
			return stunAmplifier;
		}

		/** Allows this damage source to stun players if allowed in the config */
		public DamageSourceBaseDirect setCanStunPlayers() {
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

	public static class DamageSourceBaseIndirect extends EntityDamageSourceIndirect implements IDamageAoE, IDamageType, IDamageSourceStun
	{
		/** Whether this particular damage source will result in AoE damage */
		protected final boolean isAoE;

		/** EnumDamageTypes associated with this DamageSource */
		private final Set<EnumDamageType> enumDamageTypes = new HashSet<EnumDamageType>();

		/** Maximum base stun time; will also be modified by total damage inflicted */
		private int stunDuration;

		/** Factor by which stun time will be modified, multiplied by total damage inflicted */
		private int stunAmplifier;

		/** If true, this damage source is capable of stunning players unless disabled in the config */
		private boolean canStunPlayers;

		/**
		 * Creates a non-AoE indirect damage source
		 */
		public DamageSourceBaseIndirect(String name, Entity direct, Entity indirect) {
			this(name, direct, indirect, false);
		}

		/**
		 * Creates an indirect damage source with the given AoE flag
		 */
		public DamageSourceBaseIndirect(String name, Entity direct, Entity indirect, boolean isAoE) {
			super(name, direct, indirect);
			this.isAoE = isAoE;
		}

		/**
		 * Creates a non-AoE indirect damage source with the specified damage type
		 * @param type	Only damage types with no special effects should be added this way
		 */
		public DamageSourceBaseIndirect(String name, Entity direct, Entity indirect, EnumDamageType type) {
			this(name, direct, indirect, false, type);
		}

		/**
		 * Creates an indirect damage source with the given AoE flag and specified damage type
		 * @param type	Only damage types with no special effects should be added this way
		 */
		public DamageSourceBaseIndirect(String name, Entity direct, Entity indirect, boolean isAoE, EnumDamageType type) {
			this(name, direct, indirect, isAoE);
			addDamageType(type);
		}

		@Override
		public DamageSourceBaseIndirect setFireDamage() {
			super.setFireDamage();
			this.addDamageType(EnumDamageType.FIRE);
			return this;
		}

		@Override
		public DamageSourceBaseIndirect setMagicDamage() {
			super.setMagicDamage();
			this.addDamageType(EnumDamageType.MAGIC);
			return this;
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

		/**
		 * Adds stun effect to this damage source
		 * @param duration			Maximum base stun time; will also be modified by total damage inflicted
		 * @param amplifier			Factor by which stun time will be modified, multiplied by total damage inflicted
		 * @param canStunPlayers	If true, this damage source is capable of stunning players unless disabled in the config
		 */
		public DamageSourceBaseIndirect setStunDamage(int duration, int amplifier, boolean canStunPlayers) {
			addDamageType(EnumDamageType.STUN);
			this.stunDuration = duration;
			this.stunAmplifier = amplifier;
			this.canStunPlayers = canStunPlayers;
			return this;
		}

		@Override
		public int getDuration(EnumDamageType type) {
			return stunDuration;
		}

		@Override
		public int getAmplifier(EnumDamageType type) {
			return stunAmplifier;
		}

		/** Allows this damage source to stun players if allowed in the config */
		public DamageSourceBaseIndirect setCanStunPlayers() {
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

	public static class DamageSourceShock extends DamageSourceBaseDirect
	{
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
			super(name, entity, isAoE, EnumDamageType.SHOCK);
			this.hunger = hunger;
			setDamageBypassesArmor();
			setStunDamage(duration, 5, true);
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

	public static class DamageSourceShockIndirect extends DamageSourceBaseIndirect
	{
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
			super(name, direct, indirect, isAoE, EnumDamageType.SHOCK);
			this.hunger = hunger;
			setDamageBypassesArmor(); // TODO set on a case-by-case basis
			setStunDamage(duration, 5, true);
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
			super(name, entity, isAoE, EnumDamageType.FIRE);
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
			super(name, direct, indirect, isAoE, EnumDamageType.FIRE);
			setFireDamage();
		}
	}

	public static class DamageSourceIce extends DamageSourceBaseDirect
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
			super(name, entity, isAoE, EnumDamageType.COLD);
			this.duration = duration;
			this.amplifier = amplifier;
		}

		@Override
		public int getDuration(EnumDamageType type) {
			return (type == EnumDamageType.COLD ? duration : super.getDuration(type));
		}

		@Override
		public int getAmplifier(EnumDamageType type) {
			return (type == EnumDamageType.COLD ? amplifier : super.getAmplifier(type));
		}
	}

	public static class DamageSourceIceIndirect extends DamageSourceBaseIndirect
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
			super(name, direct, indirect, isAoE, EnumDamageType.COLD);
			this.duration = duration;
			this.amplifier = amplifier;
		}

		@Override
		public int getDuration(EnumDamageType type) {
			return (type == EnumDamageType.COLD ? duration : super.getDuration(type));
		}

		@Override
		public int getAmplifier(EnumDamageType type) {
			return (type == EnumDamageType.COLD ? amplifier : super.getAmplifier(type));
		}
	}

	public static class DamageSourceQuakeIndirect extends DamageSourceBaseIndirect
	{
		/** Nausea and slow effect duration and amplifier */
		private final int duration, amplifier;

		/** Creates an AoE quake-based indirect EntityDamageSource */
		public DamageSourceQuakeIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier) {
			this(name, direct, indirect, duration, amplifier, true);
		}

		/**
		 * Creates a quake-based indirect EntityDamageSource
		 * @param isAoE	True if this damage is an AoE attack
		 */
		public DamageSourceQuakeIndirect(String name, Entity direct, Entity indirect, int duration, int amplifier, boolean isAoE) {
			super(name, direct, indirect, isAoE, EnumDamageType.QUAKE);
			this.duration = duration;
			this.amplifier = amplifier;
		}

		@Override
		public int getDuration(EnumDamageType type) {
			return (type == EnumDamageType.QUAKE ? duration : super.getDuration(type));
		}

		@Override
		public int getAmplifier(EnumDamageType type) {
			return (type == EnumDamageType.QUAKE ? amplifier : super.getAmplifier(type));
		}
	}
}

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.SyncSkillPacket;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.skills.sword.Dodge;
import zeldaswordskills.skills.sword.EndingBlow;
import zeldaswordskills.skills.sword.LeapingBlow;
import zeldaswordskills.skills.sword.MortalDraw;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.skills.sword.RisingCut;
import zeldaswordskills.skills.sword.SpinAttack;
import zeldaswordskills.skills.sword.SwordBasic;
import zeldaswordskills.skills.sword.SwordBeam;
import zeldaswordskills.skills.sword.SwordBreak;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Abstract base skill class provides foundation for both passive and active skills
 * 
 */
public abstract class SkillBase
{
	/** Default maximum skill level */
	public static final byte MAX_LEVEL = 5;

	/** For convenience in providing initial id values */
	private static byte skillIndex = 0;

	/** Map containing all registered skills */
	private static final Map<Byte, SkillBase> skillsMap = new HashMap<Byte, SkillBase>();

	/* ACTIVE SKILLS */
	public static final SkillBase swordBasic = new SwordBasic("swordbasic").addDefaultDescriptions(2);
	public static final SkillBase armorBreak = new ArmorBreak("armorbreak").addDefaultDescriptions(2);
	public static final SkillBase dodge = new Dodge("dodge").addDefaultDescriptions(1);
	public static final SkillBase leapingBlow = new LeapingBlow("leapingblow").addDefaultDescriptions(2);
	public static final SkillBase parry = new Parry("parry").addDefaultDescriptions(1);
	public static final SkillBase dash = new Dash("dash").addDefaultDescriptions(1);
	public static final SkillBase spinAttack = new SpinAttack("spinattack").addDefaultDescriptions(1);
	public static final SkillBase superSpinAttack = new SpinAttack("superspinattack").addDefaultDescriptions(1);
	public static final SkillBase swordBeam = new SwordBeam("swordbeam").addDefaultDescriptions(3);

	/* PASSIVE SKILLS */
	public static final SkillBase bonusHeart = new BonusHeart("bonusheart").addDefaultDescriptions(1);

	/* NEW SKILLS */
	public static final SkillBase mortalDraw = new MortalDraw("mortaldraw").addDefaultDescriptions(2);
	public static final SkillBase swordBreak = new SwordBreak("swordbreak").addDefaultDescriptions(2);
	public static final SkillBase risingCut = new RisingCut("risingcut").addDefaultDescriptions(2);
	public static final SkillBase endingBlow = new EndingBlow("endingblow").addDefaultDescriptions(2);

	/** Unlocalized name for language registry */
	private final String unlocalizedName;

	/** IDs are determined internally; used as key to retrieve skill instance from skills map */
	private final byte id;

	/** Mutable field storing current level for this instance of SkillBase */
	protected byte level = 0;

	/** Contains descriptions for tooltip display */
	private final List<String> tooltip = new ArrayList<String>();

	/**
	 * Constructs the first instance of a skill and stores it in the skill list
	 * @param name		this is the unlocalized name and should not contain any spaces
	 * @param register	whether to register the skill, adding the skill to the skill list;
	 * 					seems to always be true since skills are declared statically
	 */
	protected SkillBase(String name, boolean register) {
		this.unlocalizedName = name;
		this.id = skillIndex++;
		if (register) {
			if (skillsMap.containsKey(id)) {
				LogHelper.log(Level.WARNING,"CONFLICT @ skill " + id + " id already occupied by "
						+ skillsMap.get(id).unlocalizedName + " while adding " + name);
			}
			skillsMap.put(id, this);
		}
	}

	/**
	 * Copy constructor creates a level zero version of the skill
	 */
	protected SkillBase(SkillBase skill) {
		this.unlocalizedName = skill.unlocalizedName;
		this.id = skill.id;
		this.tooltip.addAll(skill.tooltip);
	}

	/** Returns true if the id provided is mapped to a skill */
	public static final boolean doesSkillExist(int id) {
		return (id >= 0 && id <= Byte.MAX_VALUE && skillsMap.containsKey((byte) id));
	}

	/** Returns a new instance of the skill with id, or null if it doesn't exist */
	public static final SkillBase getNewSkillInstance(byte id) {
		return (skillsMap.containsKey(id) ? skillsMap.get(id).newInstance() : null);
	}

	/** Returns the instance of the skill stored in the map if it exists, or null */
	public static final SkillBase getSkill(int id) {
		return (doesSkillExist(id) ? skillsMap.get((byte) id) : null);
	}

	/** Returns an iterable collection of all the skills in the map */
	public static final Collection<SkillBase> getSkills() {
		return skillsMap.values();
	}

	/** Returns the total number of registered skills */
	public static final int getNumSkills() {
		return skillsMap.size();
	}

	/**
	 * Returns a leveled skill from an ISkillItem using {@link ISkillItem#getSkillId(ItemStack)}
	 * and {@link ISkillItem#getSkillLevel(ItemStack)}, or null if not possible
	 */
	/* TODO
	public static final SkillBase getSkillFromItem(final ItemStack stack, final ISkillItem item) {
		return createLeveledSkill(item.getSkillId(stack), item.getSkillLevel(stack));
	}
	 */

	/**
	 * Returns a leveled skill from an id and level, capped at the max level for the skill;
	 * May return null if the id is invalid or level is less than 1
	 */
	public static final SkillBase createLeveledSkill(final int id, final byte level) {
		if (doesSkillExist(id) && level > 0) {
			SkillBase skill = getNewSkillInstance((byte) id);
			skill.level = (level > skill.getMaxLevel() ? skill.getMaxLevel() : level);
			return skill;
		}
		return null;
	}

	/** Note that mutable objects such as this are not suitable as Map keys */
	@Override
	public int hashCode() {
		return 31 * (31 + id) + level;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SkillBase skill = (SkillBase) obj;
		return (skill.id == this.id && skill.level == this.level);
	}

	/** Returns a new instance of the skill with appropriate class type without registering it to the Skill database */
	public abstract SkillBase newInstance();

	/** Returns the translated skill name */
	public final String getDisplayName() {
		return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	/** Returns the unlocalized name prefixed by 'skill.zss' */
	public final String getUnlocalizedName() {
		return "skill.zss." + unlocalizedName;
	}

	/** Returns texture path for the skill's icon */
	public String getIconTexture() {
		return ModInfo.ID + ":skillorb_" + unlocalizedName;
	}

	/** Returns whether this skill can drop as an orb randomly from mobs */
	public boolean canDrop() {
		return true;
	}

	/** Returns whether this skill can generate as random loot in chests */
	public boolean isLoot() {
		return true;
	}

	/** Each skill's ID can be used as a key to retrieve it from the map */
	public final byte getId() {
		return id;
	}

	/** Returns current skill level */
	public final byte getLevel() {
		return level;
	}

	/** Returns max level this skill can reach; override to change */
	public byte getMaxLevel() {
		return MAX_LEVEL;
	}

	/**
	 * Returns the key used by the language file for getting the description n
	 * Language file should contain key "skill.zss.{unlocalizedName}.desc.n"
	 * @param n if less than zero, ".n" will not be appended
	 */
	protected final String getUnlocalizedDescription(int n) {
		return getUnlocalizedName() + ".desc" + (n < 0 ? "" : ("." + n));
	}

	/**
	 * Adds n description keys to the tooltip using the default key:
	 * {@link SkillBase#getUnlocalizedDescription(int n) getUnlocalizedDescription}
	 * @param n the number of descriptions to add should be at least 1
	 */
	protected final SkillBase addDefaultDescriptions(int n) {
		for (int i = 1; i <= n; ++i) {
			tooltip.add(getUnlocalizedDescription(i));
		}
		return this;
	}

	/** Adds a single untranslated string to the skill's tooltip display */
	protected final SkillBase addDescription(String string) {
		tooltip.add(string);
		return this;
	}

	/** Adds all entries in the provided list to the skill's tooltip display */
	protected final SkillBase addDescription(List<String> list) {
		for (String s : list) { tooltip.add(s); }
		return this;
	}

	/** Returns the translated list containing Strings for tooltip display */
	@SideOnly(Side.CLIENT)
	public final List<String> getDescription() {
		List<String> desc = new ArrayList(tooltip.size());
		for (String s : tooltip) {
			desc.add(StatCollector.translateToLocal(s));
		}
		return desc;
	}

	/** Returns a personalized tooltip display containing info about skill at current level */
	@SideOnly(Side.CLIENT)
	public abstract List<String> getDescription(EntityPlayer player);

	/** Returns a translated description of the skill's AoE, using the value provided */
	public String getAreaDisplay(double area) {
		return StatCollector.translateToLocalFormatted("skill.zss.area.desc", String.format("%.1f", area));
	}

	/** Returns a translated description of the skill's charge time in ticks, using the value provided */
	public String getChargeDisplay(int chargeTime) {
		return StatCollector.translateToLocalFormatted("skill.zss.charge.desc", chargeTime);
	}

	/** Returns a translated description of the skill's damage, using the value provided and with "+" if desired */
	public String getDamageDisplay(float damage, boolean displayPlus) {
		return StatCollector.translateToLocalFormatted("skill.zss.damage.desc", (displayPlus ? "+" : ""), String.format("%.1f", damage));
	}

	/** Returns a translated description of the skill's damage, using the value provided and with "+" if desired */
	public String getDamageDisplay(int damage, boolean displayPlus) {
		return StatCollector.translateToLocalFormatted("skill.zss.damage.desc", (displayPlus ? "+" : ""), damage);
	}

	/** Returns a translated description of the skill's duration, in ticks or seconds, using the value provided */
	public String getDurationDisplay(int duration, boolean inTicks) {
		return StatCollector.translateToLocalFormatted("skill.zss.duration.desc", (inTicks ? duration : duration / 20),
				(inTicks ? StatCollector.translateToLocal("skill.zss.ticks") : StatCollector.translateToLocal("skill.zss.seconds")));
	}

	/** Returns a translated description of the skill's exhaustion, using the value provided */
	public String getExhaustionDisplay(float exhaustion) {
		return StatCollector.translateToLocalFormatted("skill.zss.exhaustion.desc", String.format("%.2f", exhaustion));
	}

	/** Returns a translated description of the skill's range, using the value provided */
	public String getRangeDisplay(double range) {
		return StatCollector.translateToLocalFormatted("skill.zss.range.desc", String.format("%.1f", range));
	}

	/** Returns true if player meets requirements to learn this skill at target level */
	protected boolean canIncreaseLevel(EntityPlayer player, int targetLevel) {
		return ((level + 1) == targetLevel && targetLevel <= getMaxLevel());
	}

	/** Called each time a skill's level increases; responsible for everything OTHER than increasing the skill's level: applying any bonuses, handling Xp, etc. */
	protected abstract void levelUp(EntityPlayer player);

	/** Recalculates bonuses, etc. upon player respawn; Override if levelUp does things other than just calculate bonuses! */
	public void validateSkill(EntityPlayer player) {
		levelUp(player);
	}

	/** Shortcut method to grant skill at current level + 1 */
	public final boolean grantSkill(EntityPlayer player) {
		return grantSkill(player, level + 1);
	}

	/**
	 * Attempts to level up the skill to target level, returning true if skill's level increased (not necessarily to the target level)
	 */
	public final boolean grantSkill(EntityPlayer player, int targetLevel) {
		if (targetLevel <= level || targetLevel > getMaxLevel()) {
			return false;
		}
		byte oldLevel = level;
		while (level < targetLevel && canIncreaseLevel(player, level + 1)) {
			++level;
			levelUp(player);
		}
		if (!player.worldObj.isRemote && oldLevel < level) {
			PacketDispatcher.sendPacketToPlayer(new SyncSkillPacket(this).makePacket(), (Player) player);
		}
		return oldLevel < level;
	}

	/** This method should be called every update tick */
	public void onUpdate(EntityPlayer player) {}

	/** Writes mutable data to NBT. */
	public abstract void writeToNBT(NBTTagCompound compound);

	/** Reads mutable data from NBT. */
	public abstract void readFromNBT(NBTTagCompound compound);

	/** Returns a new instance from NBT */
	public abstract SkillBase loadFromNBT(NBTTagCompound compound);

}

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
import java.util.logging.Level;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import zeldaswordskills.network.SyncSkillPacket;
import zeldaswordskills.skills.sword.ArmorBreak;
import zeldaswordskills.skills.sword.Dash;
import zeldaswordskills.skills.sword.Dodge;
import zeldaswordskills.skills.sword.LeapingBlow;
import zeldaswordskills.skills.sword.Parry;
import zeldaswordskills.skills.sword.SpinAttack;
import zeldaswordskills.skills.sword.SwordBasic;
import zeldaswordskills.skills.sword.SwordBeam;
import zeldaswordskills.util.LogHelper;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

/**
 * 
 * Abstract base skill class provides foundation for both passive and active skills
 * 
 */
public abstract class SkillBase
{
	/** Maximum number of skills potentially available */
	public static final byte MAX_NUM_SKILLS = 16;
	
	/** Default maximum skill level */
	public static final byte MAX_LEVEL = 5;

	/** For convenience in providing initial id values */
	private static int skillIndex = 0;

	/** Similar to itemsList in Item, giving easy access to any Skill */
	private static final SkillBase[] skillsList = new SkillBase[MAX_NUM_SKILLS];
	
	/* ACTIVE SKILLS */
	public static final SkillBase swordBasic = new SwordBasic("Basic Sword Skill", (byte) skillIndex++);
	public static final SkillBase armorBreak = new ArmorBreak("Armor Break", (byte) skillIndex++);
	public static final SkillBase dodge = new Dodge("Dodge", (byte) skillIndex++);
	public static final SkillBase leapingBlow = new LeapingBlow("Leaping Blow", (byte) skillIndex++);
	public static final SkillBase parry = new Parry("Parry", (byte) skillIndex++);
	public static final SkillBase dash = new Dash("Dash", (byte) skillIndex++);
	public static final SkillBase spinAttack = new SpinAttack("Spin Attack", (byte) skillIndex++);
	public static final SkillBase superSpinAttack = new SpinAttack("Super Spin Attack", (byte) skillIndex++);
	public static final SkillBase swordBeam = new SwordBeam("Sword Beam", (byte) skillIndex++);
	
	/* PASSIVE SKILLS */
	public static final SkillBase bonusHeart = new BonusHeart("Bonus Heart", (byte) skillIndex++);
	
	/** Skill's display name */
	public final String name;
	
	/** Unlocalized name for language registry */
	protected final String unlocalizedName;

	/** Internal id for skill; needed mainly for prerequisites */
	public final byte id;

	/** Mutable field storing current level for this instance of SkillBase */
	protected byte level = 0;

	/** Contains descriptions for tooltip display */
	protected final List<String> tooltip = new ArrayList<String>();

	public SkillBase(String name, byte id, boolean register) {
		this.name = name;
		this.unlocalizedName = this.name.replace(" ", "").toLowerCase();
		this.id = id;

		if (register) {
			if (skillsList[id] != null) {
				LogHelper.log(Level.WARNING,"CONFLICT @ skill " + id + " id already occupied by "
									+ skillsList[id].name + " while adding " + name);
			}
			skillsList[id] = this;
		}
	}
	
	public SkillBase(SkillBase skill) {
		this.name = skill.name;
		this.unlocalizedName = skill.unlocalizedName;
		this.id = skill.id;
		this.tooltip.addAll(skill.tooltip);
	}
	
	/** Returns the Master Skills List */
	public static final SkillBase[] getSkillList() { return skillsList; }

	/**
	 * Override equals for List, Set, etc. implementations; may not be necessary
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o != null && this.getClass() == o.getClass()) {
			SkillBase skill = (SkillBase) o;
			return skill.id == this.id && skill.level == this.level;
		} else {
			return false;
		}
	}

	/** Returns a new instance of the skill with appropriate class type without registering it to the Skill database */
	public abstract SkillBase newInstance();
	
	/** Returns the unlocalized name prefixed by 'skill.' and suffixed by '.name' */
	public final String getUnlocalizedName() { return "skill.zss." + unlocalizedName + ".name"; }
	
	/** Returns whether this skill can drop as an orb randomly from mobs */
	public boolean canDrop() { return true; }

	/** Returns current skill level */
	public final byte getLevel() { return level; }
	
	/** Returns max level this skill can reach; override to change */
	public int getMaxLevel() { return MAX_LEVEL; }

	/** Returns the translated list containing Strings for tooltip display, personalized if appropriate */
	public final List<String> getDescription() { return (level > 0 ? getDescription() : tooltip); }
	
	/** Returns a personalized tooltip display containing info about skill at current level */
	public abstract List<String> getDescription(EntityPlayer player);

	/** Returns this skill's icon resource location */
	public ResourceLocation getIconTexture() { return null; }// new ResourceLocation(Textures.SKILLS + name.replace(" ", "").toLowerCase() + ".png"); }

	/** Translates and adds a single string to the skill's tooltip display */
	protected final SkillBase addDescription(String string) { tooltip.add(StatCollector.translateToLocal("skill.zss." + string)); return this; }
	
	/** Translates and adds all entries in the provided list to the skill's tooltip display */
	protected final SkillBase addDescription(List<String> list) { for (String s : list) { addDescription(s); } return this; }

	/** Returns true if player meets requirements to learn this skill at target level */
	protected boolean canIncreaseLevel(EntityPlayer player, int targetLevel) {
		return ((level + 1) == targetLevel && targetLevel <= getMaxLevel());
	}

	/** Called each time a skill's level increases; responsible for everything OTHER than increasing the skill's level: applying any bonuses, handling Xp, etc. */
	protected abstract void levelUp(EntityPlayer player);
	
	/** Recalculates bonuses, etc. upon player respawn; Override if levelUp does things other than just calculate bonuses! */
	public void validateSkill(EntityPlayer player) { levelUp(player); }

	/** Shortcut method to grant skill at current level + 1 */
	public final boolean grantSkill(EntityPlayer player) { return grantSkill(player, level + 1); }

	/**
	 * Attempts to level up the skill to target level, returning true if skill's level increased (not necessarily to the target level)
	 */
	public final boolean grantSkill(EntityPlayer player, int targetLevel) {
		if (targetLevel <= level || targetLevel > getMaxLevel()) { return false; }
		
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

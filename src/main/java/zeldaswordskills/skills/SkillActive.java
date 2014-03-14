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

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.entity.ZSSPlayerInfo;
import zeldaswordskills.network.ActivateSkillPacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Base class for active skills. Extend this class to add specific functionality.
 * 
 * Note that additional fields in child classes are not saved to NBT, so should not be
 * used to store any data that needs to be maintained between game sessions.
 * 
 * Unless the skill's activation is handled exclusively by the client side, ONLY activate or trigger
 * the skill on the server, as a packet will be sent automatically to notify the client
 * 
 * Each Skill should contain the following documentation followed by a full description
 * 
 * NAME: name of the skill
 * Description: one-line summary of skill
 * Activation:  Standard - activated by selecting the skill in the Skill Bar and pressing 'x'
 * 				Triggered - this skill can not be directly activated by the player
 * 				(toggle) - this skill is toggled on or off when activated
 * 				other - give details of how to activate the skill
 * Exhaustion: format is [0.0F +- (amount * level)]
 * Damage: if any, give the amount and additional details as necessary
 * Duration: if any, give the amount in ticks or seconds, as applicable
 * Range: if restricted, give the range in an applicable format, such as in blocks
 * Area: if any, give the dimensions and additional details as necessary
 * Special: any special notes
 * 
 * Full description goes here.
 *
 */
public abstract class SkillActive extends SkillBase
{
	/** If false, the skill may not be manually activated; used for passive skills that can be triggered */
	private boolean allowUserActivation = true;

	/** If true, further left-click interactions will be canceled while this skill is active */
	private boolean disablesLMB = false;

	protected SkillActive(String name, byte id) {
		super(name, id, true);
	}

	protected SkillActive(SkillActive skill) {
		super(skill);
		this.allowUserActivation = skill.allowUserActivation;
		this.disablesLMB = skill.disablesLMB;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getDescription(EntityPlayer player) { return getDescription(); }

	/** Returns true if this skill is currently active, however that is defined by the child class */
	public abstract boolean isActive();

	/** Amount of exhaustion added to the player each time this skill is used */
	protected abstract float getExhaustion();

	/**
	 * Use for player activation of a skill; only triggers if the skill may be manually activated
	 * This method is called when the skill is used; override to specify effect(s), but be
	 * sure check if (super.activate(world,player)) or canUse(player) before activating.
	 * When activated on the server, the client will be notified automatically; if activated
	 * on the client side directly, the server will NOT be notified.
	 * 
	 * NOTE that this can be used to activate a skill the player does not have - use ZSSPlayerInfo's
	 * activateSkill method instead to ensure the skill used is the player's
	 * 
	 * @return true if skill was successfully activated
	 */
	public boolean activate(World world, EntityPlayer player) {
		if (allowUserActivation) { return trigger(world, player); }
		return false;
	}

	/**
	 * Triggers the skill if the player can currently use it, even if the skill may not
	 * be manually activated (e.g. First Aid)
	 * 
	 * This method is called when the skill is used; override to specify effect(s), but be
	 * sure check if (super.activate(world,player)) or canUse(player) before activating
	 * 
	 * NOTE that this can be used to activate a skill the player does not have - use ZSSPlayerInfo's
	 * activateSkill method instead to ensure the skill used is the player's
	 */
	public boolean trigger(World world, EntityPlayer player) {
		if (canUse(player)) {
			if (!player.capabilities.isCreativeMode) {
				player.addExhaustion(getExhaustion());
			}
			if (!world.isRemote) {
				PacketDispatcher.sendPacketToPlayer(new ActivateSkillPacket(this).makePacket(), (Player) player);
			} else if (disablesLMB) { // only care about this client side
				ZSSPlayerInfo.get(player).setCurrentActiveSkill(this);
			}
			return true;
		} else {
			if (level > 0) { player.addChatMessage(StatCollector.translateToLocalFormatted("chat.zss.skill.use.fail", getDisplayName())); }
			return false;
		}
	}

	/** Returns true if this skill can currently be used by the player; override to add further conditions */
	public boolean canUse(EntityPlayer player) { return (level > 0 && player.getFoodStats().getFoodLevel() > 0); }

	/** Disables manual activation of this skill */
	protected SkillActive disableUserActivation() { allowUserActivation = false; return this; }

	/** Sets the skill to prevent left-mouse clicks while active */
	protected SkillActive setDisablesLMB() { disablesLMB = true; return this; }

	@Override
	protected void levelUp(EntityPlayer player) {}

	@Override
	public final void writeToNBT(NBTTagCompound compound) {
		compound.setByte("id", id);
		compound.setByte("level", level);
	}

	@Override
	public final void readFromNBT(NBTTagCompound compound) {
		level = compound.getByte("level");
	}

	@Override
	public final SkillActive loadFromNBT(NBTTagCompound compound) {
		SkillActive skill = (SkillActive) getSkillList()[compound.getByte("id")].newInstance();
		skill.readFromNBT(compound);
		return skill;
	}
}

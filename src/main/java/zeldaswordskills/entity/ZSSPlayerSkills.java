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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.client.SyncPlayerInfoPacket;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Handles all skill-related information for the player.
 *
 */
public class ZSSPlayerSkills
{
	private static final UUID hardcoreHeartUUID = UUID.fromString("83E54288-6BE2-4398-A880-957654F515AB");
	/** Max health modifier for players with Hardcore Zelda Fan mode enabled */
	private static final AttributeModifier hardcoreHeartModifier = (new AttributeModifier(hardcoreHeartUUID, "Hardcore Zelda Hearts", -14.0D, 0)).setSaved(true);

	private final EntityPlayer player;

	private final ZSSPlayerInfo playerInfo;

	/** Stores information on the player's Attributes and Passive Skills */
	private final Map<Byte, SkillBase> skills;

	/** Currently active skills */
	private final List<SkillActive> activeSkills = new LinkedList<SkillActive>();

	/**
	 * Currently animating skill that {@link SkillActive#hasAnimation() has an animation};
	 * it may or may not currently be {@link SkillActive#isAnimating() animating}
	 */
	@SideOnly(Side.CLIENT)
	private SkillActive animatingSkill;

	/** Number of Super Spin Attack orbs received from the Great Fairy: used to prevent exploits */
	private int fairySpinOrbsReceived = 0;

	public ZSSPlayerSkills(ZSSPlayerInfo playerInfo, EntityPlayer player) {
		this.playerInfo = playerInfo;
		this.player = player;
		this.skills = new HashMap<Byte, SkillBase>(SkillBase.getNumSkills());
	}

	public static ZSSPlayerSkills get(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getPlayerSkills();
	}

	/**
	 * Resets all data related to skills
	 */
	public void resetSkills() {
		// need level zero skills for validation, specifically for attribute-affecting skills
		for (SkillBase skill : SkillBase.getSkills()) {
			skills.put(skill.getId(), skill.newInstance());
		}
		validateSkills();
		skills.clear();
		fairySpinOrbsReceived = 0;
		PacketDispatcher.sendTo(new SyncPlayerInfoPacket(ZSSPlayerInfo.get(player)), (EntityPlayerMP) player);
	}

	/**
	 * Validates each skill upon player respawn, ensuring all bonuses are correct
	 */
	public final void validateSkills() {
		for (SkillBase skill : skills.values()) {
			skill.validateSkill(player);
		}
	}

	/**
	 * Applies hardcore Zelda fan heart modifier, if appropriate
	 */
	public void verifyMaxHealth() {
		IAttributeInstance attributeinstance = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
		if (attributeinstance.getModifier(hardcoreHeartUUID) != null) {
			attributeinstance.removeModifier(hardcoreHeartModifier);
		}
		if (Config.isHardcoreZeldaFan()) {
			attributeinstance.applyModifier(hardcoreHeartModifier);
		}
		if (player.getHealth() > player.getMaxHealth()) {
			player.setHealth(player.getMaxHealth());
		}
	}

	/** Wrapper method, since Nayru is often checked along with skills stuff */
	public boolean isNayruActive() {
		return playerInfo.isNayruActive();
	}

	/** Returns true if the player has at least one level in the specified skill */
	public boolean hasSkill(SkillBase skill) {
		return hasSkill(skill.getId());
	}

	/** Returns true if the player has at least one level in the specified skill (of any class) */
	private boolean hasSkill(byte id) {
		return getSkillLevel(id) > 0;
	}

	/** Returns the player's skill level for given skill, or 0 if the player doesn't have that skill */
	public byte getSkillLevel(SkillBase skill) {
		return getSkillLevel(skill.getId());
	}

	/** Returns the player's skill level for given skill, or 0 if the player doesn't have that skill */
	public byte getSkillLevel(byte id) {
		return (skills.containsKey(id) ? skills.get(id).getLevel() : 0);
	}

	/**
	 * Returns true if the player has the skill and the skill is currently active
	 */
	public boolean isSkillActive(SkillBase skill) {
		for (SkillActive active : activeSkills) {
			if (active.getId() == skill.getId()) {
				return active.isActive();
			}
		}
		return false;
	}

	/**
	 * Returns the {@link #animatingSkill}, which may be null
	 */
	@SideOnly(Side.CLIENT)
	public SkillActive getCurrentlyAnimatingSkill() {
		return animatingSkill;
	}

	/**
	 * This method is called automatically from {@link #onSkillActivated} for each skill activated.
	 * @param skill If this skill {@link SkillActive#hasAnimation has an animation}, it will be set
	 * 				as the currently animating skill.
	 */
	@SideOnly(Side.CLIENT)
	public void setCurrentlyAnimatingSkill(SkillActive skill) {
		animatingSkill = (skill == null || skill.hasAnimation() ? skill : animatingSkill);
	}

	/**
	 * Returns whether key/mouse input and skill interactions are currently allowed,
	 * i.e. the {@link #animatingSkill} is either null or not currently animating
	 */
	@SideOnly(Side.CLIENT)
	public boolean canInteract() {
		// don't set the current skill to null just yet if it is still animating
		// this allows skills to prevent key/mouse input without having to be 'active'
		if (animatingSkill != null && !animatingSkill.isActive() && !animatingSkill.isAnimating()) {//!isSkillActive(currentActiveSkill)) {
			animatingSkill = null;
		}
		return animatingSkill == null || !animatingSkill.isAnimating();
	}

	/**
	 * Call when a key is pressed to pass the key press to the player's skills'
	 * {@link SkillActive#keyPressed keyPressed} method, but only if the skill returns
	 * true from {@link SkillActive#isKeyListener isKeyListener} for the key pressed.
	 * The first skill to return true from keyPressed precludes any remaining skills
	 * from receiving the key press.
	 * @return	True if a listening skill's {@link SkillActive#keyPressed} signals that the key press was handled
	 */
	@SideOnly(Side.CLIENT)
	public boolean onKeyPressed(Minecraft mc, KeyBinding key) {
		// For docs: If there is a skill currently active, it is given first priority.
		// give precedence to currently active skill? except those that need it (e.g. ArmorBreak) may not yet be set as the active skill
		/*if (currentActiveSkill != null && currentActiveSkill.isKeyListener(mc, key)) {
			if (currentActiveSkill.keyPressed(mc, key, player)) {
				return true;
			}
		}*/
		for (SkillBase skill : skills.values()) {
			if (skill instanceof SkillActive && ((SkillActive) skill).isKeyListener(mc, key)) {
				if (((SkillActive) skill).keyPressed(mc, key, player)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Called from LivingAttackEvent to trigger {@link SkillActive#onBeingAttacked} for each
	 * currently active skill, potentially canceling the event. If the event is canceled, it
	 * returns immediately without processing any remaining active skills.
	 */
	public void onBeingAttacked(LivingAttackEvent event) {
		for (SkillActive skill : activeSkills) {
			if (skill.isActive() && skill.onBeingAttacked(player, event.source)) {
				event.setCanceled(true);
				return;
			}
		}
	}

	/**
	 * Called from LivingHurtEvent to trigger {@link SkillActive#postImpact} for each
	 * currently active skill, potentially altering the value of event.ammount, as
	 * well as calling {@link ICombo#onHurtTarget onHurtTarget} for the current ICombo.
	 */
	public void onPostImpact(LivingHurtEvent event) {
		for (SkillActive skill : activeSkills) {
			if (skill.isActive()) {
				event.ammount = skill.postImpact(player, event.entityLiving, event.ammount);
			}
		}
		// combo gets updated last, after all damage modifications are completed
		if (getComboSkill() != null) {
			getComboSkill().onHurtTarget(player, event);
		}
	}

	/**
	 * Returns a SkillActive version of the player's actual skill instance, or
	 * null if the player doesn't have the skill or it is not the correct type.
	 * Note that the skill is not necessarily {@link SkillActive#isActive} - use
	 * {@link #isSkillActive} to check that.
	 */
	public SkillActive getActiveSkill(SkillBase skill) {
		SkillBase active = getPlayerSkill(skill.getId());
		return (active instanceof SkillActive ? (SkillActive) active : null);
	}

	/** Returns the player's actual skill instance or null if the player doesn't have the skill */
	public SkillBase getPlayerSkill(SkillBase skill) {
		return getPlayerSkill(skill.getId());
	}

	/** Returns the player's actual skill instance or null if the player doesn't have the skill */
	public SkillBase getPlayerSkill(byte id) {
		return (skills.containsKey(id) ? skills.get(id) : null);
	}

	/**
	 * Returns first ICombo from a currently active skill, if any; ICombo may or may not be in progress
	 */
	public ICombo getComboSkill() {
		SkillBase skill = getPlayerSkill(SkillBase.swordBasic);
		if (skill != null && (((ICombo) skill).getCombo() != null || ((SkillActive) skill).isActive())) {
			return (ICombo) skill;
		}
		return null;
	}

	/** Returns player's ILockOnTarget skill, if any */
	public ILockOnTarget getTargetingSkill() {
		return (ILockOnTarget) getPlayerSkill(SkillBase.swordBasic);
	}

	/** Grants a skill with target level of current skill level plus one */
	public boolean grantSkill(SkillBase skill) {
		return grantSkill(skill.getId(), (byte)(getSkillLevel(skill) + 1));
	}

	/**
	 * Grants skill to player if player meets the requirements; returns true if skill learned
	 */
	public boolean grantSkill(byte id, byte targetLevel) {
		SkillBase skill = skills.containsKey(id) ? (SkillBase) skills.get(id) : SkillBase.getNewSkillInstance(id);
		if (skill.grantSkill(player, targetLevel)) {
			skills.put(id, skill);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Call after activating any skill to ensure it is added to the list of 
	 * current active skills and set as the currently animating skill
	 */
	private void onSkillActivated(World world, SkillActive skill) {
		if (skill.isActive()) {
			activeSkills.add(skill);
		}
		if (world.isRemote) {
			setCurrentlyAnimatingSkill(skill);
		}
	}

	/**
	 * Returns true if the player has this skill and {@link SkillActive#activate} returns true
	 */
	public boolean activateSkill(World world, SkillBase skill) {
		return activateSkill(world, skill.getId());
	}

	/**
	 * Returns true if the player has this skill and {@link SkillActive#activate} returns true
	 */
	public boolean activateSkill(World world, byte id) {
		SkillBase skill = skills.get(id);
		if (skill instanceof SkillActive && ((SkillActive) skill).activate(world, player)) {
			onSkillActivated(world, (SkillActive) skill);
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the player has this skill and {@link SkillActive#trigger} returns true
	 */
	public boolean triggerSkill(World world, SkillBase skill) {
		return triggerSkill(world, skill.getId());
	}

	/**
	 * Returns true if the player has this skill and {@link SkillActive#trigger} returns true
	 */
	public boolean triggerSkill(World world, byte id) {
		SkillBase skill = skills.get(id);
		if (skill instanceof SkillActive && ((SkillActive) skill).trigger(world, player, true)) {
			onSkillActivated(world, (SkillActive) skill);
			return true;
		}
		return false;
	}

	/**
	 * Returns true if this player meets the skill requirements to receive a Super
	 * Spin Attack orb from the Great Fairy
	 */
	public boolean canReceiveFairyOrb() {
		return (getSkillLevel(SkillBase.spinAttack) > getSkillLevel(SkillBase.superSpinAttack) &&
				getSkillLevel(SkillBase.superSpinAttack) >= fairySpinOrbsReceived &&
				getSkillLevel(SkillBase.bonusHeart) >= (fairySpinOrbsReceived * Config.getMaxBonusHearts() / 5));
	}

	/** Returns whether the player has already received all 5 Super Spin Attack orbs */
	public boolean hasReceivedAllOrbs() {
		return (fairySpinOrbsReceived == SkillBase.MAX_LEVEL);
	}

	/** Increments the number of Super Spin Attack orbs received, returning true if it's the last one */
	public boolean receiveFairyOrb() {
		return (++fairySpinOrbsReceived == SkillBase.MAX_LEVEL);
	}

	/**
	 * Reads a SkillBase from stream and updates the local skills map
	 * Called client side only for synchronizing a skill with the server version.
	 */
	@SideOnly(Side.CLIENT)
	public void syncClientSideSkill(byte id, NBTTagCompound compound) {
		if (SkillBase.doesSkillExist(id)) {
			skills.put(id, SkillBase.getNewSkillInstance(id).loadFromNBT(compound));
		}
	}

	/**
	 * Call during the render tick to update animating and ILockOnTarget skills
	 */
	@SideOnly(Side.CLIENT)
	public void onRenderTick(float partialRenderTick) {
		// flags whether a skill is currently animating
		boolean flag = false;
		if (animatingSkill != null) {
			if (animatingSkill.isAnimating()) {
				flag = animatingSkill.onRenderTick(player, partialRenderTick);
			} else if (!animatingSkill.isActive()) {
				setCurrentlyAnimatingSkill(null);
			}
		}
		ILockOnTarget skill = getTargetingSkill();
		if (!flag && skill != null && skill.isLockedOn()) {
			((SkillActive) skill).onRenderTick(player, partialRenderTick);
		}
	}

	public void onUpdate() {
		// let skill's update tick occur first
		for (SkillBase skill : skills.values()) {
			skill.onUpdate(player);
		} // and then remove from active list if no longer active
		// must use iterators to avoid concurrent modification exceptions to list
		Iterator<SkillActive> iterator = activeSkills.iterator();
		while (iterator.hasNext()) {
			SkillActive skill = iterator.next();
			if (!skill.isActive()) {
				iterator.remove();
			}
		}
		if (player.worldObj.isRemote) {
			if (ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].getIsKeyPressed() && isSkillActive(SkillBase.swordBasic) && player.getHeldItem() != null) {
				Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, player.getHeldItem());
			}
		}
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList taglist = new NBTTagList();
		for (SkillBase skill : skills.values()) {
			NBTTagCompound skillTag = new NBTTagCompound();
			skill.writeToNBT(skillTag);
			taglist.appendTag(skillTag);
		}
		compound.setTag("ZeldaSwordSkills", taglist);
		compound.setInteger("fairySpinOrbsReceived", fairySpinOrbsReceived);
	}

	public void loadNBTData(NBTTagCompound compound) {
		skills.clear(); // allows skills to reset on client without re-adding all the skills
		NBTTagList taglist = compound.getTagList("ZeldaSwordSkills", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < taglist.tagCount(); ++i) {
			NBTTagCompound skill = taglist.getCompoundTagAt(i);
			byte id = skill.getByte("id");
			skills.put(id, SkillBase.getSkill(id).loadFromNBT(skill));
		}
		fairySpinOrbsReceived = compound.getInteger("fairySpinOrbsReceived");
	}
}

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

package zeldaswordskills.entity.player;

import java.util.ArrayList;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncPlayerInfoPacket;
import zeldaswordskills.network.client.SyncSkillPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

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

	/** Number of crests given to Orca, implying number of Hurrican Spin orbs received */
	private int crestsGiven = 0;

	public ZSSPlayerSkills(ZSSPlayerInfo playerInfo, EntityPlayer player) {
		this.playerInfo = playerInfo;
		this.player = player;
		this.skills = new HashMap<Byte, SkillBase>(SkillBase.getNumSkills());
	}

	public static ZSSPlayerSkills get(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getPlayerSkills();
	}

	/**
	 * Removes the skill with the given name, or "all" skills
	 * @param name	Unlocalized skill name or "all" to remove all skills
	 * @return		False if no skill was removed
	 */
	public boolean removeSkill(String name) {
		if (("all").equals(name)) {
			resetSkills();
			return true;
		} else {
			// TODO change skill storage to use unlocalized name instead of id
			SkillBase dummy = null;
			for (SkillBase skill : skills.values()) {
				if (skill.getUnlocalizedName().equals(name)) {
					dummy = skill;
					break;
				}
			}
			if (dummy != null) {
				removeSkill(dummy);
				return true;
			}
		}
		return false;
	}

	private void removeSkill(SkillBase skill) {
		SkillBase dummy = skill.newInstance();
		skills.put(dummy.getId(), dummy);
		validateSkills();
		skills.remove(dummy.getId());
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncSkillPacket(dummy), (EntityPlayerMP) player);
		}
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
		crestsGiven = 0;
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncPlayerInfoPacket(ZSSPlayerInfo.get(player)), (EntityPlayerMP) player);
		}
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
		SkillBase active = getPlayerSkill(skill);
		return (active instanceof SkillActive && ((SkillActive) active).isActive());
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
	 * Called after {@link SkillActive#onActivated} returns true to add the skill to the
	 * list of currently active skills, as well as set the currently animating skill
	 */
	private void onSkillActivated(World world, SkillActive skill) {
		if (skill.isActive()) {
			activeSkills.add(skill);
			if (world.isRemote) {
				setCurrentlyAnimatingSkill(skill);
			}
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
	 * Returns true if this player has completed the Knight's Crest quest line for Orca
	 */
	public boolean completedCrests() {
		return crestsGiven >= 100;
	}

	/**
	 * If the player has at least 1 level of Spin Attack and the number of crests given
	 * is less than the max, one Knight's Crest is consumed and the player's progress on
	 * Orca's quest continues; otherwise no crest is given and a chat message displays instead.
	 */
	public void giveCrest() {
		if (getSkillLevel(SkillBase.spinAttack) < 1) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.unfit." + player.worldObj.rand.nextInt(4));
		} else if (getSkillLevel(SkillBase.superSpinAttack) < Math.min(crestsGiven / 20, SkillBase.superSpinAttack.getMaxLevel())) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.unfit." + player.worldObj.rand.nextInt(4));
		} else if (getSkillLevel(SkillBase.backSlice) < Math.min((crestsGiven + 10) / 20, SkillBase.backSlice.getMaxLevel())) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.unfit." + player.worldObj.rand.nextInt(4));
		} else if (crestsGiven >= 100) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.orca.master." + player.worldObj.rand.nextInt(4));
		} else if (PlayerUtils.consumeInventoryItem(player, ZSSItems.treasure, Treasures.KNIGHTS_CREST.ordinal(), 1)) {
			++crestsGiven;
			List<IChatComponent> chat = new ArrayList<IChatComponent>();
			chat.add(new ChatComponentTranslation("chat.zss.npc.orca.redeem." + player.worldObj.rand.nextInt(4)));
			if (crestsGiven == 1) {
				new TimedChatDialogue(player,
						new ChatComponentTranslation("chat.zss.npc.orca.begin.0"),
						new ChatComponentTranslation("chat.zss.npc.orca.begin.1"),
						new ChatComponentTranslation("chat.zss.npc.orca.begin.2"));
				player.triggerAchievement(ZSSAchievements.orcaRequest);
				return; // prevent other timed chat message
			} else if (crestsGiven > 19 && (crestsGiven % 20) == 0) {
				// Every 20 crests Orca will teach one level of Super Spin Attack
				boolean flag = true; // for timed give item timing
				if (crestsGiven == 100) {
					player.triggerAchievement(ZSSAchievements.orcaMaster);
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.final.0"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.final.1"));
				} else if (crestsGiven == 20) {
					player.triggerAchievement(ZSSAchievements.orcaSecondLesson);
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.first.0"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.first.1"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.first.2"));
				} else {
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.hurricane.train"));
					flag = false;
				}
				new TimedAddItem(player, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.superSpinAttack.getId()), (flag ? 4000 : 3000), Sounds.SUCCESS);
			} else if (crestsGiven > 9 && (crestsGiven % 10) == 0) {
				// Every 10 crests Orca will teach one more level of Back Slice
				boolean flag = true; // for timed give item timing
				if (crestsGiven == 90) {
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.final.0"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.final.1"));
				} else if (crestsGiven == 10) {
					player.triggerAchievement(ZSSAchievements.orcaFirstLesson);
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.first.0"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.first.1"));
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.first.2"));
				} else {
					chat.add(new ChatComponentTranslation("chat.zss.npc.orca.backslice.train"));
					flag = false;
				}
				new TimedAddItem(player, new ItemStack(ZSSItems.skillOrb, 1, SkillBase.backSlice.getId()), (flag ? 4000 : 3000), Sounds.SUCCESS);
			} else {
				chat.add(new ChatComponentTranslation("chat.zss.npc.orca.more." + player.worldObj.rand.nextInt(4), crestsGiven));
			}
			new TimedChatDialogue(player, chat.toArray(new IChatComponent[chat.size()]));
		}
	}

	/**
	 * Reads a SkillBase from stream and updates the local skills map; if the skill
	 * loaded from NBT is level 0, that skill will be removed.
	 * Called client side only for synchronizing a skill with the server version.
	 */
	@SideOnly(Side.CLIENT)
	public void syncClientSideSkill(byte id, NBTTagCompound compound) {
		if (SkillBase.doesSkillExist(id)) {
			SkillBase skill = SkillBase.getNewSkillInstance(id).loadFromNBT(compound);
			if (skill.getLevel() > 0) {
				skills.put(id, skill);
			} else {
				skills.remove(id);
			}
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
			if (ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].isKeyDown() && isSkillActive(SkillBase.swordBasic) && player.getHeldItem() != null) {
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
		compound.setInteger("crestsGiven", crestsGiven);
	}

	public void loadNBTData(NBTTagCompound compound) {
		skills.clear(); // allows skills to reset on client without re-adding all the skills
		NBTTagList taglist = compound.getTagList("ZeldaSwordSkills", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < taglist.tagCount(); ++i) {
			NBTTagCompound skill = taglist.getCompoundTagAt(i);
			byte id = skill.getByte("id");
			skills.put(id, SkillBase.getSkill(id).loadFromNBT(skill));
		}
		crestsGiven = compound.getInteger("crestsGiven");
	}
}

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
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zeldaswordskills.CommonProxy;
import zeldaswordskills.handler.ZSSKeyHandler;
import zeldaswordskills.item.ItemArmorBoots;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.network.SpawnNayruParticlesPacket;
import zeldaswordskills.network.SyncPlayerInfoPacket;
import zeldaswordskills.skills.ICombo;
import zeldaswordskills.skills.ILockOnTarget;
import zeldaswordskills.skills.SkillActive;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSPlayerInfo implements IExtendedEntityProperties
{
	private final static String EXT_PROP_NAME = "ZSSPlayerInfo";

	private final EntityPlayer player;
	
	/** Set to true when the player receives the bonus starting gear */
	private byte receivedGear = 0;
	
	/** Flags for worn gear and other things; none of these should be saved */
	private byte flags = 0;
	
	public static final byte
	/** Flag for whether the player is wearing special boots */
	IS_WEARING_BOOTS = 1,
	/** Flag for whether the player is wearing special headgear */
	IS_WEARING_HELM = 2,
	/** Flag for whether Nayru's Love is currently active */
	IS_NAYRU_ACTIVE = 4,
	/** Flag for whether the player's lateral movement should be increased while in the air */
	MOBILITY = 8;
	
	/** Last equipped special boots */
	private int lastBootsID = -1;
	
	/** Last equipped special helm (used for masks) */
	private int lastHelmID = -1;
	
	/** Current amount of time hovered */
	public int hoverTime = 0;

	/** Stores information on the player's Attributes and Passive Skills */
	private final Map<Byte, SkillBase> skills = new HashMap<Byte, SkillBase>(SkillBase.MAX_NUM_SKILLS);
	
	/** Number of Super Spin Attack orbs received from the Great Fairy: used to prevent exploits */
	private int fairySpinOrbsReceived = 0;
	
	/** Current stage in the Mask trading sequence */
	private int maskStage = 0;
	
	/** Mask stage value for fully completed sequence */
	public static final int MASK_COMPLETE = 3;

	public ZSSPlayerInfo(EntityPlayer player) {
		this.player = player;
		initSkills();
	}
	
	/**
	 * Adds all skills to the map at level zero
	 */
	private void initSkills() {
		for (int i = 0; i < SkillBase.MAX_NUM_SKILLS; ++i) {
			if (SkillBase.getSkillList()[i] != null) {
				skills.put(SkillBase.getSkillList()[i].id, SkillBase.getSkillList()[i].newInstance());
			}
		}
	}
	
	/**
	 * Resets all data related to skills
	 */
	public void resetSkills() {
		skills.clear();
		initSkills();
		validateSkills();
		fairySpinOrbsReceived = 0;
		PacketDispatcher.sendPacketToPlayer(new SyncPlayerInfoPacket(this).setReset().makePacket(), (Player) player);
	}

	/**
	 * If player has not received starting gear, it is provided
	 */
	public void verifyStartingGear() {
		if ((receivedGear & 1) == 0 && ZSSItems.grantBonusGear(player)) {
			receivedGear |= 1;
		}
	}
	
	/**
	 * Sets a bitflag to true or false; see flags for valid flag ids
	 */
	public void setFlag(byte flag, boolean value) {
		if (value) {
			flags |= flag;
		} else {
			flags &= ~flag;
		}
	}
	
	/** Returns true if the specified flag is set */
	public boolean getFlag(byte flag) {
		return (flags & flag) == flag;
	}
	
	/** Activates Nayru's Love effect; should call on both client and server */
	public void activateNayru() { setFlag(IS_NAYRU_ACTIVE, true); }
	
	/** Returns whether the player is currently under the effects of Nayru's Love */
	public boolean isNayruActive() { return getFlag(IS_NAYRU_ACTIVE); }

	/**
	 * Checks whether player is wearing special boots based on currently equipped gear
	 * and applies / removes modifiers as appropriate
	 */
	public void setWearingBoots() {
		ItemStack boots = player.getCurrentArmor(0);
		setFlag(IS_WEARING_BOOTS, (boots != null && boots.getItem() instanceof ItemArmorBoots));
		lastBootsID = (boots != null ? boots.itemID : -1);
		ItemArmorBoots.applyAttributeModifiers(boots, player);
		if (!getFlag(IS_WEARING_BOOTS) && getFlag(IS_WEARING_HELM)) {
			ItemMask.applyAttributeModifiers(player.getCurrentArmor(3), player);
		}
	}

	/**
	 * Checks whether player is wearing a special helm based on currently equipped gear
	 * and applies / removes modifiers as appropriate
	 */
	public void setWearingHelm() {
		ItemStack helm = player.getCurrentArmor(3);
		setFlag(IS_WEARING_HELM, (helm != null && helm.getItem() instanceof ItemMask));
		lastHelmID = (helm != null ? helm.itemID : -1);
		ItemMask.applyAttributeModifiers(helm, player);
		if (!getFlag(IS_WEARING_HELM) && getFlag(IS_WEARING_BOOTS)) {
			ItemArmorBoots.applyAttributeModifiers(player.getCurrentArmor(0), player);
		}
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
	public boolean hasReceivedAllOrbs() { return (fairySpinOrbsReceived == SkillBase.MAX_LEVEL); }
	
	/** Increments the number of Super Spin Attack orbs received, returning true if it's the last one */
	public boolean receiveFairyOrb() {
		return (++fairySpinOrbsReceived == SkillBase.MAX_LEVEL);
	}
	
	/**
	 * The player's current progress along the mask quest (% 3 gives stage):
	 * 0 - can get next mask, 1 - need to sell mask, 2 - need to pay for mask
	 */
	public int getCurrentMaskStage() {
		return maskStage;
	}
	
	/** Increments the mask quest stage by one */
	public void completeCurrentMaskStage() {
		++maskStage;
	}

	/** Returns true if the player has at least one level in the specified skill */
	public boolean hasSkill(SkillBase skill) { return hasSkill(skill.id); }

	/** Returns true if the player has at least one level in the specified skill (of any class) */
	private boolean hasSkill(byte id) { return getSkillLevel(id) > 0; }

	/** Returns the player's skill level for given skill, or 0 if the player doesn't have that skill */
	public byte getSkillLevel(SkillBase skill) { return getSkillLevel(skill.id); }

	/**
	 * Returns the player's skill level for given skill, or 0 if the player doesn't have that skill
	 */
	public byte getSkillLevel(byte id) {
		if (skills.containsKey(id)) {
			return skills.get(id).getLevel();
		} else {
			return 0;
		}
	}

	/**
	 * Returns true if the player can currently use the skill;
	 * i.e. has the skill and the skill's canUse method returns true
	 */
	public boolean canUseSkill(SkillBase skill) {
		return hasSkill(skill) && (!(skill instanceof SkillActive) || ((SkillActive) getPlayerSkill(skill)).canUse(player));
	}

	/**
	 * Returns true if the player has the skill and the skill is currently active
	 */
	public boolean isSkillActive(SkillBase skill) {
		if (skills.containsKey(skill.id) && skills.get(skill.id) instanceof SkillActive) {
			return (getSkillLevel(skill) > 0 && ((SkillActive) skills.get(skill.id)).isActive());
		} else {
			return false;
		}
	}

	/**
	 * Returns true if no animated skills are active
	 */
	public boolean canInteract() {
		return (!isSkillActive(SkillBase.armorBreak) && !isSkillActive(SkillBase.dash) && !isSkillActive(SkillBase.dodge)
				&& !isSkillActive(SkillBase.leapingBlow) && !isSkillActive(SkillBase.parry) && !isSkillActive(SkillBase.spinAttack));
	}

	/** Returns the player's actual skill instance or null if the player doesn't have the skill */
	public SkillBase getPlayerSkill(SkillBase skill) { return getPlayerSkill(skill.id); }

	/**
	 * Returns the player's actual skill instance or null if the player doesn't have the skill
	 */
	public SkillBase getPlayerSkill(byte id) {
		if (skills.containsKey(id)) {
			return skills.get(id);
		} else {
			return null;
		}
	}

	/**
	 * Returns first ICombo from a currently active skill, if any; ICombo may or may not be in progress
	 */
	public ICombo getComboSkill() {
		for (SkillBase skill : skills.values()) {
			if (skill instanceof ICombo && skill instanceof SkillActive && ((SkillActive) skill).isActive()) {
				return (ICombo) skill;
			}
		}
		return null;
	}

	/**
	 * Returns an ILockOnTarget skill, if any, with preference for currently active skill
	 */
	public ILockOnTarget getTargetingSkill() {
		ILockOnTarget planB = null;
		for (SkillBase skill : skills.values()) {
			if (skill instanceof ILockOnTarget && skill instanceof SkillActive) {
				if (((SkillActive) skill).isActive()) {
					return (ILockOnTarget) skill;
				} else {
					planB = (ILockOnTarget) skill;
				}
			}
		}
		return planB;
	}

	/** Grants a skill with target level of current skill level plus one */
	public boolean grantSkill(SkillBase skill) { return grantSkill(skill.id, (byte)(getSkillLevel(skill) + 1)); }

	/**
	 * Grants skill to player if player meets the requirements; returns true if skill learned
	 */
	public boolean grantSkill(byte id, byte targetLevel) {
		SkillBase skill = skills.containsKey(id) ? (SkillBase) skills.get(id) : SkillBase.getSkillList()[id].newInstance();
		if (skill.grantSkill(player, targetLevel)) {
			skills.put(id, skill);
			return true;
		} else {
			return false;
		}
	}

	/** Returns true if the player successfully activates his/her skill */
	public boolean activateSkill(World world, SkillBase skill) { return activateSkill(world, skill.id); }

	/**
	 * Returns true if the player successfully activates his/her skill
	 */
	public boolean activateSkill(World world, byte id) {
		if (skills.containsKey(id) && skills.get(id) instanceof SkillActive) {
			return ((SkillActive) skills.get(id)).activate(world, player);
		} else {
			return false;
		}
	}

	/** Returns true if the skill was triggered (e.g. activated without player input) */
	public boolean triggerSkill(World world, SkillBase skill) { return triggerSkill(world, skill.id); }

	/**
	 * Returns true if the skill was successfully triggered (e.g. activated without player input)
	 */
	public boolean triggerSkill(World world, byte id) {
		if (skills.containsKey(id) && skills.get(id) instanceof SkillActive) {
			return ((SkillActive) skills.get(id)).trigger(world, player);
		} else {
			return false;
		}
	}

	/**
	 * Reads a SkillBase from stream and updates the local skills map
	 * Called client side only for synchronizing a skill with the server version.
	 */
	@SideOnly(Side.CLIENT)
	public void syncClientSideSkill(byte id, NBTTagCompound compound) {
		if (id < SkillBase.MAX_NUM_SKILLS && SkillBase.getSkillList()[id] != null) {
			skills.put(id, SkillBase.getSkillList()[id].newInstance().loadFromNBT(compound));
		}
	}

	/**
	 * This method should be called every update tick; currently called from LivingUpdateEvent
	 */
	public void onUpdate() {
		if (getFlag(IS_NAYRU_ACTIVE)) {
			updateNayru();
		}
		if (getFlag(IS_WEARING_BOOTS) && (player.getCurrentArmor(0) == null || player.getCurrentArmor(0).itemID != lastBootsID)) {
			setWearingBoots();
		}
		if (getFlag(IS_WEARING_HELM) && (player.getCurrentArmor(3) == null || player.getCurrentArmor(3).itemID != lastHelmID)) {
			setWearingHelm();
		}
		if (getFlag(MOBILITY) && !player.onGround && Math.abs(player.motionY) > 0.05D && !player.capabilities.isFlying && player.worldObj.getWorldTime() % 2 == 0) {
			player.motionX *= 1.15D;
			player.motionZ *= 1.15D;
		}
		for (SkillBase skill : skills.values()) {
			skill.onUpdate(player);
		}
		if (player.worldObj.isRemote) {
			if (ZSSKeyHandler.keys[ZSSKeyHandler.KEY_BLOCK].pressed && isSkillActive(SkillBase.swordBasic) && player.getHeldItem() != null) {
				Minecraft.getMinecraft().playerController.sendUseItem(player, player.worldObj, player.getHeldItem());
			}
		}
	}
	
	/**
	 * Updates effects of Nayru's Love
	 */
	private void updateNayru() {
		player.hurtResistantTime = player.maxHurtResistantTime;
		if (player.ticksExisted % 16 == 0) {
			player.addExhaustion(3.0F);
		}
		if (player.getFoodStats().getFoodLevel() == 0) {
			setFlag(IS_NAYRU_ACTIVE, false);
		} else {
			WorldUtils.sendPacketToAllAround(new SpawnNayruParticlesPacket(player).makePacket(), player.worldObj, player, 4096.0D);
		}
	}

	/**
	 * Used to register these extended properties for the player during EntityConstructing event
	 */
	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(EXT_PROP_NAME, new ZSSPlayerInfo(player));
	}

	/**
	 * Returns ExtendedPlayer properties for player
	 */
	public static final ZSSPlayerInfo get(EntityPlayer player) {
		return (ZSSPlayerInfo) player.getExtendedProperties(EXT_PROP_NAME);
	}

	/**
	 * Makes it look nicer in the methods save/loadProxyData
	 */
	private static final String getSaveKey(EntityPlayer player) {
		return player.username + ":" + EXT_PROP_NAME;
	}

	/**
	 * Does everything I did in onLivingDeathEvent and it's static,
	 * so you now only need to use the following in the above event:
	 * ExtendedPlayer.saveProxyData((EntityPlayer) event.entity));
	 */
	public static final void saveProxyData(EntityPlayer player) {
		NBTTagCompound tag = new NBTTagCompound();
		ZSSPlayerInfo.get(player).saveNBTData(tag);
		CommonProxy.storeEntityData(getSaveKey(player), tag);
	}

	/**
	 * This cleans up the onEntityJoinWorld event by replacing most of the code
	 * with a single line: ExtendedPlayer.loadProxyData((EntityPlayer) event.entity));
	 */
	public static final void loadProxyData(EntityPlayer player) {
		ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
		NBTTagCompound tag = CommonProxy.getEntityData(getSaveKey(player));
		if (tag != null) {
			info.loadNBTData(tag);
		}
		info.validateSkills();
		PacketDispatcher.sendPacketToPlayer(new SyncPlayerInfoPacket(info).makePacket(), (Player) player);
	}

	/**
	 * Validates each skill upon player respawn, ensuring all bonuses are correct
	 */
	public final void validateSkills() {
		for (SkillBase skill : skills.values()) {
			skill.validateSkill(player);
		}
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList taglist = new NBTTagList();
		for (SkillBase skill : skills.values()) {
			NBTTagCompound skillTag = new NBTTagCompound();
			skill.writeToNBT(skillTag);
			taglist.appendTag(skillTag);
		}
		compound.setTag("ZeldaSwordSkills", taglist);
		compound.setByte("ZSSGearReceived", receivedGear);
		compound.setInteger("lastBoots", lastBootsID);
		compound.setInteger("lastHelm", lastHelmID);
		compound.setInteger("fairySpinOrbsReceived", fairySpinOrbsReceived);
		compound.setInteger("maskStage", maskStage);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		NBTTagList taglist = compound.getTagList("ZeldaSwordSkills");
		for (int i = 0; i < taglist.tagCount(); ++i) {
			NBTTagCompound skill = (NBTTagCompound) taglist.tagAt(i);
			byte id = skill.getByte("id");
			skills.put(id, SkillBase.getSkillList()[id].loadFromNBT(skill));
		}
		receivedGear = compound.getByte("ZSSGearReceived");
		lastBootsID = compound.getInteger("lastBoots");
		lastHelmID = compound.getInteger("lastHelm");
		fairySpinOrbsReceived = compound.getInteger("fairySpinOrbsReceived");
		maskStage = compound.getInteger("maskStage");
	}

	@Override
	public void init(Entity entity, World world) {}

}

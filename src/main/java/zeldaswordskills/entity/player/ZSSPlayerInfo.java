/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.entity.player;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.api.item.ArmorIndex;
import zeldaswordskills.entity.ZSSEntityInfo;
import zeldaswordskills.entity.buff.Buff;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.item.ItemArmorBoots;
import zeldaswordskills.item.ItemHeroBow;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ItemZeldaShield;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.AttackBlockedPacket;
import zeldaswordskills.network.client.SetNockedArrowPacket;
import zeldaswordskills.network.client.SpawnNayruParticlesPacket;
import zeldaswordskills.network.client.SyncCurrentMagicPacket;
import zeldaswordskills.network.client.SyncPlayerInfoPacket;
import zeldaswordskills.network.client.SyncQuestsPacket;
import zeldaswordskills.network.server.RequestCurrentMagicPacket;
import zeldaswordskills.ref.Config;

public class ZSSPlayerInfo implements IExtendedEntityProperties
{
	private static final String EXT_PROP_NAME = "ZSSPlayerInfo";

	private final EntityPlayer player;

	private final ZSSPlayerSkills playerSkills;

	private final ZSSPlayerSongs playerSongs;

	/** Current magic points */
	private float mp, lastMp;

	public static final IAttribute maxMagic = (new RangedAttribute(null, "zss.max_magic", 50.0D, 0.0D, Double.MAX_VALUE)).setDescription("Max Magic").setShouldWatch(true);

	/** Last ridden entity so it can be set after player is no longer riding */
	private Entity lastRidden;

	/** Maximum time the player may be prevented from taking a left-click action */
	private final static int MAX_ATTACK_DELAY = 50;

	/** Time remaining until player may perform another left-click action, such as an attack */
	private int attackTime; // TODO move to ZSSEntityInfo ? or player skills?

	/** Special block timer for shields; player cannot block while this is greater than zero */
	private int blockTime = 0;

	public static enum Stats {
		/** Number of secret rooms this player has opened */
		STAT_SECRET_ROOMS,
		/** Bit storage for each type of boss room opened */
		STAT_BOSS_ROOMS
	};

	/** ZSS player statistics */
	private final Map<Stats, Integer> playerStats = new EnumMap<Stats, Integer>(Stats.class);

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
	private ItemStack lastBootsWorn;

	/** Last equipped special helm (used for masks) */
	private ItemStack lastHelmWorn;

	/** Current amount of time hovered */
	public int hoverTime = 0;

	@Deprecated
	private int skulltulaTokens = 0;

	/** [Hero's Bow] Stores the currently nocked arrow in order to avoid the graphical glitch caused by writing to the stack's NBT */
	private ItemStack arrowStack = null;

	/** [Hero's Bow] Part of the graphical glitch fix: Whether the player retrieved a bomb arrow via getAutoBombArrow */
	public boolean hasAutoBombArrow = false;

	/** [Slingshot] Current mode index */
	public int slingshotMode = 0;

	/** Reduces fall damage next impact; used for Rising Cut */
	public float reduceFallAmount = 0.0F;

	/** Used by certain skills for controlling the player's main arm rendering */
	public float armSwing = 0.0F;

	public ZSSPlayerInfo(EntityPlayer player) {
		this.player = player;
		playerSkills = new ZSSPlayerSkills(player);
		playerSongs = new ZSSPlayerSongs(player);
		player.getAttributeMap().registerAttribute(maxMagic).setBaseValue(50.0D);
		mp = getMaxMagic(); // don't use setter here: don't want to send packet
		initStats();
	}

	@Override
	public void init(Entity entity, World world) {}

	private void initStats() {
		for (Stats stat : Stats.values()) {
			playerStats.put(stat, 0);
		}
	}

	/**
	 * Returns the player's current MP
	 */
	public float getCurrentMagic() {
		if (mp > getMaxMagic()) {
			setCurrentMagic(getMaxMagic());
		}
		return mp;
	}

	/**
	 * Restores the given amount of MP
	 */
	public void restoreMagic(float amount) {
		setCurrentMagic(getCurrentMagic() + amount);
	}

	/**
	 * Sets the player's current MP
	 */
	public void setCurrentMagic(float value) {
		this.mp = MathHelper.clamp_float(value, 0.0F, getMaxMagic());
	}

	/**
	 * Sets the player's current MP upon first joining the world (since max MP attribute not yet synced)
	 */
	public void setInitialMagic(float value) {
		this.mp = Math.max(0.0F, value);
	}

	/**
	 * Returns the player's current maximum magic points
	 */
	public float getMaxMagic() {
		return (float) player.getAttributeMap().getAttributeInstance(maxMagic).getAttributeValue();
	}

	/**
	 * Sets the player's maximum magic points
	 */
	public void setMaxMagic(float value) {
		value = MathHelper.clamp_float(value, 0.0F, (float)Config.getMaxMagicPoints());
		player.getAttributeMap().getAttributeInstance(maxMagic).setBaseValue(value);
		if (getCurrentMagic() > getMaxMagic()) {
			setCurrentMagic(getMaxMagic());
		}
	}

	/**
	 * Depletes the magic bar by the amount whether the player has enough magic or not
	 * @return True if the player had sufficient magic for the amount
	 */
	public boolean consumeMagic(float amount) {
		return (canUseMagic() ? useMagic(amount, true) : false);
	}

	/**
	 * Depletes the magic bar by the amount only if the player has sufficient magic
	 * @return True if the player had sufficient magic for the amount
	 */
	public boolean useMagic(float amount) {
		return (canUseMagic() ? useMagic(amount, false) : false);
	}

	/**
	 * Returns true if current magic is at least equal to the amount to use
	 * @param consume true to deplete magic bar even if magic is insufficient
	 */
	private boolean useMagic(float amount, boolean consume) {
		if (player.capabilities.isCreativeMode || ZSSEntityInfo.get(player).isBuffActive(Buff.UNLIMITED_MAGIC)) {
			return true;
		}
		boolean sufficient = amount <= getCurrentMagic();
		if (sufficient || consume) {
			setCurrentMagic(getCurrentMagic() - amount);
		}
		return sufficient;
	}

	/**
	 * Returns whether the player is currently allowed to use magic
	 */
	public boolean canUseMagic() {
		return player.capabilities.isCreativeMode || (getMaxMagic() > 0 && !isNayruActive());
	}

	/** Gets the player's current stat value */
	public int getStat(Stats stat) {
		return playerStats.get(stat);
	}

	/** Adds the value to the player's stats */
	public void addStat(Stats stat, int value) {
		int i = playerStats.remove(stat);
		switch(stat) {
		case STAT_BOSS_ROOMS: playerStats.put(stat, i | value); break;
		default: playerStats.put(stat, i + value);
		}
	}

	public ZSSPlayerSkills getPlayerSkills() {
		return playerSkills;
	}

	public ZSSPlayerSongs getPlayerSongs() {
		return playerSongs;
	}

	/**
	 * True if the player can perform a left-click action (i.e. the action timer is zero)
	 */
	public boolean canAttack() {
		return attackTime == 0 || player.capabilities.isCreativeMode;
	}

	/**
	 * Returns the current amount of time remaining before a left-click action may be performed
	 */
	public int getAttackTime() {
		return attackTime;
	}

	/**
	 * Sets the number of ticks remaining before another action may be performed, but
	 * no less than the current value and no more than MAX_ATTACK_DELAY.
	 */
	public void setAttackTime(int ticks) {
		this.attackTime = MathHelper.clamp_int(ticks, attackTime, MAX_ATTACK_DELAY);
	}

	/** Whether the player is able to block at this time (block timer is zero) */
	public boolean canBlock() {
		return blockTime == 0;
	}

	/**
	 * Sets the player's block timer, clears the item in use and adds exhaustion upon blocking an attack
	 * @param damage only used server side to calculate exhaustion: 0.3F * damage
	 */
	public void onAttackBlocked(ItemStack shield, float damage) {
		ZSSCombatEvents.setPlayerAttackTime(player);
		blockTime = (shield.getItem() instanceof ItemZeldaShield ? ((ItemZeldaShield) shield.getItem()).getRecoveryTime() : 20);
		player.clearItemInUse();
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new AttackBlockedPacket(shield), (EntityPlayerMP) player);
			player.addExhaustion(0.3F * damage);
		}
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
	public void activateNayru() {
		if (Config.allowUnlimitedNayru() || !ZSSEntityInfo.get(player).isBuffActive(Buff.UNLIMITED_MAGIC)) {
			setFlag(IS_NAYRU_ACTIVE, true);
		}
	}

	/** Returns whether the player is currently under the effects of Nayru's Love */
	public boolean isNayruActive() {
		return getFlag(IS_NAYRU_ACTIVE);
	}

	/**
	 * Sets whether player is wearing special boots based on currently equipped gear
	 * and applies / removes modifiers as appropriate
	 * @param boots Currently equipped boots (null if none worn)
	 */
	public void setWearingBoots(ItemStack boots) {
		if (lastBootsWorn != null && lastBootsWorn.getItem() instanceof ItemArmorBoots) {
			((ItemArmorBoots) lastBootsWorn.getItem()).removeModifiers(lastBootsWorn, player);
		}
		lastBootsWorn = boots;
		setFlag(IS_WEARING_BOOTS, (boots != null && boots.getItem() instanceof ItemArmorBoots));
		if (getFlag(IS_WEARING_BOOTS)) {
			((ItemArmorBoots) boots.getItem()).applyModifiers(boots, player);
		}
		if (getFlag(IS_WEARING_HELM)) {
			ItemStack helm = player.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm != null && helm.getItem() instanceof ItemMask) {
				((ItemMask) helm.getItem()).applyModifiers(helm, player);
			}
		}
	}

	/**
	 * Checks whether player is wearing a special helm based on currently equipped gear
	 * and applies / removes modifiers as appropriate
	 * @param helm Currently equipped helm (null if none worn)
	 */
	public void setWearingHelm(ItemStack helm) {
		if (lastHelmWorn != null && lastHelmWorn.getItem() instanceof ItemMask) {
			((ItemMask) lastHelmWorn.getItem()).removeModifiers(lastHelmWorn, player);
		}
		lastHelmWorn = helm;
		setFlag(IS_WEARING_HELM, (helm != null && helm.getItem() instanceof ItemMask));
		if (getFlag(IS_WEARING_HELM)) {
			((ItemMask) helm.getItem()).applyModifiers(helm, player);
		}
		if (getFlag(IS_WEARING_BOOTS)) {
			ItemStack boots = player.getCurrentArmor(ArmorIndex.WORN_BOOTS);
			if (boots != null && boots.getItem() instanceof ItemArmorBoots) {
				((ItemArmorBoots) boots.getItem()).applyModifiers(boots, player);
			}
		}
	}

	@Deprecated
	public int getSkulltulaTokens() {
		return skulltulaTokens;
	}

	/**
	 * Returns the currently nocked arrow for the Hero's Bow, possibly null
	 */
	public ItemStack getNockedArrow() {
		return arrowStack;
	}

	/**
	 * Marks this arrow as nocked in the Hero's Bow
	 * @param stack The current arrow or null if empty
	 */
	public void setNockedArrow(ItemStack stack) {
		arrowStack = stack;
		if (player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SetNockedArrowPacket(stack), (EntityPlayerMP) player);
		}
	}

	/**
	 * This method should be called every update tick; currently called from LivingUpdateEvent
	 */
	public void onUpdate() {
		playerSkills.onUpdate();
		// keeps MP packet count down to maximum of 1 per tick
		if (!player.worldObj.isRemote && lastMp != mp) {
			lastMp = mp;
			if (player instanceof EntityPlayerMP) {
				PacketDispatcher.sendTo(new SyncCurrentMagicPacket(player), (EntityPlayerMP) player);
			}
		}
		if (attackTime > 0) {
			--attackTime;
		}
		if (blockTime > 0) {
			--blockTime;
		}
		if (getFlag(IS_NAYRU_ACTIVE)) {
			updateNayru();
		}
		if (getFlag(IS_WEARING_BOOTS)) {
			ItemStack boots = player.getCurrentArmor(ArmorIndex.WORN_BOOTS);
			if (boots == null || (lastBootsWorn != null && boots.getItem() != lastBootsWorn.getItem())) {
				setWearingBoots(boots);
			}
		}
		if (getFlag(IS_WEARING_HELM)) {
			ItemStack helm = player.getCurrentArmor(ArmorIndex.WORN_HELM);
			if (helm == null || (lastHelmWorn != null && helm.getItem() != lastHelmWorn.getItem())) {
				setWearingHelm(helm);
			}
		}
		if (getFlag(MOBILITY) && !player.onGround && Math.abs(player.motionY) > 0.05D
				&& !player.capabilities.isFlying && player.worldObj.getTotalWorldTime() % 2 == 0) {
			player.motionX *= 1.15D;
			player.motionZ *= 1.15D;
		}
		if (hasAutoBombArrow && (player.getHeldItem() == null || !(player.getHeldItem().getItem() instanceof ItemHeroBow))) {
			hasAutoBombArrow = false;
		}
		// Check for currently ridden horse, used for Epona's Song
		if (lastRidden == null && player.ridingEntity != null) {
			lastRidden = player.ridingEntity;
			if (lastRidden instanceof EntityHorse) {
				playerSongs.setHorseRidden((EntityHorse) lastRidden);
			}
		} else if (player.ridingEntity == null && lastRidden instanceof EntityHorse) {
			playerSongs.setHorseRidden((EntityHorse) lastRidden);
			lastRidden = null; // dismounted and horse's last known coordinates set
		}
	}

	/**
	 * Updates effects of Nayru's Love
	 */
	private void updateNayru() {
		player.hurtResistantTime = player.maxHurtResistantTime;
		if (player.ticksExisted % 4 == 0) {
			if (!useMagic(0.5F, true)) { // call private method directly to circumvent canUseMagic()
				setFlag(IS_NAYRU_ACTIVE, false);
			} else if (player instanceof EntityPlayerMP) {
				PacketDispatcher.sendToAllAround(new SpawnNayruParticlesPacket(player), player, 64.0D);
			}
		}
	}

	/** Used to register these extended properties for the player during EntityConstructing event */
	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(EXT_PROP_NAME, new ZSSPlayerInfo(player));
		ZSSQuests.register(player);
	}

	/** Returns ExtendedPlayer properties for player */
	public static final ZSSPlayerInfo get(EntityPlayer player) {
		return (ZSSPlayerInfo) player.getExtendedProperties(EXT_PROP_NAME);
	}

	/**
	 * Called when a player logs in for the first time
	 */
	public void onPlayerLoggedIn() {
		if (player instanceof EntityPlayerMP) {
			verifyStartingGear();
		}
	}

	/**
	 * Call each time the player joins the world to sync data to the client
	 */
	public void onJoinWorld() {
		if (player instanceof EntityPlayerMP) {
			playerSkills.validateSkills();
			playerSkills.verifyMaxHealth();
			PacketDispatcher.sendTo(new SyncPlayerInfoPacket(this), (EntityPlayerMP) player);
			PacketDispatcher.sendTo(new SyncQuestsPacket(ZSSQuests.get(player)), (EntityPlayerMP) player);
		} else { // Re-request current mana (truncated by attribute having incorrect value initially)
			PacketDispatcher.sendToServer(new RequestCurrentMagicPacket());
		}
	}

	/**
	 * Copies given data to this one when a player is cloned
	 * If the client also needs the data, the packet must be sent from
	 * EntityJoinWorldEvent to ensure it is sent to the new client player
	 */
	public void copy(ZSSPlayerInfo info) {
		NBTTagCompound compound = new NBTTagCompound();
		info.saveNBTData(compound);
		this.loadNBTData(compound);
		ZSSQuests.get(this.player).copy(ZSSQuests.get(info.player));
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		playerSkills.saveNBTData(compound);
		playerSongs.saveNBTData(compound);
		compound.setFloat("zssCurrentMagic", mp);
		compound.setIntArray("zssStats", ArrayUtils.toPrimitive(playerStats.values().toArray(new Integer[playerStats.size()])));
		compound.setByte("ZSSGearReceived", receivedGear);
		if (lastBootsWorn != null) {
			NBTTagCompound tag = new NBTTagCompound();
			compound.setTag("lastBootsWorn", lastBootsWorn.writeToNBT(tag));
		}
		if (lastHelmWorn != null) {
			NBTTagCompound tag = new NBTTagCompound();
			compound.setTag("lastHelmWorn", lastHelmWorn.writeToNBT(tag));
		}
		compound.setInteger("slingshotMode", slingshotMode);
		compound.setInteger("skulltulaTokens", skulltulaTokens);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		playerSkills.loadNBTData(compound);
		playerSongs.loadNBTData(compound);
		mp = compound.getFloat("zssCurrentMagic");
		int[] stats = compound.getIntArray("zssStats");
		for (int i = 0; i < stats.length; ++i) {
			playerStats.put(Stats.values()[i], stats[i]);
		}
		receivedGear = compound.getByte("ZSSGearReceived");
		if (compound.hasKey("lastBootsWorn", Constants.NBT.TAG_COMPOUND)) {
			lastBootsWorn = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("lastBootsWorn"));
		}
		if (compound.hasKey("lastHelmWorn", Constants.NBT.TAG_COMPOUND)) {
			lastHelmWorn = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("lastHelmWorn"));
		}
		// For backwards compatibility:
		Item boots = Item.getItemById(compound.getInteger("lastBoots"));
		if (boots != null) {
			lastBootsWorn = new ItemStack(boots);
		}
		Item helm = Item.getItemById(compound.getInteger("lastHelm"));
		if (helm != null) {
			lastHelmWorn = new ItemStack(helm);
		}
		// For backwards compatibility:
		slingshotMode = compound.getInteger("slingshotMode");
		skulltulaTokens = compound.getInteger("skulltulaTokens");
	}
}

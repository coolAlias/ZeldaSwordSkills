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

package zeldaswordskills.entity;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.ArrayUtils;

import zeldaswordskills.api.item.ArmorIndex;
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
import zeldaswordskills.network.client.SyncPlayerInfoPacket;
import zeldaswordskills.util.PlayerUtils;

public class ZSSPlayerInfo implements IExtendedEntityProperties
{
	private static final String EXT_PROP_NAME = "ZSSPlayerInfo";

	private final EntityPlayer player;

	private final ZSSPlayerSkills playerSkills;

	private final ZSSPlayerSongs playerSongs;

	/** Last ridden entity so it can be set after player is no longer riding */
	private Entity lastRidden;

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

	/** The last mask borrowed from the Happy Mask Salesman */
	private Item borrowedMask = null;

	/** Current stage in the Mask trading sequence */
	private int maskStage = 0;

	/** Maximum number of skulltula tokens which can be turned in */
	public static final int MAX_SKULLTULA_TOKENS = 100;

	/** Number of Gold Skulltula Tokens this player has turned in */
	private int skulltulaTokens = 0;

	/** [Hero's Bow] Stores the currently nocked arrow in order to avoid the graphical glitch caused by writing to the stack's NBT */
	private ItemStack arrowStack = null;

	/** [Hero's Bow] Part of the graphical glitch fix: Whether the player retrieved a bomb arrow via getAutoBombArrow */
	public boolean hasAutoBombArrow = false;

	/** [Slingshot] Current mode index */
	public int slingshotMode = 0;

	/** Reduces fall damage next impact; used for Rising Cut */
	public float reduceFallAmount = 0.0F;

	public ZSSPlayerInfo(EntityPlayer player) {
		this.player = player;
		playerSkills = new ZSSPlayerSkills(this, player);
		playerSongs = new ZSSPlayerSongs(player);
		initStats();
	}

	@Override
	public void init(Entity entity, World world) {}

	private void initStats() {
		for (Stats stat : Stats.values()) {
			playerStats.put(stat, 0);
		}
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
		setFlag(IS_NAYRU_ACTIVE, true);
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
		if (lastHelmWorn != null) {
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

	/** Returns the last mask borrowed, or null if no mask has been borrowed */
	public Item getBorrowedMask() {
		return borrowedMask;
	}

	/** Sets the mask that the player has borrowed */
	public void setBorrowedMask(Item item) {
		borrowedMask = item;
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

	/**
	 * Returns true if the player is able to turn in more Skulltula Tokens
	 */
	public boolean canIncrementSkulltulaTokens() {
		return skulltulaTokens < MAX_SKULLTULA_TOKENS;
	}

	/**
	 * Returns number of skulltula tokens this player has given to the Cursed Man
	 */
	public int getSkulltulaTokens() {
		return skulltulaTokens;
	}

	/**
	 * Attempts to turn in (i.e. consume) a skulltula token, returning true on success.
	 */
	public boolean incrementSkulltulaTokens() {
		if (canIncrementSkulltulaTokens() && PlayerUtils.consumeHeldItem(player, ZSSItems.skulltulaToken, 1)) {
			++skulltulaTokens;
			return true;
		}
		return false;
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
				&& !player.capabilities.isFlying && player.worldObj.getWorldTime() % 2 == 0) {
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
		if (player.ticksExisted % 16 == 0) {
			player.addExhaustion(3.0F);
		}
		if (player.getFoodStats().getFoodLevel() == 0) {
			setFlag(IS_NAYRU_ACTIVE, false);
		} else {
			PacketDispatcher.sendToAllAround(new SpawnNayruParticlesPacket(player), player, 64.0D);
		}
	}

	/** Used to register these extended properties for the player during EntityConstructing event */
	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(EXT_PROP_NAME, new ZSSPlayerInfo(player));
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
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		playerSkills.saveNBTData(compound);
		playerSongs.saveNBTData(compound);
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
		compound.setInteger("borrowedMask", borrowedMask != null ? Item.getIdFromItem(borrowedMask) : -1);
		compound.setInteger("maskStage", maskStage);
		compound.setInteger("slingshotMode", slingshotMode);
		compound.setInteger("skulltulaTokens", skulltulaTokens);

	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		playerSkills.loadNBTData(compound);
		playerSongs.loadNBTData(compound);
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
		int maskID = compound.getInteger("borrowedMask");
		borrowedMask = maskID > -1 ? Item.getItemById(maskID) : null;
		maskStage = compound.getInteger("maskStage");
		slingshotMode = compound.getInteger("slingshotMode");
		skulltulaTokens = compound.getInteger("skulltulaTokens");
	}
}

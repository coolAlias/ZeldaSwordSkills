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

package zeldaswordskills.entity;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.entity.DirtyEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.entity.mobs.EntityChu.ChuType;
import zeldaswordskills.entity.player.ZSSPlayerWallet;
import zeldaswordskills.entity.player.quests.QuestMaskSales;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * Additional data for handling extended villager trading system.
 *
 */
public class ZSSVillagerInfo implements IExtendedEntityProperties
{
	private static final String SAVE_KEY = "zssVillagerInfo";

	/** Used for masks when the villager is not interested */
	private static final int NONE = -1;

	/** The villager to which these properties belong */
	private final EntityVillager villager;

	/** The IRupeeMerchant handler for vanilla villagers */
	private final VanillaRupeeMerchant rupeeMerchant;

	/** Additional save data for villager trading */
	private NBTTagCompound data;

	/** The index of the mask that this villager wants, or NONE; initial value is masks.size() as a flag */
	private int desiredMask;

	/** Used for the Cursed Man's quest's potentially recurring reward */
	private long nextSkulltulaReward;

	/** Trade mapping for a single Treasure enum type key to the full trade recipe output */
	private static final Map<Treasures, MerchantRecipe> treasureTrades = new EnumMap<Treasures, MerchantRecipe>(Treasures.class);

	/** Maps the villager profession capable of trading for the treasure trade to the Treasure enum type key */
	private static final Map<Treasures, Integer> treasureVillager = new EnumMap<Treasures, Integer>(Treasures.class);

	/** Temporarily stores nearby village when needed for mating */
	private Village village;

	/** Temporarily stores this villager's current mate */
	private EntityVillager mate = null;

	/** Temporarily stores the time remaining before a child will be born */
	private int matingTime = 0;

	public ZSSVillagerInfo(EntityVillager villager) {
		this.villager = villager;
		this.rupeeMerchant = new VanillaRupeeMerchant(villager);
		data = new NBTTagCompound();
		desiredMask = QuestMaskSales.MASKS.size();
	}

	public static final void register(EntityVillager villager) {
		villager.registerExtendedProperties(SAVE_KEY, new ZSSVillagerInfo(villager));
	}

	public static final ZSSVillagerInfo get(EntityVillager villager) {
		return (ZSSVillagerInfo) villager.getExtendedProperties(SAVE_KEY);
	}

	/**
	 * Returns this villager's IRupeeMerchant handler
	 */
	public IRupeeMerchant getRupeeMerchant() {
		return this.rupeeMerchant;
	}

	/**
	 * Returns the mask that this villager desires, or null if none
	 */
	public Item getMaskDesired() {
		if (desiredMask == QuestMaskSales.MASKS.size()) {
			if (villager.worldObj.rand.nextFloat() < Config.getMaskBuyChance()) {
				desiredMask = villager.worldObj.rand.nextInt(QuestMaskSales.MASKS.size());
			} else {
				desiredMask = NONE;
			}
		}
		return (desiredMask != NONE ? QuestMaskSales.getMask(desiredMask) : null);
	}

	public long getNextSkulltulaReward() {
		return this.nextSkulltulaReward;
	}

	public void setNextSkulltulaReward(long nextSkulltulaReward) {
		this.nextSkulltulaReward = nextSkulltulaReward;
	}

	/** Returns true if this villager is any type of Hunter */
	public boolean isHunter() {
		return !villager.isChild() && EnumVillager.BUTCHER.is(villager) && villager.getCustomNameTag() != null && villager.getCustomNameTag().contains("Hunter");
	}

	/** Returns true if this villager is a Monster Hunter */
	public boolean isMonsterHunter() {
		return isHunter() && villager.getCustomNameTag().equals("Monster Hunter");
	}

	/**
	 * Handles hunter trading interactions for the player
	 * @param player
	 * @param stack  The stack to trade, one of which will be consumed if successful
	 * @param reward The number of rupees to reward the player
	 * @param isLeftClick True if the player is using left-click (i.e. turning in the trade)
	 */
	public void handleHunterTrade(EntityPlayer player, ItemStack stack, int reward, boolean isLeftClick) {
		if (isLeftClick) {
			if (ZSSPlayerWallet.get(player).addRupees(reward)) {
				if (PlayerUtils.consumeHeldItem(player, stack.getItem(), stack.getItemDamage(), 1)) {
					PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.hunter.trade." + player.worldObj.rand.nextInt(4));
				} else {
					ZSSPlayerWallet.get(player).spendRupees(reward); // take rupees back
				}
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.trade.wallet.insufficient");
			}
		} else {
			new TimedChatDialogue(player, 0, 1000,
					new ChatComponentTranslation("chat.zss.treasure.hunter.interested.0", new ChatComponentTranslation(stack.getUnlocalizedName() + ".name")),
					new ChatComponentTranslation("chat.zss.treasure.hunter.interested.1", reward));
		}
	}

	/**
	 * Returns what the villager is willing to trade for the treasure, if anything.
	 * Note that the first item to buy is always a stack containing one of the treasure.
	 * @param treasure the treasure to be traded
	 * @return may be null
	 */
	public MerchantRecipe getTreasureTrade(Treasures treasure) {
		if (treasureTrades.containsKey(treasure) && treasureVillager.get(treasure) == villager.getProfession()) {
			return treasureTrades.get(treasure);
		}
		return null;
	}

	/**
	 * Returns true if the villager is interested in the specified treasure
	 * @param stack the player's currently held item, which should be the treasure item
	 */
	public boolean isInterested(Treasures treasure, ItemStack stack) {
		return treasureTrades.containsKey(treasure) && treasureVillager.get(treasure) == villager.getProfession();
	}

	/** Adds the given amount of chu jellies to the current amount */
	public void addJelly(ChuType type, int amount) {
		data.setInteger(("jelliesReceived" + type.ordinal()), getJelliesReceived(type) + amount);
	}

	/** Returns the number of jellies of this type that the villager has received */
	public int getJelliesReceived(ChuType type) {
		return (data.hasKey("jelliesReceived" + type.ordinal()) ? data.getInteger("jelliesReceived" + type.ordinal()) : 0);
	}

	/** Returns whether this villager deals at all in Chu Jellies */
	public boolean isChuTrader() {
		return !villager.isChild() && EnumVillager.LIBRARIAN.is(villager) && villager.getCustomNameTag().contains("Doc");
	}

	/**
	 * Checks that this villager has received enough jellies to trade; if not, attempts
	 * to fulfill the quota from the stack provided
	 * @return true if the quota was met
	 */
	public boolean canSellType(ChuType type, ItemStack stack) {
		int jellies = getJelliesReceived(type);
		while (jellies < 15 && stack.stackSize > 0) {
			--stack.stackSize;
			++jellies;
		}
		data.setInteger(("jelliesReceived" + type.ordinal()), jellies);
		return jellies >= 15;
	}

	/** True if the villager is currently mating */
	private boolean isMating() {
		return matingTime > 0 && villager.getGrowingAge() == 0 && areSufficientDoors();
	}

	/**
	 * Sets this villager into forced mating mode if possible
	 */
	public void setMating() {
		if (villager.getGrowingAge() == 0 && !isMating()) {
			village = DirtyEntityAccessor.getVillageObject(this.villager);
			if (areSufficientDoors()) {
				Entity e = villager.worldObj.findNearestEntityWithinAABB(EntityVillager.class, villager.boundingBox.expand(8.0D, 3.0D, 8.0D), villager);
				if (e != null && ((EntityVillager) e).getGrowingAge() == 0) {
					mate = (EntityVillager) e;
					matingTime = 300;
					villager.setMating(true);
				}
			}
		}
	}

	private void updateMating() {
		if (isMating()) {
			--matingTime;
			villager.getLookHelper().setLookPositionWithEntity(mate, 10.0F, 30.0F);
			if (villager.getDistanceSqToEntity(mate) > 2.25D) {
				villager.getNavigator().tryMoveToEntityLiving(mate, 0.25D);
			} else if (matingTime == 0 && mate.isMating()) {
				giveBirth();
				resetMating();
			}
			if (villager.getRNG().nextInt(35) == 0) {
				villager.worldObj.setEntityState(villager, (byte) 12);
			}
		}
	}

	/** Returns true if there are sufficient doors, up to one villager per door (3x vanilla size) */
	private boolean areSufficientDoors() {
		return village != null ? (village.getNumVillagers() < village.getNumVillageDoors()) : false;
	}

	/** Resets the mating fields */
	private void resetMating() {
		villager.setMating(false);
		mate = null;
	}

	private void giveBirth() {
		EntityVillager baby = villager.createChild(mate);
		mate.setGrowingAge(6000);
		villager.setGrowingAge(6000);
		baby.setGrowingAge(-24000);
		baby.setLocationAndAngles(villager.posX, villager.posY, villager.posZ, 0.0F, 0.0F);
		villager.worldObj.spawnEntityInWorld(baby);
		villager.worldObj.setEntityState(baby, (byte) 12);
	}

	/**
	 * Call each update tick
	 */
	public void onUpdate() {
		updateMating();
		this.rupeeMerchant.onUpdate();
	}

	@Override
	public void init(Entity entity, World world) {}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		compound.setTag(SAVE_KEY, data);
		compound.setInteger("desiredMask", desiredMask);
		compound.setLong("NextSkulltulaReward", nextSkulltulaReward);
		this.rupeeMerchant.writeToNBT(compound);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		data = (compound.hasKey(SAVE_KEY) ? compound.getCompoundTag(SAVE_KEY) : new NBTTagCompound());
		desiredMask = compound.getInteger("desiredMask");
		nextSkulltulaReward = compound.getLong("NextSkulltulaReward");
		this.rupeeMerchant.readFromNBT(compound);
	}

	/**
	 * Adds every component involved in a treasure trade
	 * @param treasure the treasure to be traded
	 * @param villager the profession of the villager allowed to have this trade, or null for no requirement
	 * @param required the itemstack required for the trade in addition to the Treasure
	 * @param output the itemstack to be traded for
	 */
	private static final void addTreasureTrade(Treasures treasure, EnumVillager villager, ItemStack required, ItemStack output) {
		treasureVillager.put(treasure, (villager != null ? villager.ordinal() : null));
		treasureTrades.put(treasure, new MerchantRecipe(new ItemStack(ZSSItems.treasure,1,treasure.ordinal()), required, output));
	}

	/**
	 * Initializes all custom trade maps for custom villager trades
	 */
	public static void initTrades() {
		addTreasureTrade(Treasures.EVIL_CRYSTAL,EnumVillager.PRIEST,new ItemStack(ZSSItems.arrowLight,16),new ItemStack(ZSSItems.crystalSpirit));
	}
}

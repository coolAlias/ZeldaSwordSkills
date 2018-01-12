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

package zeldaswordskills.entity.player.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.api.entity.EnumVillager;
import zeldaswordskills.entity.npc.EntityGoron;
import zeldaswordskills.item.ItemTreasure;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;
import zeldaswordskills.util.VillagerDescription;

import com.google.common.collect.ImmutableSet;

public final class QuestBiggoronSword extends QuestBase
{
	/** Set of all trades in order of required completion */
	private static final ImmutableSet<TradeData> TRADES = new ImmutableSet.Builder<TradeData>()
			.add(new TradeData(Treasures.TENTACLE, new VillagerDescription("Talon", EntityVillager.class, EnumVillager.FARMER.ordinal(), true), getTreasure(Treasures.POCKET_EGG)))
			.add(new TradeData(Treasures.POCKET_EGG, new VillagerDescription("Cucco Lady", EntityVillager.class, EnumVillager.FARMER.ordinal()), getTreasure(Treasures.COJIRO)))
			.add(new TradeData(Treasures.COJIRO, new VillagerDescription("Grog", EntityVillager.class, EnumVillager.BUTCHER.ordinal()), getTreasure(Treasures.ODD_MUSHROOM)))
			.add(new TradeData(Treasures.ODD_MUSHROOM, new VillagerDescription("Old Hag", EntityVillager.class, EnumVillager.LIBRARIAN.ordinal()), getTreasure(Treasures.ODD_POTION)))
			.add(new TradeData(Treasures.ODD_POTION, new VillagerDescription("Grog", EntityVillager.class, EnumVillager.BUTCHER.ordinal()), getTreasure(Treasures.POACHER_SAW)))
			.add(new TradeData(Treasures.POACHER_SAW, new VillagerDescription("Mutoh", EntityVillager.class, EnumVillager.BLACKSMITH.ordinal()), getTreasure(Treasures.GORON_SWORD)))
			.add(new TradeData(Treasures.GORON_SWORD, new VillagerDescription("Biggoron", EntityGoron.class, EnumVillager.BLACKSMITH.ordinal()), getTreasure(Treasures.PRESCRIPTION)))
			.add(new TradeData(Treasures.PRESCRIPTION, new VillagerDescription("King Zora", EntityVillager.class, EnumVillager.PRIEST.ordinal()), getTreasure(Treasures.EYEBALL_FROG)))
			.add(new TradeData(Treasures.EYEBALL_FROG, new VillagerDescription("Lake Scientist", EntityVillager.class, EnumVillager.LIBRARIAN.ordinal()), getTreasure(Treasures.EYE_DROPS)))
			.add(new TradeData(Treasures.EYE_DROPS, new VillagerDescription("Biggoron", EntityGoron.class), getTreasure(Treasures.CLAIM_CHECK)))
			.add(new TradeData(Treasures.CLAIM_CHECK, new VillagerDescription("Biggoron", EntityGoron.class), new ItemStack(ZSSItems.swordBiggoron)))
			.build();
	private static final ItemStack getTreasure(Treasures treasure) {
		return new ItemStack(ZSSItems.treasure, 1, treasure.ordinal());
	}

	/** Index of current trade requiring completion */
	private int tradeIndex;

	private TradeData getCurrentTrade() {
		return (tradeIndex < TRADES.size() ? TRADES.asList().get(tradeIndex) : null);
	}

	public QuestBiggoronSword() {
		set(FLAG_BEGIN); // automatically begins
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // never called anyway
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		return false; // completed from #update
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		forceComplete(player, data);
		return false;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		player.triggerAchievement(ZSSAchievements.treasureBiggoron);
		tradeIndex = TRADES.size();
		set(FLAG_COMPLETE);
	}

	/**
	 * @param data[0] must be the entity with which the player is interacting
	 * @param data[1] should be a Boolean value: true if this is a left-click interaction
	 */
	@Override
	public boolean update(EntityPlayer player, Object... data) {
		if (data == null || data.length < 2 || !(data[0] instanceof Entity) || !(data[1] instanceof Boolean)) {
			return false;
		}
		ItemStack stack = player.getHeldItem();
		if (stack == null || !(stack.getItem() instanceof ItemTreasure)) {
			return false;
		}
		TradeData trade = getCurrentTrade();
		if (trade != null && trade.villager.matches((Entity) data[0]) && trade.treasure == Treasures.byDamage(stack.getItemDamage())) {
			if ((Boolean) data[1]) { // left click interaction
				if (trade.treasure != Treasures.CLAIM_CHECK || checkClaim(stack, player)) {
					if (!PlayerUtils.consumeHeldItem(player, stack.getItem(), stack.getItemDamage(), 1)) {
						return false;
					}
					rewardAndChat(player, trade, true);
					if (tradeIndex < TRADES.size()) { ++tradeIndex; }
					return true;
				} else if (trade.treasure == Treasures.CLAIM_CHECK) {
					return true; // #checkClaim would have sent a chat, so cancel interaction
				}
			} else { // right click interaction
				if (trade.treasure != Treasures.CLAIM_CHECK || checkClaim(stack, player)) {
					PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure." + trade.treasure.name + ".show");
				}
				return true;
			}
		}
		// uninterested chat handled by ItemTreasure click methods
		return false;
	}

	private void rewardAndChat(EntityPlayer player, TradeData trade, boolean first) {
		ItemStack reward = trade.reward.copy();
		List<IChatComponent> chat = new ArrayList<IChatComponent>();
		if (first) {
			chat.add(new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".give.first"));
		} else if (trade.treasure == Treasures.CLAIM_CHECK) {
			player.addChatMessage(new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".give.repeat"));
			return; // CLAIM_CHECK reward can only be received once - additional swords must be purchased
		} else {
			chat.add(new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".give.repeat"));
		}
		chat.add(new ChatComponentTranslation("chat.zss.treasure.generic.received", new ChatComponentTranslation(reward.getUnlocalizedName() + ".name")));
		if (onCompletedTrade(player, trade, reward) && first) {
			chat.add(new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".give.next"));
		}
		new TimedChatDialogue(player, chat.toArray(new IChatComponent[chat.size()]));
		new TimedAddItem(player, reward, 1250, Sounds.SUCCESS);
	}

	/**
	 * Call for CLAIM_CHECK only - returns true if 2+ days have passed; if false, may
	 * have sent some dialogue. Will create / initialize stack NBT tag if missing.
	 */
	private boolean checkClaim(ItemStack stack, EntityPlayer player) {
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		if (!stack.getTagCompound().hasKey("finishDate")) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.claim_check.no_nbt");
			stack.getTagCompound().setLong("finishDate", player.worldObj.getTotalWorldTime() + 48000);
			return false;
		} else if (player.worldObj.getTotalWorldTime() < stack.getTagCompound().getLong("finishDate")) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.treasure.claim_check.early");
			return false;
		}
		return true;
	}

	/**
	 * Returns true if 'next' portion of the chat dialogue should be added now
	 */
	private boolean onCompletedTrade(EntityPlayer player, TradeData trade, ItemStack reward) {
		switch (trade.treasure) {
		case TENTACLE: player.triggerAchievement(ZSSAchievements.treasureFirst); return false;
		case POCKET_EGG: player.triggerAchievement(ZSSAchievements.treasureSecond); return false;
		case EYE_DROPS: // traded eye drops for claim check: set date for claiming finished sword
			reward.setTagCompound(new NBTTagCompound());
			reward.getTagCompound().setLong("finishDate", player.worldObj.getTotalWorldTime() + 48000);
			return true;
		case CLAIM_CHECK: onComplete(player); return true;
		default: return true;
		}
	}

	/**
	 * Called from ItemTreasure's left- and right-click methods
	 * @param data[0] must be the entity with which the player is interacting
	 * @param data[1] should be a Boolean value: true if this is a left-click interaction
	 */
	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		if (data == null || data.length < 1 || !(data[0] instanceof Entity)) {
			return null;
		}
		Entity entity = (Entity) data[0];
		boolean isChild = (entity instanceof EntityAgeable && ((EntityAgeable) entity).isChild()); // set later as a flag
		Treasures treasure = null;
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemTreasure) {
			treasure = Treasures.byDamage(stack.getItemDamage());
		}
		boolean isLeftClick = (data.length > 1 && data[1] instanceof Boolean && (Boolean) data[1]);
		IChatComponent chat = null;
		// index is 1 greater than normal to compensate for reverse traversal, +1 more to also check next trade entry
		int index = Math.min(tradeIndex + 2, TRADES.size());
		ListIterator<TradeData> iterator = TRADES.asList().listIterator(index);
		for (int i = index - 1; iterator.hasPrevious(); --i) { // subtract 1 to compensate for call to #previous
			TradeData trade = iterator.previous();
			// Only allow 'ageable' variants for children, i.e. "Talon"
			if (trade.villager.matches(entity, false) && (!isChild || trade.villager.isChild)) {
				boolean exactMatch = trade.villager.matchChild(entity);
				if (i + 1 == tradeIndex && trade.reward.getItem() instanceof ItemTreasure && treasure == Treasures.byDamage(trade.reward.getItemDamage())) {
					// matched entity from previous stage while holding the reward from that stage - gives hint for 'next' (i.e. current) stage
					return new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".hint.next" + (exactMatch ? "" : ".ageable"));
				} else if (i == tradeIndex && (trade.treasure != treasure || !exactMatch)) {
					// matched entity from current stage, but treasure or isChild mismatch - give hint
					return new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".hint.current" + (exactMatch ? "" : ".ageable"));
				} else if (i - 1 == tradeIndex && trade.treasure != treasure && !isComplete(player)) { 
					// matched entity from next stage; give either a generic or specific hint, depending on held item
					return new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".hint.previous" + (treasure == iterator.previous().treasure ? ".match" : ""));
				} else if (i < tradeIndex) {
					// matched entity from completed stage, any treasure item; check for repeat or say 'thanks'
					if (isLeftClick && trade.treasure == treasure && exactMatch) { // player trying to repeat trade
						if (PlayerUtils.consumeHeldItem(player, stack.getItem(), stack.getItemDamage(), 1)) {
							rewardAndChat(player, trade, false);
							return new ChatComponentTranslation(""); // empty chat to prevent further interactions
						}
					}
					return new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + ".thanks" + (exactMatch ? "" : ".ageable"));
				}
				return null; // always terminate loop once an exact match is found
			} else if (trade.treasure == treasure && trade.villager.matchClassAndName(entity) && !trade.villager.matchProfession(entity)) {
				// everything matches but profession - help the player out with a hint
				EnumVillager ev = EnumVillager.values()[trade.villager.profession % EnumVillager.values().length];
				return new ChatComponentTranslation("chat.zss.treasure.generic.profession." + (isChild ? "child" : ev.unlocalizedName), new ChatComponentTranslation("entity.villager.profession." + ev.unlocalizedName));
			} else if (trade.treasure == treasure && entity instanceof EntityVillager && checkVillagerTrade(stack, player, (EntityVillager) entity, isLeftClick)) {
				return new ChatComponentTranslation(""); // empty chat to prevent further action (e.g. to stop villager trading GUI from opening)
			} else if (trade.treasure == treasure && !isChild) { // children are handled in ItemTreasure
				// hint when interacting with random villagers, at most once per loop
				if (rand.nextInt(8) < 3) {
					chat = new ChatComponentTranslation("chat.zss.treasure." + trade.treasure.name + (isLeftClick ? ".give" : ".show") + ".random");
				} else {
					chat = new ChatComponentTranslation("chat.zss.treasure.generic." + (isLeftClick ? "give." : "show.") + rand.nextInt(4));
				}
			}
		}
		return chat;
	}

	private boolean checkVillagerTrade(ItemStack stack, EntityPlayer player, EntityVillager villager, boolean isLeftClick) {
		Treasures treasure = Treasures.byDamage(stack.getItemDamage());
		return treasure.canSell() && ((ItemTreasure) stack.getItem()).handleVillagerTrade(stack, player, villager, isLeftClick);
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("tradeIndex", tradeIndex);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		tradeIndex = compound.getInteger("tradeIndex");
	}

	private static class TradeData {
		/** Treasure needed to complete this step */
		public final Treasures treasure;
		/** Description of villager interested in this trade */
		public final VillagerDescription villager;
		/** Reward for completing this step */
		public final ItemStack reward;
		public TradeData(Treasures treasure, VillagerDescription villager, ItemStack reward) {
			this.treasure = treasure;
			this.villager = villager;
			this.reward = reward;
		}
	}
}

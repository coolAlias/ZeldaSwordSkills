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

import java.util.EnumMap;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.api.entity.BombType;
import zeldaswordskills.api.entity.merchant.RupeeTrade;
import zeldaswordskills.entity.npc.EntityNpcBarnes;
import zeldaswordskills.item.ItemBomb;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Quest to track player's progress with Barnes' Bomb Shop trades.
 * 
 */
public final class QuestBombTrades extends QuestBase
{
	/** Track number of times each single-stack bomb trade has been used by the player */
	private final EnumMap<BombType, Integer> tradeUses = Maps.newEnumMap(BombType.class);

	/** Track number of times each bomb pack trade has been used by the player */
	private final EnumMap<BombType, Integer> packUses = Maps.newEnumMap(BombType.class);

	/**
	 * Tracks current progress; apply BombType's bitMask to determine if that trade
	 * is unlocked and (bitMask << BombType.values().length) for the bomb pack. 
	 */
	private int stage = BombType.BOMB_STANDARD.bitMask;

	/**
	 * Quest automatically begun when created
	 */
	public QuestBombTrades() {
		this.set(QuestBase.FLAG_BEGIN);
	}

	/**
	 * Checks whether the player has unlocked the bomb trade
	 * @param type The type of bomb trade to check for
	 * @param pack True to check if the bomb pack trade has been unlocked
	 * @return True if the player has unlocked the requested trade type
	 */
	public boolean isTradeUnlocked(BombType type, boolean pack) {
		int flag = (pack ? (type.bitMask << BombType.values().length) : type.bitMask);
		return (this.stage & flag) == flag;
	}

	/**
	 * Unlocks the specified bomb trade
	 * @param type The type of bomb trade to check for
	 * @param pack True to check if the bomb pack trade has been unlocked
	 * @return True if the trade was newly unlocked, or false if it was already unlocked
	 */
	protected boolean unlockTrade(BombType type, boolean pack) {
		int flag = (pack ? (type.bitMask << BombType.values().length) : type.bitMask);
		boolean unlocked = (this.stage & flag) != flag;
		this.stage |= flag;
		return unlocked;
	}

	/**
	 * Returns the number of times this player has used the standard or pack trade for the bomb type
	 * @param type The type of bomb for which to get the number of trades
	 * @param pack True to check if the bomb pack trade has been unlocked
	 */
	public int getTradeUses(BombType type, boolean pack) {
		Integer uses = (pack ? this.packUses.get(type) : this.tradeUses.get(type));
		return (uses == null ? 0 : uses);
	}

	/**
	 * Call each time a trade is used to allow quest to track number of times
	 * the standard or pack bomb trades have been used.
	 * @param trade
	 */
	public void useTrade(RupeeTrade trade) {
		ItemStack stack = trade.getTradeItem();
		if (stack.getItem() == ZSSItems.bomb) {
			BombType type = ItemBomb.getType(stack);
			if (stack.stackSize > 1) {
				this.packUses.put(type, this.getTradeUses(type, true) + 1);
			} else {
				this.tradeUses.put(type, this.getTradeUses(type, false) + 1);
			}
		}
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // always already begun
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		int max = this.getMaxStage();
		return (super.canComplete(player) && (this.stage & max) == max);
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		return false; // completed via #update and #forceComplete
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		this.stage = this.getMaxStage();
		this.completeBombBagTradingQuest(player);
		this.set(FLAG_COMPLETE);
	}

	/**
	 * Adds and completes the single-use bomb bag trading quest if not already done
	 */
	private void completeBombBagTradingQuest(EntityPlayer player) {
		ZSSQuests quests = ZSSQuests.get(player);
		IQuest quest = quests.add(new QuestBombBagTrade());
		quest.forceComplete(player);
	}

	private int getMaxStage() {
		int i = 0;
		for (BombType type : BombType.values()) {
			i |= type.bitMask;
			i |= (type.bitMask << BombType.values().length);
		}
		return i;
	}

	/**
	 * @param data Expects data[0] to be an instance of EntityNpcBarnes
	 */
	@Override
	public boolean update(EntityPlayer player, Object... data) {
		if (data == null || data.length < 1 || !(data[0] instanceof EntityNpcBarnes)) {
			return false;
		}
		String chat = null;
		ItemStack stack = player.getHeldItem();
		if (stack == null) {
			// nothing to do
		} else if (stack.getItem() == Items.fish) {
			if (this.unlockTrade(BombType.BOMB_WATER, false)) {
				chat = "chat.zss.npc.barnes.trade.water";
			}
		} else if (stack.getItem() == Items.magma_cream) {
			if (!this.unlockTrade(BombType.BOMB_FIRE, false)) {
				chat = "chat.zss.npc.barnes.trade.fire";
			}
		}
		// Check if any bomb packs can be unlocked and inform the player
		if (chat == null) {
			for (BombType type : BombType.values()) {
				int required = 10; // TODO config?
				if (this.getTradeUses(type, false) >= required && this.unlockTrade(type, true)) {
					chat = "chat.zss.npc.barnes.trade." + type.unlocalizedName + ".pack";
					break;
				}
			}
		}
		// Check if quest can be completed when no other notifications pending
		if (chat == null && this.canComplete(player)) {
			this.forceComplete(player);
			chat = "chat.zss.npc.barnes.trade.complete";
		}
		if (chat != null) {
			PlayerUtils.sendTranslatedChat(player, chat);
			return true;
		}
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		return null; // hints handled externally
	}

	@Override
	public boolean requiresSync() {
		return true; // used to populated trades which can be used during interaction event on both sides
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("stage", this.stage);
		for (BombType type : BombType.values()) {
			Integer uses = this.tradeUses.get(type);
			if (uses != null) {
				compound.setInteger(type.unlocalizedName, uses);
			}
			uses = this.packUses.get(type);
			if (uses != null) {
				compound.setInteger(type.unlocalizedName + "_pack", uses);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.stage = compound.getInteger("stage");
		for (BombType type : BombType.values()) {
			int uses = compound.getInteger(type.unlocalizedName);
			if (uses > 0) {
				this.tradeUses.put(type, uses);
			}
			uses = compound.getInteger(type.unlocalizedName + "_pack");
			if (uses > 0) {
				this.packUses.put(type, uses);
			}
		}
	}
}

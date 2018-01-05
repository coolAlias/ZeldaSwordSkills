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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.item.ItemMask;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

public final class QuestMaskSales extends QuestBase
{
	/** Mapping of masks to give for each quest stage */
	public static final ImmutableSet<Item> MASKS = new ImmutableSet.Builder<Item>()
			.add(ZSSItems.maskKeaton)
			.add(ZSSItems.maskSkull)
			.add(ZSSItems.maskSpooky)
			.add(ZSSItems.maskScents)
			.add(ZSSItems.maskCouples)
			.add(ZSSItems.maskBunny)
			.build();

	/** Number of stages per mask */
	private static final int NUM_STAGES = 3;

	/** Current stage of the quest, increments by one each step */
	private int currentStage;

	public QuestMaskSales() {}

	/**
	 * Returns the ItemMask stored at i, or null
	 */
	public static Item getMask(int i) {
		if (i < 0 || i >= MASKS.size()) {
			return null;
		}
		return MASKS.asList().get(i);
	}

	/**
	 * Returns the mask which the player is currently responsible for selling, if any
	 */
	public Item getCurrentMask() {
		return getMask(currentStage / NUM_STAGES);
	}

	/**
	 * Return true if the mask has already been sold by the quest owner
	 */
	public boolean hasSold(Item mask) {
		ImmutableList<Item> list = MASKS.asList();
		for (int i = 0; i < list.size(); ++i) {
			if (mask == list.get(i)) {
				return currentStage >= ((i * NUM_STAGES) + 2); // stage 2 is sold
			}
		}
		return false;
	}

	@Override
	public boolean canBegin(EntityPlayer player) {
		return super.canBegin(player) && ZSSQuests.get(player).hasCompleted(QuestMaskShop.class);
	}

	/**
	 * @param data Expects data[0] to be an EntityNpcMaskTrader
	 */
	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		if (data == null || data.length < 1 || !(data[0] instanceof EntityNpcMaskTrader)) {
			return false;
		}
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.begin.0"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.begin.1"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.begin.2"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.begin.3"));
		new TimedAddItem(player, new ItemStack(getMask(0)), 4000);
		currentStage = 1;
		return true;
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		return super.canComplete(player) && currentStage >= (MASKS.size() * NUM_STAGES);
	}

	/**
	 * @param data Expects data[0] to be an EntityNpcMaskTrader
	 */
	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		if (data == null || data.length < 1 || !(data[0] instanceof EntityNpcMaskTrader)) {
			return false;
		}
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.complete.0"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.complete.1"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.complete.2"),
				new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.complete.3"));
		new TimedAddItem(player, new ItemStack(ZSSItems.maskTruth), 4000);
		ZSSQuests.get(player).setBorrowedMask(ZSSItems.maskTruth);
		player.triggerAchievement(ZSSAchievements.maskShop);
		return true;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		currentStage = (MASKS.size() * NUM_STAGES);
		set(FLAG_COMPLETE);
		player.triggerAchievement(ZSSAchievements.maskShop);
	}

	/**
	 * @param data Expects data[0] to be set to either an EntityNpcMaskTrader or an ItemMask
	 */
	@Override
	public boolean update(EntityPlayer player, Object... data) {
		Item mask = getCurrentMask();
		if (data == null || data.length < 1) {
			return false;
		} else if (data[0] instanceof EntityNpcMaskTrader) {
			switch (currentStage % NUM_STAGES) {
			case 0: // new mask
				new TimedChatDialogue(player,
						new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.next.0"),
						new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.next.1"));
				if (mask != null) {
					new TimedAddItem(player, new ItemStack(mask), 2000);
				}
				++currentStage;
				return true;
			case 1: // still need to sell mask
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.sales.pep." + player.worldObj.rand.nextInt(4));
				return false;
			case 2: // need to pay for mask
				int price = (mask instanceof ItemMask ? ((ItemMask) mask).getBuyPrice() : 16);
				if (PlayerUtils.consumeInventoryItem(player, Items.emerald, price)) {
					PlayerUtils.playSound(player, Sounds.CASH_SALE, 1.0F, 1.0F);
					++currentStage;
					if (!complete(player)) { // checks #canComplete and #onComplete; if not complete, regular sale
						PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.sales.paid");
					}
				} else {
					new TimedChatDialogue(player,
							new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.penniless.0"),
							new ChatComponentTranslation("chat.zss.npc.mask_salesman.sales.penniless.1", price));
				}
				return true;
			}
		} else if (data[0] instanceof ItemMask) {
			ItemMask sell = (ItemMask) data[0];
			if (hasSold(sell)) { // already sold this mask
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.sales.repeat." + (data.length > 1 ? "pushy." : "") + player.worldObj.rand.nextInt(4));
			} else if (sell == mask && currentStage % NUM_STAGES == 1) { // selling mask to villager
				++currentStage;
				player.setCurrentItemOrArmor(0, new ItemStack(Items.emerald, ((ItemMask) mask).getSellPrice()));
				PlayerUtils.playSound(player, Sounds.CASH_SALE, 1.0F, 1.0F);
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.sales.sold." + player.worldObj.rand.nextInt(4));
				player.triggerAchievement(ZSSAchievements.maskSold);
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.sales.later." + player.worldObj.rand.nextInt(4));
			}
			return true;
		}
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		return null;
	}

	@Override
	public boolean requiresSync() {
		return true; // required as it is used client-side in EntityNpcMaskTrader#getSongToLearn
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("currentStage", currentStage);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		currentStage = compound.getInteger("currentStage");
	}
}

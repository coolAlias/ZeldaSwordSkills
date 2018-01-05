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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.item.ItemTreasure.Treasures;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

public class QuestMaskShop extends QuestBase
{
	public QuestMaskShop() {
		set(FLAG_BEGIN); // auto-begin when added
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false;
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		// This quest is special in that it can be continually 'completed', i.e. villagers
		// can be converted into Mask Salesmen at any time by giving them Zelda's Letter
		return isHoldingZeldasLetter(player);
	}

	private boolean isHoldingZeldasLetter(EntityPlayer player) {
		ItemStack stack = player.getHeldItem();
		return stack != null && stack.getItem() == ZSSItems.treasure && Treasures.byDamage(stack.getItemDamage()) == Treasures.ZELDAS_LETTER;
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		if (PlayerUtils.consumeHeldItem(player, ZSSItems.treasure, Treasures.ZELDAS_LETTER.ordinal(), 1)) {
			PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
			player.triggerAchievement(ZSSAchievements.maskTrader);
			if (isComplete(player)) { // converted another mask salesman
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.mask_salesman.shop.open");
			} else { // first time opening up shop
				IChatComponent[] chat = new IChatComponent[5];
				for (int i = 0; i < 5; ++i) {
					chat[i] = new ChatComponentTranslation("chat.zss.npc.mask_salesman.shop.complete." + i);
				}
				new TimedChatDialogue(player, chat);
				ZSSQuests.get(player).add(new QuestMaskSales());
			}
			return true;
		}
		return false;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		set(FLAG_COMPLETE);
		player.triggerAchievement(ZSSAchievements.maskTrader);
		ZSSQuests.get(player).add(new QuestMaskSales());
	}

	@Override
	public boolean update(EntityPlayer player, Object... data) {
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		if (isComplete(player)) {
			if (!isHoldingZeldasLetter(player)) {
				return null;
			}
			boolean flag = ZSSQuests.get(player).hasCompleted(QuestMaskSales.class);
			return new ChatComponentTranslation("chat.zss.npc.mask_salesman.shop." + (flag ? "open" : "opening"));
		} else if (canComplete(player)) {
			return new ChatComponentTranslation("chat.zss.npc.mask_salesman.shop.hint.letter");
		} else if (ZSSQuests.get(player).hasCompleted(QuestZeldasLetter.class)) {
			return new ChatComponentTranslation("chat.zss.npc.mask_salesman.shop.hint.zelda");
		} else {
			return new ChatComponentTranslation("chat.zss.npc.mask_salesman.shop.hint." + rand.nextInt(4));
		}
	}

	@Override
	public boolean requiresSync() {
		return true; // required as it is used client-side in EntityNpcMaskTrader#getSongToLearn
	}
}

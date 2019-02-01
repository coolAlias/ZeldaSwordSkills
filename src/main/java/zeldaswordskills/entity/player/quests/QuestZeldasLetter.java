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

package zeldaswordskills.entity.player.quests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.item.ItemTreasure;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Quest is automatically begun and can be immediately completed by speaking to Zelda.
 *
 */
public final class QuestZeldasLetter extends QuestBase
{
	public QuestZeldasLetter() {
		set(FLAG_BEGIN); // quest is automatically begun
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // already begun
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		return !isComplete(player) && ZSSQuests.get(player).hasBegun(QuestMaskShop.class);
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		PlayerUtils.addItemToInventory(player, new ItemStack(ZSSItems.treasure, 1, ItemTreasure.Treasures.ZELDAS_LETTER.ordinal()));
		PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.letter.give");
		return true;
	}

	@Override
	public boolean isComplete(EntityPlayer player) {
		if (!isset(FLAG_COMPLETE) && ZSSQuests.get(player).hasCompleted(QuestMaskShop.class)) {
			forceComplete(player);
		}
		return super.isComplete(player);
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		set(FLAG_COMPLETE);
	}

	@Override
	public boolean update(EntityPlayer player, Object... data) {
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		ZSSQuests quests = ZSSQuests.get(player);
		if (!isComplete(player) && !quests.hasBegun(QuestMaskShop.class) && quests.hasCompleted(QuestPendants.class) && rand.nextInt(8) < 3) {
			return new ChatComponentTranslation("chat.zss.npc.zelda.letter.hint." + rand.nextInt(3));
		}
		return null;
	}
}

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
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * Straightforward quest begun after retrieving the Master Sword:
 * show Princess Zelda the Hero's Bow to unlock a special trade.
 *
 */
public class QuestLightArrows extends QuestBase {

	public QuestLightArrows() {
		set(FLAG_BEGIN); // quest is automatically begun
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return false; // already begun
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		ItemStack stack = player.getHeldItem();
		return stack != null && stack.getItem() == ZSSItems.heroBow && !isComplete(player) && ZSSQuests.get(player).hasCompleted(QuestMasterSword.class);
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
		if (hasBegun(player) && !isComplete(player) && rand.nextInt(8) < 3) {
			return new ChatComponentTranslation("chat.zss.npc.zelda.arrow_light.hint");
		}
		return null;
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		ItemStack reward = new ItemStack(ZSSItems.arrowLight, 8);
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.arrow_light.complete"),
				new ChatComponentTranslation("chat.zss.treasure.generic.received.plural", new ChatComponentTranslation(reward.getUnlocalizedName() + ".name"), reward.stackSize));
		new TimedAddItem(player, reward, 1250, Sounds.SUCCESS);
		return true;
	}
}

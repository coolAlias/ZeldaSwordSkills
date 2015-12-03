/**
    Copyright (C) <2016> <coolAlias>

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
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.item.ItemPendant;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * This quest is begun immediately after completing the quest to speak with Zelda.
 * 
 * The goal is to retrieve all 3 pendants, starting with the Pendant of Wisdom.
 * Once retrieved, they are returned and the quest for the Master Sword is begun.
 * 
 * This quest is begun after the player has retrieved all 3 Pendants and speaks
 * to Princess Zelda again. Zelda gives the pendants back to the player in order
 * for him to retrieve the Master Sword, proving that he is the hero reborn.
 * 
 * Upon completion, Zelda gives the player the Ocarina of Time, then gets whisked
 * away by Ganondorf. (TODO)
 *
 */
public final class QuestMasterSword extends QuestBase
{
	@Override
	public boolean canBegin(EntityPlayer player) {
		return super.canBegin(player) && ZSSQuests.get(player).hasCompleted(QuestPendants.class);
	}

	@Override
	public boolean begin(EntityPlayer player, Object... data) {
		if (!canBegin(player)) {
			return false;
		}
		for (ItemPendant.PendantType pendant : ItemPendant.PendantType.values()) {
			ItemStack stack = new ItemStack(ZSSItems.pendant, 1, pendant.ordinal());
			int delay = 3000 + (pendant.ordinal() * 250);
			String sound = (pendant == ItemPendant.PendantType.WISDOM ? Sounds.SUCCESS : null);
			new TimedAddItem(player, stack, delay, sound);
		}
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.begin.0"),
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.begin.1"),
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.begin.2"));
		set(FLAG_BEGIN);
		return true;
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		return true;
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.complete.0"),
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.complete.1"),
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.complete.2"),
				new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.complete.3"));
		ItemStack stack = new ItemStack(ZSSItems.instrument, 1, ItemInstrument.Instrument.OCARINA_TIME.ordinal());
		new TimedAddItem(player, stack, 3250, Sounds.SUCCESS);
		return true;
	}

	@Override
	public boolean canComplete(EntityPlayer player) {
		return super.canComplete(player) && PlayerUtils.isHoldingMasterSword(player);
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
			return new ChatComponentTranslation("chat.zss.npc.zelda.master_sword.hint." + rand.nextInt(3));
		}
		return null;
	}

	@Override
	public boolean requiresSync() {
		return true; // required as it is used client-side in EntityNpcZelda#getSongToLearn
	}
}

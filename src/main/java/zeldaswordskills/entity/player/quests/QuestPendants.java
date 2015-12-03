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
import zeldaswordskills.item.ItemPendant;
import zeldaswordskills.item.ItemPendant.PendantType;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.BossType;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedAddItem;
import zeldaswordskills.util.TimedChatDialogue;

/**
 * 
 * This quest is begun immediately after completing the quest to speak with Zelda.
 * 
 * The goal is to retrieve all 3 pendants, starting with the Pendant of Wisdom.
 * 
 * Once the first pendant is retrieved, the player may receive Zelda's Letter.
 * (handled externally from this quest)
 * 
 * The pendants will be returned to the player upon beginning the next quest.
 *
 */
public final class QuestPendants extends QuestBase
{
	/** Combined bit flags of all Pendants */
	private static final int FLAG_ALL_PENDANTS = 7;

	/** List of pendants in order they should be found */
	private static final PendantType[] pendants = {PendantType.WISDOM, PendantType.COURAGE, PendantType.POWER};

	/**
	 * Returns true if the specified pendant has not yet been given and was removed from the player's inventory
	 */
	public boolean givePendant(EntityPlayer player, ItemPendant.PendantType pendant) {
		if (!isset(pendant.bitFlag) && PlayerUtils.consumeHeldItem(player, ZSSItems.pendant, pendant.ordinal(), 1)) {
			set(pendant.bitFlag);
			return true;
		}
		return false;
	}

	@Override
	public boolean canBegin(EntityPlayer player) {
		return super.canBegin(player) && ZSSQuests.get(player).hasCompleted(QuestZeldaTalk.class);
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.begin.0"),
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.begin.1"),
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.begin.2"));
		return true;
	}

	@Override
	public boolean canComplete(EntityPlayer player) {
		return false; // can only complete via #update
	}

	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given.final.0"),
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given.final.1"),
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given.final.2"),
				new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given.final.3"));
		ItemStack stack = new ItemStack(ZSSItems.keyBig, 1, BossType.FOREST.doorKeyMeta);
		new TimedAddItem(player, stack, 4000, Sounds.SUCCESS);
		return true;
	}

	@Override
	public boolean complete(EntityPlayer player, Object... data) {
		if (!isset(FLAG_COMPLETE) && isset(FLAG_ALL_PENDANTS) && onComplete(player, data)) {
			forceComplete(player, data);
			return true;
		}
		return false;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		set(FLAG_COMPLETE);
	}

	@Override
	public boolean update(EntityPlayer player, Object... data) {
		ItemStack stack = player.getHeldItem();
		if (stack != null && stack.getItem() instanceof ItemPendant) {
			ItemPendant.PendantType pendant = ItemPendant.PendantType.byDamage(stack.getItemDamage());
			if (pendant != ItemPendant.PendantType.WISDOM && !isset(ItemPendant.PendantType.WISDOM.bitFlag)) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.pendant.wrong");
			} else if (givePendant(player, pendant)) {
				if (pendant == ItemPendant.PendantType.WISDOM) {
					ZSSQuests.get(player).add(new QuestZeldasLetter());
				} else if (isset(FLAG_ALL_PENDANTS)) { // final pendant was given
					complete(player);
				} // else 2nd pendant, nothing special to do aside from chat
				// Completion chat messages added in #complete
				if (!isComplete(player)) {
					new TimedChatDialogue(player,
							new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given." + pendant.unlocalizedName + ".0"),
							new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given." + pendant.unlocalizedName + ".1"),
							new ChatComponentTranslation("chat.zss.npc.zelda.pendant.given." + pendant.unlocalizedName + ".2"));
				}
			} else { // duplicate pendant
				PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.pendant.duplicate");
			}
			return true;
		}
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		// Provide hints in expected order of retrieval
		if (hasBegun(player) && rand.nextInt(8) < 3) {
			for (ItemPendant.PendantType pendant : QuestPendants.pendants) {
				if (!isset(pendant.bitFlag)) {
					return new ChatComponentTranslation("chat.zss.npc.zelda.pendant.hint." + pendant.unlocalizedName + "." + rand.nextInt(3));
				}
			}
		}
		return null;
	}
}

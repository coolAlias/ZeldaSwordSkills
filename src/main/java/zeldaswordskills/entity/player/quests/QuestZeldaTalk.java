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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.TimedChatDialogue;

public final class QuestZeldaTalk extends QuestBase
{
	private ItemStack ocarina;

	/**
	 * Returns true if the stack is the Fairy Ocarina (null expected)
	 */
	private boolean isFairyOcarina(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemInstrument && ((ItemInstrument) stack.getItem()).getInstrument(stack) == ItemInstrument.Instrument.OCARINA_FAIRY;
	}

	@Override
	protected boolean onBegin(EntityPlayer player, Object... data) {
		ItemStack stack = player.getHeldItem();
		if (isFairyOcarina(stack)) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.npc.zelda.ocarina.give");
			this.ocarina = stack.copy();
			this.ocarina.stackSize = 1;
			--stack.stackSize;
			set(FLAG_BEGIN);
			return true;
		}
		return false;
	}

	@Override
	public boolean canComplete(EntityPlayer player, Object... data) {
		return super.canComplete(player) && ocarina != null;
	}

	/**
	 * @param data[0] may contain a Boolean indicating whether the player waited for Zelda to finish playing
	 */
	@Override
	protected boolean onComplete(EntityPlayer player, Object... data) {
		PlayerUtils.playSound(player, Sounds.SUCCESS, 1.0F, 1.0F);
		if (player.getHeldItem() == null) {
			player.setCurrentItemOrArmor(0, ocarina);
		} else {
			PlayerUtils.addItemToInventory(player, ocarina);
		}
		String chat = (data != null && data.length > 0 && data[0] instanceof Boolean && ((Boolean) data[0]).booleanValue() ? "played" : "return");
		new TimedChatDialogue(player,
				new ChatComponentTranslation("chat.zss.npc.zelda.ocarina." + chat + ".0"),
				new ChatComponentTranslation("chat.zss.npc.zelda.ocarina." + chat + ".1"));
		ocarina = null;
		// Add next quest so it triggers when player talks to Zelda next
		ZSSQuests.get(player).add(new QuestPendants());
		return true;
	}

	@Override
	public void forceComplete(EntityPlayer player, Object... data) {
		ocarina = null;
		set(FLAG_COMPLETE);
		ZSSQuests.get(player).add(new QuestPendants());
	}

	@Override
	public boolean update(EntityPlayer player, Object... data) {
		return false;
	}

	@Override
	public IChatComponent getHint(EntityPlayer player, Object... data) {
		if (!hasBegun(player) && rand.nextInt(8) < 3) {
			return new ChatComponentTranslation("chat.zss.npc.zelda.ocarina.hint." + rand.nextInt(4));
		}
		return null;
	}

	@Override
	public boolean requiresSync() {
		return true; // required as it is used client-side in EntityNpcZelda#getSongToLearn
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (ocarina != null) {
			compound.setTag("ocarina", ocarina.writeToNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("ocarina", Constants.NBT.TAG_COMPOUND)) { 
			ocarina = ItemStack.loadItemStackFromNBT(compound.getCompoundTag("OcarinaStack"));
		}
	}
}

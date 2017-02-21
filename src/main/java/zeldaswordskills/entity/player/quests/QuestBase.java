/**
    Copyright (C) <2015> <coolAlias>

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

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.SyncQuestPacket;

public abstract class QuestBase implements IQuest
{
	/** Dummy quest handler */
	public static final IQuestHandler DEFAULT_QUEST_HANDLER = (new IQuestHandler() {
		@Override
		public void onQuestBegun(IQuest quest, EntityPlayer player) {}
		@Override
		public void onQuestChanged(IQuest quest, EntityPlayer player) {}
		@Override
		public void onQuestCompleted(IQuest quest, EntityPlayer player) {}
	});

	/** Bit flag (bit 6, i.e. 64) set when the quest has begun */
	protected static final int FLAG_BEGIN = 64;

	/** Bit flag (bit 7, i.e. 128) set when the quest is complete */
	protected static final int FLAG_COMPLETE = 128;

	/** RNG used by quests */
	protected static final Random rand = new Random();

	/**
	 * Combined total of all quest flags which have been set; each flag should be a
	 * specific bit - used bits are {@link #FLAG_BEGIN} and {@link #FLAG_COMPLETE} 
	 */
	protected int flag;

	/**
	 * Returns true if the flag is set; flags can be checked in combination by adding them together
	 */
	protected boolean isset(int flag) {
		return (this.flag & flag) == flag;
	}

	/**
	 * Sets the flag; flags can be set in combination by adding them together
	 */
	protected void set(int flag) {
		this.flag |= flag;
	}

	/**
	 * Unsets the flag; flags can be unset in combination by adding them together
	 */
	protected void unset(int flag) {
		this.flag &= ~flag;
	}

	@Override
	public boolean canBegin(EntityPlayer player) {
		return !isset(FLAG_BEGIN);
	}

	@Override
	public boolean begin(EntityPlayer player, Object... data) {
		if (canBegin(player) && onBegin(player, data)) {
			set(FLAG_BEGIN);
			return true;
		}
		return false;
	}

	/**
	 * Called from the default {@link #begin} implementation after {@link #canBegin}
	 * has already returned true; this is usually a good time to send chat messages, etc.
	 * @param data any extra data from {@link #begin}
	 * @return true to allow the quest to begin, or false to abort
	 */
	protected abstract boolean onBegin(EntityPlayer player, Object... data);

	@Override
	public boolean hasBegun(EntityPlayer player) {
		return isset(FLAG_BEGIN);
	}

	@Override
	public boolean canComplete(EntityPlayer player) {
		return !isComplete(player);
	}

	@Override
	public boolean complete(EntityPlayer player, Object... data) {
		if (canComplete(player) && onComplete(player, data)) {
			set(FLAG_COMPLETE);
			return true;
		}
		return false;
	}

	/**
	 * Called from the default {@link #complete} implementation after {@link #canComplete}
	 * has already returned true; this is usually a good time to give quest rewards, etc.
	 * @param data any extra data from {@link #complete}
	 * @return true to allow the quest to complete, or false to abort
	 */
	protected abstract boolean onComplete(EntityPlayer player, Object... data);

	@Override
	public boolean isComplete(EntityPlayer player) {
		return isset(FLAG_COMPLETE);
	}

	@Override
	public boolean requiresSync() {
		return false;
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("questFlag", flag);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		flag = compound.getInteger("questFlag");
	}

	/**
	 * Writes the quest to NBT, guaranteeing that the fully qualified class name is included
	 */
	public static NBTTagCompound saveToNBT(IQuest quest) {
		NBTTagCompound compound = new NBTTagCompound();
		quest.writeToNBT(compound);
		compound.setString("QuestClass", quest.getClass().getName());
		return compound;
	}

	/**
	 * Reconstructs the IQuest instance from the NBTTagCompound, assuming the
	 * fully qualified class name is stored using the key "QuestClass"
	 */
	public static IQuest loadFromNBT(NBTTagCompound compound) {
		try {
			Class<?> clazz = Class.forName(compound.getString("QuestClass"));
			Object o = clazz.newInstance();
			if (o instanceof IQuest) {
				((IQuest) o).readFromNBT(compound);
				return (IQuest) o;
			} else {
				ZSSMain.logger.warn("Failed to load quest from NBT: " + compound);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns a new instance of the given quest class, or null if there was an exception
	 */
	public static IQuest getQuestInstance(Class<? extends IQuest> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Returns true if the given quest made progress and/or no further interactions should occur.
	 * @param player
	 * @param quest   Quest instance to check for progress
	 * @param handler IQuestHandler will have its methods called at appropriate times; pass {@link #DEFAULT_QUEST_HANDLER} if none available
	 * @param data    Optional arguments passed to the various {@code IQuest} methods, e.g. {@link IQuest#begin begin} and {@link IQuest#complete complete}
	 * @return        True if something happened, i.e. the calling interaction should be canceled
	 */
	public static boolean checkQuestProgress(EntityPlayer player, IQuest quest, IQuestHandler handler, Object... data) {
		boolean changed = false;
		if (quest.isComplete(player)) {
			// do nothing if quest is already complete
		} else if (quest.canBegin(player)) {
			if (quest.begin(player, data)) {
				handler.onQuestBegun(quest, player);
				changed = true;
			}
		} else if (quest.canComplete(player)) {
			if (quest.complete(player, data)) {
				handler.onQuestCompleted(quest, player);
				changed = true;
			}
		} else if (quest.hasBegun(player) && quest.update(player, data)) {
			handler.onQuestChanged(quest, player);
			changed = true;
		}
		if (!changed) { // check for hint
			IChatComponent hint = quest.getHint(player, data);
			if (hint != null) { // result may be different on client vs. server due to Random
				if (!player.worldObj.isRemote && !hint.getUnformattedText().equals("")) {
					player.addChatMessage(hint);
				}
				return true;
			}
		}
		if (changed && quest.requiresSync() && player instanceof EntityPlayerMP) {
			PacketDispatcher.sendTo(new SyncQuestPacket(quest), (EntityPlayerMP) player);
		}
		return changed;
	}
}

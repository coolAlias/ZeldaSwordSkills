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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 
 * Implemented as its own IEEP so quests that do not require being sent to client
 * are not bundled with ZSSPlayerInfo sync packet, but its registration, etc. is
 * all handled in ZSSPlayerInfo.
 *
 */
public class ZSSQuests implements IExtendedEntityProperties
{
	private static final String EXT_PROP_NAME = "ZssPlayerQuests";

	/** Stores an instance of each quest the player has received */
	private final Map<Class<? extends IQuest>, IQuest> quests = new HashMap<Class<? extends IQuest>, IQuest>();

	/** The last mask borrowed from the Happy Mask Salesman */
	private Item borrowedMask = null;

	private final EntityPlayer player;

	public ZSSQuests(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void init(Entity entity, World world) {}

	/** Used to register these extended properties for the player during EntityConstructing event */
	public static final void register(EntityPlayer player) {
		player.registerExtendedProperties(EXT_PROP_NAME, new ZSSQuests(player));
	}

	/** Returns ZSSQuests properties for player */
	public static final ZSSQuests get(EntityPlayer player) {
		return (ZSSQuests) player.getExtendedProperties(EXT_PROP_NAME);
	}

	/** Returns a read-only view of all the player's quests */
	public Map<Class<? extends IQuest>, IQuest> getQuests() {
		return Collections.unmodifiableMap(quests);
	}

	/**
	 * Returns the instance of the quest for the given IQuest class, if any
	 */
	public IQuest get(Class<? extends IQuest> quest) {
		return quests.get(quest);
	}

	/**
	 * Adds the IQuest to this player's list of quests
	 * @param quest Null is expected in some cases and is handled appropriately
	 */
	public boolean add(IQuest quest) {
		if (quest == null || quests.containsKey(quest.getClass())) {
			return false;
		}
		quests.put(quest.getClass(), quest);
		return true;
	}

	/**
	 * Updates a client-side quest with the version sent from the server via {@code SyncQuestPacket}
	 */
	@SideOnly(Side.CLIENT)
	public void update(IQuest quest) {
		quests.put(quest.getClass(), quest);
	}

	/**
	 * Returns true if the specified quest has been begun
	 */
	public boolean hasBegun(Class<? extends IQuest> quest) {
		return quests.containsKey(quest) && quests.get(quest).hasBegun(player);
	}

	/**
	 * Returns true if the specified quest has been completed
	 */
	public boolean hasCompleted(Class<? extends IQuest> quest) {
		return quests.containsKey(quest) && quests.get(quest).isComplete(player);
	}

	/**
	 * Returns the last mask borrowed, or null if no mask has been borrowed
	 */
	public Item getBorrowedMask() {
		// For backwards compatibility, check for old mask and swap it out
		Item mask = ZSSPlayerInfo.get(player).getBorrowedMask();
		if (mask != null) {
			borrowedMask = mask;
			ZSSPlayerInfo.get(player).setBorrowedMask(null);
		}
		return borrowedMask;
	}

	/**
	 * Sets the mask that the player has borrowed
	 */
	public void setBorrowedMask(Item item) {
		borrowedMask = item;
	}

	/**
	 * Copies given data to this one when a player is cloned
	 */
	public void copy(ZSSQuests quests) {
		NBTTagCompound compound = new NBTTagCompound();
		quests.saveNBTData(compound);
		this.loadNBTData(compound);
	}

	@Override
	public void saveNBTData(NBTTagCompound compound) {
		writeToNBT(compound, false);
	}

	/**
	 * Writes ZSSQuest data to the tag compound
	 * @param syncOnly True to ignore quests for which {@link IQuest#requiresSync} returns false
	 */
	public void writeToNBT(NBTTagCompound compound, boolean syncOnly) {
		NBTTagList questList = new NBTTagList();
		for (IQuest quest : quests.values()) {
			if (!syncOnly || quest.requiresSync()) {
				questList.appendTag(QuestBase.saveToNBT(quest));
			}
		}
		compound.setTag("ZssQuests", questList);
		String maskId = (borrowedMask == null ? null : GameData.getItemRegistry().getNameForObject(borrowedMask));
		if (maskId != null) {
			compound.setString("borrowedMask", maskId);
		}
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		NBTTagList questList = compound.getTagList("ZssQuests", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < questList.tagCount(); ++i) {
			NBTTagCompound tag = (NBTTagCompound) questList.getCompoundTagAt(i);
			IQuest quest = QuestBase.loadFromNBT(tag);
			if (quest != null && !this.add(quest)) {
				ZSSMain.logger.warn("Duplicate quest entry loaded from NBT: " + quest);
			}
		}
		if (compound.hasKey("borrowedMask", Constants.NBT.TAG_STRING)) {
			borrowedMask = GameData.getItemRegistry().getObject(compound.getString("borrowedMask"));
		}
	}
}

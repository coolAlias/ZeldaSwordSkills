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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;

public interface IQuest {

	/**
	 * True if this quest may be begun; usually false if {@link #hasBegun} returns true
	 * @return False once {@link #begin} is successful
	 */
	boolean canBegin(EntityPlayer player);

	/**
	 * Call to begin the quest, returning false if it could not be begun
	 * @param data Allows additional data to be provided; implementing class should provide documentation
	 */
	boolean begin(EntityPlayer player, Object... data);

	/**
	 * Returns true if the quest has begun.
	 * @return True once {@link #begin} is successful
	 */
	boolean hasBegun(EntityPlayer player);

	/**
	 * True if this quest is able to be completed at this time
	 */
	boolean canComplete(EntityPlayer player);

	/**
	 * Call this to complete the quest; {@link #isComplete()} should now return true
	 * <br>Be sure to check {@link #canComplete(EntityPlayer, Object...)} first if calling this method manually.
	 * @param data Allows additional data to be provided; implementing class should provide documentation
	 * @return false if for some reason the quest failed to complete
	 */
	boolean complete(EntityPlayer player, Object... data);

	/**
	 * Forcefully complete this quest, regardless of its current state.
	 * Quest completion is typically silent, i.e. chat messages, rewards, etc. are ignored.
	 * {@link #isComplete()} must return true after calling this method.
	 * @param data Allows additional data to be provided; implementing class should provide documentation
	 */
	void forceComplete(EntityPlayer player, Object... data);

	/**
	 * True if this quest has been completed
	 * @return Must return true once {@link #complete) has been called
	 */
	boolean isComplete(EntityPlayer player);

	/**
	 * Call when the quest has already begun but could not be completed, giving
	 * a chance for intermediate steps, if any, to process.
	 * @param data Allows additional data to be provided; implementing class should provide documentation
	 * @return true if something changed or the entity interaction should be canceled
	 */
	boolean update(EntityPlayer player, Object... data);

	/**
	 * Possibly return a hint for the player, or null for no hint.
	 * Returning an empty chat will cancel the interaction without sending any further chat,
	 * e.g. if chat was already sent some other way.
	 * Note that this may be called at any time, e.g. when {@link #hasBegun} is not yet true.
	 * @param data Allows additional data to be provided; implementing class should provide documentation
	 */
	IChatComponent getHint(EntityPlayer player, Object... data);

	/**
	 * Return true if this quest object requires synchronization to the client.
	 * Note that it is up to individual implementations or quest handlers to enforce this.
	 */
	boolean requiresSync();

	/**
	 * Save any data related to this quest to NBT; be sure to save the
	 * fully qualified class name so the quest can be reconstructed
	 */
	void writeToNBT(NBTTagCompound compound);

	/**
	 * Read quest data from NBT
	 */
	void readFromNBT(NBTTagCompound compound);

}

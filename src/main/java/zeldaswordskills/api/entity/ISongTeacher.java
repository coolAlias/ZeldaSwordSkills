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

package zeldaswordskills.api.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.songs.AbstractZeldaSong;

/**
 * 
 * Interface for entities that can teach the player an {@link AbstractZeldaSong}
 * when right-clicked with an {@link ItemInstrument}
 *
 */
public interface ISongTeacher {

	/**
	 * Called when the player right-clicks on the song teacher with an instrument
	 * to determine which song, if any, the teacher will teach.
	 * Note that this method is called and should return the same result on both sides. 
	 * @param stack The ItemInstrument stack the player is holding
	 * @return a {@link TeachingResult#TeachingResult TeachingResult} object to determine what will happen, or null to proceed directly to {@code Entity#interact}  
	 */
	TeachingResult getTeachingResult(ItemStack stack, EntityPlayer player);

	public static class TeachingResult {
		/** If not null, {@link #cancel} will be set to true and the song learning GUI will open */
		public final AbstractZeldaSong songToLearn;
		/** True to display default chat messages when learning or reviewing a song */
		public final boolean displayChat;
		/** If true, {@code Entity#interact} is skipped and either the regular or learning song GUI will open */
		public final boolean cancel;
		/**
		 * Return object for {@link ISongTeacher#getSongToLearn}
		 * @param songToLearn see {@link #songToLearn} - may be null
		 * @param displayChat see {@link #displayChat}
		 * @param cancel      see {@link #cancel} - to prevent the song GUI from opening, do not cancel; instead,
		 * 					  return true from {@code Entity#interact} if the player is holding an {@code ItemInstrument}
		 */
		public TeachingResult(AbstractZeldaSong songToLearn, boolean displayChat, boolean cancel) {
			this.songToLearn = songToLearn;
			this.displayChat = displayChat;
			this.cancel = (cancel || songToLearn != null);
		}
	}
}

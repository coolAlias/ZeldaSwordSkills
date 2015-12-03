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

package zeldaswordskills.api;

import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.block.BlockSongInscription;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.SongNote;

/**
 * 
 * Assortment of methods to facilitate interaction of custom {@link AbstractZeldaSong}
 * classes with the Zelda Sword Skills mod.
 * 
 * To add a new song:
 * 
 * 1. Create a class extending AbstractZeldaSong (or use an anonymous class during registration)
 * 
 * 2. Override {@link AbstractZeldaSong#getSoundString} to return your sound file
 * 		location (be sure to add the sound file and edit your sounds.JSON).
 * 
 * 3. Override the abstract method to perform whatever effect you want.
 * 
 * 4. Register a a new instance of your class during {@code FMLPreInitializationEvent}
 * 		using {@link SongAPI#registerSong}: SongAPI.registerSong(new YourSongClass(args));
 *
 *		If your song conflicted with any previously registered songs, warnings will be
 *		generated in the console log. Song names and notes must be sufficiently unique:
 *		- For names, it is advised to prepend them with a unique identifier, e.g. 'zss.songname'
 *		- For notes, consider loading them from config to allow for future conflict resolution
 *
 *		If you need a reference to your song, create a separate reference class similar to Items:
 *			public class YourSongRefs {
 *				public static final AbstractZeldaSong yourSong = SongAPI.getSongByName("yoursongname");
 *			}
 * 
 * 5. Done. Now the player just has to learn it using one of the following methods:
 *		a. Use {@link SongAPI#openSongLearningGui} to open the song gui, learning it note by note
 *		b. Use {@link SongAPI#learnSong} to add a song directly to the player's known songs
 *		c. {@link BlockSongInscription} blocks can be set to teach specific songs, but must
 *			first be placed into the world and set to the correct song.
 *
 *		The first two methods can be used pretty much anywhere, e.g.
 *			{@link Item#onItemRightClick}, {@link Block#onBlockActivated}, Forge Events, etc.
 *
 *	Once a player has learned a song, they can play it using any {@link ItemInstrument}
 *
 */
public final class SongAPI {

	/**
	 * Registers a new song which can then be retrieved using {@link SongAPI#getSongByName}
	 * 
	 * Keep in mind that for the standard music GUI, only notes ranging from B1 natural
	 * up to F2 natural are playable, and D1, F1, A2, B2, D2 are the 'standard' keys.
	 * 
	 * @param song	Use a new song instance, not a reference, i.e.:
	 * 				registerSong(new YourSongClass(args)) instead of registerSong(yourSongInstance)
	 * If an instance is required, retrieve it after registration.
	 */
	public static void registerSong(AbstractZeldaSong song) {}

	/**
	 * Returns true if the player knows the song.
	 */
	public static boolean isSongKnown(EntityPlayer player, AbstractZeldaSong song) {
		return ZSSPlayerSongs.get(player).isSongKnown(song);
	}

	/**
	 * Attempts to add the song to the player's repertoire of known songs.
	 * @return	False if song already known or {@link AbstractZeldaSong#canLearn canLearn} returned false
	 */
	public static boolean learnSong(EntityPlayer player, AbstractZeldaSong song) {
		return ZSSPlayerSongs.get(player).learnSong(song, null);
	}

	/**
	 * Checks the player's known songs to see if any match the notes played
	 * @return	The song matching the notes played or null
	 */
	public static AbstractZeldaSong getKnownSongFromNotes(EntityPlayer player, List<SongNote> notesPlayed) {
		return ZSSPlayerSongs.get(player).getKnownSongFromNotes(notesPlayed);
	}

	/**
	 * Opens the standard song-learning GUI, teaching the player the song note by note.
	 * Use this method on the CLIENT side; server side will have no effect.
	 */
	public static void openSongLearningGui(EntityPlayer player, AbstractZeldaSong song, int x, int y, int z) {
		if (player.worldObj.isRemote) {
			ZSSPlayerSongs.get(player).songToLearn = song;
			player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, x, y, z);
		}
	}

	/**
	 * Returns all registered song names, useful if you want to check availability
	 */
	public static List<String> getRegisteredNames() {
		return ZeldaSongs.getRegisteredNames();
	}

	/**
	 * Returns all registered songs
	 */
	public static Collection<AbstractZeldaSong> getRegisteredSongs() {
		return ZeldaSongs.getRegisteredSongs();
	}

	/**
	 * Returns the total number of registered songs
	 */
	public static int getTotalSongs() {
		return ZeldaSongs.getTotalSongs();
	}

	/**
	 * Returns the song matching the unlocalized name given, or null
	 */
	public static AbstractZeldaSong getSongByName(String name) {
		return ZeldaSongs.getSongByName(name);
	}

	/**
	 * If the notes played make up a valid melody, that song will be returned.
	 * @return	Null if the notes are not a valid song
	 */
	public static AbstractZeldaSong getSongFromNotes(List<SongNote> notesPlayed) {
		return ZeldaSongs.getSongFromNotes(notesPlayed);
	}

	/**
	 * Returns true if the song notes played are unique enough to be used as a new song
	 */
	public static boolean areNotesUnique(List<SongNote> notesPlayed) {
		return ZeldaSongs.areNotesUnique(notesPlayed);
	}
}

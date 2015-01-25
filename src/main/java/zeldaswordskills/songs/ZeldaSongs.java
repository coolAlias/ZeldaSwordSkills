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

package zeldaswordskills.songs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.PlaySoundPacket;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.SongNote;

public final class ZeldaSongs
{
	private ZeldaSongs() {}

	/** List of all registered song names */
	private static final List<String> songNames = new ArrayList<String>();

	/** Map of unlocalized name to ZeldaSong */
	private static final Map<String, AbstractZeldaSong> songMap = new HashMap<String, AbstractZeldaSong>();

	/**
	 * Returns all registered song names
	 */
	public static final List<String> getRegisteredNames() {
		return Collections.unmodifiableList(ZeldaSongs.songNames);
	}

	/**
	 * Returns all registered songs
	 */
	public static final Collection<AbstractZeldaSong> getRegisteredSongs() {
		return Collections.unmodifiableCollection(ZeldaSongs.songMap.values());
	}

	/**
	 * Returns the total number of registered songs
	 */
	public static final int getTotalSongs() {
		return ZeldaSongs.songNames.size();
	}

	/**
	 * Returns the ZeldaSong matching the unlocalized name given, or null
	 */
	public static final AbstractZeldaSong getSongByName(String name) {
		return ZeldaSongs.songMap.get(name);
	}

	/**
	 * If the notes played make up a valid melody, that song will be returned.
	 * @return	Null if the notes are not a valid song
	 */
	public static final AbstractZeldaSong getSongFromNotes(List<SongNote> notesPlayed) {
		for (AbstractZeldaSong song : ZeldaSongs.getRegisteredSongs()) {
			if (song.areCorrectNotes(notesPlayed)) {
				return song;
			}
		}
		return null;
	}

	/**
	 * Returns true if the notes played are unique enough to form a new song
	 */
	public static final boolean areNotesUnique(List<SongNote> notesPlayed) {
		for (AbstractZeldaSong song : ZeldaSongs.getRegisteredSongs()) {
			if (song.isSongPartOfNotes(notesPlayed)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Called automatically when a new song is instantiated. Do not call this method manually.
	 * Registration fails if the song's unlocalized name is already registered
	 * or if the song's notes would cause ambiguity when trying to retrieve a
	 * song: {A, A, A, X} is not allowed if {A, A, A} exists.
	 */
	public static void register(AbstractZeldaSong song) {
		if (songNames.contains(song.getUnlocalizedName()) || songMap.containsKey(song.getUnlocalizedName())) {
			LogHelper.warning("Failed to register " + song.getUnlocalizedName() + ": Unlocalized name must be unique.");
		} else if (!ZeldaSongs.areNotesUnique(song.getNotes())) {
			LogHelper.warning("Failed to register " + song.getUnlocalizedName() + ": Notes provided are not unique " + song.getNotes());
		} else {
			songNames.add(song.getUnlocalizedName());
			songMap.put(song.getUnlocalizedName(), song);
			LogHelper.info("Registered " + song.getDisplayName() + " as '" + song.getUnlocalizedName() + "' with notes " + song.getNotes());
		}
	}

	public static final AbstractZeldaSong songEpona = new ZeldaSongEpona("epona", 90, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.D2, SongNote.B2, SongNote.A2);
	public static final AbstractZeldaSong songHealing = new ZeldaSongHealing("healing", 78, SongNote.B2, SongNote.A2, SongNote.F1, SongNote.B2, SongNote.A2, SongNote.F1);
	public static final AbstractZeldaSong songSaria = new ZeldaSongNoEffect("saria", 71, SongNote.F1, SongNote.A2, SongNote.B2, SongNote.F1, SongNote.A2, SongNote.B2);
	public static final AbstractZeldaSong songSoaring = new ZeldaSongSoaring("soaring", 120, SongNote.F1, SongNote.B2, SongNote.D2, SongNote.F1, SongNote.B2, SongNote.D2);
	public static final AbstractZeldaSong songStorms = new ZeldaSongStorms("storms", 85, SongNote.D1, SongNote.F1, SongNote.D2, SongNote.D1, SongNote.F1, SongNote.D2);
	public static final AbstractZeldaSong songSun = new ZeldaSongSun("sun", 100, SongNote.A2, SongNote.F1, SongNote.D2, SongNote.A2, SongNote.F1, SongNote.D2);
	public static final AbstractZeldaSong songTime = new ZeldaSongNoEffect("time", 90, SongNote.A2, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D1, SongNote.F1);
	public static final AbstractZeldaSong songWarpFire = new ZeldaSongWarp("bolero", 60, SongNote.F1, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.A2, SongNote.F1);
	public static final AbstractZeldaSong songWarpForest = new ZeldaSongWarp("minuet", 90, SongNote.D1, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.B2, SongNote.A2);
	public static final AbstractZeldaSong songWarpLight = new ZeldaSongWarp("prelude", 92, SongNote.D2, SongNote.A2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2);
	public static final AbstractZeldaSong songWarpOrder = new ZeldaSongWarp("oath", 110, SongNote.A2, SongNote.F1, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D2);
	public static final AbstractZeldaSong songWarpShadow = new ZeldaSongWarp("nocturne", 116, SongNote.B2, SongNote.A2, SongNote.A2, SongNote.D1, SongNote.B2, SongNote.A2, SongNote.F1);
	public static final AbstractZeldaSong songWarpSpirit = new ZeldaSongWarp("requiem", 125, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.D1);
	public static final AbstractZeldaSong songWarpWater = new ZeldaSongWarp("serenade", 83, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.A2, SongNote.B2);
	public static final AbstractZeldaSong songZeldasLullaby = new ZeldaSongNoEffect("lullaby", 129, SongNote.B2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2, SongNote.A2);
	/**
	 * The Scarecrow Song is the only song with notes determined by the player.
	 * When played, a scarecrow will pop up temporarily.
	 */
	public static final AbstractZeldaSong songScarecrow = new AbstractZeldaSong("scarecrow", 160) {
		@Override
		protected void performEffect(EntityPlayer player, ItemStack instrument, int power) {
			// TODO
			PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
		}
	};
}

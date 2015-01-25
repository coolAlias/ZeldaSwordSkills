/**
    Copyright (C) <2014> <coolAlias>

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

package zeldaswordskills.util;


/**
 * 
 * 12 notes per octave for 2 octaves, starting on A1 (natural) up to G2-sharp,
 * with an extra flat A1 as the first value to round out the control-scheme.
 * 
 * Keep in mind that for the standard music GUI used by the ocarina, only notes
 * from B1 natural to F2 natural are playable.
 *
 */
public enum SongNote
{
	A1_FLAT(Note.A, Pitch.FLAT),
	A1(Note.A, Pitch.NATURAL),
	B1_FLAT(Note.B, Pitch.FLAT),
	B1(Note.B, Pitch.NATURAL),
	C1(Note.C, Pitch.NATURAL),
	C1_SHARP(Note.C, Pitch.SHARP),
	D1(Note.D, Pitch.NATURAL),
	D1_SHARP(Note.D, Pitch.SHARP),
	E1(Note.E, Pitch.NATURAL),
	F1(Note.F, Pitch.NATURAL),
	F1_SHARP(Note.F, Pitch.SHARP),
	G1(Note.G, Pitch.NATURAL),
	G1_SHARP(Note.G, Pitch.SHARP),
	A2(Note.A, Pitch.NATURAL),
	B2_FLAT(Note.B, Pitch.FLAT),
	B2(Note.B, Pitch.NATURAL),
	C2(Note.C, Pitch.NATURAL),
	C2_SHARP(Note.C, Pitch.SHARP),
	D2(Note.D, Pitch.NATURAL),
	D2_SHARP(Note.D, Pitch.SHARP),
	E2(Note.E, Pitch.NATURAL),
	F2(Note.F, Pitch.NATURAL),
	F2_SHARP(Note.F, Pitch.SHARP),
	G2(Note.G, Pitch.NATURAL),
	G2_SHARP(Note.G, Pitch.SHARP);

	/** The corresponding musical note, e.g. C, G, etc. */
	public final Note note;
	/** The pitch of the note, i.e. flat, sharp, or natural */
	public final Pitch pitch;

	private SongNote(Note note, Pitch pitch) {
		this.note = note;
		this.pitch = pitch;
	}

	@Override
	public String toString() {
		return note.toString() + getOctave() + "-" + pitch.toString();
	}

	/**
	 * Returns the octave of this note, determined by ordinal position
	 * Note that the main scale is considered octave 1, with lower or 
	 * higher octaves relative to that value.
	 */
	public int getOctave() {
		return ((ordinal() - 1) / 12) + 1;
	}

	public boolean isFlat() {
		return pitch == Pitch.FLAT;
	}

	public boolean isSharp() {
		return pitch == Pitch.SHARP;
	}

	/**
	 * Returns a SongNote from a PlayableNote and a modifier
	 * @param note		Any {@link PlayableNote}
	 * @param modifier	Number of half-steps to modify the note, in either direction
	 * 					(negative lowers pitch, positive raises pitch, 0 stays the same).
	 * 					Valid values range from -3 to 3, inclusive.
	 * @return			Corresponding SongNote based on the note played and the modifier
	 * 					Null should be impossible, but check anyway to be safe
	 */
	public static SongNote getNote(PlayableNote note, int modifier) {
		modifier = (modifier < -3 ? -3 : (modifier > 3 ? 3 : modifier));
		int i = note.note.ordinal() + modifier;
		return (i < 0 || i > SongNote.values().length ? null : SongNote.values()[i]);
	}

	/** Playable notes, with their corresponding {@link SongNote} */
	public static enum PlayableNote {
		D1(SongNote.D1),
		F1(SongNote.F1),
		A2(SongNote.A2),
		B2(SongNote.B2),
		D2(SongNote.D2);

		protected final SongNote note;

		private PlayableNote(SongNote note) {
			this.note = note;
		}

		@Override
		public String toString() {
			return this.name();
		}

		/**
		 * Returns the ordinal position of PlayableNote corresponding to the note,
		 * or PlayableNote.values().length if the note is not a PlayableNote.
		 * Used for getting the texture y position.
		 */
		public static int getOrdinalFromNote(SongNote note) {
			for (PlayableNote playable : PlayableNote.values()) {
				if (playable.note == note) {
					return playable.ordinal();
				}
			}
			return PlayableNote.values().length;
		}
	}

	public static enum Pitch {
		FLAT, NATURAL, SHARP;

		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}

	/** Scale starts on A, meaning the lowest possible is A1-flat and the highest is A2-sharp */
	public static enum Note {
		A, B, C, D, E, F, G;

		@Override
		public String toString() {
			return this.name();
		}

		/** Returns the previous note (previous from A is G) */
		public Note prev() {
			return (this == A ? G : Note.values()[ordinal() - 1]);
		}

		/** Returns the next note (next from G is A) */
		public Note next() {
			return (this == G ? A : Note.values()[ordinal() + 1]);
		}
	}
}

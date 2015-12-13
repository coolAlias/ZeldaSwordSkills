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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import zeldaswordskills.api.SongAPI;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.block.BlockSongInscription;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.WorldUtils;

/**
 * 
 * See the {@link SongAPI} class for instructions on adding new songs.
 * 
 * Note that like Items, songs should not rely on any mutable class members as there
 * is only one instance of each song.
 *
 */
public abstract class AbstractZeldaSong
{
	/** Maximum effect radius for notifying blocks and entities */
	public static final int MAX_SONG_RADIUS = 16;

	/** Unique name, preferably lowercase, used to retrieve this song from {@link SongAPI#getSongByName} */
	private final String unlocalizedName;

	/** Minimum duration song required to play before any effect occurs, in ticks */
	private final int minDuration;

	/** Notes required to play the song */
	private final List<SongNote> notes;

	/** Whether the song's main effect is enabled */
	protected boolean isEnabled;

	/**
	 * Verifies uniqueness of song name and notes and adds it to the registry.
	 * @param unlocalizedName	See {@link #unlocalizedName}, e.g. 'songname'
	 * @param minDuration		See {@link #minDuration}; measured in ticks
	 * @param notes				Minimum of 3 notes are required
	 */
	public AbstractZeldaSong(String unlocalizedName, int minDuration, SongNote... notes) {
		if (("scarecrow").equals(unlocalizedName)) {
			// special case allows Scarecrow Song to register with no notes
		} else if (notes == null || notes.length < 3) {
			throw new IllegalArgumentException("Songs must be composed of at least 3 notes!");
		}
		this.unlocalizedName = unlocalizedName;
		this.minDuration = minDuration;
		this.notes = Collections.unmodifiableList(Arrays.asList(notes));
		this.isEnabled = true;
		ZeldaSongs.register(this);
	}

	/**
	 * Return the translated name of this song. Note that the translation is only correct on the client;
	 * on the server, send a ChatComponentTranslation using {@link #getTranslationString()} instead.
	 */
	public String getDisplayName() {
		return StatCollector.translateToLocal(getTranslationString());
	}

	/**
	 * Returns the string used to translate this song's name
	 */
	public String getTranslationString() {
		return "song.zss." + unlocalizedName + ".name";
	}

	/**
	 * Returns the sound file to play for this song
	 */
	public String getSoundString() {
		return ModInfo.ID + ":song." + unlocalizedName;
	}

	/**
	 * Whether the 'success' sound should play from the standard GUI when performed correctly
	 */
	public boolean playSuccessSound() {
		return true;
	}

	/**
	 * Use to control if player is able to learn this song or not.
	 * Should return the same result on both server and client.
	 */
	public boolean canLearn(EntityPlayer player) {
		return true;
	}

	/**
	 * Whether this song may be learned via Command
	 */
	public boolean canLearnFromCommand() {
		return true;
	}

	/**
	 * True if this song can be learned from {@link BlockSongInscription}
	 */
	public boolean canLearnFromInscription(World world, IBlockState state) {
		return true;
	}

	/**
	 * If the song has a special effect, handle it here. Called when the song is
	 * played and both {@link #isEnabled} and {@link #hasEffect} return true.
	 * This method is only called on the server.
	 * @param instrument	Instrument used to play the song
	 * @param power			Power level of the instrument used to play the song
	 */
	protected abstract void performEffect(EntityPlayer player, ItemStack instrument, int power);

	/**
	 * Whether the player and instrument playing the song will cause the main effect to occur.
	 * @param instrument	The {@link ItemInstrument} stack used to play the song
	 * @param power			Power level of the instrument used
	 * @return	True to allow {@link #performEffect} to be called
	 */
	protected boolean hasEffect(EntityPlayer player, ItemStack instrument, int power) {
		return power > 1;
	}

	/**
	 * Returns the radius within which {@link ISongBlock ISongBlocks} will be notified when the song is performed.
	 * @param instrument	The {@link ItemInstrument} stack used to play the song
	 * @param power			Power level of the instrument used
	 * @return 0 or less to not notify blocks; {@link #MAX_SONG_RADIUS} is the max.
	 */
	protected int getNotifyBlockRadius(EntityPlayer player, ItemStack stack, int power) {
		return 8;
	}

	/**
	 * Returns the radius within which {@link ISongEntity ISongEntities} will be notified when the song is performed.
	 * @param instrument	The {@link ItemInstrument} stack used to play the song
	 * @param power			Power level of the instrument used
	 * @return 0 or less to not notify entities; {@link #MAX_SONG_RADIUS} is the max.
	 */
	protected int getNotifyEntityRadius(EntityPlayer player, ItemStack stack, int power) {
		return 8;
	}

	@Override
	public final boolean equals(Object o) {
		return unlocalizedName.equals(o);
	}

	@Override
	public final int hashCode() {
		return unlocalizedName.hashCode();
	}

	/**
	 * Returns the unlocalized name used to retrieve the song instance from {@link SongAPI#getSongByName}
	 */
	public final String getUnlocalizedName() {
		return unlocalizedName;
	}

	/**
	 * Returns the minimum number of ticks the song must be allowed to play before any effects will occur
	 */
	public final int getMinDuration() {
		return minDuration;
	}

	/**
	 * Returns unmodifiable list of notes required to play this song
	 */
	public final List<SongNote> getNotes() {
		return notes;
	}

	/**
	 * Returns true if the notes played are the correct notes to play this song
	 */
	public final boolean areCorrectNotes(List<SongNote> notesPlayed) {
		if (notes == null || notes.size() < 1 || notesPlayed == null || notesPlayed.size() != notes.size()) {
			return false;
		}
		for (int i = 0; i < notes.size(); ++i) {
			if (notes.get(i) != notesPlayed.get(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if song's notes are contained within the notesPlayed, starting from the first note
	 * @param notesPlayed	May have the same number or more notes than the song
	 */
	public final boolean isSongPartOfNotes(List<SongNote> notesPlayed) {
		if (notes == null || notes.size() < 1 || notesPlayed == null || notesPlayed.size() < notes.size()) {
			return false;
		}
		for (int i = 0; i < notes.size(); ++i) {
			if (notes.get(i) != notesPlayed.get(i)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Whether this song's {@link #performEffect} is enabled or not
	 */
	public final boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Enables or disables this song's main effect, but not notification of {@link ISongBlock ISongBlocks}
	 * or {@link ISongEntity ISongEntities}. Controlled for all songs by the zeldaswordskills.cfg file.
	 */
	public final void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Performs any effects of the song when played; only called on the server.
	 */
	public final void performSongEffects(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			ItemStack instrument = player.getHeldItem();
			if (instrument == null || !(instrument.getItem() instanceof ItemInstrument)) {
				return;
			}
			int power = ((ItemInstrument) instrument.getItem()).getSongStrength(instrument);
			int r = getNotifyBlockRadius(player, instrument, power);
			if (r > 0) {
				notifySongBlocks(player.worldObj, player, power, Math.min(r, MAX_SONG_RADIUS));
			}
			r = getNotifyEntityRadius(player, instrument, power);
			if (r > 0) {
				notifySongEntities(player.worldObj, player, power, Math.min(r, MAX_SONG_RADIUS));
			}
			if (!isEnabled()) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.song.disabled", new ChatComponentTranslation(getTranslationString()));
			} else if (hasEffect(player, instrument, power)) {
				performEffect(player, instrument, power);
			} else {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.song.failed", new ChatComponentTranslation(getTranslationString()));
			}
		}
	}

	/**
	 * Notifies all {@link ISongBlock ISongBlocks} within the given radius of the song played
	 */
	private void notifySongBlocks(World world, EntityPlayer player, int power, int radius) {
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.getEntityBoundingBox().minY);
		int z = MathHelper.floor_double(player.posZ);
		int affected = 0;
		for (int i = (x - radius); i <= (x + radius); ++i) {
			for (int j = (y - (radius / 2)); j <= (y + (radius / 2)); ++ j) {
				for (int k = (z - radius); k <= (z + radius); ++k) {
					BlockPos pos = new BlockPos(i, j, k);
					Block block = world.getBlockState(pos).getBlock();
					if (block instanceof ISongBlock) {
						if (((ISongBlock) block).onSongPlayed(world, pos, player, this, power, affected)) {
							++affected;
						}
					}
				}
			}
		}
	}

	/**
	 * Notifies all {@link ISongEntity ISongEntities} within the given radius of the song played
	 */
	private void notifySongEntities(World world, EntityPlayer player, int power, int radius) {
		int affected = 0;
		List<ISongEntity> entities = WorldUtils.getEntitiesWithinAABB(world, ISongEntity.class, player.getEntityBoundingBox().expand(radius, (double) radius / 2.0D, radius));
		for (ISongEntity entity : entities) {
			if (entity.onSongPlayed(player, this, power, affected)) {
				++affected;
			}
		}
	}
}

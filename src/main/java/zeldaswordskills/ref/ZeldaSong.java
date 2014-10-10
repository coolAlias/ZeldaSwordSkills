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

package zeldaswordskills.ref;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import scala.actors.threadpool.Arrays;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.util.SongNote;

public enum ZeldaSong {
	EPONAS_SONG("epona"),
	SARIAS_SONG("saria"),
	SONG_OF_STORMS("storms"),
	SONG_OF_TIME("time"),
	SUN_SONG("sun"),
	ZELDAS_LULLABY("lullaby");

	private final String unlocalizedName;

	/** Whether the song's main effect is enabled */
	private boolean isEnabled;

	private ZeldaSong(String unlocalizedName) {
		this.unlocalizedName = unlocalizedName;
		this.isEnabled = true;
	}

	@Override
	public String toString() {
		return StatCollector.translateToLocal("song.zss." + unlocalizedName + ".name");
	}

	/** Returns the sound file to play for this song */
	public String getSoundString() {
		return ModInfo.ID + ":song_" + unlocalizedName;
	}

	/** Enables or disables this song's main effect, but not notification of {@link ISoundBlock}s */
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/** Map of notes required to play each song */
	private static final Map<ZeldaSong, List<SongNote>> melodies = new EnumMap<ZeldaSong, List<SongNote>>(ZeldaSong.class);

	/** Adds all of the notes listed as the required melody for the song */
	private static void addMelody(ZeldaSong song, SongNote... notes) {
		melodies.put(song, Arrays.asList(notes));
	}

	static {
		addMelody(EPONAS_SONG, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.D2, SongNote.B2, SongNote.A2);
		addMelody(SARIAS_SONG, SongNote.F1, SongNote.A2, SongNote.B2, SongNote.F1, SongNote.A2, SongNote.B2);
		addMelody(SONG_OF_STORMS, SongNote.D1, SongNote.F1, SongNote.D2, SongNote.D1, SongNote.F1, SongNote.D2);
		addMelody(SONG_OF_TIME, SongNote.A2, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D1, SongNote.F1);
		addMelody(SUN_SONG, SongNote.A2, SongNote.F1, SongNote.D2, SongNote.A2, SongNote.F1, SongNote.D2);
		addMelody(ZELDAS_LULLABY, SongNote.B2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2, SongNote.A2);
	}

	/**
	 * If the notes played make up a valid melody, that song will be returned.
	 * @return	Null if the notes are not a valid song
	 */
	public static ZeldaSong getSongFromNotes(List<SongNote> notesPlayed) {
		for (ZeldaSong song : ZeldaSong.values()) {
			List<SongNote> melody = melodies.get(song);
			if (melody != null && melody.size() == notesPlayed.size()) {
				boolean isIdentical = true;
				for (int i = 0; i < melody.size() && isIdentical; ++i) {
					isIdentical = (melody.get(i) == notesPlayed.get(i));
				}
				if (isIdentical) {
					return song;
				}
			}
		}
		return null;
	}

	/**
	 * Plays the song and performs any effects; only called on the server
	 */
	public void playSong(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			player.worldObj.playSoundAtEntity(player, getSoundString(), 1.0F, 1.0F);
			// notify all ISongBlocks within an 8 block radius (radius could be determined by song?)
			notifySongBlocks(player.worldObj, player, 8);
			// notify all ISongEntities within 8 block radius
			notifySongEntities(player.worldObj, player, 8);
			if (!isEnabled) {
				return;
			}
			// perform main effect
			switch(this) {
			case EPONAS_SONG:
				List<EntityHorse> horses = player.worldObj.getEntitiesWithinAABB(EntityHorse.class, player.boundingBox.expand(8.0D, 4.0D, 8.0D));
				for (EntityHorse horse : horses) {
					// TODO check random chance?
					if (!horse.isTame()) {
						horse.setTamedBy(player);
					}
				}
				break;
			case SONG_OF_STORMS:
				WorldInfo worldinfo = MinecraftServer.getServer().worldServers[0].getWorldInfo();
				if (worldinfo.isRaining()) {
					worldinfo.setRainTime(0);
					worldinfo.setRaining(false);
				} else {
					worldinfo.setRainTime(2000);
					worldinfo.setRaining(true);
				}
				if (worldinfo.isThundering()) {
					worldinfo.setThunderTime(0);
					worldinfo.setThundering(false);
				} else {
					worldinfo.setThunderTime(2000);
					worldinfo.setThundering(true);
				}
				break;
			case SUN_SONG:
				for (int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
					WorldServer worldserver = MinecraftServer.getServer().worldServers[i];
					worldserver.setWorldTime(worldserver.getWorldTime() + (long) 12000); // adds half a day
				}
				break;
			default: // activating nearby ISongBlocks only, no other effect
			}
		}
	}

	/**
	 * Notifies all {@link ISongBlock}s within the given radius of the song played
	 */
	private void notifySongBlocks(World world, EntityPlayer player, int radius) {
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.boundingBox.minY);
		int z = MathHelper.floor_double(player.posZ);
		for (int i = (x - radius); i <= (x + radius); ++i) {
			for (int j = (y - (radius / 2)); j <= (y + (radius / 2)); ++ j) {
				for (int k = (z - radius); k <= (z + radius); ++k) {
					Block block = world.getBlock(i, j, k);
					if (block instanceof ISongBlock) {
						((ISongBlock) block).onSongPlayed(world, i, j, k, player, this);
					}
				}
			}
		}
	}

	/**
	 * Notifies all {ISongEntity}s within the given radius of the song played
	 */
	private void notifySongEntities(World world, EntityPlayer player, int radius) {
		List<ISongEntity> entities = world.getEntitiesWithinAABB(ISongEntity.class, player.boundingBox.expand(radius, (double) radius / 2.0D, radius));
		for (ISongEntity entity : entities) {
			entity.onSongPlayed(player, this);
		}
	}
}

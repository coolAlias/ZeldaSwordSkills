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

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;

public enum ZeldaSong {
	ZELDAS_LULLABY("lullaby", 100, SongNote.B2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2, SongNote.A2),
	TIME_SONG("time", 100, SongNote.A2, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D1, SongNote.F1),
	EPONAS_SONG("epona", 100, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.D2, SongNote.B2, SongNote.A2),
	STORMS_SONG("storms", 100, SongNote.D1, SongNote.F1, SongNote.D2, SongNote.D1, SongNote.F1, SongNote.D2),
	SUN_SONG("sun", 100, SongNote.A2, SongNote.F1, SongNote.D2, SongNote.A2, SongNote.F1, SongNote.D2),
	SARIAS_SONG("saria", 100, SongNote.F1, SongNote.A2, SongNote.B2, SongNote.F1, SongNote.A2, SongNote.B2),
	SCARECROW_SONG("scarecrow", 100), // user-defined song?
	FOREST_MINUET("minuet", 100, SongNote.D1, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.B2, SongNote.A2),
	FIRE_BOLERO("bolero", 100, SongNote.F1, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.A2, SongNote.F1),
	WATER_SERENADE("serenade", 100, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.A2, SongNote.B2),
	SPIRIT_REQUIEM("requiem", 100, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.D1),
	SHADOW_NOCTURNE("nocturne", 100, SongNote.B2, SongNote.A2, SongNote.A2, SongNote.D1, SongNote.B2, SongNote.A2, SongNote.F1),
	LIGHT_PRELUDE("prelude", 100, SongNote.D2, SongNote.A2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2),
	HEALING_SONG("healing", 100, SongNote.B2, SongNote.A2, SongNote.F1, SongNote.B2, SongNote.A2, SongNote.F1);

	private final String unlocalizedName;

	/** Minimum duration song required to play before any effect occurs, in ticks */
	private final int minDuration;

	/** Notes required to play the song */
	private final SongNote[] notes;

	/** Whether the song's main effect is enabled */
	private boolean isEnabled;

	private ZeldaSong(String unlocalizedName, int minDuration, SongNote... notes) {
		this.unlocalizedName = unlocalizedName;
		this.minDuration = minDuration;
		this.notes = notes;
		this.isEnabled = true;
	}

	/** Returns the translated name of this song */
	@Override
	public String toString() {
		return StatCollector.translateToLocal("song.zss." + unlocalizedName + ".name");
	}

	/** Returns the minimum number of ticks the song must be allowed to play before any effects will occur */
	public int getMinDuration() {
		return minDuration;
	}

	/** Returns the sound file to play for this song */
	public String getSoundString() {
		return ModInfo.ID + ":song." + unlocalizedName;
	}

	/** Enables or disables this song's main effect, but not notification of {@link ISoundBlock}s */
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * If the notes played make up a valid melody, that song will be returned.
	 * @return	Null if the notes are not a valid song
	 */
	public static ZeldaSong getSongFromNotes(List<SongNote> notesPlayed) {
		for (ZeldaSong song : ZeldaSong.values()) {
			// TODO special case for Scarecrow Song? Would then need a player instance
			if (song.notes != null && song.notes.length == notesPlayed.size()) {
				boolean isIdentical = true;
				for (int i = 0; i < song.notes.length && isIdentical; ++i) {
					isIdentical = (song.notes[i] == notesPlayed.get(i));
				}
				if (isIdentical) {
					return song;
				}
			}
		}
		return null;
	}

	/**
	 * Performs any effects of the song when played; only called on the server
	 */
	public void performSongEffects(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			// notify all ISongBlocks within an 8 block radius (radius could be determined by song?)
			notifySongBlocks(player.worldObj, player, 8);
			// notify all ISongEntities within 8 block radius
			notifySongEntities(player.worldObj, player, 8);
			if (!isEnabled) {
				PlayerUtils.sendChat(player, "This song's main effect has been disabled.");
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
						// flag for heart particles
						player.worldObj.setEntityState(horse, (byte) 18);
					}
				}
				break;
			case HEALING_SONG:
				// TODO only allow once per day
				player.curePotionEffects(new ItemStack(Items.milk_bucket));
				player.heal(player.getMaxHealth());
				break;
			case STORMS_SONG:
				if (player.worldObj instanceof WorldServer) {
					WorldInfo worldinfo = ((WorldServer) player.worldObj).getWorldInfo();
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
		int affected = 0;
		for (int i = (x - radius); i <= (x + radius); ++i) {
			for (int j = (y - (radius / 2)); j <= (y + (radius / 2)); ++ j) {
				for (int k = (z - radius); k <= (z + radius); ++k) {
					Block block = world.getBlock(i, j, k);
					if (block instanceof ISongBlock) {
						if (((ISongBlock) block).onSongPlayed(world, i, j, k, player, this, affected)) {
							++affected;
						}
					}
				}
			}
		}
	}

	/**
	 * Notifies all {ISongEntity}s within the given radius of the song played
	 */
	private void notifySongEntities(World world, EntityPlayer player, int radius) {
		int affected = 0;
		List<ISongEntity> entities = world.getEntitiesWithinAABB(ISongEntity.class, player.boundingBox.expand(radius, (double) radius / 2.0D, radius));
		for (ISongEntity entity : entities) {
			if (entity.onSongPlayed(player, this, affected)) {
				++affected;
			}
		}
	}
}

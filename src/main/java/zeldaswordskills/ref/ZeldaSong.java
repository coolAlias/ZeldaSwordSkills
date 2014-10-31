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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldInfo;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.api.entity.ISongEntity;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.entity.ai.EntityAITeleport;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.PlaySoundPacket;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.WarpPoint;
import zeldaswordskills.world.TeleporterNoPortal;

public enum ZeldaSong {
	ZELDAS_LULLABY("lullaby", 260, SongNote.B2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2, SongNote.A2),
	TIME_SONG("time", 212, SongNote.A2, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D1, SongNote.F1),
	EPONAS_SONG("epona", 170, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.D2, SongNote.B2, SongNote.A2),
	STORMS_SONG("storms", 315, SongNote.D1, SongNote.F1, SongNote.D2, SongNote.D1, SongNote.F1, SongNote.D2),
	SUN_SONG("sun", 110, SongNote.A2, SongNote.F1, SongNote.D2, SongNote.A2, SongNote.F1, SongNote.D2),
	SARIAS_SONG("saria", 100, SongNote.F1, SongNote.A2, SongNote.B2, SongNote.F1, SongNote.A2, SongNote.B2),
	SCARECROW_SONG("scarecrow", 160), // user-defined song, 8 notes long at 20 ticks each
	FOREST_MINUET("minuet", 290, SongNote.D1, SongNote.D2, SongNote.B2, SongNote.A2, SongNote.B2, SongNote.A2),
	FIRE_BOLERO("bolero", 100, SongNote.F1, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.A2, SongNote.F1),
	WATER_SERENADE("serenade", 100, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.A2, SongNote.B2),
	SPIRIT_REQUIEM("requiem", 100, SongNote.D1, SongNote.F1, SongNote.D1, SongNote.A2, SongNote.F1, SongNote.D1),
	SHADOW_NOCTURNE("nocturne", 270, SongNote.B2, SongNote.A2, SongNote.A2, SongNote.D1, SongNote.B2, SongNote.A2, SongNote.F1),
	ORDER_OATH("order", 100, SongNote.A2, SongNote.F1, SongNote.D1, SongNote.F1, SongNote.A2, SongNote.D2),
	LIGHT_PRELUDE("prelude", 100, SongNote.D2, SongNote.A2, SongNote.D2, SongNote.A2, SongNote.B2, SongNote.D2),
	HEALING_SONG("healing", 150, SongNote.B2, SongNote.A2, SongNote.F1, SongNote.B2, SongNote.A2, SongNote.F1),
	SOARING_SONG("soaring", 100, SongNote.F1, SongNote.B2, SongNote.D2, SongNote.F1, SongNote.B2, SongNote.D2);

	private final String unlocalizedName;

	/** Minimum duration song required to play before any effect occurs, in ticks */
	private final int minDuration;

	/** Notes required to play the song */
	private final List<SongNote> notes;

	/** Whether the song's main effect is enabled */
	private boolean isEnabled;

	private ZeldaSong(String unlocalizedName, int minDuration, SongNote... notes) {
		this.unlocalizedName = unlocalizedName;
		this.minDuration = minDuration;
		this.notes = Collections.unmodifiableList(Arrays.asList(notes));
		this.isEnabled = true;
	}

	/** Returns the translated name of this song */
	@Override
	public String toString() {
		return StatCollector.translateToLocal("song.zss." + unlocalizedName + ".name");
	}

	public String getUnlocalizedName() {
		return unlocalizedName;
	}

	/** Returns the sound file to play for this song */
	public String getSoundString() {
		return ModInfo.ID + ":song." + unlocalizedName;
	}

	/** Returns the minimum number of ticks the song must be allowed to play before any effects will occur */
	public int getMinDuration() {
		return minDuration;
	}

	/** Returns unmodifiable list of notes required to play this song */
	public List<SongNote> getNotes() {
		return notes;
	}

	/** Enables or disables this song's main effect, but not notification of {@link ISoundBlock}s */
	public void setIsEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Returns the ZeldaSong matching the unlocalized name given, or null
	 */
	public static ZeldaSong getSongFromUnlocalizedName(String name) {
		if (name != null && name.length() > 0) {
			for (ZeldaSong song : ZeldaSong.values()) {
				if (song.getUnlocalizedName().equalsIgnoreCase(name)) {
					return song;
				}
			}
		}
		return null;
	}

	/** Returns true if the notes played are the correct notes to play this song */
	public boolean areCorrectNotes(List<SongNote> notesPlayed) {
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
	 * If the notes played make up a valid melody, that song will be returned.
	 * @return	Null if the notes are not a valid song
	 */
	public static ZeldaSong getSongFromNotes(List<SongNote> notesPlayed) {
		for (ZeldaSong song : ZeldaSong.values()) {
			if (song.areCorrectNotes(notesPlayed)) {
				return song;
			}
		}
		return null;
	}

	/**
	 * Checks if song's notes are contained within the notesPlayed, starting from the first note
	 * @param notesPlayed	May have the same number or more notes than the song
	 */
	public boolean isSongPartOfNotes(List<SongNote> notesPlayed) {
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
	 * Returns true if the notes played are unique enough to form a new song
	 */
	public static boolean areNotesUnique(List<SongNote> notesPlayed) {
		for (ZeldaSong song : ZeldaSong.values()) {
			if (song.isSongPartOfNotes(notesPlayed)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs any effects of the song when played; only called on the server
	 */
	public void performSongEffects(EntityPlayer player) {
		if (!player.worldObj.isRemote) {
			ItemStack instrument = player.getHeldItem();
			if (instrument == null || !(instrument.getItem() instanceof ItemInstrument)) {
				return;
			}
			int power = ((ItemInstrument) instrument.getItem()).getSongStrength(instrument);
			if (power < 1) {
				return;
			}
			// notify all ISongBlocks within an 8 block radius (radius could be determined by song?)
			notifySongBlocks(player.worldObj, player, power, 8);
			// notify all ISongEntities within 8 block radius
			notifySongEntities(player.worldObj, player, power, 8);
			if (!isEnabled) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.disabled"));
				return;
			}

			switch(this) {
			case EPONAS_SONG:
				// Only max power instruments can teleport a horse
				if (power > 4 && player.worldObj.provider.isSurfaceWorld() &&
				player.worldObj.canBlockSeeTheSky(MathHelper.floor_double(player.posX),
						MathHelper.floor_double(player.boundingBox.maxY),
						MathHelper.floor_double(player.posZ)))
				{
					EntityHorse epona = ZSSPlayerSongs.get(player).getLastHorseRidden();
					if (epona != null) {
						// TODO check for clear space where horse should spawn?
						if (epona.riddenByEntity != null) {
							epona.riddenByEntity.mountEntity(null);
						}
						if (epona.getLeashed()) {
							epona.clearLeashed(true, true);
						}
						((WorldServer) player.worldObj).getEntityTracker().removeEntityFromAllTrackingPlayers(epona);
						Vec3 vec3 = player.getLookVec();
						epona.setPosition(player.posX + (vec3.xCoord * 2.0D), player.posY + 1, player.posZ + (vec3.zCoord * 2.0D));
						((WorldServer) player.worldObj).getEntityTracker().addEntityToTracker(epona);
						epona.makeHorseRearWithSound();
					}
				}
				List<EntityHorse> horses = player.worldObj.getEntitiesWithinAABB(EntityHorse.class, player.boundingBox.expand(8.0D, 4.0D, 8.0D));
				for (EntityHorse horse : horses) {
					if (!horse.isTame()) {
						horse.setTamedBy(player);
						// 18 is flag for heart particles
						player.worldObj.setEntityState(horse, (byte) 18);
					}
				}
				break;
			case HEALING_SONG:
				if (power > 4 && ZSSPlayerSongs.get(player).canHealFromSong()) {
					PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
					player.curePotionEffects(new ItemStack(Items.milk_bucket));
					player.heal(player.getMaxHealth());
					ZSSPlayerSongs.get(player).setNextHealTime();
				}
				break;
			case SCARECROW_SONG:
				// TODO
				PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
				break;
			case SOARING_SONG:
				// Not usable in the Nether or the End, mainly due to unpredictable results
				if (power > 4 && Math.abs(player.dimension) != 1) {
					ChunkCoordinates cc = player.getBedLocation(player.dimension);
					if (cc != null) {
						cc = EntityPlayer.verifyRespawnCoordinates(player.worldObj, cc, player.isSpawnForced(player.dimension));
					}
					if (cc == null) {
						cc = player.worldObj.getSpawnPoint();
					}
					if (cc != null) {
						if (player.ridingEntity != null) {
							player.mountEntity(null);
						}
						player.setPosition((double) cc.posX + 0.5D, (double) cc.posY + 0.1D, (double) cc.posZ + 0.5D);
						while (!player.worldObj.getCollidingBoundingBoxes(player, player.boundingBox).isEmpty()) {
							player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
						}
						player.setPositionAndUpdate(player.posX, player.posY, player.posZ);
						PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
					}
				}
				break;
			case STORMS_SONG:
				if (power > 4 && player.worldObj instanceof WorldServer) {
					PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
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
				if (power > 4) {
					PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
					long time = (player.worldObj.getWorldTime() % 24000);
					long addTime = (time < 12000) ? (12000 - time) : (24000 - time);
					for (int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
						WorldServer worldserver = MinecraftServer.getServer().worldServers[i];
						worldserver.setWorldTime(worldserver.getWorldTime() + addTime);
					}
				}
				break;
				// All warping songs
			case FOREST_MINUET:
			case FIRE_BOLERO:
			case WATER_SERENADE:
			case SPIRIT_REQUIEM:
			case SHADOW_NOCTURNE:
			case LIGHT_PRELUDE:
				WarpPoint warp = ZSSPlayerSongs.get(player).getWarpPoint(this);
				if (power > 4 && warp != null) {
					int dimension = player.worldObj.provider.dimensionId;
					if (dimension == 1 && warp.dimensionId != 1) { // can't teleport from the end to other dimensions
						PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.warp.end"));
					} else {
						if (player.ridingEntity != null) {
							player.mountEntity(null);
						}
						double dx = player.posX;
						double dy = player.posY;
						double dz = player.posZ;
						if (dimension != warp.dimensionId) {
							((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, warp.dimensionId, new TeleporterNoPortal((WorldServer) player.worldObj));
						}
						boolean noBlock = false;
						boolean noAir = false;
						Block block = player.worldObj.getBlock(warp.x, warp.y, warp.z);
						int meta = player.worldObj.getBlockMetadata(warp.x, warp.y, warp.z);
						if (block instanceof BlockWarpStone && BlockWarpStone.warpBlockSongs.get(meta) == this) {
							if (!EntityAITeleport.teleportTo(player.worldObj, player, (double) warp.x + 0.5D, warp.y + 1, (double) warp.z + 0.5D, null, true, false)) {
								noBlock = true;
							}
						} else {
							noAir = true;
						}
						// set back to original dimension and position if it failed
						if (noBlock || noAir) {
							if (dimension != warp.dimensionId) {
								((EntityPlayerMP) player).mcServer.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, dimension, new TeleporterNoPortal((WorldServer) player.worldObj));
							}
							player.setPositionAndUpdate(dx, dy, dz);
							PlayerUtils.sendChat(player, StatCollector.translateToLocal(noBlock ? "chat.zss.song.warp.blocked" : "chat.zss.song.warp.missing"));
						} else {
							PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
						}
					}
				}
				break;
			default: // activating nearby ISongBlocks only, no other effect
			}
		}
	}

	/**
	 * Notifies all {@link ISongBlock}s within the given radius of the song played
	 */
	private void notifySongBlocks(World world, EntityPlayer player, int power, int radius) {
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.boundingBox.minY);
		int z = MathHelper.floor_double(player.posZ);
		int affected = 0;
		for (int i = (x - radius); i <= (x + radius); ++i) {
			for (int j = (y - (radius / 2)); j <= (y + (radius / 2)); ++ j) {
				for (int k = (z - radius); k <= (z + radius); ++k) {
					Block block = world.getBlock(i, j, k);
					if (block instanceof ISongBlock) {
						if (((ISongBlock) block).onSongPlayed(world, i, j, k, player, this, power, affected)) {
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
	private void notifySongEntities(World world, EntityPlayer player, int power, int radius) {
		int affected = 0;
		List<ISongEntity> entities = world.getEntitiesWithinAABB(ISongEntity.class, player.boundingBox.expand(radius, (double) radius / 2.0D, radius));
		for (ISongEntity entity : entities) {
			if (entity.onSongPlayed(player, this, power, affected)) {
				++affected;
			}
		}
	}
}

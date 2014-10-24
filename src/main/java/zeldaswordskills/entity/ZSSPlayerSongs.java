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

package zeldaswordskills.entity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.Constants;
import zeldaswordskills.ZSSAchievements;
import zeldaswordskills.block.BlockWarpStone;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.PlaySoundPacket;
import zeldaswordskills.network.packet.client.LearnSongPacket;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.WarpPoint;
import zeldaswordskills.util.WorldUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ZSSPlayerSongs
{
	private final EntityPlayer player;

	private final Set<ZeldaSong> knownSongs = EnumSet.noneOf(ZeldaSong.class);

	/** Coordinates of most recently activated Warp Stones, stored using the block's metadata as key */
	private final Map<Integer, WarpPoint> warpPoints = new HashMap<Integer, WarpPoint>();

	/** Song to be learned from the learning GUI is set by the block or entity triggering the GUI */
	@SideOnly(Side.CLIENT)
	public ZeldaSong songToLearn;

	/** Notes set by the player to play the Scarecrow's Song */
	private final List<SongNote> scarecrowNotes = new ArrayList<SongNote>();

	/** World time marking one week after player first played the Scarecrow Song */
	private long scarecrowTime;

	/** World time at which this player will next be able to use the Song of Healing */
	private long nextSongHealTime;

	/** UUID of last horse ridden, for playing Epona's Song (persistent across world saves) */
	private UUID horseUUID = null;

	/** Entity ID of last horse ridden, should be more efficient when getting entity */
	private int horseId = -1;

	/** Set of all NPCs this player has cured */
	private final Set<String> curedNpcs = new HashSet<String>();

	public ZSSPlayerSongs(EntityPlayer player) {
		this.player = player;
	}

	public static ZSSPlayerSongs get(EntityPlayer player) {
		return ZSSPlayerInfo.get(player).getPlayerSongs();
	}

	/**
	 * Returns true if the player knows the song
	 */
	public boolean isSongKnown(ZeldaSong song) {
		return knownSongs.contains(song);
	}

	/**
	 * Adds the song to the player's repertoire, or returns false if already known.
	 * When called on the server, sends a packet to update the client.
	 * @param notes	Only used when learning the Scarecrow Song, otherwise null
	 */
	public boolean learnSong(ZeldaSong song, List<SongNote> notes) {
		boolean addSong = true;
		if (isSongKnown(song)) {
			return false;
		} else if (song == ZeldaSong.SCARECROW_SONG) {
			if (notes == null || notes.size() != 8 || !ZeldaSong.areNotesUnique(notes)) {
				LogHelper.warning("Trying to add Scarecrow's Song with invalid list: " + notes);
				return false;
			}
			// first time add notes only
			if (scarecrowNotes.isEmpty()) {
				addSong = false;
				scarecrowNotes.addAll(notes);
				scarecrowTime = player.worldObj.getWorldTime() + (24000 * 7);
			} else if (player.worldObj.getWorldTime() > scarecrowTime) {
				// validate notes before adding the song for good
				for (int i = 0; i < scarecrowNotes.size() && addSong; ++i) {
					addSong = (scarecrowNotes.get(i) == notes.get(i));
				}
			} else if (!player.worldObj.isRemote) { // only play chat once
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.scarecrow.later"));
			}
		}
		if (addSong) {
			knownSongs.add(song);
			PacketDispatcher.sendTo(new PlaySoundPacket(Sounds.SUCCESS, 1.0F, 1.0F), (EntityPlayerMP) player);
			player.triggerAchievement(ZSSAchievements.ocarinaSong);
			if (song == ZeldaSong.SCARECROW_SONG) {
				player.triggerAchievement(ZSSAchievements.ocarinaScarecrow);
			}
			if (knownSongs.size() == ZeldaSong.values().length) {
				player.triggerAchievement(ZSSAchievements.ocarinaMaestro);
			}
			if (!player.worldObj.isRemote) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocalFormatted("chat.zss.song.learned", song.toString()));
				PacketDispatcher.sendTo(new LearnSongPacket(song, notes), (EntityPlayerMP) player);
			}
		}
		return true;
	}

	/**
	 * Checks the player's known songs to see if any match the notes played
	 * @return	The song matching the notes played or null
	 */
	public ZeldaSong getKnownSongFromNotes(List<SongNote> notesPlayed) {
		for (ZeldaSong song : knownSongs) {
			if (song == ZeldaSong.SCARECROW_SONG) {
				if (notesPlayed != null && notesPlayed.size() == scarecrowNotes.size()) {
					for (int i = 0; i < scarecrowNotes.size(); ++i) {
						if (notesPlayed.get(i) != scarecrowNotes.get(i)) {
							return null;
						}
					}
					return song;
				}
			} else if (song.areCorrectNotes(notesPlayed)) {
				return song;
			}
		}
		return null;
	}

	/**
	 * Call each time a warp stone is activated to set the warp coordinates for that block type
	 */
	public void onActivatedWarpStone(int x, int y, int z, int meta) {
		if (warpPoints.containsKey(meta)) {
			warpPoints.remove(meta);
		}
		warpPoints.put(meta, new WarpPoint(player.worldObj.provider.dimensionId, x, y, z));
	}

	/**
	 * Returns the chunk coordinates to warp to for the various warp songs, or null if not yet set
	 */
	public WarpPoint getWarpPoint(ZeldaSong song) {
		Integer meta = BlockWarpStone.reverseLookup.get(song);
		return (meta == null ? null : warpPoints.get(meta));
	}

	/**
	 * Returns true if the player can open the Scarecrow Song gui: i.e.,
	 * notes have not been set or song not yet learned and enough time has passed,
	 * with appropriate chat messages for failed conditions.
	 */
	public boolean canOpenScarecrowGui(boolean addChat) {
		if (scarecrowNotes.isEmpty()) {
			return true;
		} else if (isSongKnown(ZeldaSong.SCARECROW_SONG)) {
			if (addChat) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.scarecrow.known"));
			}
			return false;
		} else if (player.worldObj.getWorldTime() < scarecrowTime) {
			if (addChat) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.scarecrow.later"));
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns true if the player can currently benefit from the Song of Healing
	 */
	public boolean canHealFromSong() {
		return player.getHealth() < player.getMaxHealth() && player.worldObj.getWorldTime() > nextSongHealTime;
	}

	/**
	 * Sets the next time the player can benefit from the Song of Healing
	 */
	public void setNextHealTime() {
		nextSongHealTime = player.worldObj.getWorldTime() + 24000;
	}

	/**
	 * Returns a copy of the notes set for the Scarecrow song, if any
	 */
	public List<SongNote> getScarecrowNotes() {
		return new ArrayList<SongNote>(scarecrowNotes);
	}

	/**
	 * Returns last horse ridden or null if unavailable for some reason
	 */
	public EntityHorse getLastHorseRidden() {
		Entity entity = (horseId < 0 ? null : player.worldObj.getEntityByID(horseId));
		if (entity == null && horseUUID != null) {
			entity = WorldUtils.getEntityByUUID(player.worldObj, horseUUID);
			if (entity != null) {
				horseId = entity.getEntityId();
			}
		}
		if (entity instanceof EntityHorse && entity.isEntityAlive()) {
			return (EntityHorse) entity;
		}
		// don't reset id fields, as horse may simply be in an unloaded chunk
		return null;
	}

	/**
	 * Sets the horse as this player's last horse ridden, for Epona's Song
	 */
	public void setHorseRidden(EntityHorse horse) {
		if (horse.getEntityId() != horseId && horse.isTame() && horse.func_152119_ch().equals(player.getUniqueID().toString())) {
			this.horseId = horse.getEntityId();
			this.horseUUID = horse.getPersistentID();
		}
	}

	/**
	 * Returns whether this player has already cured an Npc with the given name
	 */
	public boolean hasCuredNpc(String name) {
		return curedNpcs.contains(name);
	}

	/**
	 * Call after curing an Npc to save that information
	 * @return false if the Npc has already been marked as cured by this player
	 */
	public boolean onCuredNpc(String name) {
		return curedNpcs.add(name);
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList songs = new NBTTagList();
		for (ZeldaSong song : knownSongs) {
			NBTTagCompound tag = new NBTTagCompound();
			// using unlocalized name instead of ordinal in case enum order/size ever changes
			tag.setString("song", song.getUnlocalizedName());
			songs.appendTag(tag);
		}
		compound.setTag("KnownSongs", songs);

		if (!scarecrowNotes.isEmpty()) {
			int[] notes = new int[scarecrowNotes.size()];
			for (int i = 0; i < scarecrowNotes.size(); ++i) {
				notes[i] = scarecrowNotes.get(i).ordinal();
			}
			compound.setTag("ScarecrowNotes", new NBTTagIntArray(notes));
		}

		compound.setLong("ScarecrowTime", scarecrowTime);
		compound.setLong("NextSongHealTime", nextSongHealTime);
		if (horseUUID != null) {
			compound.setLong("HorseUUIDMost", horseUUID.getMostSignificantBits());
			compound.setLong("HorseUUIDLeast", horseUUID.getLeastSignificantBits());
		}

		if (!warpPoints.isEmpty()) {
			NBTTagList warpList = new NBTTagList();
			for (Integer i : warpPoints.keySet()) {
				WarpPoint warp = warpPoints.get(i);
				if (warp != null) {
					NBTTagCompound warpTag = warp.writeToNBT();
					warpTag.setInteger("WarpKey", i);
					warpList.appendTag(warpTag);
				} else {
					LogHelper.warning("NULL warp point stored in map with key " + i);
				}
			}
			compound.setTag("WarpList", warpList);
		}

		if (!curedNpcs.isEmpty()) {
			NBTTagList npcs = new NBTTagList();
			for (String name : curedNpcs) {
				NBTTagCompound npc = new NBTTagCompound();
				npc.setString("NpcName", name);
				npcs.appendTag(npc);
			}
			compound.setTag("CuredNpcs", npcs);
		}
	}

	public void loadNBTData(NBTTagCompound compound) {
		NBTTagList songs = compound.getTagList("KnownSongs", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < songs.tagCount(); ++i) {
			NBTTagCompound tag = songs.getCompoundTagAt(i);
			ZeldaSong song = ZeldaSong.getSongFromUnlocalizedName(tag.getString("song"));
			if (song != null) {
				knownSongs.add(song);
			}
		}

		if (compound.hasKey("ScarecrowNotes")) {
			try {
				int[] notes = compound.getIntArray("ScarecrowNotes");
				for (int n : notes) {
					scarecrowNotes.add(SongNote.values()[n]);
				}
			} catch (Exception e) {
				LogHelper.warning("Exception thrown while loading Scarecrow's Song notes: " + e.getMessage());
			}
		}

		scarecrowTime = compound.getLong("ScarecrowTime");
		nextSongHealTime = compound.getLong("NextHealSongTime");
		if (compound.hasKey("HorseUUIDMost") && compound.hasKey("HorseUUIDLeast")) {
			horseUUID = new UUID(compound.getLong("HorseUUIDMost"), compound.getLong("HorseUUIDLeast"));
		}

		if (compound.hasKey("WarpList")) {
			NBTTagList warpList = compound.getTagList("WarpList", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < warpList.tagCount(); ++i) {
				NBTTagCompound warpTag = warpList.getCompoundTagAt(i);
				WarpPoint warp = WarpPoint.readFromNBT(warpTag);
				warpPoints.put(warpTag.getInteger("WarpKey"), warp);
			}
		}

		if (compound.hasKey("CuredNpcs")) {
			NBTTagList npcs = compound.getTagList("CuredNpcs", Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < npcs.tagCount(); ++i) {
				NBTTagCompound npc = npcs.getCompoundTagAt(i);
				curedNpcs.add(npc.getString("NpcName"));
			}
		}
	}
}

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

package zeldaswordskills.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.bidirectional.LearnSongPacket;
import zeldaswordskills.network.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.server.ZeldaSongPacket;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.TimedChatDialogue;

@SideOnly(Side.CLIENT)
public class GuiOcarina extends GuiMusicBase
{
	protected static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_ocarina.png");

	/** Notes played when learning the Scarecrow's Song */
	private List<SongNote> scarecrowNotes;

	/** Whether this is the first time the Scarecrow Song is being played */
	private boolean scarecrowFirst;

	/** Index of note currently being played, used for the Scarecrow Song only */
	private int currentNoteIndex;

	/** If the player was sneaking when opening the GUI, the song is played without effect */
	private final boolean wasSneaking;

	public GuiOcarina(int x, int y, int z) {
		this(x, y, z, false);
	}

	public GuiOcarina(int x, int y, int z, boolean isScarecrow) {
		super(x, y, z);
		if (isScarecrow) {
			scarecrowNotes = ZSSPlayerSongs.get(mc.thePlayer).getScarecrowNotes();
			if (scarecrowNotes == null || scarecrowNotes.isEmpty()) {
				scarecrowFirst = true;
				scarecrowNotes = new ArrayList<SongNote>();
			}
		}
		wasSneaking = mc.thePlayer.isSneaking();
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scarecrowFirst) {
			new TimedChatDialogue(mc.thePlayer, Arrays.asList(
					StatCollector.translateToLocal("chat.zss.song.scarecrow.greet.0"),
					StatCollector.translateToLocal("chat.zss.song.scarecrow.greet.1")),
					0, 1600);
		} else if (scarecrowNotes != null && !scarecrowNotes.isEmpty()) {
			new TimedChatDialogue(mc.thePlayer, Arrays.asList(
					StatCollector.translateToLocal("chat.zss.song.scarecrow.last.0"),
					StatCollector.translateToLocal("chat.zss.song.scarecrow.last.1")),
					0, 1600);
		}
	}

	@Override
	protected ResourceLocation getTexture() {
		return texture;
	}

	@Override
	public void updateScreen() {
		if (song == ZeldaSongs.songScarecrow && scarecrowNotes != null) {
			if (++ticksSinceLastNote == 20) {
				if (currentNoteIndex == scarecrowNotes.size()) {
					mc.thePlayer.closeScreen();
				} else {
					onNotePlayed(scarecrowNotes.get(currentNoteIndex++));
				}
			}
		} else {
			super.updateScreen();
		}
	}

	@Override
	public void onGuiClosed() {
		if (song != null) {
			if (ticksSinceLastNote > song.getMinDuration() || (scarecrowNotes != null && currentNoteIndex == scarecrowNotes.size())) {
				if (scarecrowFirst || (scarecrowNotes != null && !ZSSPlayerSongs.get(mc.thePlayer).isSongKnown(ZeldaSongs.songScarecrow))) {
					PacketDispatcher.sendToServer(new LearnSongPacket(song, scarecrowNotes));
				} else if (!wasSneaking) {
					PacketDispatcher.sendToServer(new ZeldaSongPacket(song));
				}
			} else {
				PacketDispatcher.sendToServer(new PlayRecordPacket(null, new BlockPos(x, y, z)));
			}
		}
	}

	@Override
	protected void onNoteAdded() {
		// For learning Scarecrow's Song, enter 8 notes once, then same notes again to send AddSongPacket to server
		if (scarecrowNotes != null) {
			if (melody.size() == 8) {
				if (!ZeldaSongs.areNotesUnique(melody)) {
					melody.clear();
					PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.song.scarecrow.copycat");
				} else if (scarecrowNotes.isEmpty()) {
					boolean flag = true;
					for (int i = 0; i < (melody.size() - 1) && flag; ++i) {
						flag = (melody.get(i) == melody.get(i + 1));
					}
					if (flag) {
						melody.clear();
						PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.song.scarecrow.boring");
					} else {
						scarecrowNotes.addAll(melody);
						PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.song.scarecrow.again");
					}
				} else {
					boolean flag = true;
					for (int i = 0; i < melody.size() && flag; ++i) {
						flag = (scarecrowNotes.get(i) == melody.get(i));
					}
					if (flag) {
						if (scarecrowFirst) {
							new TimedChatDialogue(mc.thePlayer, Arrays.asList(
									StatCollector.translateToLocal("chat.zss.song.scarecrow.first.0"),
									StatCollector.translateToLocal("chat.zss.song.scarecrow.first.1")),
									0, 1600);
						} else {
							new TimedChatDialogue(mc.thePlayer, Arrays.asList(
									StatCollector.translateToLocal("chat.zss.song.scarecrow.learn.0"),
									StatCollector.translateToLocal("chat.zss.song.scarecrow.learn.1")),
									0, 1600);
						}
						song = ZeldaSongs.songScarecrow;
						mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
						ticksSinceLastNote = 0;
					} else {
						melody.clear();
						PlayerUtils.sendTranslatedChat(mc.thePlayer, "chat.zss.song.scarecrow.forgot");
					}
				}
			}
		} else if (melody.size() < 9) { // no songs are longer than 8 notes
			song = ZSSPlayerSongs.get(mc.thePlayer).getKnownSongFromNotes(melody);
			if (song != null) { // indicates player knows the song
				if (song.playSuccessSound()) {
					mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
				}
				if (song == ZeldaSongs.songScarecrow) {
					scarecrowNotes = new ArrayList<SongNote>(melody);
					ticksSinceLastNote = 0;
				} else {
					PacketDispatcher.sendToServer(new PlayRecordPacket(song.getSoundString(), new BlockPos(x, y, z)));
				}
			}
		}
	}
}

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

package zeldaswordskills.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.packet.client.LearnSongPacket;
import zeldaswordskills.network.packet.server.ZeldaSongPacket;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOcarina extends GuiMusicBase
{
	protected static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_ocarina.png");

	/** Notes played when learning the Scarecrow's Song */
	private List<SongNote> scarecrowNotes;

	/** Whether this is the first time the Scarecrow Song is being played */
	private boolean scarecrowFirst;

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
	public void onGuiClosed() {
		if (song != null) {
			if (ticksSinceLastNote > song.getMinDuration()) {
				PacketDispatcher.sendToServer(new ZeldaSongPacket(song));
			} else {
				PacketDispatcher.sendToServer(new PlayRecordPacket(null, x, y, z));
			}
		}
	}

	@Override
	protected void onNoteAdded() {
		// For learning Scarecrow's Song, enter 8 notes once, then same notes again to send AddSongPacket to server
		if (scarecrowNotes != null) {
			if (melody.size() == 8) {
				if (!ZeldaSong.areNotesUnique(melody)) {
					melody.clear();
					PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.scarecrow.copycat"));
				} else if (scarecrowNotes.isEmpty()) {
					boolean flag = true;
					for (int i = 0; i < (melody.size() - 1) && flag; ++i) {
						flag = (melody.get(i) == melody.get(i + 1));
					}
					if (flag) {
						melody.clear();
						PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.scarecrow.boring"));
					} else {
						scarecrowNotes.addAll(melody);
						PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.scarecrow.again"));
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
						song = ZeldaSong.SCARECROW_SONG;
						mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
						PacketDispatcher.sendToServer(new PlayRecordPacket(song.getSoundString(), x, y, z));
						PacketDispatcher.sendToServer(new LearnSongPacket(song, scarecrowNotes));
					} else {
						melody.clear();
						PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.scarecrow.forgot"));
					}
				}
			}
		} else if (melody.size() < 9) { // no songs are longer than 8 notes
			song = ZSSPlayerSongs.get(mc.thePlayer).getKnownSongFromNotes(melody);
			if (song != null) { // indicates player knows the song
				mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
				PacketDispatcher.sendToServer(new PlayRecordPacket(song.getSoundString(), x, y, z));
			}
		}
	}
}
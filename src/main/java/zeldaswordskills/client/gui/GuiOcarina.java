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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.packet.client.AddSongPacket;
import zeldaswordskills.network.packet.server.ZeldaSongPacket;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.SongNote.PlayableNote;
import zeldaswordskills.util.TimedChatDialogue;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOcarina extends GuiScreen
{
	private final Minecraft mc;

	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_ocarina.png");

	/** Maximum number of notes that can display on the GUI at any given time */
	private static final int MAX_NOTES = 8;

	/** Note texture height and width */
	private static final int NOTE_SIZE = 12;

	/** Y interval between lines */
	private static final int INT_Y = 5;

	/** The X size of the window in pixels */
	private int xSize = 213;

	/** The Y size of the window in pixels */
	private int ySize = 90;

	/** Full width of texture file, in pixels */
	private int fullX = 256;

	/** Full height of texture file, in pixels */
	private int fullY = 128;

	/** Starting X position for the Gui */
	private int guiLeft;

	/** Starting Y position for the Gui */
	private int guiTop;

	/** Stores the notes played so far */
	private final List<SongNote> melody = new ArrayList<SongNote>();

	/** Notes played when learning the Scarecrow's Song */
	private List<SongNote> scarecrowNotes;

	/** Whether this is the first time the Scarecrow Song is being played */
	private boolean scarecrowFirst;

	/** Currently playing song, if any */
	private ZeldaSong song;

	/** Number of ticks since last note played; after a certain threshold, current melody clears */
	private int ticksSinceLastNote;

	/** Location of the player when the gui is opened, makes it easier to handle sounds */
	private int x, y, z;

	public GuiOcarina(int x, int y, int z) {
		this(x, y, z, false);
	}

	public GuiOcarina(int x, int y, int z, boolean isScarecrow) {
		mc = Minecraft.getMinecraft();
		this.x = x;
		this.y = y;
		this.z = z;
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
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2 + 25;
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
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		RenderHelperQ.drawTexturedRect(guiLeft, guiTop, 0, 0, xSize, ySize, fullX, fullY);
		GL11.glPopAttrib();
		int i1 = (melody.size() > MAX_NOTES ? ((melody.size() - 1) / MAX_NOTES) * MAX_NOTES : 0);
		for (int i = 0; (i + i1) < melody.size(); ++i) {
			SongNote note = melody.get(i + i1);
			// j is factor of how far down the screen note should be drawn
			int j = SongNote.Note.values().length - (note.note.ordinal() + 1) + (SongNote.Note.values().length * (2 - note.getOctave()));
			int dy = 6 + (INT_Y * j);
			int dx = 40 + (NOTE_SIZE + 8) * i;
			// draw supplementary line(s) under staff and behind note
			if (j > 10) { // j goes from 0-13, not 1-14
				int dy2 = (10 + INT_Y * 11);
				// given the control scheme, this loop is not really necessary as it's not possible to reach the low A note
				for (int n = 0; n < ((j - 9) / 2); ++n) {
					// each line segment is 16x5 pixels, using first line in .png file at 8,15
					RenderHelperQ.drawTexturedRect(guiLeft + (dx - 2), guiTop + dy2 + (n * 2 * INT_Y), 8, 15, 16, 5, fullX, fullY);
				}
			}
			RenderHelperQ.drawTexturedRect(guiLeft + dx, guiTop + dy, xSize, PlayableNote.getOrdinalFromNote(note) * NOTE_SIZE, NOTE_SIZE, NOTE_SIZE, fullX, fullY);
			// draw additional sharp / flat if applicable
			if (note.isSharp() || note.isFlat()) {
				RenderHelperQ.drawTexturedRect(guiLeft + dx + NOTE_SIZE - 2, guiTop + dy, xSize + NOTE_SIZE, (note.isSharp() ? 0 : 5), 5, 5, fullX, fullY);
			}
		}
		if (song != null) {
			String s = song.toString();
			fontRendererObj.drawString(s, guiLeft + (xSize / 2) - (fontRendererObj.getStringWidth(s) / 2), guiTop + 3, 0xFFFFFF);
		}
		super.drawScreen(mouseX, mouseY, f);
	}

	@Override
	public void updateScreen() {
		++ticksSinceLastNote;
		if (song != null) {
			if (ticksSinceLastNote > song.getMinDuration()) {
				mc.thePlayer.closeScreen();
			}
		} else if (ticksSinceLastNote > Config.getNoteResetInterval()) {
			ticksSinceLastNote = 0;
			melody.clear();
		}
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
	protected void keyTyped(char c, int key) {
		// don't let more notes be added while a song is playing
		if (song != null) {
			super.keyTyped(c, key);
			return;
		}
		PlayableNote playedNote = null;
		// Change to use your own KeyBindings, of course
		if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_ATTACK].getKeyCode()) {
			playedNote = PlayableNote.D2; // high D
		} else if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_DOWN].getKeyCode()) {
			playedNote = PlayableNote.F1; // low F
		} else if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_LEFT].getKeyCode()) {
			playedNote = PlayableNote.B2; // high B
		} else if (key == ZSSKeyHandler.keys[ZSSKeyHandler.KEY_RIGHT].getKeyCode()) {
			playedNote = PlayableNote.A2; // high A
		} else if (key == mc.gameSettings.keyBindJump.getKeyCode()) {
			playedNote = PlayableNote.D1; // low D
		}

		// No note key was pressed, call super and get out
		if (playedNote == null) {
			super.keyTyped(c, key);
		} else {
			int modifier = 0;
			// Half-step modifier keys
			if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
				++modifier;
			} else if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
				--modifier;
			}
			// Whole step modifier keys are in addition to half-step modifiers
			if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
				modifier += 2;
			} else if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
				modifier -= 2;
			}

			SongNote note = SongNote.getNote(playedNote, modifier);
			if (note != null) {
				melody.add(note);
				ticksSinceLastNote = 0;
				// play note on client side:
				float f = (float) Math.pow(2.0D, (double)(note.ordinal() - 12) / 12.0D);
				mc.thePlayer.playSound(ModInfo.ID + ":note.ocarina", 3.0F, f);
				Vec3 look = mc.thePlayer.getLookVec();
				mc.theWorld.spawnParticle("note",
						mc.thePlayer.posX + look.xCoord + mc.theWorld.rand.nextDouble() - 0.5D,
						mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + mc.theWorld.rand.nextDouble() - 0.5D,
						mc.thePlayer.posZ + look.zCoord + mc.theWorld.rand.nextDouble() - 0.5D,
						(double) note.ordinal() / 24.0D, 0.0D, 0.0D);
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
								PacketDispatcher.sendToServer(new AddSongPacket(song, scarecrowNotes));
							} else {
								melody.clear();
								PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.scarecrow.forgot"));
							}
						}
					}
				} else if (melody.size() < 9) { // no songs are longer than 8 notes
					song = ZSSPlayerSongs.get(mc.thePlayer).getKnownSongFromNotes(melody);
					// TODO remove following check after implementing song-learning mechanism for all songs
					if (song == null) {
						song = ZeldaSong.getSongFromNotes(melody);
					}
					if (song != null) { // indicates player knows the song
						mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
						PacketDispatcher.sendToServer(new PlayRecordPacket(song.getSoundString(), x, y, z));
					}
				}
			}
		}
	}
}

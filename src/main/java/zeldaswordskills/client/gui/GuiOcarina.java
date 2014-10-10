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
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.server.ZeldaSongPacket;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.SongNote.PlayableNote;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOcarina extends GuiScreen
{
	private final Minecraft mc;

	private static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_ocarina.png");

	/** Note texture height and width */
	private static final int NOTE_SIZE = 6;
	
	/** The X size of the window in pixels */
	private int xSize = 176;

	/** The Y size of the window in pixels */
	private int ySize = 90;

	/** Full width of texture file, in pixels */
	private int fullX = 200;
	
	/** Full height of texture file, in pixels */
	private int fullY = 100;

	/** Starting X position for the Gui */
	private int guiLeft;

	/** Starting Y position for the Gui */
	private int guiTop;

	/** Stores the notes played so far */
	private final List<SongNote> melody = new ArrayList<SongNote>();

	/** Currently playing song, if any */
	private ZeldaSong song;

	/** Number of ticks since last note played; after a certain threshold, current melody clears */
	private int ticksSinceLastNote;

	public GuiOcarina() {
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2; // TODO adjust downward on screen
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(texture);
		RenderHelperQ.drawTexturedRect(guiLeft, guiTop + 50, 0, 0, xSize, ySize, fullX, fullY);
		//drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		for (int i = 0; i < melody.size(); ++i) {
			SongNote note = melody.get(i);
			// increasing y moves further down the screen, so last note is highest
			int dy = NOTE_SIZE * (SongNote.values().length - note.ordinal());
			int dx = 4 + (NOTE_SIZE + 4) * i;
			// assuming note textures are vertically aligned on the right side of the texture
			RenderHelperQ.drawTexturedRect(guiLeft + dx, guiTop + dy, xSize, 0, NOTE_SIZE, NOTE_SIZE, fullX, fullY);
		}
		super.drawScreen(mouseX, mouseY, f);
	}

	@Override
	public void updateScreen() {
		// how long until it clears? should it be configurable?
		if (++ticksSinceLastNote > 30) {
			ticksSinceLastNote = 0;
			melody.clear();
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
			if (Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())) {
				++modifier;
			} else if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
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
				mc.thePlayer.playSound(note.getSoundString(), 1.0F, 1.0F);
				song = ZeldaSong.getSongFromNotes(melody);
				if (song != null) {
					// TODO close Gui ???
					PacketDispatcher.sendToServer(new ZeldaSongPacket(song));
				}
			}
		}
	}
}

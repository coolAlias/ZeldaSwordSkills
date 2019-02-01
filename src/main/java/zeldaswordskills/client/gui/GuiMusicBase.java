/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import zeldaswordskills.client.RenderHelperQ;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.SongNote.PlayableNote;

/**
 * 
 * Basic Zelda music GUI shows notes on the screen as played, leaving it up to
 * child implementations to determine the song played and effects it should have
 *
 */
@SideOnly(Side.CLIENT)
public abstract class GuiMusicBase extends GuiScreen
{
	protected final Minecraft mc;

	/** Maximum number of notes that can display on the GUI at any given time */
	protected static final int MAX_NOTES = 8;

	/** Note texture height and width */
	protected static final int NOTE_SIZE = 12;

	/** Y interval between lines */
	protected static final int INT_Y = 5;

	/** The X size of the window in pixels */
	protected int xSize = 213;

	/** The Y size of the window in pixels */
	protected int ySize = 90;

	/** Full width of texture file, in pixels */
	protected int fullX = 256;

	/** Full height of texture file, in pixels */
	protected int fullY = 128;

	/** Starting X position for the Gui */
	protected int guiLeft;

	/** Starting Y position for the Gui */
	protected int guiTop;

	/** Currently playing song, if any */
	protected AbstractZeldaSong song;

	/** Stores the notes played so far */
	protected final List<SongNote> melody = new ArrayList<SongNote>();

	/** Number of ticks since last note played; after a certain threshold, current melody clears */
	protected int ticksSinceLastNote;

	/** Location of the player when the gui is opened, makes it easier to handle packets and such */
	protected final int x, y, z;

	public GuiMusicBase(int x, int y, int z) {
		mc = Minecraft.getMinecraft();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2 + 25;
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	protected abstract ResourceLocation getTexture();

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(getTexture());
		RenderHelperQ.drawTexturedRect(guiLeft, guiTop, 0, 0, xSize, ySize, fullX, fullY);
		GlStateManager.popAttrib();
		int i1 = (melody.size() > MAX_NOTES ? ((melody.size() - 1) / MAX_NOTES) * MAX_NOTES : 0);
		for (int i = 0; (i + i1) < melody.size(); ++i) {
			SongNote note = melody.get(i + i1);
			// j is factor of how far down the screen note should be drawn
			int j = SongNote.Note.values().length - (note.note.ordinal() + 1) + (SongNote.Note.values().length * (2 - note.getOctave()));
			int dy = 6 + (INT_Y * j);
			int dx = 46 + (NOTE_SIZE + 8) * i;
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
			String s = song.getDisplayName();
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

	/**
	 * Returning false prevents further key inputs from affecting the notes played,
	 * though Esc will still close the GUI; default allows input as long as song is null
	 */
	protected boolean allowKeyInput() {
		return song == null;
	}

	@Override
	protected void keyTyped(char c, int key) throws IOException {
		if (!allowKeyInput()) {
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
				onNotePlayed(note);
				onNoteAdded();
			}
		}
	}

	/**
	 * Called after each note is added; check if notes match a song here
	 */
	protected abstract void onNoteAdded();

	/**
	 * Adds the note to the list of notes played, plays the note sound, and spawns particles.
	 */
	protected void onNotePlayed(SongNote note) {
		melody.add(note);
		ticksSinceLastNote = 0;
		float f = (float) Math.pow(2.0D, (double)(note.ordinal() - 12) / 12.0D);
		// TODO retrieve note to play from player's held ItemInstrument when gui constructed
		mc.thePlayer.playSound(ModInfo.ID + ":note.ocarina", 3.0F, f);
		Vec3 look = mc.thePlayer.getLookVec();
		mc.theWorld.spawnParticle(EnumParticleTypes.NOTE,
				mc.thePlayer.posX + look.xCoord + mc.theWorld.rand.nextDouble() - 0.5D,
				mc.thePlayer.posY + look.yCoord + mc.thePlayer.getEyeHeight() + mc.theWorld.rand.nextDouble() - 0.5D,
				mc.thePlayer.posZ + look.zCoord + mc.theWorld.rand.nextDouble() - 0.5D,
				(double) note.ordinal() / 24.0D, 0.0D, 0.0D);
	}
}

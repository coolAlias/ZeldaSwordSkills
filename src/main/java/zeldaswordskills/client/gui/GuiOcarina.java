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
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.server.ZeldaSongPacket;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.SongNote;
import zeldaswordskills.util.SongNote.PlayableNote;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOcarina extends GuiScreen
{
	private final Minecraft mc;

	/** Stores the notes played so far */
	private final List<SongNote> melody = new ArrayList<SongNote>();

	/** Number of ticks since last note played; after a certain threshold, current melody clears */
	private int ticksSinceLastNote;

	public GuiOcarina() {
		mc = Minecraft.getMinecraft();
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
			if (mc.gameSettings.keyBindSprint.getIsKeyPressed()) {
				++modifier;
			} else if (mc.gameSettings.keyBindSneak.getIsKeyPressed()) {
				--modifier;
			}
			// Whole step modifier keys are in addition to half-step modifiers
			if (mc.gameSettings.keyBindForward.getIsKeyPressed()) {
				modifier += 2;
			} else if (mc.gameSettings.keyBindBack.getIsKeyPressed()) {
				modifier -= 2;
			}

			SongNote note = SongNote.getNote(playedNote, modifier);
			if (note != null) {
				melody.add(note);
				ticksSinceLastNote = 0;
				// play note on client side:
				mc.thePlayer.playSound(note.getSoundString(), 1.0F, 1.0F);
				ZeldaSong song = ZeldaSong.getSongFromNotes(melody);
				if (song != null) {
					// TODO close Gui ???
					PacketDispatcher.sendToServer(new ZeldaSongPacket(song));
				}
			}
		}
	}
}

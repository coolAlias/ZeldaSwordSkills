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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.packet.bidirectional.PlayRecordPacket;
import zeldaswordskills.network.packet.client.LearnSongPacket;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.ref.Sounds;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.PlayerUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiLearnSong extends GuiMusicBase
{
	protected static final ResourceLocation texture = new ResourceLocation(ModInfo.ID, "textures/gui/gui_ocarina.png");

	/** The song being learned */
	private ZeldaSong songToLearn;

	/** Index of note currently being played; also used to flag song to learn has finished playing or not */
	private int currentNoteIndex;

	/** Flag set to true when the demo has finished playing */
	private boolean demoPlayed;

	/**
	 * Throws an IllegalArgumentException if {@link ZSSPlayerSongs#songToLearn} is null or empty
	 */
	public GuiLearnSong(int x, int y, int z) {
		super(x, y, z);
		songToLearn = ZSSPlayerSongs.get(mc.thePlayer).songToLearn;
		if (songToLearn == null || songToLearn.getNotes() == null || songToLearn.getNotes().isEmpty()) {
			throw new IllegalArgumentException("ZSSPlayerSongs#songToLearn may not be null or empty when opening the Song Learning GUI!");
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (ZSSPlayerSongs.get(mc.thePlayer).isSongKnown(songToLearn)) {
			PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.forgot"));
		}
	}

	@Override
	protected ResourceLocation getTexture() {
		return texture;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		super.drawScreen(mouseX, mouseY, f);
		if (!demoPlayed && song == null) {
			String s = songToLearn.toString();
			fontRendererObj.drawString(s, guiLeft + (xSize / 2) - (fontRendererObj.getStringWidth(s) / 2), guiTop + 3, 0xFFFFFF);
		}
	}

	@Override
	public void updateScreen() {
		if (!demoPlayed) {
			if (++ticksSinceLastNote == 20) {
				if (currentNoteIndex == songToLearn.getNotes().size()) {
					PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.mimic"));
					demoPlayed = true;
					melody.clear();
					ticksSinceLastNote = 0;
				} else {
					onNotePlayed(songToLearn.getNotes().get(currentNoteIndex++));
				}
			}
		} else {
			super.updateScreen();
		}
	}

	@Override
	protected boolean allowKeyInput() {
		return demoPlayed && super.allowKeyInput();
	}

	@Override
	public void onGuiClosed() {
		// hack for learning songs from entities and onItemRightClick processing
		ZSSPlayerSongs.get(mc.thePlayer).songToLearn = null;
		if (song != null) {
			if (ticksSinceLastNote > song.getMinDuration()) {
				PacketDispatcher.sendToServer(new LearnSongPacket(song));
			} else {
				PacketDispatcher.sendToServer(new PlayRecordPacket(null, x, y, z));
				PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.premature"));
			}
		}
	}

	@Override
	protected void onNoteAdded() {
		int i = melody.size() - 1;
		if (melody.get(i) != songToLearn.getNotes().get(i)) {
			PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.incorrect." + mc.theWorld.rand.nextInt(4)));
			melody.clear();
		} else if (songToLearn.areCorrectNotes(melody)) {
			song = songToLearn;
			mc.thePlayer.playSound(Sounds.SUCCESS, 0.3F, 1.0F);
			PacketDispatcher.sendToServer(new PlayRecordPacket(song.getSoundString(), x, y, z));
			PlayerUtils.sendChat(mc.thePlayer, StatCollector.translateToLocal("chat.zss.song.correct." + mc.theWorld.rand.nextInt(4)));
		}
	}
}

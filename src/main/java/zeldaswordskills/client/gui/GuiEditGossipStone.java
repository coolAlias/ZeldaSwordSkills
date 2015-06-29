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

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Keyboard;

import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.network.server.SetGossipStoneMessagePacket;
import zeldaswordskills.util.StringUtils;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEditGossipStone extends GuiScreen
{
	private final TileEntityGossipStone te;
	private GuiButton btnDone;
	private StringBuilder message;

	public GuiEditGossipStone(TileEntityGossipStone te) {
		this.te = te;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		Keyboard.enableRepeatEvents(true);
		btnDone = new GuiButton(0, width / 2 - 100, height / 4 + 120, StatCollector.translateToLocal("gui.done"));
		buttonList.add(btnDone);
		message = new StringBuilder(TileEntityGossipStone.MAX_MESSAGE_LENGTH);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		te.setMessage(message.toString());
		PacketDispatcher.sendPacketToServer(new SetGossipStoneMessagePacket(te).makePacket());
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled && button.id == btnDone.id) {
			te.onInventoryChanged(); // marks chunk as modified and updates client TE
			mc.displayGuiScreen(null);
		}
	}

	@Override
	protected void keyTyped(char c, int keyCode) {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(btnDone);
		} else if (keyCode == Keyboard.KEY_BACK && message.length() > 0) {
			message.deleteCharAt(message.length() - 1);
		} else if (keyCode == Keyboard.KEY_RETURN) {
			message.append("\n");
		} else if (ChatAllowedCharacters.isAllowedCharacter(c) && message.length() < TileEntityGossipStone.MAX_MESSAGE_LENGTH) {
			message.append(c);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, StatCollector.translateToLocal("gui.zss.gossip_stone.name"), width / 2, 40, 16777215);
		String[] lines = StringUtils.wrapString(message.toString(), TileEntityGossipStone.LINE_LENGTH, 5);
		for (int i = 0; i < lines.length; ++i) {
			if (i == 0) {
				lines[i] = "> " + lines[i];
			} else if (i == lines.length - 1) {
				lines[i] += " <";
			}
			fontRenderer.drawString(lines[i], (width / 2) - (fontRenderer.getStringWidth(lines[i]) / 2), 80 + i * fontRenderer.FONT_HEIGHT, 16777215);
		}
		super.drawScreen(mouseX, mouseY, f);
	}
}

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

package zeldaswordskills.util;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.entity.player.EntityPlayer;

public class TimedChatDialogue
{
	Timer timer;
	/** The player which should receive the chat */
	final EntityPlayer player;
	/** The list of Strings to send */
	final List<String> chat;
	
	/**
	 * Use to send multiple chat messages to a player, one line at a time, with no initial
	 * delay and a standardized delay between each line (1250 milliseconds)
	 * @param player the player to receive the chat
	 * @param chat a complete list of all dialogue, line-by-line and pre-translated
	 */
	public TimedChatDialogue(EntityPlayer player, List<String> chat) {
		this(player, chat, 0, 1250);
	}

	/**
	 * Use to send multiple chat messages to a player, one line at a time
	 * @param player the player to receive the chat
	 * @param chat a complete list of all dialogue, line-by-line and pre-translated
	 * @param start the delay, in milliseconds, before the first chat message is sent
	 * @param delay the delay, in milliseconds, between each subsequent chat message
	 */
	public TimedChatDialogue(EntityPlayer player, List<String> chat, int start, int delay) {
		this.player = player;
		this.chat = chat;
		timer = new Timer();
		timer.schedule(new ChatTask(), start, delay);
	}
	
	class ChatTask extends TimerTask {
		int i = 0;
		public void run() {
			if (i == chat.size()) {
				timer.cancel();
			} else {
				player.addChatMessage(chat.get(i++));
			}
		}
	}
}

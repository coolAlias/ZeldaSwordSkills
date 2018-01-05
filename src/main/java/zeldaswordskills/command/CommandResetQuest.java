/**
    Copyright (C) <2018> <coolAlias>

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

package zeldaswordskills.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import zeldaswordskills.entity.player.quests.ZSSQuests;
import zeldaswordskills.util.PlayerUtils;

public class CommandResetQuest extends CommandBase
{
	public static final ICommand INSTANCE = new CommandResetQuest();

	private CommandResetQuest() {}

	@Override
	public String getCommandName() {
		return "zssresetquest";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * zssresetquests <player>
	 */
	@Override
	public String getCommandUsage(ICommandSender player) {
		return "commands.zssresetquest.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args == null || args.length != 1) {
			throw new WrongUsageException(getCommandUsage(sender));
		}
		EntityPlayerMP commandSender = getCommandSenderAsPlayer(sender);
		EntityPlayerMP player = getPlayer(sender, args[0]);
		ZSSQuests.get(player).reset();
		PlayerUtils.sendTranslatedChat(commandSender, "commands.zssresetquest.all", player.getCommandSenderName());
		if (player != commandSender) {
			PlayerUtils.sendTranslatedChat(player, "commands.zssresetquest.notify.all", commandSender.getCommandSenderName());
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		switch (args.length) {
		case 1: return getListOfStringsMatchingLastWord(args, getPlayers());
		default: return null;
		}
	}

	protected String[] getPlayers() {
		return MinecraftServer.getServer().getAllUsernames();
	}
}

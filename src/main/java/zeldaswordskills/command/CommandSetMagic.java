/**
    Copyright (C) <2017> <coolAlias>

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

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import zeldaswordskills.entity.player.ZSSPlayerInfo;
import zeldaswordskills.util.PlayerUtils;

public class CommandSetMagic extends CommandBase
{
	public static final ICommand INSTANCE = new CommandSetMagic();

	private CommandSetMagic() {}

	@Override
	public String getCommandName() {
		return "zssmagic";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * 	grantskill <player> <skill> <level> OR grantskill <player> all
	 */
	@Override
	public String getCommandUsage(ICommandSender player) {
		return "commands.zssmagic.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 4 || (!("add").equals(args[1]) && !("set").equals(args[1])) || (!("current").equals(args[2]) && !("max").equals(args[2]))) {
			throw new WrongUsageException(getCommandUsage(sender));
		}
		EntityPlayerMP commandSender = CommandBase.getCommandSenderAsPlayer(sender);
		EntityPlayerMP player = CommandBase.getPlayer(sender, args[0]);
		ZSSPlayerInfo info = ZSSPlayerInfo.get(player);
		boolean max = ("max").equals(args[2]);
		boolean add = ("add").equals(args[1]);
		int mp = CommandBase.parseInt(args[3]);
		if (mp < 0 && !add) {
			throw new CommandException("commands.zssmagic.negative", args[2]);
		}
		if (add) {
			mp += (max ? info.getMaxMagic() : info.getCurrentMagic());
		}
		if (max) {
			info.setMaxMagic(mp);
			mp = Math.round(info.getMaxMagic());
		} else {
			info.setCurrentMagic(mp);
			mp = Math.round(info.getCurrentMagic());
		}
		PlayerUtils.sendTranslatedChat(commandSender, "commands.zssmagic.success", player.getName(), args[2], mp);
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		switch(args.length) {
		case 1: return CommandBase.getListOfStringsMatchingLastWord(args, getPlayers());
		case 2: return Arrays.asList("add","set");
		case 3: return Arrays.asList("current","max");
		default: return null;
		}
	}

	protected String[] getPlayers() {
		return MinecraftServer.getServer().getAllUsernames();
	}
}

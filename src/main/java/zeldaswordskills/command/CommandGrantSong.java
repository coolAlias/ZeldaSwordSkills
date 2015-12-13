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

package zeldaswordskills.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.api.SongAPI;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.network.client.OpenSongGuiPacket;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.PlayerUtils;

public class CommandGrantSong extends CommandBase
{
	public static final ICommand INSTANCE = new CommandGrantSong();

	private CommandGrantSong() {}

	@Override
	public String getCommandName() {
		return "grantsong";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * grantsong <player> <song | all> <true>
	 */
	@Override
	public String getCommandUsage(ICommandSender player) {
		return "commands.grantsong.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayerMP commandSender = getCommandSenderAsPlayer(sender);
		EntityPlayerMP player = getPlayer(sender, args[0]);
		ZSSPlayerSongs info = ZSSPlayerSongs.get(player);
		if (args.length != 2 && args.length != 3) {
			throw new WrongUsageException(getCommandUsage(sender));
		} else if (("all").equals(args[1])) {
			if (args.length != 2) {
				throw new WrongUsageException(getCommandUsage(sender));
			}
			boolean flag = true;
			for (AbstractZeldaSong song : SongAPI.getRegisteredSongs()) {
				if (song.canLearnFromCommand() && !info.learnSong(song, null)) {
					flag = false;
				}
			}
			if (!flag) {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.grantsong.success.partial", player.getCommandSenderName());
			} else if (commandSender != player) {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.grantsong.success.all", player.getCommandSenderName());
			}
		} else {
			AbstractZeldaSong song = SongAPI.getSongByName(args[1]);
			if (song == null) {
				throw new CommandException("commands.song.generic.unknown", args[1]);
			} else if (!song.canLearnFromCommand()) {
				throw new CommandException("commands.grantsong.failure.denied", new ChatComponentTranslation(song.getTranslationString()));
			} else if (args.length == 3) {
				if (("true").equals(args[2])) {
					PacketDispatcher.sendTo(new OpenSongGuiPacket(song), player);
				} else {
					throw new WrongUsageException(getCommandUsage(sender));
				}
			} else if (info.learnSong(song, null)) {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.grantsong.success.one", player.getCommandSenderName(), new ChatComponentTranslation(song.getTranslationString()));
			} else {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.grantsong.failure.player", player.getCommandSenderName(), new ChatComponentTranslation(song.getTranslationString()));
			}
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		switch(args.length) {
		case 1: return getListOfStringsMatchingLastWord(args, getPlayers());
		case 2: return getListOfStringsMatchingLastWord(args, SongAPI.getRegisteredNames().toArray(new String[SongAPI.getTotalSongs()]));
		case 3: return getListOfStringsMatchingLastWord(args, new String[]{"true"});
		default: return null;
		}
	}

	protected String[] getPlayers() {
		return MinecraftServer.getServer().getAllUsernames();
	}
}

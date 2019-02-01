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

package zeldaswordskills.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.api.SongAPI;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.util.PlayerUtils;

public class CommandRemoveSong extends CommandBase
{
	public static final ICommand INSTANCE = new CommandRemoveSong();

	private CommandRemoveSong() {}

	@Override
	public String getCommandName() {
		return "removesong";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * removesong <player> <song | all>
	 */
	@Override
	public String getCommandUsage(ICommandSender player) {
		return "commands.removesong.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if (args == null || args.length != 2) {
			throw new WrongUsageException(getCommandUsage(sender));
		}
		EntityPlayerMP commandSender = getCommandSenderAsPlayer(sender);
		EntityPlayerMP player = getPlayer(sender, args[0]);
		ZSSPlayerSongs info = ZSSPlayerSongs.get(player);
		if (("all").equals(args[1])) {
			info.resetKnownSongs();
			PlayerUtils.sendTranslatedChat(commandSender, "commands.removesong.all", player.getName());
			if (player != commandSender) {
				PlayerUtils.sendTranslatedChat(player, "commands.removesong.notify.all", commandSender.getName());
			}
		} else {
			AbstractZeldaSong song = SongAPI.getSongByName(args[1]);
			if (song == null) {
				throw new CommandException("commands.song.generic.unknown", args[1]);
			}
			if (info.removeSong(song)) {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.removesong.one", player.getName(), new ChatComponentTranslation(song.getTranslationString()));
				if (player != commandSender) {
					PlayerUtils.sendTranslatedChat(player, "commands.removesong.notify.one", commandSender.getName(), new ChatComponentTranslation(song.getTranslationString()));
				}
			} else {
				PlayerUtils.sendTranslatedChat(commandSender, "commands.removesong.fail", player.getName(), new ChatComponentTranslation(song.getTranslationString()));
			}
		}
	}

	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		switch (args.length) {
		case 1: return getListOfStringsMatchingLastWord(args, getPlayers());
		case 2: return getListOfStringsMatchingLastWord(args, SongAPI.getRegisteredNames().toArray(new String[SongAPI.getTotalSongs()]));
		default: return null;
		}
	}

	protected String[] getPlayers() {
		return MinecraftServer.getServer().getAllUsernames();
	}
}

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
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.entity.player.ZSSPlayerSkills;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;

/**
 * 
 * Grants the skill named at the designated level; if the player's skill level
 * is already equal or higher, nothing happens.
 *
 */
public class CommandGrantSkill extends CommandBase
{
	public static final ICommand INSTANCE = new CommandGrantSkill();

	private CommandGrantSkill() {}

	@Override
	public String getCommandName() {
		return "grantskill";
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
		return "commands.grantskill.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP commandSender = getCommandSenderAsPlayer(sender);
		EntityPlayerMP player = getPlayer(sender, args[0]);
		ZSSPlayerSkills skills = ZSSPlayerSkills.get(player);
		if (args.length == 2 && ("all").equals(args[1])) {
			boolean flag = true;
			for (SkillBase skill : SkillBase.getSkills()) {
				// Config.isSkillEnabled(skill.getId()) && 
				if (!skills.grantSkill(skill.getId(), skill.getMaxLevel())) {
					flag = false;
				}
			}
			if (flag) {
				PlayerUtils.sendTranslatedChat(player, "commands.grantskill.notify.all");
				if (commandSender != player) {
					PlayerUtils.sendFormattedChat(commandSender, "commands.grantskill.success.all", player.getCommandSenderName());
				}
			} else {
				PlayerUtils.sendFormattedChat(commandSender, "commands.grantskill.success.partial", player.getCommandSenderName());
			}
		} else if (args.length == 3) {
			SkillBase skill = SkillBase.getSkillByName(args[1]);
			if (skill == null) {
				throw new CommandException("commands.skill.generic.unknown", args[1]);
			}
			int level = parseInt(args[2], 1, 10);
			int oldLevel = skills.getSkillLevel(skill);
			if (level > oldLevel) { // grants skill up to level or max level, whichever is reached first
				/*
				if (!Config.isSkillEnabled(skill.getId())) {
					throw new CommandException("commands.grantskill.failure.disabled", skill.getDisplayName());
				} else if (skills.grantSkill(skill.getId(), (byte) level)) {
				 */ 
				if (skills.grantSkill(skill.getId(), (byte) level)) {
					PlayerUtils.sendFormattedChat(player, "commands.grantskill.notify.one", new ChatComponentTranslation(skill.getTranslationString()), skills.getSkillLevel(skill));
					if (commandSender != player) {
						PlayerUtils.sendFormattedChat(commandSender, "commands.grantskill.success.one", player.getCommandSenderName(), new ChatComponentTranslation(skill.getTranslationString()), skills.getSkillLevel(skill));
					}
				} else {
					throw new CommandException("commands.grantskill.failure.player", player.getCommandSenderName(), new ChatComponentTranslation(skill.getTranslationString()));
				}
			} else {
				throw new CommandException("commands.grantskill.failure.low", player.getCommandSenderName(), new ChatComponentTranslation(skill.getTranslationString()), oldLevel);
			}
		} else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		switch(args.length) {
		case 1: return getListOfStringsMatchingLastWord(args, getPlayers());
		case 2: return getListOfStringsMatchingLastWord(args, SkillBase.getSkillNames());
		default: return null;
		}
	}

	protected String[] getPlayers() {
		return MinecraftServer.getServer().getAllUsernames();
	}
}

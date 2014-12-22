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

package zeldaswordskills.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import zeldaswordskills.entity.ZSSPlayerSkills;
import zeldaswordskills.skills.SkillBase;
import zeldaswordskills.util.PlayerUtils;

public class CommandRemoveSkill extends CommandBase
{
	public static final ICommand INSTANCE = new CommandRemoveSkill();

	public CommandRemoveSkill() {}

	@Override
	public String getCommandName() {
		return "removeskill";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * removeskill <skill | all>
	 */
	@Override
	public String getCommandUsage(ICommandSender player) {
		return "commands.removeskill.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args != null && args.length == 1) {
			boolean all = ("all").equals(args[0]);
			SkillBase skill = null;
			if (!all) {
				skill = SkillBase.getSkillByName(args[0]);
				if (skill == null) {
					throw new CommandException("commands.skill.generic.unknown", new Object[] {args[0]});
				}
			}
			EntityPlayerMP player = getCommandSenderAsPlayer(sender);
			if (ZSSPlayerSkills.get(player).removeSkill(args[0])) {
				if (all) {
					PlayerUtils.sendFormattedChat(player, "commands.removeskill.success.all", player.getCommandSenderName());
				} else {
					PlayerUtils.sendFormattedChat(player, "commands.removeskill.success.one", player.getCommandSenderName(), skill.getDisplayName());
				}
			} else { // player didn't have this skill
				if (all) {
					throw new CommandException("commands.removeskill.failure.all", new Object[] {player.getCommandSenderName()});
				} else {
					throw new CommandException("commands.removeskill.failure.one", new Object[] {player.getCommandSenderName(), skill.getDisplayName()});
				}
			}
		} else {
			throw new WrongUsageException(getCommandUsage(sender), new Object[0]);
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		return args.length == 1 ? getListOfStringsMatchingLastWord(args, SkillBase.getSkillNames()) : null;
	}
}

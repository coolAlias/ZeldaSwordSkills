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
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.api.item.WeaponRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommandWeaponRegistry extends CommandBase
{
	public static final ICommand INSTANCE = new CommandWeaponRegistry();

	private CommandWeaponRegistry() {}

	@Override
	public String getCommandName() {
		return "zssweaponregistry";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	/**
	 * zssweaponregistry <allow|forbid> <sword|weapon> modid:item_name
	 */
	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.zssweaponregistry.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if (args == null || args.length != 3) {
			throw new WrongUsageException(getCommandUsage(sender));
		}
		String[] parts = WeaponRegistry.parseString(args[2]);
		if (parts != null) {
			Item item = GameRegistry.findItem(parts[0], parts[1]);
			if (item == null) {
				throw new WrongUsageException("commands.zssweaponregistry.item.unknown", parts[1], parts[0]);
			}
			boolean isSword = isSword(args[1]);
			String msg = "commands.zssweaponregistry." + (isSword ? "sword." : "weapon.");
			if (isAllow(args[0])) {
				msg += "allow.";
				if (isSword) {
					msg += (WeaponRegistry.INSTANCE.registerSword("Command", parts[0], item) ? "success" : "fail");
				} else if (WeaponRegistry.INSTANCE.registerWeapon("Command", parts[0], item)) {
					msg += "success";
				} else {
					msg += "fail";
				}
			} else {
				msg += "forbid.";
				if (isSword) {
					msg += (WeaponRegistry.INSTANCE.removeSword("Command", parts[0], item) ? "success" : "fail");
				} else if (WeaponRegistry.INSTANCE.removeWeapon("Command", parts[0], item)) {
					msg += "success";
				} else {
					msg += "fail";
				}
			}
			sender.addChatMessage(new ChatComponentTranslation(msg, args[2]));
		} else {
			throw new WrongUsageException(getCommandUsage(sender));
		}
	}

	private boolean isAllow(String arg) {
		if (arg.equalsIgnoreCase("allow")) {
			return true;
		} else if (arg.equalsIgnoreCase("forbid")) {
			return false;
		}
		throw new WrongUsageException("commands.zssweaponregistry.action.unknown");
	}

	private boolean isSword(String arg) {
		if (arg.equalsIgnoreCase("sword")) {
			return true;
		} else if (arg.equalsIgnoreCase("weapon")) {
			return false;
		}
		throw new WrongUsageException("commands.zssweaponregistry.type.unknown");
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] args) {
		switch (args.length) {
		case 1: return getListOfStringsMatchingLastWord(args, "allow", "forbid");
		case 2: return getListOfStringsMatchingLastWord(args, "sword", "weapon");
		}
		return null;
	}
}

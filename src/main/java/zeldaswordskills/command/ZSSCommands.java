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

import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class ZSSCommands
{
	/**
	 * Call during FMLServerStartingEvent to register all commands
	 */
	public static void registerCommands(FMLServerStartingEvent event) {
		event.registerServerCommand(CommandGrantSkill.INSTANCE);
		event.registerServerCommand(CommandRemoveSkill.INSTANCE);
		event.registerServerCommand(CommandGrantSong.INSTANCE);
		event.registerServerCommand(CommandRemoveSong.INSTANCE);
		event.registerServerCommand(CommandSetMagic.INSTANCE);
		event.registerServerCommand(CommandWeaponRegistry.INSTANCE);
	}
}

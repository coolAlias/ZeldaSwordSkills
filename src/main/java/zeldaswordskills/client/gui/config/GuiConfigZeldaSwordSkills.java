/**
    Copyright (C) <2019> <coolAlias>

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

package zeldaswordskills.client.gui.config;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.util.StringUtils;

/**
 * 
 * Adds the GuiConfig element of Zelda Sword Skills to the mod list, both in-game and from the main menu. This
 * allows the player to change configurations within Minecraft, without having to manually edit the configuration
 * file.
 * 
 * @author Spitfyre03
 * 
 */
public class GuiConfigZeldaSwordSkills extends GuiConfig {

	public GuiConfigZeldaSwordSkills(GuiScreen parentScreen) {
		super(parentScreen, getElements(), ModInfo.ID, GuiConfig.getAbridgedConfigPath(Config.config.toString()), false, false, StringUtils.translateKey("config.zss.parent.title"));
	}

	/**
	 * @return the list of elements to display on the ZSS GuiConfig screen. The list returned is displayed on the main configuration screen, 
	 * and contains the categories of ZSS configurations that open into child screens.
	 */
	private static List<IConfigElement> getElements() {
		return Lists.<IConfigElement>newArrayList(
				new ConfigElement(Config.config.getCategory("general")),
				new ConfigElement(Config.config.getCategory("client")),
				new ConfigElement(Config.config.getCategory("mod support")),
				new ConfigElement(Config.config.getCategory("weapon registry").setRequiresMcRestart(true)),
				new ConfigElement(Config.config.getCategory("item")),
				new ConfigElement(Config.config.getCategory("bonus gear")),
				new ConfigElement(Config.config.getCategory("skills")),
				new ConfigElement(Config.config.getCategory("dungeon generation")),
				new ConfigElement(Config.config.getCategory("world generation")),
				new ConfigElement(Config.config.getCategory("loot")),
				new ConfigElement(Config.config.getCategory("drops")),
				new ConfigElement(Config.config.getCategory("trade")),
				new ConfigElement(Config.config.getCategory("mob spawns").setRequiresMcRestart(true)),
				new ConfigElement(Config.config.getCategory("recipes").setRequiresMcRestart(true))
			);
	}

}

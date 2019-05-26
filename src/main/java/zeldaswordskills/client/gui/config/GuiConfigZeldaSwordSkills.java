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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
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
		List<IConfigElement> categories = new ArrayList<>();

		//Create the hierarchy of elements to be displayed to the ConfigGui screen
		List<IConfigElement> general = new ConfigElement(Config.config.getCategory("general")).getChildElements();
		List<IConfigElement> client = new ConfigElement(Config.config.getCategory("client")).getChildElements();
		List<IConfigElement> weaponRegistry = new ConfigElement(Config.config.getCategory("weapon registry")).getChildElements();
		List<IConfigElement> items = new ConfigElement(Config.config.getCategory("item")).getChildElements();
		List<IConfigElement> bonusGear = new ConfigElement(Config.config.getCategory("bonus gear")).getChildElements();
		List<IConfigElement> skills = new ConfigElement(Config.config.getCategory("skills")).getChildElements();
		List<IConfigElement> dunGen = new ConfigElement(Config.config.getCategory("dungeon generation")).getChildElements();
		List<IConfigElement> worldGen = new ConfigElement(Config.config.getCategory("world generation")).getChildElements();
		List<IConfigElement> loot = new ConfigElement(Config.config.getCategory("loot")).getChildElements();
		List<IConfigElement> trades = new ConfigElement(Config.config.getCategory("trade")).getChildElements();
		List<IConfigElement> mobSpawning = new ConfigElement(Config.config.getCategory("mob spawns")).getChildElements();
		List<IConfigElement> recipes = new ConfigElement(Config.config.getCategory("recipes")).getChildElements();

		/*
		 * Add each category to the list to display
		 * Utilizing this versus a ConfigElement of the category type allows for capitalizing the name in the button, and lets you set the lang key
		 * Very Gui-friendly method
		 */
		Collections.addAll(categories,
				new DummyConfigElement.DummyCategoryElement("General", "config.zss.general.title", general),
				new DummyConfigElement.DummyCategoryElement("Client", "config.zss.client.title", client),
				new DummyConfigElement.DummyCategoryElement("Weapon Registry", "config.zss.weapon_registry.title", weaponRegistry).setRequiresMcRestart(true),
				new DummyConfigElement.DummyCategoryElement("Items", "config.zss.item.title", items),
				new DummyConfigElement.DummyCategoryElement("Bonus Gear", "config.zss.bonus_gear.title", bonusGear),
				new DummyConfigElement.DummyCategoryElement("Skills", "config.zss.skills.title", skills),
				new DummyConfigElement.DummyCategoryElement("Dungeon Generation", "config.zss.dun_gen.title", dunGen),
				new DummyConfigElement.DummyCategoryElement("World Generation", "config.zss.world_gen.title", worldGen),
				new DummyConfigElement.DummyCategoryElement("Loot", "config.zss.loot.title", loot),
				new DummyConfigElement.DummyCategoryElement("Trades", "config.zss.trade.title", trades),
				new DummyConfigElement.DummyCategoryElement("Mob Spawns", "config.zss.mob_spawns.title", mobSpawning).setRequiresMcRestart(true),
				new DummyConfigElement.DummyCategoryElement("Recipes", "config.zss.recipes.title", recipes).setRequiresMcRestart(true),
				// Individual property not yet categorized
				new ConfigElement(Config.config.get("mod support", "Can Offhand Master Swords", false, "[BattleGear2] Allow Master Swords to be held in the off-hand")));

		return Lists.<IConfigElement>newArrayList(new DummyConfigElement.DummyCategoryElement("Configurations", "config.zss.parent.configs", categories));
	}
}

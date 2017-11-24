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

package zeldaswordskills.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import scala.actors.threadpool.Arrays;
import zeldaswordskills.client.gui.config.overlays.GuiZSSFakeScreen;
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

	private GuiButtonExt fakeScreen;

	public GuiConfigZeldaSwordSkills(GuiScreen parentScreen) {
		super(parentScreen, getElements(), ModInfo.ID, GuiConfig.getAbridgedConfigPath(Config.config.toString()), false, false, I18n.format("config.zss.parent.title"));
		String key = "config.zss.parent.overlays";
		String overlayTitle = StringUtils.translateKey(key);
		fakeScreen = new GuiButtonExt(26, 0, 50, 300, 18, overlayTitle);
	}

	@Override
	public void initGui() {
		buttonList.add(this.fakeScreen);
		super.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.fakeScreen.xPosition = this.entryList.width / 2 - fakeScreen.width / 2;
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.fakeScreen.isMouseOver()) {
			String title = EnumChatFormatting.GREEN + fakeScreen.displayString;
			String key = "config.zss.parent.overlays.tooltip";
			String tooltip = EnumChatFormatting.YELLOW + StringUtils.translateKey(key);
			this.drawToolTip(Arrays.asList((title + "\n" + tooltip).split("\n")), mouseX, mouseY);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) {
		if (button.id == 26) {
			this.mc.displayGuiScreen(new GuiZSSFakeScreen(this));
		}
		super.actionPerformed(button);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
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
		categories.add(new DummyConfigElement.DummyCategoryElement("General", "config.zss.general.title", general));
		categories.add(new DummyConfigElement.DummyCategoryElement("Client", "config.zss.client.title", client));
		categories.add(new DummyConfigElement.DummyCategoryElement("Weapon Registry", "config.zss.weapon_registry.title", weaponRegistry).setRequiresMcRestart(true));
		categories.add(new DummyConfigElement.DummyCategoryElement("Items", "config.zss.item.title", items));
		categories.add(new DummyConfigElement.DummyCategoryElement("Bonus Gear", "config.zss.bonus_gear.title", bonusGear));
		categories.add(new DummyConfigElement.DummyCategoryElement("Skills", "config.zss.skills.title", skills));
		categories.add(new DummyConfigElement.DummyCategoryElement("Dungeon Generation", "config.zss.dun_gen.title", dunGen));
		categories.add(new DummyConfigElement.DummyCategoryElement("World Generation", "config.zss.world_gen.title", worldGen));
		categories.add(new DummyConfigElement.DummyCategoryElement("Loot", "config.zss.loot.title", loot));
		categories.add(new DummyConfigElement.DummyCategoryElement("Trades", "config.zss.trade.title", trades));
		categories.add(new DummyConfigElement.DummyCategoryElement("Mob Spawns", "config.zss.mob_spawns.title", mobSpawning).setRequiresMcRestart(true));
		categories.add(new DummyConfigElement.DummyCategoryElement("Recipes", "config.zss.recipes.title", recipes).setRequiresMcRestart(true));

		//This config is the only one of its category. Add to the main GuiConfig screen as its own element, since it is an independent Property
		categories.add(new ConfigElement(Config.config.get("mod support", "Can Offhand Master Swords", false, "[BattleGear2] Allow Master Swords to be held in the off-hand")));

		List<IConfigElement> list = new ArrayList<>();
		list.add(new DummyConfigElement.DummyCategoryElement("Configurations", "config.zss.parent.configs", categories));

		return list;
	}
}

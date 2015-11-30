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

package zeldaswordskills;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zeldaswordskills.api.item.WeaponRegistry;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.command.ZSSCommands;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.handler.ZSSEntityEvents;
import zeldaswordskills.handler.ZSSEventsFML;
import zeldaswordskills.handler.ZSSItemEvents;
import zeldaswordskills.item.ItemHeroBow;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.PacketDispatcher;
import zeldaswordskills.ref.Config;
import zeldaswordskills.ref.ModInfo;
import zeldaswordskills.world.gen.AntiqueAtlasHelper;
import zeldaswordskills.world.gen.DungeonLootLists;
import zeldaswordskills.world.gen.ZSSWorldGenEvent;
import zeldaswordskills.world.gen.feature.WorldGenGossipStones;
import zeldaswordskills.world.gen.feature.WorldGenJars;

/**
 * 
 * A mod that adds Zelda-like sword skills to Minecraft. Players will start with only the
 * ability to 'lock-on' to targets and perform combos; they must learn the other skills
 * throughout the game by getting rare 'skill orb' drops from different kinds of mobs.
 * 
 * Other Zelda-like features such as bombs, secret dungeons, heart pieces, and more are also
 * included.
 *
 */
@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION)
public class ZSSMain
{
	@Mod.Instance(ModInfo.ID)
	public static ZSSMain instance;

	@SidedProxy(clientSide = ModInfo.CLIENT_PROXY, serverSide = ModInfo.SERVER_PROXY)
	public static CommonProxy proxy;

	public static final Logger logger = LogManager.getLogger(ModInfo.ID);

	/** Helper class for registering custom tiles with Antique Atlas mod if loaded */
	public static AntiqueAtlasHelper atlasHelper = new AntiqueAtlasHelper();
	/** Whether Antique Atlas mod is loaded */
	public static boolean isAtlasEnabled;
	/** Whether Battlegear2 mod is loaded */
	public static boolean isBG2Enabled;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.preInit(event);
		isAtlasEnabled = Loader.isModLoaded("antiqueatlas");
		isBG2Enabled = Loader.isModLoaded("battlegear2");
		ZSSBlocks.preInit();
		ZSSItems.preInit();
		ZSSEntities.preInit();
		ZSSAchievements.preInit();
		proxy.preInit();
		PacketDispatcher.preInit();
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.init();
		ZSSItems.init();
		MinecraftForge.EVENT_BUS.register(new ZSSCombatEvents());
		MinecraftForge.EVENT_BUS.register(new ZSSEntityEvents());
		MinecraftForge.EVENT_BUS.register(new ZSSItemEvents());
		FMLCommonHandler.instance().bus().register(new ZSSEventsFML());
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
		ZSSWorldGenEvent dungeonGen = new ZSSWorldGenEvent();
		MinecraftForge.EVENT_BUS.register(dungeonGen);
		if (Config.areBossDungeonsEnabled()) {
			MinecraftForge.TERRAIN_GEN_BUS.register(dungeonGen);
		}
		if (Config.getGossipStoneRate() > 0) {
			MinecraftForge.EVENT_BUS.register(WorldGenGossipStones.INSTANCE);
		}
		MinecraftForge.EVENT_BUS.register(WorldGenJars.INSTANCE);
		String link = "https://raw.githubusercontent.com/coolAlias/ZeldaSwordSkills/master/src/main/resources/versionlist.json";
		FMLInterModComms.sendRuntimeMessage(ModInfo.ID, "VersionChecker", "addVersionCheck", link);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Config.postInit();
		if (isBG2Enabled) {
			ItemHeroBow.registerBG2();
			// TODO MinecraftForge.EVENT_BUS.register(new BattlegearEvents());
		}
		DungeonLootLists.init();
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		ZSSItems.onServerStarting();
		ZSSCommands.registerCommands(event);
	}

	@Mod.EventHandler
	public void processMessages(FMLInterModComms.IMCEvent event) {
		for (final FMLInterModComms.IMCMessage msg : event.getMessages()) {
			WeaponRegistry.INSTANCE.processMessage(msg);
		}
	}
}

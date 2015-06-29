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

import java.util.logging.Level;

import net.minecraftforge.common.MinecraftForge;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.command.ZSSCommands;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.handler.BattlegearEvents;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.handler.ZSSCombatEvents;
import zeldaswordskills.handler.ZSSEntityEvents;
import zeldaswordskills.handler.ZSSItemEvents;
import zeldaswordskills.item.ItemHeroBow;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.lib.Config;
import zeldaswordskills.lib.ModInfo;
import zeldaswordskills.network.ZSSPacketHandler;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.world.gen.DungeonLootLists;
import zeldaswordskills.world.gen.ZSSWorldGenEvent;
import zeldaswordskills.world.gen.feature.WorldGenGossipStones;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;

@Mod(modid = ModInfo.ID, name = ModInfo.NAME, version = ModInfo.VERSION)
@NetworkMod(clientSideRequired=true, serverSideRequired=true, channels = {ModInfo.CHANNEL}, packetHandler = ZSSPacketHandler.class)

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
public class ZSSMain
{
	@Mod.Instance(ModInfo.ID)
	public static ZSSMain instance;

	@SidedProxy(clientSide = ModInfo.CLIENT_PROXY, serverSide = ModInfo.COMMON_PROXY)
	public static CommonProxy proxy;

	/** Whether Antique Atlas mod is loaded */
	public static boolean isAtlasEnabled;
	/** Whether Battlegear2 mod is loaded */
	public static boolean isBG2Enabled;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LogHelper.init(Level.INFO);
		Config.init(event);
		isAtlasEnabled = Loader.isModLoaded("antiqueatlas");
		isBG2Enabled = Loader.isModLoaded("battlegear2");
		ZSSBlocks.preInit();
		ZSSItems.preInit();
		ZSSEntities.preInit();
		ZSSAchievements.preInit();
		proxy.initialize();
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
		ZSSItems.init();
		MinecraftForge.EVENT_BUS.register(new ZSSCombatEvents());
		MinecraftForge.EVENT_BUS.register(new ZSSEntityEvents());
		MinecraftForge.EVENT_BUS.register(new ZSSItemEvents());
		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
		ZSSWorldGenEvent dungeonGen = new ZSSWorldGenEvent();
		MinecraftForge.EVENT_BUS.register(dungeonGen);
		if (Config.areBossDungeonsEnabled()) {
			MinecraftForge.TERRAIN_GEN_BUS.register(dungeonGen);
		}
		if (Config.getGossipStoneRate() > 0) {
			MinecraftForge.EVENT_BUS.register(WorldGenGossipStones.INSTANCE);
		}
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Config.postInit();
		if (isBG2Enabled) {
			ItemHeroBow.registerBG2();
			MinecraftForge.EVENT_BUS.register(new BattlegearEvents());
		}
		DungeonLootLists.init();
	}

	@Mod.EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		ZSSItems.onServerStarting();
		ZSSCommands.registerCommands(event);
	}
}

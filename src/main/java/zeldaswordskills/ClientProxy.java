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

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.client.TargetingTickHandler;
import zeldaswordskills.client.ZSSClientEvents;
import zeldaswordskills.client.ZSSKeyHandler;
import zeldaswordskills.client.gui.ComboOverlay;
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.client.gui.GuiItemModeOverlay;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.item.ZSSItems;
import zeldaswordskills.network.client.UnpressKeyPacket;
import zeldaswordskills.world.gen.AntiqueAtlasHelper;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
		MinecraftForge.EVENT_BUS.register(new ComboOverlay());
		MinecraftForge.EVENT_BUS.register(new GuiBuffBar());
		MinecraftForge.EVENT_BUS.register(new GuiItemModeOverlay());
		MinecraftForge.EVENT_BUS.register(new ZSSClientEvents());
		FMLCommonHandler.instance().bus().register(new TargetingTickHandler());
		FMLCommonHandler.instance().bus().register(new ZSSKeyHandler());
		UnpressKeyPacket.init();
	}

	@Override
	public int addArmor(String armor) {
		return RenderingRegistry.addNewArmourRendererPrefix(armor);
	}

	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft().thePlayer : super.getPlayerEntity(ctx));
	}

	@Override
	public void registerRenderers() {
		AntiqueAtlasHelper.registerTextures();
		ZSSBlocks.registerRenderers();
		ZSSEntities.registerRenderers();
		ZSSItems.registerRenderers();
	}
}

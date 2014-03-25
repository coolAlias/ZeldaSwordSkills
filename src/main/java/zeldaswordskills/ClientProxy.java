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

package zeldaswordskills;

import net.minecraftforge.common.MinecraftForge;
import zeldaswordskills.block.ZSSBlocks;
import zeldaswordskills.client.gui.ComboOverlay;
import zeldaswordskills.client.gui.GuiBuffBar;
import zeldaswordskills.entity.ZSSEntities;
import zeldaswordskills.handler.SoundHandler;
import zeldaswordskills.handler.TargetingTickHandler;
import zeldaswordskills.handler.ZSSKeyHandler;
import zeldaswordskills.item.ZSSItems;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ClientProxy extends CommonProxy {

	@Override
	public void initialize() {
		super.initialize();
		ZSSKeyHandler.init();
		MinecraftForge.EVENT_BUS.register(new SoundHandler());
		MinecraftForge.EVENT_BUS.register(new ComboOverlay());
		MinecraftForge.EVENT_BUS.register(new GuiBuffBar());
		TickRegistry.registerTickHandler(new TargetingTickHandler(), Side.CLIENT);
	}

	@Override
	public int addArmor(String armor) {
		return RenderingRegistry.addNewArmourRendererPrefix(armor);
	}

	@Override
	public void registerRenderers() {
		ZSSMain.atlasHelper.registerTextures();
		ZSSBlocks.registerRenderers();
		ZSSEntities.registerRenderers();
		ZSSItems.registerRenderers();
	}
}

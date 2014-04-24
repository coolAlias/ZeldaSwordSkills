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

package zeldaswordskills.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.client.gui.GuiMaskTrader;
import zeldaswordskills.client.gui.GuiPedestal;
import zeldaswordskills.client.gui.GuiSkills;
import zeldaswordskills.inventory.ContainerMaskTrader;
import zeldaswordskills.inventory.ContainerPedestal;
import zeldaswordskills.inventory.ContainerSkills;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	public static final int GUI_PEDESTAL = 0, GUI_MASK_TRADER = 1, GUI_SKILLS = 2;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		switch(id) {
		case GUI_PEDESTAL:
			if (te instanceof TileEntityPedestal) {
				return new ContainerPedestal(player.inventory, (TileEntityPedestal) te);
			}
			break;
		case GUI_MASK_TRADER:
			return new ContainerMaskTrader();
		case GUI_SKILLS:
			return new ContainerSkills(player);
		default:
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		switch(id) {
		case GUI_PEDESTAL:
			if (te instanceof TileEntityPedestal) {
				return new GuiPedestal(player.inventory, (TileEntityPedestal) te);
			}
			break;
		case GUI_MASK_TRADER:
			return new GuiMaskTrader();
		case GUI_SKILLS:
			return new GuiSkills(player);
		default:
		}
		return null;
	}

}

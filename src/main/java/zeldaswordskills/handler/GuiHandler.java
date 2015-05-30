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

package zeldaswordskills.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.client.gui.GuiEditGossipStone;
import zeldaswordskills.client.gui.GuiLearnSong;
import zeldaswordskills.client.gui.GuiMaskTrader;
import zeldaswordskills.client.gui.GuiOcarina;
import zeldaswordskills.client.gui.GuiPedestal;
import zeldaswordskills.client.gui.GuiSkills;
import zeldaswordskills.inventory.ContainerMaskTrader;
import zeldaswordskills.inventory.ContainerPedestal;
import zeldaswordskills.inventory.ContainerSkills;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	public static final int GUI_PEDESTAL = 0, GUI_MASK_TRADER = 1, GUI_SKILLS = 2,
			/** Gui for playing musical instruments with the same control scheme as Ocarina of Time */
			GUI_OCARINA = 3,
			/** Same as GUI_OCARINA but with a flag set for learning the Scarecrow Song */
			GUI_SCARECROW = 4,
			/** Gui to open for learning all songs but the Scarecrow Song */
			GUI_LEARN_SONG = 5,
			/** Gui opened when a Gossip Stone is placed, like the vanilla sign editor */
			GUI_EDIT_GOSSIP_STONE = 6;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch(id) {
		case GUI_PEDESTAL:
			TileEntity te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				return new ContainerPedestal(player.inventory, (TileEntityPedestal) te);
			}
			break;
		case GUI_MASK_TRADER:
			return new ContainerMaskTrader();
		case GUI_SKILLS:
			return new ContainerSkills(player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(x, y, z);
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
		case GUI_OCARINA:
			return new GuiOcarina(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		case GUI_EDIT_GOSSIP_STONE:
			if (te == null) {
				// modeled after vanilla sign editor handling, since TE is not yet available on client
				te = new TileEntityGossipStone();
				te.setWorldObj(world);
				te.xCoord = x;
				te.yCoord = y;
				te.zCoord = z;
			}
			if (te instanceof TileEntityGossipStone) {
				return new GuiEditGossipStone((TileEntityGossipStone) te);
			}
			break;
		case GUI_SCARECROW:
			return new GuiOcarina(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), true);
		case GUI_LEARN_SONG:
			try {
				return new GuiLearnSong(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
			} catch (IllegalArgumentException e) {
				ZSSMain.logger.error(e.getMessage());
				return null;
			}
		}
		return null;
	}
}

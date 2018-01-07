/**
    Copyright (C) <2018> <coolAlias>

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

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.api.entity.merchant.IRupeeMerchant;
import zeldaswordskills.api.entity.merchant.RupeeMerchantHelper;
import zeldaswordskills.block.tileentity.TileEntityGossipStone;
import zeldaswordskills.block.tileentity.TileEntityPedestal;
import zeldaswordskills.client.gui.GuiEditGossipStone;
import zeldaswordskills.client.gui.GuiLearnSong;
import zeldaswordskills.client.gui.GuiMaskTrader;
import zeldaswordskills.client.gui.GuiOcarina;
import zeldaswordskills.client.gui.GuiPedestal;
import zeldaswordskills.client.gui.GuiRupeeMerchant;
import zeldaswordskills.client.gui.GuiSkills;
import zeldaswordskills.client.gui.GuiWallet;
import zeldaswordskills.entity.npc.EntityNpcMaskTrader;
import zeldaswordskills.inventory.ContainerMaskTrader;
import zeldaswordskills.inventory.ContainerPedestal;
import zeldaswordskills.inventory.ContainerRupeeMerchant;
import zeldaswordskills.inventory.ContainerSkills;
import zeldaswordskills.inventory.ContainerWallet;

public class GuiHandler implements IGuiHandler
{
	/** Gui for the sword pedestal block */
	public static final int GUI_PEDESTAL = 0;
	/** Gui for Mask Salesman expects parameter 'x' to be the salesman's entity ID */
	public static final int GUI_MASK_TRADER = 1;
	/** Sword skill interface showing player's current skill levels with skill descriptions */
	public static final int GUI_SKILLS = 2;
	/** Gui for playing musical instruments with the same control scheme as Ocarina of Time */
	public static final int GUI_OCARINA = 3;
	/** Gui to open for learning all songs but the Scarecrow Song */
	public static final int GUI_LEARN_SONG = 4;
	/** Same as GUI_LEARN_SONG but with a flag set for learning the Scarecrow Song */
	public static final int GUI_SCARECROW = 5;
	/** Gui opened when a Gossip Stone is placed, like the vanilla sign editor */
	public static final int GUI_EDIT_GOSSIP_STONE = 6;
	/** Gui for managing rupees via the Wallet */
	public static final int GUI_WALLET = 7;
	/** Gui for Entity-based rupee trading (player is buying) interface; expects parameter 'x' to be the IRupeeMerchant's entity ID */
	public static final int GUI_RUPEE_SHOP = 8;
	/** Gui for Entity-based rupee trading (player is selling) interface; expects parameter 'x' to be the IRupeeMerchant's entity ID */
	public static final int GUI_RUPEE_SALES = 9;
	/** Gui for TileEntity-based rupee trading (player is buying) interface; expects parameters x/y/z to be the IRupeeMerchant's TileEntity coordinates  */
	public static final int GUI_RUPEE_TILE_SHOP = 10;
	/** Gui for TileEntity-based rupee trading (player is selling) interface; expects parameters x/y/z to be the IRupeeMerchant's TileEntity coordinates */
	public static final int GUI_RUPEE_TILE_SALES = 11;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te;
		Entity entity;
		IRupeeMerchant merchant;
		switch(id) {
		case GUI_PEDESTAL:
			te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				return new ContainerPedestal(player, (TileEntityPedestal) te);
			}
			return null;
		case GUI_RUPEE_SHOP:
			entity = world.getEntityByID(x);
			merchant = RupeeMerchantHelper.getRupeeMerchant(entity);
			if (merchant != null) {
				return new ContainerRupeeMerchant.Shop(player, merchant);
			}
			return null;
		case GUI_RUPEE_SALES:
			entity = world.getEntityByID(x);
			merchant = RupeeMerchantHelper.getRupeeMerchant(entity);
			if (merchant != null) {
				return new ContainerRupeeMerchant.Sales(player, merchant);
			}
			return null;
		case GUI_RUPEE_TILE_SHOP:
			te = world.getTileEntity(x, y, z);
			merchant = RupeeMerchantHelper.getRupeeMerchant(te);
			if (merchant != null) {
				return new ContainerRupeeMerchant.Shop(player, merchant);
			}
			return null;
		case GUI_RUPEE_TILE_SALES:
			te = world.getTileEntity(x, y, z);
			merchant = RupeeMerchantHelper.getRupeeMerchant(te);
			if (merchant != null) {
				return new ContainerRupeeMerchant.Sales(player, merchant);
			}
			return null;
		case GUI_MASK_TRADER:
			entity = world.getEntityByID(x);
			if (entity instanceof EntityNpcMaskTrader) {
				return new ContainerMaskTrader((EntityNpcMaskTrader) entity);
			}
			return null;
		case GUI_SKILLS:
			return new ContainerSkills(player);
		case GUI_WALLET:
			return new ContainerWallet(player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		TileEntity te;
		Entity entity;
		IRupeeMerchant merchant;
		switch(id) {
		case GUI_PEDESTAL:
			te = world.getTileEntity(x, y, z);
			if (te instanceof TileEntityPedestal) {
				return new GuiPedestal(player, (TileEntityPedestal) te);
			}
			return null;
		case GUI_MASK_TRADER:
			entity = world.getEntityByID(x);
			if (entity instanceof EntityNpcMaskTrader) {
				return new GuiMaskTrader((EntityNpcMaskTrader) entity);
			}
			return null;
		case GUI_RUPEE_SHOP:
			entity = world.getEntityByID(x);
			merchant = RupeeMerchantHelper.getRupeeMerchant(entity);
			if (merchant != null) {
				return new GuiRupeeMerchant.Shop(player, merchant).setRenderEntity(entity);
			}
			return null;
		case GUI_RUPEE_SALES:
			entity = world.getEntityByID(x);
			merchant = RupeeMerchantHelper.getRupeeMerchant(entity);
			if (merchant != null) {
				return new GuiRupeeMerchant.Sales(player, merchant).setRenderEntity(entity);
			}
			return null;
		case GUI_RUPEE_TILE_SHOP:
			te = world.getTileEntity(x, y, z);
			merchant = RupeeMerchantHelper.getRupeeMerchant(te);
			if (merchant != null) {
				return new GuiRupeeMerchant.Shop(player, merchant);
			}
			return null;
		case GUI_RUPEE_TILE_SALES:
			te = world.getTileEntity(x, y, z);
			merchant = RupeeMerchantHelper.getRupeeMerchant(te);
			if (merchant != null) {
				return new GuiRupeeMerchant.Sales(player, merchant);
			}
			return null;
		case GUI_SKILLS:
			return new GuiSkills(player);
		case GUI_OCARINA:
			return new GuiOcarina(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
		case GUI_EDIT_GOSSIP_STONE:
			te = world.getTileEntity(x, y, z);
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
			return null;
		case GUI_SCARECROW:
			return new GuiOcarina(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), true);
		case GUI_LEARN_SONG:
			try {
				return new GuiLearnSong(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ));
			} catch (IllegalArgumentException e) {
				ZSSMain.logger.error(e.getMessage());
				return null;
			}
		case GUI_WALLET:
			return new GuiWallet(player);
		}
		return null;
	}
}

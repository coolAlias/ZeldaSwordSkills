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

package zeldaswordskills.block.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.ref.ZeldaSong;
import zeldaswordskills.util.LogHelper;
import zeldaswordskills.util.PlayerUtils;

public class TileEntityInscription extends TileEntity
{
	private ZeldaSong song;

	public TileEntityInscription() {
		song = ZeldaSong.TIME_SONG;
	}

	@Override
	public boolean canUpdate() {
		return false;
	}

	public ZeldaSong getSong() {
		return song;
	}

	/**
	 * Sets the song that will be learned from this inscription
	 */
	public void setSong(ZeldaSong song) {
		if (song == ZeldaSong.SCARECROW_SONG) {
			LogHelper.warning("Scarecrow's Song cannot be learned from inscriptions; coordinates: " + xCoord + "/" + yCoord + "/" + zCoord);
			return;
		}
		this.song = song;
		if (!worldObj.isRemote) {
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
	}

	private void setNextSong() {
		int i = (song == null ? 0 : song.ordinal() + 1);
		if (i == ZeldaSong.values().length) {
			i = 0;
		} else if (i == ZeldaSong.SCARECROW_SONG.ordinal()) {
			++i;
		}
		setSong(ZeldaSong.values()[i]);
	}

	/**
	 * Call when BlockSongInscription is activated
	 * @return true if something happened
	 */
	public boolean onActivated(EntityPlayer player) {
		if (song == null) {
			LogHelper.warning("TileEntityInscription at " + xCoord + "/" + yCoord + "/" + zCoord + " is not valid! Is client? " + worldObj.isRemote);
			return false;
		}
		ItemStack stack = player.getHeldItem();
		if (player.capabilities.isCreativeMode) {
			if (player.isSneaking()) {
				if (!worldObj.isRemote) {
					setNextSong();
					PlayerUtils.sendChat(player, StatCollector.translateToLocalFormatted("chat.zss.song.inscription.new", song.toString()));
				}
			} else if (!worldObj.isRemote) {
				PlayerUtils.sendChat(player, StatCollector.translateToLocalFormatted("chat.zss.song.inscription.current", song.toString()));
			}
			return true;
		} else if (stack != null && stack.getItem() instanceof ItemInstrument) {
			if (worldObj.isRemote) {
				ZSSPlayerSongs.get(player).songToLearn = song;
				player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, xCoord, yCoord, zCoord);
			}
			return true;
		} else if (worldObj.isRemote) {
			PlayerUtils.sendChat(player, StatCollector.translateToLocal("chat.zss.song.inscription.fail"));
		}
		return false;
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.func_148857_g());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("SongName")) {
			song = ZeldaSong.getSongFromUnlocalizedName(compound.getString("SongName"));
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (song != null) {
			compound.setString("SongName", song.getUnlocalizedName());
		}
	}
}

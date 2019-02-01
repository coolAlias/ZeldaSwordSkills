/**
 * Copyright (C) <2019> <coolAlias>
 *
 * This file is part of coolAlias' Zelda Sword Skills Minecraft Mod; as such,
 * you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package zeldaswordskills.block.tileentity;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.ChatComponentTranslation;
import zeldaswordskills.ZSSMain;
import zeldaswordskills.entity.player.ZSSPlayerSongs;
import zeldaswordskills.handler.GuiHandler;
import zeldaswordskills.item.ItemInstrument;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;
import zeldaswordskills.util.PlayerUtils;

public class TileEntityInscription extends TileEntityBase
{
	private AbstractZeldaSong song;

	public TileEntityInscription() {
		song = ZeldaSongs.songTime;
	}

	public AbstractZeldaSong getSong() {
		return song;
	}

	/**
	 * Sets the song that will be learned from this inscription
	 */
	public void setSong(AbstractZeldaSong song) {
		if (song != null && !song.canLearnFromInscription(worldObj, worldObj.getBlockState(pos))) {
			ZSSMain.logger.warn(String.format("%s cannot be learned from inscriptions; coordinates: %d/%d/%d", song.getDisplayName(), pos.getX(), pos.getY(), pos.getZ()));
			return;
		}
		this.song = song;
		if (!worldObj.isRemote) {
			worldObj.markBlockForUpdate(pos);
		}
	}

	private void setNextSong() {
		List<String> songs = ZeldaSongs.getRegisteredNames();
		int i = Math.max(0, songs.indexOf(song) + 1);
		if (i == songs.size()) {
			i = 0;
		}
		song = null;
		boolean looped = false;
		while (song == null && i < songs.size()) {
			song = ZeldaSongs.getSongByName(songs.get(i));
			if (!song.canLearnFromInscription(worldObj, worldObj.getBlockState(pos))) {
				song = null;
				++i;
				if (i == songs.size() && !looped) {
					looped = true;
					i = 0;
				}
			}
		}
		setSong(song);
	}

	/**
	 * Call when BlockSongInscription is activated
	 * @return true if something happened
	 */
	public boolean onActivated(EntityPlayer player) {
		if (song == null) {
			ZSSMain.logger.warn(String.format("TileEntityInscription at %d/%d/%d does not have a valid song!", pos.getX(), pos.getY(), pos.getZ()));
			return false;
		}
		ItemStack stack = player.getHeldItem();
		if (player.capabilities.isCreativeMode) {
			if (player.isSneaking()) {
				if (!worldObj.isRemote) {
					setNextSong();
					PlayerUtils.sendTranslatedChat(player, "chat.zss.song.inscription.new", new ChatComponentTranslation(song.getTranslationString()));
				}
			} else if (!worldObj.isRemote) {
				PlayerUtils.sendTranslatedChat(player, "chat.zss.song.inscription.current", new ChatComponentTranslation(song.getTranslationString()));
			}
			return true;
		} else if (stack != null && stack.getItem() instanceof ItemInstrument) {
			if (worldObj.isRemote) {
				ZSSPlayerSongs.get(player).songToLearn = song;
				player.openGui(ZSSMain.instance, GuiHandler.GUI_LEARN_SONG, player.worldObj, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else if (worldObj.isRemote) {
			PlayerUtils.sendTranslatedChat(player, "chat.zss.song.inscription.fail");
		}
		return false;
	}

	@Override
	public Packet<?> getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		this.writeToNBT(tag);
		return new S35PacketUpdateTileEntity(pos, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("SongName")) {
			song = ZeldaSongs.getSongByName(compound.getString("SongName"));
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

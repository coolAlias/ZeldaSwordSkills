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

package zeldaswordskills.block.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import zeldaswordskills.api.block.ISongBlock;
import zeldaswordskills.entity.EntityFairy;
import zeldaswordskills.ref.Config;
import zeldaswordskills.songs.AbstractZeldaSong;
import zeldaswordskills.songs.ZeldaSongs;

public class TileEntityGossipStone extends TileEntity
{
	/** Maximum number of characters that will fit on one chat line */
	public static final int LINE_LENGTH = 64;
	public static final int MAX_MESSAGE_LENGTH = LINE_LENGTH * 3;
	private String message = "";
	private long nextFairySpawn;

	public TileEntityGossipStone() {}

	@Override
	public boolean canUpdate() {
		return false;
	}

	/**
	 * Returns this Gossip Stone's message, or a default message if none was set.
	 * Note that the message is NOT updated on the client side.
	 */
	public String getMessage() {
		return (message == null || message.equals("")) ? StatCollector.translateToLocal("chat.zss.block.gossip_stone.default") : message;
	}

	/**
	 * Sets the message to display when a Gossip Stone is activated while wearing the Mask of Truth
	 */
	public void setMessage(String message) {
		this.message = message;
		markDirty();
	}

	/**
	 * Call this when the block is notified by a ZeldaSong via {@link ISongBlock#onSongPlayed}
	 * @return TRUE if this block was affected
	 */
	public boolean onSongPlayed(EntityPlayer player, AbstractZeldaSong song, int power, int affected) {
		if (worldObj.isDaytime() || nextFairySpawn > worldObj.getWorldTime() || power < 5) {
			return false;
		}
		if (song == ZeldaSongs.songStorms || song == ZeldaSongs.songSun || song == ZeldaSongs.songZeldasLullaby) {
			EntityFairy fairy = new EntityFairy(worldObj);
			fairy.setFairyHome(xCoord, yCoord + 2, zCoord);
			worldObj.spawnEntityInWorld(fairy);
			nextFairySpawn = worldObj.getWorldTime() + 24000 * (worldObj.rand.nextInt(Config.getDaysToRespawn()) + 1);
			markDirty();
			return true;
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		message = compound.getString("message");
		nextFairySpawn = compound.getLong("nextFairySpawn");
	}

	@Override
	public void writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setString("message", message == null ? "" : message);
		compound.setLong("nextFairySpawn", nextFairySpawn);
	}
}

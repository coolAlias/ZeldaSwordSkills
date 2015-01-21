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

package zeldaswordskills.network.bidirectional;

import java.io.IOException;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import zeldaswordskills.network.AbstractMessage;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * Plays a sound on the client or server side
 *
 */
public class PlaySoundPacket extends AbstractMessage
{
	private String sound;

	private float volume;

	private float pitch;

	/** Coordinates at which to play the sound; used on the server side */
	private double x, y, z;

	public PlaySoundPacket() {}

	public PlaySoundPacket(String sound, float volume, float pitch, double x, double y, double z) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Use only when sending to the SERVER to use the entity's coordinates as the center;
	 * if sent to the client, the position coordinates will be ignored.
	 */
	public PlaySoundPacket(String sound, float volume, float pitch, Entity entity) {
		this(sound, volume, pitch, entity.posX, entity.posY, entity.posZ);
	}

	/**
	 * Use only when sending to the CLIENT - the sound will play at the player's position
	 */
	public PlaySoundPacket(String sound, float volume, float pitch) {
		this(sound, volume, pitch, 0, 0, 0);
	}

	@Override
	protected void read(PacketBuffer buffer) throws IOException {
		sound = ByteBufUtils.readUTF8String(buffer);
		volume = buffer.readFloat();
		pitch = buffer.readFloat();
		x = buffer.readDouble();
		y = buffer.readDouble();
		z = buffer.readDouble();
	}

	@Override
	protected void write(PacketBuffer buffer) throws IOException {
		ByteBufUtils.writeUTF8String(buffer, sound);
		buffer.writeFloat(volume);
		buffer.writeFloat(pitch);
		buffer.writeDouble(x);
		buffer.writeDouble(y);
		buffer.writeDouble(z);
	}

	@Override
	protected void process(EntityPlayer player, Side side) {
		if (side.isClient()) {
			player.playSound(sound, volume, pitch);
		} else {
			player.worldObj.playSoundEffect(x, y, z, sound, volume, pitch);
		}
	}
}
